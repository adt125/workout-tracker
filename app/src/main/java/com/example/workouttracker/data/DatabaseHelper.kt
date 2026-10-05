package com.example.workouttracker.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "workout_db"
        private const val DATABASE_VERSION = 7

        private const val TABLE_EXERCISES = "exercises"
        private const val TABLE_HEALTH_METRICS = "health_metrics"
        private const val TABLE_WORKOUT_SESSIONS = "workout_sessions"
        private const val TABLE_WORKOUT_ENTRIES = "workout_entries"
        private const val TABLE_WORKOUT_SETS = "workout_sets"
        private const val TABLE_USER_GOALS = "user_goals"
        private const val TABLE_ACTIVITY_LOGS = "activity_logs"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Exercises table
        db.execSQL("""
            CREATE TABLE $TABLE_EXERCISES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
        """.trimIndent())

        // 2. Health Metrics table
        db.execSQL("""
            CREATE TABLE $TABLE_HEALTH_METRICS (
                date TEXT PRIMARY KEY,
                water REAL DEFAULT 0,
                protein REAL DEFAULT 0,
                body_weight REAL DEFAULT 0
            )
        """.trimIndent())

        // 3. Workout Sessions table
        db.execSQL("""
            CREATE TABLE $TABLE_WORKOUT_SESSIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                start_time TEXT,
                notes TEXT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_sessions_date ON $TABLE_WORKOUT_SESSIONS(date)")

        // 4. Workout Entries table (links session to exercise)
        db.execSQL("""
            CREATE TABLE $TABLE_WORKOUT_ENTRIES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                exercise_id INTEGER,
                FOREIGN KEY(session_id) REFERENCES $TABLE_WORKOUT_SESSIONS(id) ON DELETE CASCADE,
                FOREIGN KEY(exercise_id) REFERENCES $TABLE_EXERCISES(id)
            )
        """.trimIndent())

        // 5. Workout Sets table (the actual lifting/cardio data)
        db.execSQL("""
            CREATE TABLE $TABLE_WORKOUT_SETS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                entry_id INTEGER NOT NULL,
                weight REAL,
                reps INTEGER,
                duration INTEGER,
                set_order INTEGER NOT NULL,
                FOREIGN KEY(entry_id) REFERENCES $TABLE_WORKOUT_ENTRIES(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // 6. User Goals table
        db.execSQL("""
            CREATE TABLE $TABLE_USER_GOALS (
                id INTEGER PRIMARY KEY DEFAULT 1,
                current_weight REAL,
                target_weight REAL,
                fitness_goal TEXT,
                target_date TEXT,
                daily_protein REAL DEFAULT 120,
                daily_water REAL DEFAULT 3.0,
                daily_calorie REAL,
                workouts_per_week INTEGER DEFAULT 4,
                target_duration INTEGER
            )
        """.trimIndent())

        // 7. Activity Logs table
        db.execSQL("""
            CREATE TABLE $TABLE_ACTIVITY_LOGS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                date TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                title TEXT NOT NULL,
                detail TEXT NOT NULL,
                numeric_value REAL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_activity_logs_date ON $TABLE_ACTIVITY_LOGS(date)")

        // Add some default exercises
        db.execSQL("INSERT INTO $TABLE_EXERCISES (name) VALUES ('Incline bench press')")
        db.execSQL("INSERT INTO $TABLE_EXERCISES (name) VALUES ('Flat bench press')")
        db.execSQL("INSERT INTO $TABLE_EXERCISES (name) VALUES ('Incline walk')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_USER_GOALS (
                    id INTEGER PRIMARY KEY DEFAULT 1,
                    current_weight REAL,
                    target_weight REAL,
                    fitness_goal TEXT,
                    target_date TEXT,
                    daily_protein REAL DEFAULT 120,
                    daily_water REAL DEFAULT 3.0,
                    daily_calorie REAL,
                    workouts_per_week INTEGER DEFAULT 4,
                    target_duration INTEGER
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_ACTIVITY_LOGS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    date TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    title TEXT NOT NULL,
                    detail TEXT NOT NULL,
                    numeric_value REAL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_activity_logs_date ON $TABLE_ACTIVITY_LOGS(date)")
        }
        if (oldVersion < 7) {
            mergeDuplicateExercises(db)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Safe downgrade: Do nothing to preserve data
    }

    // --- Exercise methods ---
    fun getOrCreateExerciseId(name: String): Int {
        val db = writableDatabase
        val trimmed = name.trim()
        val cursor = db.query(TABLE_EXERCISES, arrayOf("id"), "LOWER(name) = LOWER(?)", arrayOf(trimmed), null, null, null)
        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            cursor.close()
            id
        } else {
            cursor.close()
            val cv = ContentValues().apply { put("name", trimmed) }
            db.insert(TABLE_EXERCISES, null, cv).toInt()
        }
    }

    fun getAllExercises(): List<Exercise> {
        val list = mutableListOf<Exercise>()
        val cursor = readableDatabase.query(TABLE_EXERCISES, arrayOf("id", "name"), null, null, null, null, "name COLLATE NOCASE ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(Exercise(it.getInt(0), it.getString(1)))
            }
        }
        return list
    }

    /**
     * Merges duplicate exercise records that have the same name (case-insensitive / trimmed).
     * Re-links all existing workout_entries referencing duplicate exercise IDs to the primary exercise ID,
     * and deletes the duplicate records from the exercises table.
     */
    fun mergeDuplicateExercises(database: SQLiteDatabase? = null) {
        val db = database ?: writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.query(TABLE_EXERCISES, arrayOf("id", "name"), null, null, null, null, "id ASC")
            val exercises = mutableListOf<Exercise>()
            cursor.use {
                while (it.moveToNext()) {
                    exercises.add(Exercise(it.getInt(0), it.getString(1)))
                }
            }

            val grouped = exercises.groupBy { it.name.trim().lowercase() }

            for ((_, group) in grouped) {
                if (group.size > 1) {
                    val primary = group.first()
                    val primaryId = primary.id
                    val duplicateIds = group.drop(1).map { it.id }

                    for (dupId in duplicateIds) {
                        val cv = ContentValues().apply {
                            put("exercise_id", primaryId)
                        }
                        db.update(TABLE_WORKOUT_ENTRIES, cv, "exercise_id = ?", arrayOf(dupId.toString()))
                        db.delete(TABLE_EXERCISES, "id = ?", arrayOf(dupId.toString()))
                    }

                    val cv = ContentValues().apply {
                        put("name", primary.name.trim())
                    }
                    db.update(TABLE_EXERCISES, cv, "id = ?", arrayOf(primaryId.toString()))
                }
            }
            db.setTransactionSuccessful()
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }
        }
    }

    /**
     * Manually merges a specific duplicate exercise (removeId) into a target exercise (keepId).
     */
    fun mergeExercisePair(keepId: Int, removeId: Int) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply { put("exercise_id", keepId) }
            db.update(TABLE_WORKOUT_ENTRIES, cv, "exercise_id = ?", arrayOf(removeId.toString()))
            db.delete(TABLE_EXERCISES, "id = ?", arrayOf(removeId.toString()))
            db.setTransactionSuccessful()
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }
        }
    }

    // --- Session methods ---
    fun getOrCreateSessionId(date: String): Long {
        val db = writableDatabase
        val cursor = db.query(TABLE_WORKOUT_SESSIONS, arrayOf("id"), "date = ?", arrayOf(date), null, null, null)
        return if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            cursor.close()
            id
        } else {
            cursor.close()
            val cv = ContentValues().apply {
                put("date", date)
            }
            db.insert(TABLE_WORKOUT_SESSIONS, null, cv)
        }
    }

    fun getSessionForDate(date: String): WorkoutSession? {
        val cursor = readableDatabase.query(
            TABLE_WORKOUT_SESSIONS,
            arrayOf("id", "date", "start_time", "notes"),
            "date = ?",
            arrayOf(date),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return WorkoutSession(
                    id = it.getLong(0),
                    date = it.getString(1),
                    startTime = it.getString(2),
                    notes = it.getString(3)
                )
            }
        }
        return null
    }

    fun getSessionsBetween(from: String, to: String): List<WorkoutSession> {
        val list = mutableListOf<WorkoutSession>()
        val cursor = readableDatabase.query(
            TABLE_WORKOUT_SESSIONS,
            arrayOf("id", "date", "start_time", "notes"),
            "date BETWEEN ? AND ?",
            arrayOf(from, to),
            null, null, "date DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(WorkoutSession(
                    id = it.getLong(0),
                    date = it.getString(1),
                    startTime = it.getString(2),
                    notes = it.getString(3)
                ))
            }
        }
        return list
    }

    fun updateSessionNotes(date: String, notes: String?) {
        val db = writableDatabase
        val sessionId = getOrCreateSessionId(date)
        val cv = ContentValues().apply {
            put("notes", notes)
        }
        db.update(TABLE_WORKOUT_SESSIONS, cv, "id = ?", arrayOf(sessionId.toString()))
    }

    // --- Entry & Set methods ---
    fun insertWorkout(sessionId: Long, exerciseId: Int, sets: List<SetRecord>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val entryCv = ContentValues().apply {
                put("session_id", sessionId)
                put("exercise_id", exerciseId)
            }
            val entryId = db.insert(TABLE_WORKOUT_ENTRIES, null, entryCv)

            sets.forEachIndexed { index, set ->
                val setCv = ContentValues().apply {
                    put("entry_id", entryId)
                    put("weight", set.weight)
                    put("reps", set.reps)
                    put("duration", set.duration)
                    put("set_order", index)
                }
                db.insert(TABLE_WORKOUT_SETS, null, setCv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteWorkoutEntry(entryId: Long) {
        writableDatabase.delete(TABLE_WORKOUT_ENTRIES, "id = ?", arrayOf(entryId.toString()))
    }

    fun getWorkoutEntryById(entryId: Long): Pair<WorkoutEntry, String>? {
        val query = """
            SELECT e.id as entry_id, e.session_id, ex.id as ex_id, ex.name, s.date
            FROM $TABLE_WORKOUT_ENTRIES e
            JOIN $TABLE_WORKOUT_SESSIONS s ON e.session_id = s.id
            JOIN $TABLE_EXERCISES ex ON e.exercise_id = ex.id
            WHERE e.id = ?
        """.trimIndent()
        
        val cursor = readableDatabase.rawQuery(query, arrayOf(entryId.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                val entry = WorkoutEntry(
                    id = entryId,
                    sessionId = it.getLong(it.getColumnIndexOrThrow("session_id")),
                    exerciseId = it.getInt(it.getColumnIndexOrThrow("ex_id")),
                    exerciseName = it.getString(it.getColumnIndexOrThrow("name")),
                    date = it.getString(it.getColumnIndexOrThrow("date")),
                    sets = getSetsForEntry(entryId)
                )
                return entry to entry.exerciseName
            }
        }
        return null
    }

    fun getWorkoutsForDate(date: String): List<Pair<WorkoutEntry, String>> {
        val list = mutableListOf<Pair<WorkoutEntry, String>>()
        val query = """
            SELECT e.id as entry_id, e.session_id, ex.id as ex_id, ex.name, s.date 
            FROM $TABLE_WORKOUT_ENTRIES e
            JOIN $TABLE_WORKOUT_SESSIONS s ON e.session_id = s.id
            JOIN $TABLE_EXERCISES ex ON e.exercise_id = ex.id
            WHERE s.date = ?
        """.trimIndent()
        
        val cursor = readableDatabase.rawQuery(query, arrayOf(date))
        cursor.use {
            while (it.moveToNext()) {
                val entryId = it.getLong(it.getColumnIndexOrThrow("entry_id"))
                val entry = WorkoutEntry(
                    id = entryId,
                    sessionId = it.getLong(it.getColumnIndexOrThrow("session_id")),
                    exerciseId = it.getInt(it.getColumnIndexOrThrow("ex_id")),
                    exerciseName = it.getString(it.getColumnIndexOrThrow("name")),
                    date = it.getString(it.getColumnIndexOrThrow("date")),
                    sets = getSetsForEntry(entryId)
                )
                list.add(entry to entry.exerciseName)
            }
        }
        return list
    }

    fun getSetsForEntry(entryId: Long): List<SetRecord> {
        val list = mutableListOf<SetRecord>()
        val cursor = readableDatabase.query(
            TABLE_WORKOUT_SETS,
            arrayOf("weight", "reps", "duration"),
            "entry_id = ?",
            arrayOf(entryId.toString()),
            null, null, "set_order ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(SetRecord(
                    weight = if (it.isNull(0)) null else it.getFloat(0),
                    reps = if (it.isNull(1)) null else it.getInt(1),
                    duration = if (it.isNull(2)) null else it.getInt(2)
                ))
            }
        }
        return list
    }

    fun getDailyMetrics(date: String): HealthMetric {
        val cursor = readableDatabase.query(
            TABLE_HEALTH_METRICS,
            arrayOf("water", "protein", "body_weight"),
            "date = ?",
            arrayOf(date),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return HealthMetric(
                    date = date,
                    water = it.getFloat(0),
                    protein = it.getFloat(1),
                    bodyWeight = it.getFloat(2)
                )
            }
        }
        return HealthMetric(date)
    }

    fun updateHealthMetrics(metric: HealthMetric) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("date", metric.date)
            put("water", metric.water)
            put("protein", metric.protein)
            put("body_weight", metric.bodyWeight)
        }
        db.insertWithOnConflict(TABLE_HEALTH_METRICS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getLatestWeight(): Float? {
        val cursor = readableDatabase.query(
            TABLE_HEALTH_METRICS,
            arrayOf("body_weight"),
            "body_weight > 0",
            null, null, null, "date DESC", "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getFloat(0)
            }
        }
        return null
    }

    fun getMetricsBetween(from: String, to: String): List<HealthMetric> {
        val list = mutableListOf<HealthMetric>()
        val cursor = readableDatabase.query(
            TABLE_HEALTH_METRICS,
            null,
            "date BETWEEN ? AND ?",
            arrayOf(from, to),
            null, null, "date ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(HealthMetric(
                    date = it.getString(it.getColumnIndexOrThrow("date")),
                    water = it.getFloat(it.getColumnIndexOrThrow("water")),
                    protein = it.getFloat(it.getColumnIndexOrThrow("protein")),
                    bodyWeight = it.getFloat(it.getColumnIndexOrThrow("body_weight"))
                ))
            }
        }
        return list
    }

    fun getWorkoutCountsBetween(from: String, to: String): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val query = """
            SELECT s.date, COUNT(e.id) as count 
            FROM $TABLE_WORKOUT_SESSIONS s
            JOIN $TABLE_WORKOUT_ENTRIES e ON s.id = e.session_id
            WHERE s.date BETWEEN ? AND ?
            GROUP BY s.date
        """.trimIndent()
        val cursor = readableDatabase.rawQuery(query, arrayOf(from, to))
        cursor.use {
            while (it.moveToNext()) {
                map[it.getString(0)] = it.getInt(1)
            }
        }
        return map
    }

    fun getCardioWorkoutCountsBetween(from: String, to: String): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val query = """
            SELECT s.date, COUNT(e.id) as count 
            FROM $TABLE_WORKOUT_SESSIONS s
            JOIN $TABLE_WORKOUT_ENTRIES e ON s.id = e.session_id
            JOIN $TABLE_WORKOUT_SETS ws ON e.id = ws.entry_id
            WHERE s.date BETWEEN ? AND ? AND ws.duration IS NOT NULL
            GROUP BY s.date
        """.trimIndent()
        val cursor = readableDatabase.rawQuery(query, arrayOf(from, to))
        cursor.use {
            while (it.moveToNext()) {
                map[it.getString(0)] = it.getInt(1)
            }
        }
        return map
    }

    fun getWorkoutsBetween(from: String, to: String): List<Pair<WorkoutEntry, String>> {
        val list = mutableListOf<Pair<WorkoutEntry, String>>()
        val query = """
            SELECT e.id as entry_id, e.session_id, ex.id as ex_id, ex.name, s.date
            FROM $TABLE_WORKOUT_ENTRIES e
            JOIN $TABLE_WORKOUT_SESSIONS s ON e.session_id = s.id
            JOIN $TABLE_EXERCISES ex ON e.exercise_id = ex.id
            WHERE s.date BETWEEN ? AND ?
            ORDER BY s.date DESC, e.id DESC
        """.trimIndent()
        
        val cursor = readableDatabase.rawQuery(query, arrayOf(from, to))
        cursor.use {
            while (it.moveToNext()) {
                val entryId = it.getLong(it.getColumnIndexOrThrow("entry_id"))
                val entry = WorkoutEntry(
                    id = entryId,
                    sessionId = it.getLong(it.getColumnIndexOrThrow("session_id")),
                    exerciseId = it.getInt(it.getColumnIndexOrThrow("ex_id")),
                    exerciseName = it.getString(it.getColumnIndexOrThrow("name")),
                    date = it.getString(it.getColumnIndexOrThrow("date")),
                    sets = getSetsForEntry(entryId)
                )
                list.add(entry to entry.exerciseName)
            }
        }
        return list
    }

    fun getLatestSetDataForExercise(exerciseId: Int, beforeDate: String): List<SetRecord> {
        val query = """
            SELECT e.id FROM $TABLE_WORKOUT_ENTRIES e
            JOIN $TABLE_WORKOUT_SESSIONS s ON e.session_id = s.id
            WHERE e.exercise_id = ? AND s.date < ?
            ORDER BY s.date DESC, e.id DESC LIMIT 1
        """.trimIndent()
        
        val cursor = readableDatabase.rawQuery(query, arrayOf(exerciseId.toString(), beforeDate))
        val entryId = if (cursor.moveToFirst()) cursor.getLong(0) else -1L
        cursor.close()
        
        return if (entryId != -1L) getSetsForEntry(entryId) else emptyList()
    }

    fun getRecentWorkouts(limit: Int): List<Pair<WorkoutEntry, String>> {
        val list = mutableListOf<Pair<WorkoutEntry, String>>()
        val query = """
            SELECT e.id as entry_id, e.session_id, ex.id as ex_id, ex.name, s.date
            FROM $TABLE_WORKOUT_ENTRIES e
            JOIN $TABLE_WORKOUT_SESSIONS s ON e.session_id = s.id
            JOIN $TABLE_EXERCISES ex ON e.exercise_id = ex.id
            ORDER BY s.date DESC, e.id DESC
            LIMIT ?
        """.trimIndent()
        
        val cursor = readableDatabase.rawQuery(query, arrayOf(limit.toString()))
        cursor.use {
            while (it.moveToNext()) {
                val entryId = it.getLong(it.getColumnIndexOrThrow("entry_id"))
                val entry = WorkoutEntry(
                    id = entryId,
                    sessionId = it.getLong(it.getColumnIndexOrThrow("session_id")),
                    exerciseId = it.getInt(it.getColumnIndexOrThrow("ex_id")),
                    exerciseName = it.getString(it.getColumnIndexOrThrow("name")),
                    date = it.getString(it.getColumnIndexOrThrow("date")),
                    sets = getSetsForEntry(entryId)
                )
                list.add(entry to entry.exerciseName)
            }
        }
        return list
    }

    fun getAllDataForBackup(): Map<String, Any> {
        val allMetrics = mutableListOf<HealthMetric>()
        val cursorMetrics = readableDatabase.query(TABLE_HEALTH_METRICS, null, null, null, null, null, null)
        cursorMetrics.use {
            while (it.moveToNext()) {
                allMetrics.add(HealthMetric(
                    date = it.getString(it.getColumnIndexOrThrow("date")),
                    water = it.getFloat(it.getColumnIndexOrThrow("water")),
                    protein = it.getFloat(it.getColumnIndexOrThrow("protein")),
                    bodyWeight = it.getFloat(it.getColumnIndexOrThrow("body_weight"))
                ))
            }
        }

        val allWorkouts = mutableListOf<Pair<WorkoutEntry, String>>()
        val query = """
            SELECT e.id as entry_id, e.session_id, ex.id as ex_id, ex.name, s.date
            FROM $TABLE_WORKOUT_ENTRIES e
            JOIN $TABLE_WORKOUT_SESSIONS s ON e.session_id = s.id
            JOIN $TABLE_EXERCISES ex ON e.exercise_id = ex.id
            ORDER BY s.date DESC
        """.trimIndent()
        val cursorWorkouts = readableDatabase.rawQuery(query, null)
        cursorWorkouts.use {
            while (it.moveToNext()) {
                val entryId = it.getLong(it.getColumnIndexOrThrow("entry_id"))
                val entry = WorkoutEntry(
                    id = entryId,
                    sessionId = it.getLong(it.getColumnIndexOrThrow("session_id")),
                    exerciseId = it.getInt(it.getColumnIndexOrThrow("ex_id")),
                    exerciseName = it.getString(it.getColumnIndexOrThrow("name")),
                    date = it.getString(it.getColumnIndexOrThrow("date")),
                    sets = getSetsForEntry(entryId)
                )
                allWorkouts.add(entry to entry.exerciseName)
            }
        }

        return mapOf(
            "metrics" to allMetrics,
            "workouts" to allWorkouts.map { it.first }
        )
    }

    // --- User Goal Methods ---
    fun getUserGoal(): UserGoal? {
        val cursor = readableDatabase.query(
            TABLE_USER_GOALS,
            null,
            "id = 1",
            null, null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val currentWeight = if (it.isNull(it.getColumnIndexOrThrow("current_weight"))) null else it.getFloat(it.getColumnIndexOrThrow("current_weight"))
                val targetWeight = if (it.isNull(it.getColumnIndexOrThrow("target_weight"))) null else it.getFloat(it.getColumnIndexOrThrow("target_weight"))
                val fitnessGoalName = it.getString(it.getColumnIndexOrThrow("fitness_goal"))
                val targetDate = it.getString(it.getColumnIndexOrThrow("target_date"))
                val dailyProtein = it.getFloat(it.getColumnIndexOrThrow("daily_protein"))
                val dailyWater = it.getFloat(it.getColumnIndexOrThrow("daily_water"))
                val dailyCalorie = if (it.isNull(it.getColumnIndexOrThrow("daily_calorie"))) null else it.getFloat(it.getColumnIndexOrThrow("daily_calorie"))
                val workoutsPerWeek = it.getInt(it.getColumnIndexOrThrow("workouts_per_week"))
                val targetDuration = if (it.isNull(it.getColumnIndexOrThrow("target_duration"))) null else it.getInt(it.getColumnIndexOrThrow("target_duration"))

                return UserGoal(
                    currentWeight = currentWeight,
                    targetWeight = targetWeight,
                    fitnessGoal = FitnessGoal.fromName(fitnessGoalName),
                    targetDate = targetDate,
                    dailyProteinTarget = if (dailyProtein > 0) dailyProtein else 120f,
                    dailyWaterTarget = if (dailyWater > 0) dailyWater else 3.0f,
                    dailyCalorieTarget = dailyCalorie,
                    workoutsPerWeek = if (workoutsPerWeek > 0) workoutsPerWeek else 4,
                    targetWorkoutDurationMinutes = targetDuration
                )
            }
        }
        return null
    }

    fun saveUserGoal(goal: UserGoal) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("id", 1)
            put("current_weight", goal.currentWeight)
            put("target_weight", goal.targetWeight)
            put("fitness_goal", goal.fitnessGoal.name)
            put("target_date", goal.targetDate)
            put("daily_protein", goal.dailyProteinTarget)
            put("daily_water", goal.dailyWaterTarget)
            put("daily_calorie", goal.dailyCalorieTarget)
            put("workouts_per_week", goal.workoutsPerWeek)
            put("target_duration", goal.targetWorkoutDurationMinutes)
        }
        db.insertWithOnConflict(TABLE_USER_GOALS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // --- Activity Log Methods ---
    fun insertActivityLog(log: ActivityLogItem): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("type", log.type.name)
            put("date", log.date)
            put("timestamp", log.timestamp)
            put("title", log.title)
            put("detail", log.detail)
            put("numeric_value", log.numericValue)
        }
        return db.insert(TABLE_ACTIVITY_LOGS, null, cv)
    }

    fun getActivityLogsForDate(date: String): List<ActivityLogItem> {
        val list = mutableListOf<ActivityLogItem>()
        val cursor = readableDatabase.query(
            TABLE_ACTIVITY_LOGS,
            null,
            "date = ?",
            arrayOf(date),
            null, null, "id DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id"))
                val typeStr = it.getString(it.getColumnIndexOrThrow("type"))
                val type = try { ActivityType.valueOf(typeStr) } catch (_: Exception) { ActivityType.NOTE }
                val time = it.getString(it.getColumnIndexOrThrow("timestamp"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val detail = it.getString(it.getColumnIndexOrThrow("detail"))
                val numVal = if (it.isNull(it.getColumnIndexOrThrow("numeric_value"))) null else it.getFloat(it.getColumnIndexOrThrow("numeric_value"))

                list.add(ActivityLogItem(id, type, time, date, title, detail, numVal))
            }
        }
        return list
    }

    // --- Weight History Methods ---
    fun getWeightHistory(fromDate: String): List<WeightEntry> {
        val list = mutableListOf<WeightEntry>()
        val cursor = readableDatabase.query(
            TABLE_HEALTH_METRICS,
            arrayOf("rowid", "date", "body_weight"),
            "date >= ? AND body_weight > 0",
            arrayOf(fromDate),
            null, null, "date ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(WeightEntry(
                    id = it.getLong(0),
                    date = it.getString(1),
                    weightKg = it.getFloat(2)
                ))
            }
        }
        return list
    }
}
