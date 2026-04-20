package com.charbeljoe.weathermood.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.charbeljoe.weathermood.R
import com.charbeljoe.weathermood.data.remote.models.PlaceResult
import com.charbeljoe.weathermood.databinding.BottomSheetNavigationBinding
import com.charbeljoe.weathermood.databinding.FragmentHomeBinding
import com.charbeljoe.weathermood.util.LocationHelper
import com.charbeljoe.weathermood.util.isOnline
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // USE activityViewModels() to keep the ViewModel alive across fragments
    private val viewModel: HomeViewModel by activityViewModels()
    private val locationHelper by lazy { LocationHelper(requireContext()) }
    private lateinit var adapter: PlaceListAdapter

    private var mapViewRef: MapView? = null
    private var googleMap: GoogleMap? = null
    private var isSatellite = false
    private val markerPlaceMap = HashMap<Marker, PlaceResult>()

    companion object {
        private const val REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        mapViewRef = _binding!!.mapView
        mapViewRef!!.onCreate(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.offlineBanner.visibility =
            if (isOnline(requireContext())) View.GONE else View.VISIBLE

        adapter = PlaceListAdapter { place ->
            val weatherCondition = viewModel.displayCondition.value ?: ""
            val bundle = Bundle().apply {
                putString("placeName", place.name)
                putString("placeVicinity", place.vicinity)
                putDouble("placeLat", place.geometry.location.lat)
                putDouble("placeLng", place.geometry.location.lng)
                putString("placeCategory", place.types.firstOrNull() ?: "")
                putString("placePhotoRef", place.photos?.firstOrNull()?.photoReference ?: "")
                putDouble("placeRating", place.rating ?: 0.0)
                putString("weatherCondition", weatherCondition)
            }
            findNavController().navigate(R.id.navigation_place_detail, bundle)
        }
        binding.placesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.placesRecyclerView.adapter = adapter

        binding.viewToggleGroup.check(R.id.btnListView)
        binding.viewToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            if (checkedId == R.id.btnListView) {
                binding.placesRecyclerView.visibility = View.VISIBLE
                binding.mapView.visibility = View.GONE
                binding.mapSatelliteToggle.visibility = View.GONE
            } else {
                binding.placesRecyclerView.visibility = View.GONE
                binding.mapView.visibility = View.VISIBLE
                binding.mapSatelliteToggle.visibility = View.VISIBLE
                showPlacesOnMap()
            }
        }

        binding.mapSatelliteToggle.setOnClickListener {
            isSatellite = !isSatellite
            googleMap?.mapType = if (isSatellite) GoogleMap.MAP_TYPE_HYBRID
                                  else GoogleMap.MAP_TYPE_NORMAL
        }

        binding.chooseLocationButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_location_picker)
        }

        setFragmentResultListener(LocationPickerFragment.REQUEST_KEY) { _, bundle ->
            val lat = bundle.getDouble(LocationPickerFragment.ARG_LAT)
            val lng = bundle.getDouble(LocationPickerFragment.ARG_LNG)
            saveLastLocation(lat, lng)
            viewModel.isDataLoaded = false // Force reload for new location
            viewModel.loadData(lat, lng)
        }

        // --- Filter Setup ---
        
        viewModel.availableDates.observe(viewLifecycleOwner) { dates ->
            val dateAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, dates)
            dateAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.dateSpinner.adapter = dateAdapter
            
            viewModel.selectedDate?.let { 
                val pos = dates.indexOf(it)
                if (pos >= 0) binding.dateSpinner.setSelection(pos)
            }
        }

        binding.dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDate = parent?.getItemAtPosition(position) as? String
                selectedDate?.let { viewModel.onDateSelected(it) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewModel.availableHours.observe(viewLifecycleOwner) { hours ->
            val hourAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, hours)
            hourAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.hourSpinner.adapter = hourAdapter

            viewModel.selectedHour?.let {
                val pos = hours.indexOf(it)
                if (pos >= 0) binding.hourSpinner.setSelection(pos)
            }
        }

        binding.hourSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedHour = parent?.getItemAtPosition(position) as? String
                selectedHour?.let { viewModel.onHourSelected(it) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- Weather UI Updates ---

        viewModel.displayTemp.observe(viewLifecycleOwner) { temp ->
            binding.tempText.text = temp
        }

        viewModel.displayCondition.observe(viewLifecycleOwner) { condition ->
            binding.conditionText.text = condition
        }

        viewModel.displayIcon.observe(viewLifecycleOwner) { iconCode ->
            if (iconCode.isNotEmpty()) {
                Glide.with(this)
                    .load("https://openweathermap.org/img/wn/$iconCode@2x.png")
                    .into(binding.weatherIcon)
            }
        }

        viewModel.places.observe(viewLifecycleOwner) { places ->
            adapter.submitList(places)
            if (binding.mapView.visibility == View.VISIBLE) {
                showPlacesOnMap()
            }
        }

        viewModel.weatherDescription.observe(viewLifecycleOwner) { description ->
            binding.weatherDescriptionText.text = description
            binding.weatherDescriptionText.visibility = View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            message ?: return@observe
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }

        // --- Persistent Loading ---
        if (!viewModel.isDataLoaded) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationHelper.getCurrentLocation(requireContext()) { lat, lon ->
                    if (!isAdded) return@getCurrentLocation
                    saveLastLocation(lat, lon)
                    viewModel.loadData(lat, lon)
                }
            } else {
                @Suppress("DEPRECATION")
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            }
        }
    }

    private fun showPlacesOnMap() {
        mapViewRef?.getMapAsync { map ->
            googleMap = map
            map.clear()
            markerPlaceMap.clear()
            map.uiSettings.isZoomControlsEnabled = true

            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            }

            val places = viewModel.places.value ?: return@getMapAsync
            if (places.isEmpty()) return@getMapAsync

            places.forEach { place ->
                val position = LatLng(place.geometry.location.lat, place.geometry.location.lng)
                val marker = map.addMarker(MarkerOptions().position(position).title(place.name))
                if (marker != null) markerPlaceMap[marker] = place
            }

            map.setOnMarkerClickListener { marker ->
                val place = markerPlaceMap[marker] ?: return@setOnMarkerClickListener false
                showNavigationBottomSheet(place)
                true
            }

            val first = places.first()
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(first.geometry.location.lat, first.geometry.location.lng), 14f
                )
            )
        }
    }

    private fun showNavigationBottomSheet(place: PlaceResult) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetNavigationBinding.inflate(layoutInflater)

        sheetBinding.bsPlaceName.text = place.name
        sheetBinding.bsPlaceAddress.text = place.vicinity

        val startLat = viewModel.currentLat
        val startLng = viewModel.currentLon

        if (startLat != 0.0 || startLng != 0.0) {
            val results = FloatArray(1)
            Location.distanceBetween(
                startLat, startLng,
                place.geometry.location.lat, place.geometry.location.lng,
                results
            )
            val distanceM = results[0]
            val distanceText = if (distanceM >= 1000) "%.1f km".format(distanceM / 1000)
                               else "${distanceM.toInt()} m"
            val etaMinutes = (distanceM / 1000.0 / 30.0 * 60).toInt().coerceAtLeast(1)
            sheetBinding.bsDistance.text = "Distance: $distanceText"
            sheetBinding.bsEta.text = "~$etaMinutes min by car"
        }

        sheetBinding.bsNavigateButton.setOnClickListener {
            val destLat = place.geometry.location.lat
            val destLng = place.geometry.location.lng
            
            val uriString = "https://www.google.com/maps/dir/?api=1&origin=$startLat,$startLng&destination=$destLat,$destLng&travelmode=driving"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
            intent.setPackage("com.google.android.apps.maps")
            
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
                startActivity(webIntent)
            }
            dialog.dismiss()
        }

        dialog.setContentView(sheetBinding.root)
        dialog.show()
    }

    private fun saveLastLocation(lat: Double, lon: Double) {
        context?.getSharedPreferences("weathermood_prefs", Context.MODE_PRIVATE)
            ?.edit()
            ?.putFloat("last_lat", lat.toFloat())
            ?.putFloat("last_lon", lon.toFloat())
            ?.apply()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            locationHelper.getCurrentLocation(requireContext()) { lat, lon ->
                if (!isAdded) return@getCurrentLocation
                saveLastLocation(lat, lon)
                viewModel.loadData(lat, lon)
            }
        }
    }

    override fun onResume() { super.onResume(); mapViewRef?.onResume() }
    override fun onPause() { super.onPause(); mapViewRef?.onPause() }
    override fun onStop() { super.onStop(); mapViewRef?.onStop() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapViewRef?.onSaveInstanceState(outState)
    }
    override fun onLowMemory() { super.onLowMemory(); mapViewRef?.onLowMemory() }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        mapViewRef?.onDestroy()
        mapViewRef = null
        super.onDestroy()
    }
}
