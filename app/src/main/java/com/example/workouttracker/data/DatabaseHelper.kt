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
        private const val DATABASE_VERSION = 5

        private const val TABLE_EXERCISES = "exercises"
        private const val TABLE_HEALTH_METRICS = "health_metrics"
        private const val TABLE_WORKOUT_SESSIONS = "workout_sessions"
        private const val TABLE_WORKOUT_ENTRIES = "workout_entries"
        private const val TABLE_WORKOUT_SETS = "workout_sets"
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
        
        // Add some default exercises
        db.execSQL("INSERT INTO $TABLE_EXERCISES (name) VALUES ('Incline bench press')")
        db.execSQL("INSERT INTO $TABLE_EXERCISES (name) VALUES ('Flat bench press')")
        db.execSQL("INSERT INTO $TABLE_EXERCISES (name) VALUES ('Incline walk')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Safe upgrade: Do not drop tables. 
        // In the future, add migrations here using db.execSQL("ALTER TABLE ...")
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
            SELECT date, COUNT(*) as count 
            FROM $TABLE_WORKOUT_SESSIONS 
            WHERE date BETWEEN ? AND ?
            GROUP BY date
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
}
