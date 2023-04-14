package com.vladima.cursandroid.models

import java.util.*

data class DbUserPost(
    var fileName: String = "",
    var userUID: String = "",
    var description: String = "",
    var createDate: Date = Date()
)
