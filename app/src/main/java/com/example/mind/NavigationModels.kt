package com.example.mind

data class Question(
    val text: String,
    val options: List<String>,
    val multi: Boolean
)

enum class Screen {
    SPLASH, AUTH, QUESTIONNAIRE, AVATAR, MAIN
}

enum class AppDestinations {
    HOME, FEED, CHAT, PROFILE
}
