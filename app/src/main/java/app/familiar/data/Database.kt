package app.familiar.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

enum class Shelf { READING, WANT, DONE, PAUSED }

/** A book on the shelf. [page] is the last page read. */
@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String = "",
    val pages: Int,
    /** [app.familiar.domain.Genre] name. */
    val genre: String,
    /** [Shelf] name. */
    val shelf: String = Shelf.READING.name,
    val page: Int = 0,
    val added: Long,
    val started: Long? = null,
    val finished: Long? = null,
    /** 1..5 once finished, or null. */
    val rating: Int? = null,
    /** Cover picture address from a book catalogue, or empty for a drawn cover. */
    val cover: String = "",
) {
    val shelfOf: Shelf get() = runCatching { Shelf.valueOf(shelf) }.getOrDefault(Shelf.READING)
    val fraction: Float get() = if (pages <= 0) 0f else (page.toFloat() / pages).coerceIn(0f, 1f)
}

/** One sitting with a book: a ritual with the candle, or pages logged by hand. */
@Entity(tableName = "readings")
data class Reading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    /** The familiar that ate the mana. */
    val petId: Long,
    val start: Long,
    val end: Long,
    val fromPage: Int,
    val toPage: Int,
    val mana: Int,
    /** Read with the candle (a timed ritual) rather than logged afterwards. */
    val timed: Boolean,
    /** Seconds the phone lay face down. */
    val stillSec: Int = 0,
    /** Something from the book the slime swallowed. */
    val item: String = "",
    /** This sitting finished the book. */
    val finishedBook: Boolean = false,
) {
    val pages: Int get() = (toPage - fromPage).coerceAtLeast(0)
    val minutes: Int get() = ((end - start) / 60_000).toInt()
}

/** A familiar that has joined the den. [mana] is everything it has eaten. */
@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** [app.familiar.domain.Species] name. */
    val species: String,
    val name: String,
    val joined: Long,
    val mana: Int = 0,
    val active: Boolean = false,
)

class Snapshot(val books: List<Book>, val readings: List<Reading>, val pets: List<Pet>)

@Dao
abstract class FamiliarDao {
    @Query("SELECT * FROM books ORDER BY added DESC")
    abstract fun observeBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY added DESC")
    abstract suspend fun books(): List<Book>

    @Query("SELECT * FROM books WHERE id = :id")
    abstract suspend fun book(id: Long): Book?

    @Insert abstract suspend fun insertBook(b: Book): Long
    @Update abstract suspend fun updateBook(b: Book)
    @Delete abstract suspend fun deleteBook(b: Book)

    @Query("SELECT * FROM readings ORDER BY start")
    abstract fun observeReadings(): Flow<List<Reading>>

    @Query("SELECT * FROM readings ORDER BY start")
    abstract suspend fun readings(): List<Reading>

    @Insert abstract suspend fun insertReading(r: Reading): Long
    @Update abstract suspend fun updateReading(r: Reading)
    @Delete abstract suspend fun deleteReading(r: Reading)

    @Query("DELETE FROM readings WHERE bookId = :bookId")
    abstract suspend fun deleteReadingsOf(bookId: Long)

    @Query("SELECT * FROM pets ORDER BY joined")
    abstract fun observePets(): Flow<List<Pet>>

    @Query("SELECT * FROM pets ORDER BY joined")
    abstract suspend fun pets(): List<Pet>

    @Query("SELECT * FROM pets WHERE active = 1 LIMIT 1")
    abstract suspend fun activePet(): Pet?

    @Insert abstract suspend fun insertPet(p: Pet): Long
    @Update abstract suspend fun updatePet(p: Pet)

    @Query("UPDATE pets SET mana = mana + :mana WHERE id = :id")
    abstract suspend fun feed(id: Long, mana: Int)

    @Query("UPDATE pets SET active = (id = :id)")
    abstract suspend fun activate(id: Long)

    /** Removes a book with its sittings. */
    @Transaction
    open suspend fun removeBook(b: Book) {
        deleteReadingsOf(b.id)
        deleteBook(b)
    }

    @Transaction
    open suspend fun snapshot() = Snapshot(books(), readings(), pets())

    @Query("DELETE FROM books") protected abstract suspend fun clearBooks()
    @Query("DELETE FROM readings") protected abstract suspend fun clearReadings()
    @Query("DELETE FROM pets") protected abstract suspend fun clearPets()
    @Insert protected abstract suspend fun insertBooks(l: List<Book>)
    @Insert protected abstract suspend fun insertReadings(l: List<Reading>)
    @Insert protected abstract suspend fun insertPets(l: List<Pet>)

    @Transaction
    open suspend fun replaceAll(s: Snapshot) {
        clearBooks(); clearReadings(); clearPets()
        insertBooks(s.books); insertReadings(s.readings); insertPets(s.pets)
    }
}

@Database(entities = [Book::class, Reading::class, Pet::class], version = 1, exportSchema = true)
abstract class FamiliarDatabase : RoomDatabase() {
    abstract fun dao(): FamiliarDao

    companion object {
        fun create(context: Context): FamiliarDatabase =
            Room.databaseBuilder(context.applicationContext, FamiliarDatabase::class.java, "familiar.db").build()
    }
}
