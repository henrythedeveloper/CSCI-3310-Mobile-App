package com.example.csci_3310_nav;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocationsMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<CampusLocation> locations;
    private Spinner categorySpinner;

    // Card UI Elements
    private CardView locationCard;
    private ImageView locImage;
    private TextView locTitle, locAddress, locDesc;
    private Button btnGoHere, btnClose;

    private CampusLocation selectedLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations_map, container, false);

        // Bind UI
        categorySpinner = view.findViewById(R.id.category_spinner);
        locationCard = view.findViewById(R.id.location_card);
        locImage = view.findViewById(R.id.loc_image);
        locTitle = view.findViewById(R.id.loc_title);
        locAddress = view.findViewById(R.id.loc_address);
        locDesc = view.findViewById(R.id.loc_desc);
        btnGoHere = view.findViewById(R.id.btn_go_here);
        btnClose = view.findViewById(R.id.btn_close_card);

        locations = JsonUtils.loadLocations(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnGoHere.setOnClickListener(v -> {
            if (selectedLocation != null) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToDirections(selectedLocation);
                }
            }
        });

        btnClose.setOnClickListener(v -> locationCard.setVisibility(View.GONE));

        setupCategories();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng campusCenter = new LatLng(39.744, -105.003);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusCenter, 15));
        mMap.getUiSettings().setMapToolbarEnabled(false);

        filterMap("All"); // Initial load

        mMap.setOnMarkerClickListener(marker -> {
            CampusLocation loc = (CampusLocation) marker.getTag();
            if (loc != null) {
                showLocationDetails(loc);
            }
            return false;
        });

        mMap.setOnMapClickListener(latLng -> locationCard.setVisibility(View.GONE));
    }

    private void setupCategories() {
        Set<String> categoriesSet = new HashSet<>();
        categoriesSet.add("All");
        if (locations != null) {
            for (CampusLocation loc : locations) {
                if (loc.getCategory() != null && !loc.getCategory().isEmpty()) {
                    categoriesSet.add(loc.getCategory());
                }
            }
        }

        // Convert to list and sort
        List<String> categoriesList = new ArrayList<>(categoriesSet);
        Collections.sort(categoriesList);
        // Ensure "All" is first
        categoriesList.remove("All");
        categoriesList.add(0, "All");

        // Setup Adapter for Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoriesList);
        categorySpinner.setAdapter(adapter);

        // Handle Selection
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCat = categoriesList.get(position);
                filterMap(selectedCat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterMap(String category) {
        if (mMap == null || locations == null) return;
        mMap.clear();
        locationCard.setVisibility(View.GONE);

        for (CampusLocation loc : locations) {
            // Check if location matches selected category
            if (category.equals("All") || (loc.getCategory() != null && loc.getCategory().equals(category))) {

                // 1. Create the options object
                MarkerOptions options = new MarkerOptions()
                        .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .title(loc.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(getCategoryColor(loc.getCategory()))); // Set Color Here

                // 2. Add to map
                Marker marker = mMap.addMarker(options);

                // 3. Tag the marker so we can find the data on click
                if (marker != null) {
                    marker.setTag(loc);
                }
            }
        }
    }

    private void showLocationDetails(CampusLocation loc) {
        selectedLocation = loc;
        locTitle.setText(loc.getName() + " (" + loc.getId() + ")");
        locAddress.setText(loc.getAddress());
        locDesc.setText(loc.getDescription());

        try {
            InputStream ims = requireContext().getAssets().open("images/" + loc.getImageFileName());
            Bitmap bitmap = BitmapFactory.decodeStream(ims);
            locImage.setImageBitmap(bitmap);
            locImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (IOException e) {
            e.printStackTrace();
            locImage.setImageResource(R.mipmap.ic_launcher);
        }

        locationCard.setVisibility(View.VISIBLE);
    }

    // Helper to assign colors based on Category
    private float getCategoryColor(String category) {
        if (category == null) return BitmapDescriptorFactory.HUE_RED;

        switch (category) {
            case "Library":
                return BitmapDescriptorFactory.HUE_BLUE;

            case "Academic & Administrative":
                return BitmapDescriptorFactory.HUE_YELLOW; // Gold-ish for main buildings

            case "Housing":
                return BitmapDescriptorFactory.HUE_ORANGE;

            case "Parking":
                return BitmapDescriptorFactory.HUE_RED;

            case "Wellness and Recreation":
                return BitmapDescriptorFactory.HUE_GREEN;

            case "Museum and Historical":
                return BitmapDescriptorFactory.HUE_VIOLET;

            case "Performance Venues":
                return BitmapDescriptorFactory.HUE_ROSE;

            case "Dining / Historical":
                return BitmapDescriptorFactory.HUE_AZURE;

            default:
                return BitmapDescriptorFactory.HUE_RED;
        }
    }
}