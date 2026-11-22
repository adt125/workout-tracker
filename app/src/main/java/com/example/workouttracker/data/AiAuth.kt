package com.example.workouttracker.data

import com.example.workouttracker.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object AiAuth {
    private val auth = FirebaseAuth.getInstance().also {
        if (BuildConfig.DEBUG) {
            // Android Emulator -> host machine.
            it.useEmulator("10.0.2.2", 9099)
        }
    }

    suspend fun ensureSignedIn() {
        if (auth.currentUser == null) auth.signInAnonymously().await()
    }

}
