package com.example.workouttracker.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "workout_db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_WEIGHT = "weight_entries"
        private const val TABLE_WATER = "water_entries"
        private const val TABLE_WORKOUT = "workout_entries"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_WEIGHT (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                weightKg REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_WATER (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                amountMl INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_WORKOUT (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                exerciseName TEXT NOT NULL,
                sets INTEGER,
                reps INTEGER,
                weightKg REAL,
                durationMin INTEGER,
                distanceKm REAL,
                avgSpeed REAL,
                incline REAL,
                notes TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_WORKOUT ADD COLUMN distanceKm REAL")
            db.execSQL("ALTER TABLE $TABLE_WORKOUT ADD COLUMN avgSpeed REAL")
            db.execSQL("ALTER TABLE $TABLE_WORKOUT ADD COLUMN incline REAL")
        }
    }

    // Weight methods
    fun insertWeight(entry: WeightEntry): Long {
        val cv = ContentValues().apply {
            put("date", entry.date)
            put("weightKg", entry.weightKg)
        }
        return writableDatabase.insert(TABLE_WEIGHT, null, cv)
    }

    fun getWeightForDate(date: String): List<WeightEntry> {
        val list = mutableListOf<WeightEntry>()
        val cursor: Cursor = readableDatabase.query(TABLE_WEIGHT, arrayOf("id", "date", "weightKg"), "date = ?", arrayOf(date), null, null, "id DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(WeightEntry(it.getLong(0), it.getString(1), it.getDouble(2).toFloat()))
            }
        }
        return list
    }

    fun avgWeightBetween(from: String, to: String): Double? {
        val cursor = readableDatabase.rawQuery("SELECT AVG(weightKg) FROM $TABLE_WEIGHT WHERE date BETWEEN ? AND ?", arrayOf(from, to))
        cursor.use {
            if (it.moveToFirst()) return if (!it.isNull(0)) it.getDouble(0) else null
        }
        return null
    }

    // Water methods
    fun insertWater(entry: WaterEntry): Long {
        val cv = ContentValues().apply {
            put("date", entry.date)
            put("amountMl", entry.amountMl)
        }
        return writableDatabase.insert(TABLE_WATER, null, cv)
    }

    fun totalWaterForDate(date: String): Int {
        val cursor = readableDatabase.rawQuery("SELECT SUM(amountMl) FROM $TABLE_WATER WHERE date = ?", arrayOf(date))
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0)
        }
        return 0
    }

    fun totalWaterBetween(from: String, to: String): Int {
        val cursor = readableDatabase.rawQuery("SELECT SUM(amountMl) FROM $TABLE_WATER WHERE date BETWEEN ? AND ?", arrayOf(from, to))
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0)
        }
        return 0
    }

    // Workout methods
    fun insertWorkout(entry: WorkoutEntry): Long {
        val cv = ContentValues().apply {
            put("date", entry.date)
            put("exerciseName", entry.exerciseName)
            put("sets", entry.sets)
            put("reps", entry.reps)
            put("weightKg", entry.weightKg)
            put("durationMin", entry.durationMin)
            put("distanceKm", entry.distanceKm)
            put("avgSpeed", entry.avgSpeed)
            put("incline", entry.incline)
            put("notes", entry.notes)
        }
        return writableDatabase.insert(TABLE_WORKOUT, null, cv)
    }

    fun getAllExerciseNames(): List<String> {
        val list = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery("SELECT DISTINCT exerciseName FROM $TABLE_WORKOUT ORDER BY exerciseName ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        return list
    }

    fun countWorkoutsBetween(from: String, to: String): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_WORKOUT WHERE date BETWEEN ? AND ?", arrayOf(from, to))
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0)
        }
        return 0
    }

    fun getWorkoutsForDate(date: String): List<WorkoutEntry> {
        val list = mutableListOf<WorkoutEntry>()
        val cursor = readableDatabase.query(TABLE_WORKOUT, arrayOf("id", "date", "exerciseName", "sets", "reps", "weightKg", "durationMin", "distanceKm", "avgSpeed", "incline", "notes"), "date = ?", arrayOf(date), null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutEntry(
                        id = it.getLong(0),
                        date = it.getString(1),
                        exerciseName = it.getString(2),
                        sets = if (it.isNull(3)) null else it.getInt(3),
                        reps = if (it.isNull(4)) null else it.getInt(4),
                        weightKg = if (it.isNull(5)) null else it.getDouble(5).toFloat(),
                        durationMin = if (it.isNull(6)) null else it.getInt(6),
                        distanceKm = if (it.isNull(7)) null else it.getFloat(7),
                        avgSpeed = if (it.isNull(8)) null else it.getFloat(8),
                        incline = if (it.isNull(9)) null else it.getFloat(9),
                        notes = if (it.isNull(10)) null else it.getString(10)
                    )
                )
            }
        }
        return list
    }
}
