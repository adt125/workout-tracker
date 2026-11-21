package com.example.workouttracker.data

import android.content.pm.ApplicationInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object AiAuth {
    private val auth = FirebaseAuth.getInstance().also {
        if (isDebugBuild()) {
            // Android Emulator -> host machine.
            it.useEmulator("10.0.2.2", 9099)
        }
    }

    suspend fun ensureSignedIn() {
        if (auth.currentUser == null) auth.signInAnonymously().await()
    }

    private fun isDebugBuild(): Boolean =
        FirebaseApp.getInstance().applicationContext.applicationInfo.flags and
            ApplicationInfo.FLAG_DEBUGGABLE != 0
}
