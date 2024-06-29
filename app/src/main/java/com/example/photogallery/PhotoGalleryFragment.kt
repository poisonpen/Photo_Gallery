package com.example.photogallery

import androidx.fragment.app.Fragment
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.OneTimeWorkRequest
import com.example.photogallery.api.FlickrApi
import com.example.photogallery.databinding.FragmentPhotoGalleryBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Constraints
class PhotoGalleryFragment : Fragment() {
    private var _binding: FragmentPhotoGalleryBinding? = null
    private var searchView: SearchView?= null
    private val binding

        get() = checkNotNull(_binding) {
            "Connot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        binding.photoGrid.layoutManager = GridLayoutManager(context, 3)
        return binding.root
    }

    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val retrofit: Retrofit = Retrofit.Builder()
//            .baseUrl("https://www.flickr.com")
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .build()
//        val flickrApi: FlickrApi = retrofit.create<FlickrApi>()
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                photoGalleryViewModel.uiState.collect { state->
                    binding.photoGrid.adapter = PhotoListAdapter(state.images)
                    searchView?.setQuery(state.query, false)
                }
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        searchView = null
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val workRequest = OneTimeWorkRequest
            .Builder(PollWorker::class.java)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(requireContext())
            .enqueue(workRequest)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)
        Log.d("displayChangeHere", "startSearch")

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        searchView = searchItem.actionView as SearchView?
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("displayChangeHere", "QueryTextSubmit: $query")
                photoGalleryViewModel.setQuery(query ?: "")
                return true;
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("displayChangeHere", "QueryTextChange: $newText")
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}