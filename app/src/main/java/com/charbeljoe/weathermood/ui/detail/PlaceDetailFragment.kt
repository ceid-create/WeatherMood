package com.charbeljoe.weathermood.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.charbeljoe.weathermood.BuildConfig
import com.charbeljoe.weathermood.data.local.FavoritePlace
import com.charbeljoe.weathermood.databinding.FragmentPlaceDetailBinding
import com.charbeljoe.weathermood.ui.favorites.FavoritesViewModel

class PlaceDetailFragment : Fragment() {

    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding get() = _binding!!

    private val favoritesViewModel: FavoritesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("placeName") ?: ""
        val vicinity = arguments?.getString("placeVicinity") ?: ""
        val lat = arguments?.getDouble("placeLat") ?: 0.0
        val lng = arguments?.getDouble("placeLng") ?: 0.0
        val category = arguments?.getString("placeCategory") ?: ""
        val photoRef = arguments?.getString("placePhotoRef") ?: ""
        val rating = arguments?.getDouble("placeRating") ?: 0.0
        val weatherCondition = arguments?.getString("weatherCondition") ?: ""

        binding.placeNameText.text = name
        binding.placeAddressText.text = vicinity
        binding.placeRatingText.text = if (rating > 0) "Rating: $rating" else "Rating: N/A"

        if (photoRef.isNotEmpty()) {
            val photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=800&photo_reference=$photoRef&key=${BuildConfig.GOOGLE_PLACES_API_KEY}"
            Glide.with(this)
                .load(photoUrl)
                .placeholder(com.charbeljoe.weathermood.R.drawable.ic_launcher_background)
                .error(com.charbeljoe.weathermood.R.drawable.ic_launcher_background)
                .into(binding.placeImage)
        }

        favoritesViewModel.findFavorite(name, lat, lng).observe(viewLifecycleOwner) { existing ->
            if (existing != null) {
                binding.addToFavoritesButton.visibility = View.GONE
                binding.removeFromFavoritesButton.visibility = View.VISIBLE
                binding.removeFromFavoritesButton.setOnClickListener {
                    favoritesViewModel.deleteFavorite(existing)
                }
            } else {
                binding.addToFavoritesButton.visibility = View.VISIBLE
                binding.removeFromFavoritesButton.visibility = View.GONE
                binding.addToFavoritesButton.setOnClickListener {
                    favoritesViewModel.saveFavorite(
                        FavoritePlace(
                            name = name,
                            category = category,
                            lat = lat,
                            lng = lng,
                            weatherCondition = weatherCondition,
                            username = favoritesViewModel.getUsername()
                        )
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
