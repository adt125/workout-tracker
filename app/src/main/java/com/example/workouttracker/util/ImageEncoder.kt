package com.example.workouttracker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

fun encodeMealPhoto(context: Context, uri: Uri): String? {
    val source = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return null
    val scale = minOf(1f, 1024f / max(source.width, source.height))
    val bitmap = if (scale < 1f) Bitmap.createScaledBitmap(source, (source.width * scale).toInt(), (source.height * scale).toInt(), true) else source
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
    if (bitmap !== source) bitmap.recycle()
    val encoded = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    return encoded.takeIf { it.length <= 1_500_000 }
}
