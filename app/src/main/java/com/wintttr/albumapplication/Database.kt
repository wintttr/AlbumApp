package com.wintttr.albumapplication

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

@Entity
data class Album(
    @PrimaryKey val title: String
)

@Entity
data class Photo(
    val fileName: String,
    val albumTitle: String,
    val comment: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Dao
interface AlbumDao {
    @Insert
    suspend fun insertAlbum(album: Album)

    @Insert
    suspend fun insertPhoto(photo: Photo): Long

    @Query("SELECT * FROM album")
    suspend fun getAlbums(): List<Album>

    @Query("SELECT * FROM album WHERE title=:title")
    suspend fun getAlbum(title: String): Album

    @Query("SELECT * FROM photo WHERE albumTitle=:title")
    suspend fun getPhotos(title: String): List<Photo>

    @Query("SELECT * FROM photo WHERE id=:id")
    suspend fun getPhoto(id: Long): Photo

    @Update
    suspend fun updatePhoto(photo: Photo)
}

@Database(entities = [Album::class, Photo::class], version=1)
abstract class AlbumDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
}

private const val DATABASE_NAME = "album-database"

class AlbumRepository private constructor(context: Context) {
    private val database: AlbumDatabase = Room.databaseBuilder(
        context.applicationContext,
        AlbumDatabase::class.java,
        DATABASE_NAME
    ).build()

    suspend fun insertAlbum(title: String) = database.albumDao().insertAlbum(Album(title))

    suspend fun insertPhoto(photo: Photo) = database.albumDao().insertPhoto(photo)

    suspend fun getAlbums() = database.albumDao().getAlbums()

    suspend fun getAlbum(title: String) = database.albumDao().getAlbum(title)

    suspend fun getPhoto(id: Long) = database.albumDao().getPhoto(id)

    suspend fun updatePhoto(photo: Photo) = database.albumDao().updatePhoto(photo)

    suspend fun getPhotos(title: String) = database.albumDao().getPhotos(title)

    companion object {
        private var INSTANCE: AlbumRepository? = null

        fun initialize(context: Context) {
            INSTANCE = AlbumRepository(context)
        }

        fun get() = checkNotNull(INSTANCE)
    }
}