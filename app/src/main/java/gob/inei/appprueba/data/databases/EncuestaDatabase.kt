package gob.inei.appprueba.data.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import gob.inei.appprueba.data.databases.dao.AlternativeDao
import gob.inei.appprueba.data.databases.dao.AnswerDao
import gob.inei.appprueba.data.databases.dao.FlowRuleDao
import gob.inei.appprueba.data.databases.dao.QuestionDao
import gob.inei.appprueba.data.databases.dao.UserDao
import gob.inei.appprueba.data.databases.entities.AlternativeEntity
import gob.inei.appprueba.data.databases.entities.AnswerEntity
import gob.inei.appprueba.data.databases.entities.FlowRuleEntity
import gob.inei.appprueba.data.databases.entities.QuestionEntity
import gob.inei.appprueba.data.databases.entities.UserEntity

@Database(
    entities = [
        QuestionEntity::class,
        AlternativeEntity::class,
        FlowRuleEntity::class,
        AnswerEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class EncuestaDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun alternativeDao(): AlternativeDao
    abstract fun flowRuleDao(): FlowRuleDao
    abstract fun answerDao(): AnswerDao
    abstract fun userDao(): UserDao

    companion object {
        /** Solo crea la tabla de usuarios; no toca las tablas de la encuesta (RF-11). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `users` (" +
                        "`usuario` TEXT NOT NULL, " +
                        "`hash` TEXT NOT NULL, " +
                        "`sal` TEXT NOT NULL, " +
                        "`iteraciones` INTEGER NOT NULL, " +
                        "`creadoEn` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`usuario`))"
                )
            }
        }
    }
}
