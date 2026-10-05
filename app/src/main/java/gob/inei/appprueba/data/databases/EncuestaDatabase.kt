package gob.inei.appprueba.data.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import gob.inei.appprueba.data.databases.dao.AlternativeDao
import gob.inei.appprueba.data.databases.dao.AnswerDao
import gob.inei.appprueba.data.databases.dao.FlowRuleDao
import gob.inei.appprueba.data.databases.dao.QuestionDao
import gob.inei.appprueba.data.databases.entities.AlternativeEntity
import gob.inei.appprueba.data.databases.entities.AnswerEntity
import gob.inei.appprueba.data.databases.entities.FlowRuleEntity
import gob.inei.appprueba.data.databases.entities.QuestionEntity

@Database(
    entities = [QuestionEntity::class, AlternativeEntity::class, FlowRuleEntity::class, AnswerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EncuestaDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun alternativeDao(): AlternativeDao
    abstract fun flowRuleDao(): FlowRuleDao
    abstract fun answerDao(): AnswerDao
}
