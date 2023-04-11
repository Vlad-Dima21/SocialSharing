package com.vladima.cursandroid.models

import android.graphics.Bitmap

data class UserPost(
    var imageBitmap: Bitmap,
    var imageDescription: String = ""
)
