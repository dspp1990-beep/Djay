package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.IrrigationLogEntity
import com.example.data.model.FieldValveLogEntity
import com.example.data.model.IrrigationSystemEntity
import com.example.data.model.PumpHouseEntity
import com.example.data.model.ScheduleConfigEntity
import com.example.data.model.SubmainValveEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PumpHouseEntity::class,
        IrrigationSystemEntity::class,
        SubmainValveEntity::class,
        ScheduleConfigEntity::class,
        IrrigationLogEntity::class,
        FieldValveLogEntity::class
    ],
    // v4: real valve coordinates from the Jain drawing + field valve log (destructive re-seed)
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun irrigationDao(): IrrigationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_irrigation_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDatabase(dao: IrrigationDao) {
            val count = dao.getValvesCount()
            if (count == 0) {
                dao.insertPumpHouses(SeedData.getInitialPumpHouses())
                dao.insertSystems(SeedData.getInitialSystems())
                dao.insertValves(SeedData.getInitialValves())
                dao.insertScheduleConfigs(SeedData.getInitialScheduleConfigs())
                dao.insertLog(
                    IrrigationLogEntity(
                        pumpHouseId = "PH-15",
                        systemId = "SYS-32",
                        submainTag = "P32-1, P32-2",
                        action = "SHIFT_1_ACTIVE",
                        details = "Initialized automated shift 1 operation (2.0 hrs). Normal discharge 51.75 lps.",
                        sectionalFlowLps = 51.75
                    )
                )
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.irrigationDao())
                    }
                }
            }
        }
    }
}
