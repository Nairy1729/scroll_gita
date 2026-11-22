package com.scrollthegita.data.repository

import com.scrollthegita.core.database.SearchDao
import com.scrollthegita.core.database.toDomain
import com.scrollthegita.core.model.SearchFilter
import com.scrollthegita.core.model.SearchRanking
import com.scrollthegita.core.model.SearchResult
import com.scrollthegita.core.model.SearchText
import com.scrollthegita.core.model.VerseReference
import com.scrollthegita.domain.repository.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val searchDao: SearchDao,
) : SearchRepository {
    override suspend fun search(rawQuery: String, filter: SearchFilter): List<SearchResult> {
        val query = rawQuery.trim()
        if (query.isEmpty()) return emptyList()
        val exactId = VerseReference.parse(query)
        val ids = searchDao.searchIds(
            exactId = exactId.orEmpty(),
            filter = filter.name,
            like = SearchText.likePattern(query),
        )
        if (ids.isEmpty()) return emptyList()
        val normalized = SearchText.normalize(query)
        val chapters = searchDao.chapters().map { it.toDomain() }.associateBy { it.number }
        return searchDao.findShlokas(ids)
            .map { it.toDomain() }
            .map { shloka ->
                val chapter = chapters[shloka.chapter]
                SearchResult(
                    shloka = shloka,
                    chapterTitle = chapter?.nameEnglish.orEmpty(),
                    rank = SearchRanking.rank(shloka, chapter, exactId, normalized),
                )
            }
            .sortedWith(compareBy({ it.rank }, { it.shloka.sortOrder }))
    }
}
