package com.wintttr.albumapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.wintttr.albumapplication.databinding.FragmentPhotoDescriptionBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoDescriptionFragment : Fragment() {
    private var _binding: FragmentPhotoDescriptionBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<PhotoDescriptionFragmentArgs>()
    private lateinit var photo: Photo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoDescriptionBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap: Bitmap

            withContext(Dispatchers.IO) {
                photo = AlbumRepository.get().getPhoto(args.id)

                bitmap = BitmapFactory.decodeStream(
                    requireContext().applicationContext.openFileInput(photo.fileName)
                )
            }

            binding.photoImageView.setImageBitmap(bitmap)
            binding.photoDescription.setText(photo.comment)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDestroyView() {
        val newPhoto = photo.copy(comment = binding.photoDescription.text.toString())

        GlobalScope.launch(Dispatchers.IO) {
            AlbumRepository.get().updatePhoto(newPhoto)
        }

        _binding = null

        super.onDestroyView()
    }
}