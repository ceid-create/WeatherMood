package com.charbeljoe.weathermood.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.charbeljoe.weathermood.databinding.FragmentLocationPickerBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar

class LocationPickerFragment : Fragment() {

    private var _binding: FragmentLocationPickerBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var selectedLatLng: LatLng? = null
    private var isSatellite = false

    private lateinit var placesClient: PlacesClient
    private lateinit var autocompleteAdapter: AutocompleteAdapter
    private var sessionToken = AutocompleteSessionToken.newInstance()
    private val debounceHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesClient = Places.createClient(requireContext())

        // Autocomplete RecyclerView
        autocompleteAdapter = AutocompleteAdapter { prediction ->
            binding.autocompleteList.visibility = View.GONE
            binding.searchInput.setText(prediction.getPrimaryText(null))
            hideKeyboard()
            fetchPlaceCoordinates(prediction.placeId)
            sessionToken = AutocompleteSessionToken.newInstance()
        }
        binding.autocompleteList.layoutManager = LinearLayoutManager(requireContext())
        binding.autocompleteList.adapter = autocompleteAdapter

        // Search with debounce
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: return
                debounceHandler.removeCallbacksAndMessages(null)
                if (query.length >= 2) {
                    debounceHandler.postDelayed({ fetchPredictions(query) }, 300)
                } else {
                    binding.autocompleteList.visibility = View.GONE
                }
            }
        })

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.autocompleteList.visibility = View.GONE
                hideKeyboard()
                true
            } else false
        }

        // Map
        binding.pickerMapView.onCreate(savedInstanceState)
        binding.pickerMapView.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = true

            // Open at the last known location so the user doesn't start at world view
            val prefs = requireContext()
                .getSharedPreferences("weathermood_prefs", android.content.Context.MODE_PRIVATE)
            val lastLat = prefs.getFloat("last_lat", 0f).toDouble()
            val lastLng = prefs.getFloat("last_lon", 0f).toDouble()
            if (lastLat != 0.0 || lastLng != 0.0) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLat, lastLng), 14f))
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 2f))
            }

            map.setOnMapClickListener { latLng ->
                placeMarker(map, latLng)
                binding.autocompleteList.visibility = View.GONE
            }
        }

        // Satellite/Hybrid toggle
        binding.satelliteToggle.setOnClickListener {
            isSatellite = !isSatellite
            googleMap?.mapType = if (isSatellite) GoogleMap.MAP_TYPE_HYBRID
                                  else GoogleMap.MAP_TYPE_NORMAL
        }

        // Confirm
        binding.confirmLocationButton.setOnClickListener {
            val latLng = selectedLatLng ?: return@setOnClickListener
            setFragmentResult(
                REQUEST_KEY,
                bundleOf(
                    ARG_LAT to latLng.latitude,
                    ARG_LNG to latLng.longitude
                )
            )
            findNavController().popBackStack()
        }
    }

    private fun fetchPredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(sessionToken)
            .setQuery(query)
            .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                autocompleteAdapter.submitPredictions(predictions)
                binding.autocompleteList.visibility =
                    if (predictions.isEmpty()) View.GONE else View.VISIBLE
            }
            .addOnFailureListener {
                binding.autocompleteList.visibility = View.GONE
            }
    }

    private fun fetchPlaceCoordinates(placeId: String) {
        val request = FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.LAT_LNG))
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val latLng = response.place.latLng ?: return@addOnSuccessListener
                googleMap?.let { placeMarker(it, LatLng(latLng.latitude, latLng.longitude)) }
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Could not load place location", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun placeMarker(map: GoogleMap, latLng: LatLng) {
        selectedLatLng = latLng
        map.clear()
        map.addMarker(MarkerOptions().position(latLng))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        binding.confirmLocationButton.isEnabled = true
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
        binding.searchInput.clearFocus()
    }

    override fun onResume() { super.onResume(); binding.pickerMapView.onResume() }
    override fun onPause() { super.onPause(); binding.pickerMapView.onPause() }
    override fun onStop() { super.onStop(); binding.pickerMapView.onStop() }
    override fun onDestroyView() {
        debounceHandler.removeCallbacksAndMessages(null)
        binding.pickerMapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.pickerMapView?.onSaveInstanceState(outState)
    }
    override fun onLowMemory() { super.onLowMemory(); _binding?.pickerMapView?.onLowMemory() }

    companion object {
        const val REQUEST_KEY = "location_picker_result"
        const val ARG_LAT = "picked_lat"
        const val ARG_LNG = "picked_lng"
    }
}
