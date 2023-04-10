package com.vladima.cursandroid.models

data class User(
    var userUID: String = "",
    var userName: String = "",
    var age: Int = 0,
    var friends: List<User> = listOf()
)
