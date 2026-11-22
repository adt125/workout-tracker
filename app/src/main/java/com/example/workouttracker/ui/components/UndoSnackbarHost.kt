package com.example.workouttracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.MintAccent
import com.example.workouttracker.ui.theme.ProgressTrack
import com.example.workouttracker.ui.theme.SnackbarBg
import com.example.workouttracker.ui.theme.TextHi

@Composable
fun UndoSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            Snackbar(
                containerColor = SnackbarBg,
                contentColor = TextHi,
                shape = shape,
                modifier = Modifier.border(width = 1.dp, color = ProgressTrack, shape = shape),
                action = snackbarData.visuals.actionLabel?.let { actionLabel ->
                    {
                        TextButton(
                            onClick = { snackbarData.performAction() }
                        ) {
                            Text(
                                text = actionLabel,
                                color = MintAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    fontSize = 14.sp,
                    color = TextHi
                )
            }
        }
    )
}
