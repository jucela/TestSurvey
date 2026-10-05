package gob.inei.appprueba.data.repositories

import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.databases.toDomain
import gob.inei.appprueba.data.databases.toEntity
import gob.inei.appprueba.data.sources.CatalogParser
import gob.inei.appprueba.data.sources.RawCatalogDataSource
import gob.inei.appprueba.domain.entities.Alternative
import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.FlowRule
import gob.inei.appprueba.domain.entities.Question
import gob.inei.appprueba.domain.repositories.SurveyRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SurveyRepositoryImpl @Inject constructor(
    private val database: EncuestaDatabase,
    private val rawCatalog: RawCatalogDataSource
) : SurveyRepository {

    override suspend fun seedIfEmpty() {
        val questions = database.questionDao().count()
        val alternatives = database.alternativeDao().count()
        val rules = database.flowRuleDao().count()
        if (questions > 0 && alternatives > 0 && rules > 0) return

        database.alternativeDao().deleteAll()
        database.flowRuleDao().deleteAll()
        database.questionDao().deleteAll()

        val seed = CatalogParser.parse(rawCatalog.catalogo(), rawCatalog.alternativas(), rawCatalog.flujo())
        database.questionDao().insertAll(seed.questions.map { it.toEntity() })
        database.alternativeDao().insertAll(
            seed.alternatives.map { it.toEntity(CatalogParser.ANIO_BASE) }
        )
        database.flowRuleDao().insertAll(seed.flowRules.map { it.toEntity() })
    }

    override fun observeQuestions(): Flow<List<Question>> =
        database.questionDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getQuestions(): List<Question> =
        database.questionDao().getAll().map { it.toDomain() }

    override suspend fun getQuestion(id: String): Question? =
        database.questionDao().getById(id)?.toDomain()

    override suspend fun getChildren(parentId: String): List<Question> =
        getQuestions()
            .filter { it.padre == parentId && !it.esPrincipal }
            .sortedWith(compareBy({ it.orden ?: Int.MAX_VALUE }, { it.sortOrder }))

    override fun observeAlternatives(): Flow<Map<String, List<Alternative>>> =
        database.alternativeDao().observeAll().map { list -> list.map { it.toDomain() }.groupBy { it.pregunta } }

    override suspend fun getAllAlternatives(): Map<String, List<Alternative>> =
        database.alternativeDao().getAll().map { it.toDomain() }.groupBy { it.pregunta }

    override suspend fun getAlternatives(preguntaId: String): List<Alternative> =
        database.alternativeDao().getByPregunta(preguntaId).map { it.toDomain() }

    override fun observeFlowRules(): Flow<List<FlowRule>> =
        database.flowRuleDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getFlowRules(): List<FlowRule> =
        database.flowRuleDao().getAll().map { it.toDomain() }

    override fun observeAnswers(): Flow<Map<String, Answer>> =
        database.answerDao().observeAll().map { list -> list.associate { it.preguntaId to it.toDomain() } }

    override suspend fun getAnswers(): Map<String, Answer> =
        database.answerDao().getAll().associate { it.preguntaId to it.toDomain() }

    override suspend fun saveAnswer(answer: Answer) {
        database.answerDao().upsert(answer.toEntity())
    }

    override suspend fun clearAnswers() {
        database.answerDao().deleteAll()
    }
}
