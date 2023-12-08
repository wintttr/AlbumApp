package com.wintttr.albumapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wintttr.albumapplication.databinding.FragmentPhotoGridBinding
import com.wintttr.albumapplication.databinding.ViewHolderPhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private class PhotoViewHolder(
    private val binding: ViewHolderPhotoBinding,
    private val onClick: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(photoId: Long, bitmap: Bitmap) {
        binding.root.setImageBitmap(bitmap)

        binding.root.setOnClickListener {
            onClick(photoId)
        }
    }
}

private class PhotoGridAdapter(
    private val onClick: (Long) -> Unit
): RecyclerView.Adapter<PhotoViewHolder>() {
    private val idList = mutableListOf<Long>()
    private val innerList = mutableListOf<Bitmap>()

    fun add(photoId: Long, bitmap: Bitmap) {
        idList.add(photoId)
        innerList.add(bitmap)
        notifyItemInserted(innerList.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ViewHolderPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClick
        )
    }

    override fun getItemCount() = innerList.size

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(idList[position], innerList[position])
    }
}

class PhotoGridFragment : Fragment() {
    private var _binding: FragmentPhotoGridBinding? = null
    private val binding get() = checkNotNull(_binding)
    private val args by navArgs<PhotoGridFragmentArgs>()

    private var adapter: PhotoGridAdapter? = null

    private var photoName: String? = null

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {
        if(it && photoName != null && adapter != null) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val photoId = AlbumRepository.get().insertPhoto(Photo(
                    photoName!!,
                    args.albumTitle
                ))

                val bitmap = BitmapFactory.decodeStream(
                    requireContext().applicationContext.openFileInput(photoName)
                )

                withContext(Dispatchers.Main) {
                    adapter!!.add(photoId, bitmap)
                }
            }
        } else {
            Toast.makeText(requireContext(), "Что-то пошло не так((", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhoto() {
        val id = UUID.randomUUID()

        photoName = "${id}.jpg"

        val photoFile = File(
            requireContext().applicationContext.filesDir,
            photoName!!
        )

        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "com.wintttr.albumapplication.fileprovider",
            photoFile
        )

        takePhotoLauncher.launch(photoUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val photoList = AlbumRepository.get().getPhotos(args.albumTitle)

            adapter = PhotoGridAdapter {
                findNavController().navigate(PhotoGridFragmentDirections.navigatePhotoDescription(it))
            }

            withContext(Dispatchers.Main) {
                binding.photoGrid.adapter = adapter
            }

            // Аыаыаы костыль
            for(photo in photoList) {
                val bitmap = BitmapFactory.decodeStream(
                    requireContext().applicationContext.openFileInput(
                        photo.fileName
                    )
                )

                withContext(Dispatchers.Main) {
                    adapter!!.add(photo.id, bitmap)
                }
            }
        }

        // Настраиваем грид
        binding.photoGrid.layoutManager = GridLayoutManager(requireContext(), 2)

        // Настраиваем тулбар
        binding.photoGridToolbar.title = args.albumTitle
        binding.photoGridToolbar.inflateMenu(R.menu.menu_photo_grid)
        binding.photoGridToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.take_photo_item -> {
                    takePhoto()
                    true
                }
                else -> {
                    Toast.makeText(requireContext(), "ЕЩЁ НЕ ГОТОВО", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter = null
    }
}