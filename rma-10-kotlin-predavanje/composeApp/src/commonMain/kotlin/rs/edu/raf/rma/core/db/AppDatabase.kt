package rs.edu.raf.rma.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import rs.edu.raf.rma.core.db.converters.DateConverters
import rs.edu.raf.rma.posts.db.CategoryEntity
import rs.edu.raf.rma.posts.db.PostCategoryCrossRef
import rs.edu.raf.rma.posts.db.PostDao
import rs.edu.raf.rma.posts.db.PostDetailsEntity
import rs.edu.raf.rma.posts.db.PostEntity
import rs.edu.raf.rma.showtime.quiz.data.db.QuizDao
import rs.edu.raf.rma.showtime.quiz.data.db.QuizMovieEntity
import rs.edu.raf.rma.showtime.quiz.data.db.QuizResultEntity

@Database(
    entities = [
        PostEntity::class,
        PostDetailsEntity::class,
        CategoryEntity::class,
        PostCategoryCrossRef::class,
        QuizMovieEntity::class,
        QuizResultEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(DateConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun quizDao(): QuizDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun buildAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
