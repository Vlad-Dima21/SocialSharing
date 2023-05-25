package com.vladima.cursandroid.models

import android.graphics.Bitmap

data class RVFriendPost (
    var fileName: String = "",
    var imageBitmap: Bitmap,
    var authorName: String = "",
    var imageDescription: String = ""
)