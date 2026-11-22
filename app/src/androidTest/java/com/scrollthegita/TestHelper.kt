package com.scrollthegita

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.scrollthegita.app.di.AppSettingsEntryPoint
import com.scrollthegita.core.database.BookmarkDao
import com.scrollthegita.core.model.OnboardingDraft
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.core.model.ShlokaIds
import com.scrollthegita.domain.repository.UserSettingsRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

object TestHelper {
    fun getTargetContext(): Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    fun getUserSettingsRepository(): UserSettingsRepository {
        return EntryPointAccessors.fromApplication(
            getTargetContext().applicationContext,
            AppSettingsEntryPoint::class.java,
        ).userSettingsRepository()
    }

    fun getBookmarkDao(): BookmarkDao {
        return EntryPointAccessors.fromApplication(
            getTargetContext().applicationContext,
            AppSettingsEntryPoint::class.java,
        ).bookmarkDao()
    }

    fun setOnboardingCompleted(completed: Boolean = true) {
        val repository = getUserSettingsRepository()
        runBlocking {
            if (completed) {
                repository.completeOnboarding(
                    OnboardingDraft(
                        preferredLanguage = PreferredLanguage.ENGLISH,
                        showSanskrit = true,
                        showTransliteration = true,
                        animationsEnabled = true,
                    ),
                )
                repository.saveProgress(
                    ReadingProgress(
                        lastShlokaId = ShlokaIds.of(2, 47),
                        lastChapter = 2,
                        lastVerse = 47,
                        updatedAtEpochMillis = 0L,
                    ),
                )
            } else {
                repository.resetOnboarding()
                repository.saveProgress(
                    ReadingProgress(
                        lastShlokaId = ShlokaIds.of(2, 47),
                        lastChapter = 2,
                        lastVerse = 47,
                        updatedAtEpochMillis = 0L,
                    ),
                )
            }
        }
    }
}

class OnboardingPreferenceRule(
    private val completed: Boolean,
    private val clearBookmarks: Boolean = false,
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                TestHelper.setOnboardingCompleted(completed)
                if (clearBookmarks) {
                    runBlocking {
                        TestHelper.getBookmarkDao().deleteAll()
                    }
                }
                base.evaluate()
            }
        }
    }
}
