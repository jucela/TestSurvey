package gob.inei.appprueba.domain.repositories

import gob.inei.appprueba.domain.entities.Alternative
import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.FlowRule
import gob.inei.appprueba.domain.entities.Question
import kotlinx.coroutines.flow.Flow

interface SurveyRepository {
    suspend fun seedIfEmpty()
    fun observeQuestions(): Flow<List<Question>>
    suspend fun getQuestions(): List<Question>
    suspend fun getQuestion(id: String): Question?
    suspend fun getChildren(parentId: String): List<Question>
    fun observeAlternatives(): Flow<Map<String, List<Alternative>>>
    suspend fun getAllAlternatives(): Map<String, List<Alternative>>
    suspend fun getAlternatives(preguntaId: String): List<Alternative>
    fun observeFlowRules(): Flow<List<FlowRule>>
    suspend fun getFlowRules(): List<FlowRule>
    fun observeAnswers(): Flow<Map<String, Answer>>
    suspend fun getAnswers(): Map<String, Answer>
    suspend fun saveAnswer(answer: Answer)
    suspend fun clearAnswers()
}
