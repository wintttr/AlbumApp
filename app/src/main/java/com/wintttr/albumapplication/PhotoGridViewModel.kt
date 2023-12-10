package com.wintttr.albumapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch    

class PhotoGridViewModel(
    private val albumTitle: String
) : ViewModel() {
    private val _photoList: MutableStateFlow<List<Photo>> = MutableStateFlow(emptyList())
    val photoList get() = _photoList.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            AlbumRepository.get().getPhotos(albumTitle).collect {
                _photoList.value = it
            }
        }
    }

    fun getBitmap(context: Context, photo: Photo): Bitmap {
        if (!AlbumRepository.get().photoCache.containsKey(photo)) {
            AlbumRepository.get().photoCache[photo] = BitmapFactory.decodeStream(
                context.openFileInput(photo.fileName)
            )
        }

        return AlbumRepository.get().photoCache[photo]!!
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun insertPhoto(photo: Photo) = GlobalScope.launch(Dispatchers.IO) {
        AlbumRepository.get().insertPhoto(photo)
    }
}

class PhotoGridViewModelFactory(
    private val albumTitle: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotoGridViewModel(albumTitle) as T
    }
}