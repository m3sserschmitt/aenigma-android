package ro.aenigma.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.apache.commons.rng.UniformRandomProvider
import org.apache.commons.rng.sampling.distribution.AliasMethodDiscreteSampler
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler
import org.apache.commons.rng.simple.RandomSource
import ro.aenigma.data.Repository
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toArticle
import ro.aenigma.models.Article
import ro.aenigma.util.Constants.Companion.ARTICLES_FEED_WEIGHT
import ro.aenigma.util.Constants.Companion.ARTICLES_INDEX_URL_TEMPLATE
import ro.aenigma.util.Constants.Companion.SHARED_FILES_FEED_WEIGHT
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.listOf
import kotlin.math.abs

@ViewModelScoped
class FeedSampler @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val repository: Repository
) {
    companion object {
        @JvmStatic
        fun <T> weightedInterleave(
            sourceLists: List<List<T>>,
            weights: List<Double>,
            rng: UniformRandomProvider = RandomSource.MT.create()
        ): List<T> {

            require(sourceLists.size == weights.size && sourceLists.isNotEmpty()) {
                "sourceLists and weights must be non‑empty and same size"
            }
            require(weights.all { it > 0.0 }) { "all weights must be >0" }
            require(abs(weights.sum()) > 0) { "sum of weights must be >0" }

            val queues = sourceLists.map { it.toMutableList() }.toMutableList()
            val liveWeights = weights.toMutableList()

            fun buildSampler(): SharedStateDiscreteSampler? {
                val total = liveWeights.sum()
                val probs = liveWeights.map { it / total }.toDoubleArray()
                return AliasMethodDiscreteSampler.of(rng, probs)
            }

            var sampler: SharedStateDiscreteSampler? = buildSampler() ?: return listOf()

            val outCapacity = queues.sumOf { it.size }
            val out = HashSet<T>(outCapacity)

            while (out.size < outCapacity) {

                val idx = sampler?.sample() ?: 0

                if (queues[idx].isEmpty()) {
                    queues.removeAt(idx)
                    liveWeights.removeAt(idx)
                    if (queues.isNotEmpty()) {
                        sampler = buildSampler() ?: break
                    }
                    continue
                }

                out.add(queues[idx].removeAt(0))
            }
            return out.toList()
        }
    }

    fun getFeed(): Flow<List<Article>> {
        val latestSharedFilesFlow = repository.local.getLatestSharedFiles().catch {
            emit(listOf())
        }.map { items -> items.map { m -> m.toArticle(applicationContext) } }
        val indexUrl =
            String.format(ARTICLES_INDEX_URL_TEMPLATE, Locale.getDefault().language)
        val articlesFlow = repository.remote.getArticles(indexUrl).catch {
            emit(listOf())
        }
        return articlesFlow.combine(latestSharedFilesFlow) { a, b ->
            var i = 0L
            weightedInterleave(
                listOf(a, b),
                listOf(ARTICLES_FEED_WEIGHT, SHARED_FILES_FEED_WEIGHT)
            ).map { article -> i++
                article.copy(id = i)
            }
        }.catch {
            emit(listOf())
        }
    }
}
