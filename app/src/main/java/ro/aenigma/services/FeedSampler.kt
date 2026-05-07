package ro.aenigma.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.apache.commons.rng.UniformRandomProvider
import org.apache.commons.rng.sampling.distribution.AliasMethodDiscreteSampler
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler
import org.apache.commons.rng.simple.RandomSource
import ro.aenigma.data.Repository
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.models.extensions.ArticleDtoExtensions.prettyFormat
import ro.aenigma.models.extensions.MessageWithDetailsDtoExtensions.isWithinNewsfeedPeriod
import ro.aenigma.models.extensions.MessageWithDetailsDtoExtensions.toArticleDto
import ro.aenigma.util.Constants.Companion.WEB_ARTICLES_FEED_WEIGHT
import ro.aenigma.util.Constants.Companion.ARTICLES_INDEX_URL_TEMPLATE
import ro.aenigma.util.Constants.Companion.JSON_FILE_EXTENSION
import ro.aenigma.util.Constants.Companion.LOCAL_MEDIA_FEED_WEIGHT
import ro.aenigma.util.Constants.Companion.MARKDOWN_FILE_EXTENSION
import ro.aenigma.util.Constants.Companion.NEWS_FEED_SIZE
import ro.aenigma.util.ContextExtensions.getFileExtension
import ro.aenigma.util.ContextExtensions.isJsonUri
import ro.aenigma.util.ContextExtensions.isMarkdownUri
import ro.aenigma.util.ContextExtensions.isTextUri
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.emptyList
import kotlin.collections.listOf

@Singleton
class FeedSampler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: Repository
) {
    companion object {
        @JvmStatic
        fun buildSampler(
            weights: List<Int>,
            rng: UniformRandomProvider
        ): SharedStateDiscreteSampler {
            val probabilities =
                weights.map { weight -> weight.toDouble() / weights.sum().toDouble() }
                    .toDoubleArray()
            return AliasMethodDiscreteSampler.of(rng, probabilities)
        }

        @JvmStatic
        fun weightedInterleave(
            sourcesList: List<List<ArticleDto>>,
            sourcesWeight: List<Int>,
            rng: UniformRandomProvider = RandomSource.MT.create()
        ): List<ArticleDto> {
            if (sourcesList.size != sourcesWeight.size || sourcesList.isEmpty() || sourcesWeight.any { weight -> weight < 0 }) {
                return listOf()
            }
            return try {
                val sources = sourcesList.map { source -> source.toMutableList() }.toMutableList()
                val weights = sourcesWeight.toMutableList()
                var sampler = buildSampler(weights, rng)

                val outCapacity = sources.sumOf { source -> source.size }
                val out = mutableListOf<ArticleDto>()
                val hashes = mutableSetOf<Int>()

                while (out.size < outCapacity) {

                    val idx = sampler.sample()

                    if (sources[idx].isEmpty()) {
                        sources.removeAt(idx)
                        weights.removeAt(idx)
                        if (sources.isNotEmpty()) {
                            sampler = buildSampler(weights, rng)
                        }
                        continue
                    }
                    val item = sources[idx].removeAt(0)
                    if (hashes.add(item.hashCode())) {
                        out.add(item)
                    }
                }
                out
            } catch (_: Exception) {
                listOf()
            }
        }
    }

    private suspend fun getLocalArticle(message: MessageWithDetailsDto): ArticleDto? {
        return try {
            val metadataFile = message.message.files?.firstOrNull { file ->
                context.isJsonUri(file) || context.isTextUri(file)
            } ?: message.message.files?.firstOrNull { file ->
                context.getFileExtension(file) == JSON_FILE_EXTENSION
            }
            val markdownFile = message.message.files?.firstOrNull { file ->
                context.isMarkdownUri(file)
            } ?: message.message.files?.firstOrNull { file ->
                context.getFileExtension(file) == MARKDOWN_FILE_EXTENSION
            }

            val files = message.message.files?.minus(setOf(metadataFile, markdownFile))
                ?.mapNotNull { uri -> uri }
            val metadata = if (!metadataFile.isNullOrBlank()) {
                repository.local.readMetadata(metadataFile)
            } else {
                null
            }
            message.toArticleDto(uri = markdownFile, files, metadata)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getLocalArticles(): List<ArticleDto> {
        val results = mutableListOf<ArticleDto>()
        return try {
            var lastIndex = Long.MAX_VALUE
            do {
                val messages = repository.local.getSharedFiles(lastIndex)

                for (message in messages) {
                    results.add(getLocalArticle(message) ?: continue)
                }

                val last = messages.lastOrNull() ?: break
                lastIndex = last.message.id
            } while (last.isWithinNewsfeedPeriod() && results.size < NEWS_FEED_SIZE)

            results
        } catch (_: Exception) {
            results
        }
    }

    private suspend fun getWebArticles(): List<ArticleDto> {
        return try {
            val indexUrl = String.format(ARTICLES_INDEX_URL_TEMPLATE, Locale.getDefault().language)
            repository.remote.getArticles(indexUrl).map { article -> article.prettyFormat() }
        } catch (_: Exception) {
            listOf()
        }
    }

    suspend fun getFeed(): List<ArticleDto> = coroutineScope {
        val dbDeferred = async { runCatching { getLocalArticles() } }
        val webDeferred = async { runCatching { getWebArticles() } }
        val dbResult = dbDeferred.await().getOrElse { emptyList() }
        val webResult = webDeferred.await().getOrElse { emptyList() }
        weightedInterleave(
            sourcesList = listOf(webResult, dbResult),
            sourcesWeight = listOf(WEB_ARTICLES_FEED_WEIGHT, LOCAL_MEDIA_FEED_WEIGHT)
        )
    }
}
