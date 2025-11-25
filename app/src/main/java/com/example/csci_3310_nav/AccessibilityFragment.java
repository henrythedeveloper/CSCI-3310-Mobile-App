package com.example.csci_3310_nav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccessibilityFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<CampusLocation> accessLocations;
    private Spinner categorySpinner;

    // Card UI
    private CardView locationCard;
    private TextView locTitle, locDesc;
    private Button btnGoHere, btnClose;
    private CampusLocation selectedLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accessibility, container, false);

        // Bind UI
        categorySpinner = view.findViewById(R.id.category_spinner);
        locationCard = view.findViewById(R.id.location_card);
        locTitle = view.findViewById(R.id.loc_title);
        locDesc = view.findViewById(R.id.loc_desc);
        btnGoHere = view.findViewById(R.id.btn_go_here);
        btnClose = view.findViewById(R.id.btn_close_card);

        // Load Specific Accessibility Data
        accessLocations = JsonUtils.loadLocations(requireContext(), "accessibility_locations.json");

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

        filterMap("All");

        mMap.setOnMarkerClickListener(marker -> {
            CampusLocation loc = (CampusLocation) marker.getTag();
            if (loc != null) {
                showDetails(loc);
            }
            return false;
        });
        mMap.setOnMapClickListener(latLng -> locationCard.setVisibility(View.GONE));
    }

    private void setupCategories() {
        Set<String> categoriesSet = new HashSet<>();
        categoriesSet.add("All");
        if (accessLocations != null) {
            for (CampusLocation loc : accessLocations) {
                if (loc.getCategory() != null) categoriesSet.add(loc.getCategory());
            }
        }

        List<String> categoriesList = new ArrayList<>(categoriesSet);
        Collections.sort(categoriesList);
        categoriesList.remove("All");
        categoriesList.add(0, "All");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoriesList);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterMap(categoriesList.get(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterMap(String category) {
        if (mMap == null || accessLocations == null) return;
        mMap.clear();
        locationCard.setVisibility(View.GONE);

        for (CampusLocation loc : accessLocations) {
            if (category.equals("All") || (loc.getCategory() != null && loc.getCategory().equals(category))) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .title(loc.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(getColorForCategory(loc.getCategory()))));

                if (marker != null) marker.setTag(loc);
            }
        }
    }

    private void showDetails(CampusLocation loc) {
        selectedLocation = loc;
        locTitle.setText(loc.getName());
        locDesc.setText(loc.getCategory());
        locationCard.setVisibility(View.VISIBLE);
    }

    private float getColorForCategory(String category) {
        if (category == null) return BitmapDescriptorFactory.HUE_RED;
        switch (category) {
            case "Access-a-Ride RTD": return BitmapDescriptorFactory.HUE_AZURE;
            case "ADA Accessible Entry": return BitmapDescriptorFactory.HUE_GREEN;
            case "Campus Accessible Shuttle": return BitmapDescriptorFactory.HUE_YELLOW;
            case "Pet Relief Area": return BitmapDescriptorFactory.HUE_ROSE;
            default: return BitmapDescriptorFactory.HUE_RED;
        }
    }
}