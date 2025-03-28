package com.tripwizard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [Trip::class, Attraction::class, Label::class, Notification::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TripWizardDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun attractionDao(): AttractionDao
    abstract fun labelDao(): LabelDao
    abstract fun notificationDao(): NotificationDao

    companion object {

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context,
                TripWizardDatabase::class.java,
                "tripwizard_database"
            ).fallbackToDestructiveMigration().build()

        @Volatile
        private var Instance: TripWizardDatabase? = null

        fun getDatabase(context: Context): TripWizardDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TripWizardDatabase::class.java, "tripwizard_database")
                    /**
                     * Setting this option in your app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}