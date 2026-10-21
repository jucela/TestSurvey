package gob.inei.appprueba

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.sources.MaestrasSeeder

@HiltAndroidApp
class EncuestaApp : Application() {

    init {
        android.util.Log.d("EncuestaApp", "EncuestaApp INIT")
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("EncuestaApp", "EncuestaApp onCreate INICIO")
        sembrarMaestrasSiVacio()
        android.util.Log.d("EncuestaApp", "EncuestaApp onCreate FIN")
    }

    private fun sembrarMaestrasSiVacio() {
        try {
            val seeder = EntryPointAccessors.fromApplication(this, MaestrasSeederEntryPoint::class.java).maestrasSeeder()
            val db = EntryPointAccessors.fromApplication(this, DatabaseEntryPoint::class.java).database()
            
            val writableDb = db.openHelper.writableDatabase
            seeder.sembrarSiVacio(writableDb)
            Log.i("EncuestaApp", "Verificación de siembra de maestras completada")
        } catch (e: Exception) {
            Log.e("EncuestaApp", "Error en siembra de maestras", e)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MaestrasSeederEntryPoint {
        fun maestrasSeeder(): MaestrasSeeder
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DatabaseEntryPoint {
        fun database(): EncuestaDatabase
    }
}