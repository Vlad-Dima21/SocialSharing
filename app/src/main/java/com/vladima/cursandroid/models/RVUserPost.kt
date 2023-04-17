package com.vladima.cursandroid.models

import android.graphics.Bitmap

data class RVUserPost(
    var fileName: String = "",
    var imageBitmap: Bitmap,
    var imageDescription: String = ""
)
