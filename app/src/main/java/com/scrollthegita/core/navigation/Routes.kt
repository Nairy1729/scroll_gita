package com.scrollthegita.core.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val FEED = "feed?shlokaId={shlokaId}&journeyId={journeyId}"
    const val CHAPTERS = "chapters"
    const val JOURNEYS = "journeys"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val REFLECTION = "reflection/{shlokaId}"
    const val SHLOKA_ARG = "shlokaId"
    const val JOURNEY_ID_ARG = "journeyId"

    fun feed(shlokaId: String? = null, journeyId: String? = null): String {
        val s = shlokaId.orEmpty()
        val j = journeyId.orEmpty()
        return "feed?shlokaId=$s&journeyId=$j"
    }

    fun reflection(shlokaId: String): String = "reflection/$shlokaId"
}
