const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();
const geminiApiKey = defineSecret("GEMINI_API_KEY");
const MODEL = "gemini-2.5-flash";
// One shared quota for all AI endpoints. Food estimates and weekly summaries
// both increment this same per-user/day counter.
const PER_USER_DAILY = 5;
const GLOBAL_DAILY = 150;

const dayKey = () => new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" });
const isWeekendInIndia = () => ["Sat", "Sun"].includes(
  new Intl.DateTimeFormat("en-US", { weekday: "short", timeZone: "Asia/Kolkata" }).format(new Date())
);

async function takeQuota(uid) {
  const day = dayKey();
  const userRef = db.doc(`usage/${day}_${uid}`);
  const globalRef = db.doc(`usage/${day}_global`);
  await db.runTransaction(async (tx) => {
    const [user, global] = await Promise.all([tx.get(userRef), tx.get(globalRef)]);
    const userCount = user.exists ? user.data().count : 0;
    const globalCount = global.exists ? global.data().count : 0;
    if (globalCount >= GLOBAL_DAILY) throw new HttpsError("resource-exhausted", "global_cap");
    if (userCount >= PER_USER_DAILY) throw new HttpsError("resource-exhausted", "user_cap");
    const expireAt = new Date(Date.now() + 8 * 24 * 60 * 60 * 1000);
    tx.set(userRef, { count: userCount + 1, expireAt });
    tx.set(globalRef, { count: globalCount + 1, expireAt });
  });
}

async function askGemini(system, parts, maxOutputTokens) {
  const response = await fetch(
    `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent?key=${geminiApiKey.value()}`,
    {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        system_instruction: { parts: [{ text: system }] },
        contents: [{ role: "user", parts }],
        generationConfig: {
          temperature: 0.2,
          maxOutputTokens,
          responseMimeType: "application/json",
          thinkingConfig: { thinkingBudget: 0 },
        },
      }),
    }
  );
  const responseText = await response.text();
  if (!response.ok) {
    console.error("Gemini request failed", {
      status: response.status,
      statusText: response.statusText,
      body: responseText,
    });
    throw new HttpsError("internal", "ai_failed");
  }

  let data;
  try {
    data = JSON.parse(responseText);
  } catch (error) {
    console.error("Gemini returned invalid JSON", {
      status: response.status,
      body: responseText,
      error: error.message,
    });
    throw new HttpsError("internal", "ai_failed");
  }
  const raw = data.candidates?.[0]?.content?.parts?.[0]?.text || "";
  const cleaned = raw
    .trim()
    .replace(/^```(?:json)?\s*/i, "")
    .replace(/\s*```$/i, "")
    .trim();

  try {
    return JSON.parse(cleaned);
  } catch (error) {
    const start = cleaned.indexOf("{");
    const end = cleaned.lastIndexOf("}");
    if (start >= 0 && end > start) {
      try {
        return JSON.parse(cleaned.slice(start, end + 1));
      } catch (_) {
        // Fall through to the diagnostic below.
      }
    }

    console.error("Gemini returned invalid model JSON", {
      raw: cleaned,
      error: error.message,
    });
    throw new HttpsError("internal", "ai_bad_json");
  }
}

const clamp = (value, max) => Math.max(0, Math.min(max, Math.round(Number(value) || 0)));
const FOOD_SYSTEM = "You estimate nutrition for described or photographed meals. Use typical Indian home portions when unclear and mention assumptions in qty. Reply only with JSON: {items:[{name,qty,calories,protein_g,carbs_g,fat_g,confidence:'low|medium|high'}]}. At most 12 items.";

exports.parseFood = onCall(
  { region: "asia-south1", secrets: [geminiApiKey], maxInstances: 3, timeoutSeconds: 40 },
  async (req) => {
    if (!req.auth) throw new HttpsError("unauthenticated", "sign_in");
    const text = String(req.data?.text ?? "").trim().slice(0, 500);
    const image = req.data?.imageBase64 ? String(req.data.imageBase64) : null;
    if (!text && !image) throw new HttpsError("invalid-argument", "empty");
    if (image && image.length > 1_500_000) throw new HttpsError("invalid-argument", "image_too_large");
    await takeQuota(req.auth.uid);
    const parts = [];
    if (image) parts.push({ inline_data: { mime_type: "image/jpeg", data: image } });
    parts.push({ text: text || "Estimate the meal in this photo." });
    const out = await askGemini(FOOD_SYSTEM, parts, 1200);
    const items = (Array.isArray(out.items) ? out.items : []).slice(0, 12).map((item) => ({
      name: String(item.name ?? "").slice(0, 60), qty: String(item.qty ?? "").slice(0, 40),
      calories: clamp(item.calories, 3000), protein_g: clamp(item.protein_g, 300),
      carbs_g: clamp(item.carbs_g, 500), fat_g: clamp(item.fat_g, 300),
      confidence: ["low", "medium", "high"].includes(item.confidence) ? item.confidence : "medium",
    }));
    if (!items.length) throw new HttpsError("not-found", "no_food_found");
    return { items };
  }
);

const SUMMARY_SYSTEM = "Write a concise weekly fitness summary using only the supplied JSON. Treat the has*Logs fields as authoritative. A numeric value of 0 or a false has*Logs field means no logs, not a real measurement. If either the current or previous period has no logs for a metric, say no logs found for that period and do not calculate a change or percentage. Never describe missing current data as down, zero, or a percentage change. Compare current values with previous-week values only when both periods have logs, and explicitly say up or down with the percentage. Relate protein to proteinGoalG, calories to dailyCalorieTargetKcal when it is greater than zero, workouts to workoutGoal, and cardioDays to cardioGoal. Include these five facts in order: daily protein intake and goal comparison; daily average calorie intake and last-week comparison; average weight and last-week comparison; workouts this week versus last week; cardio days out of the cardio goal. Never invent numbers or give medical advice. Reply only with JSON: {headline,bullets:[string,string,string,string,string],suggestion}. Headline under 12 words; each bullet under 35 words.";
exports.weeklySummary = onCall(
  { region: "asia-south1", secrets: [geminiApiKey], maxInstances: 3, timeoutSeconds: 40 },
  async (req) => {
    if (!req.auth) throw new HttpsError("unauthenticated", "sign_in");
    if (!isWeekendInIndia()) throw new HttpsError("failed-precondition", "summary_weekend_only");
    if (!req.data?.stats || typeof req.data.stats !== "object") throw new HttpsError("invalid-argument", "empty");
    const stats = JSON.stringify(req.data.stats).slice(0, 3000);
    await takeQuota(req.auth.uid);
    const out = await askGemini(SUMMARY_SYSTEM, [{ text: stats }], 800);
    return { headline: String(out.headline ?? "").slice(0, 120), bullets: (Array.isArray(out.bullets) ? out.bullets : []).slice(0, 3).map(String), suggestion: String(out.suggestion ?? "").slice(0, 200) };
  }
);
