package com.scrollthegita.core.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val FEED = "feed?shlokaId={shlokaId}"
    const val CHAPTERS = "chapters"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val REFLECTION = "reflection/{shlokaId}"
    const val SHLOKA_ARG = "shlokaId"

    fun feed(shlokaId: String? = null): String = "feed?shlokaId=${shlokaId.orEmpty()}"

    fun reflection(shlokaId: String): String = "reflection/$shlokaId"
}
