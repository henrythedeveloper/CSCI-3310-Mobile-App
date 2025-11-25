package com.example.csci_3310_nav;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DirectionsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private List<CampusLocation> locations;
    private CampusLocation selectedDestination;

    // UI Elements
    private CardView directionsListCard;
    private TextView txtTotalInfo;
    private AutoCompleteTextView searchBox;
    private ImageButton btnGo;
    private RecyclerView recyclerSteps;

    // Data
    private List<NavStep> routeSteps = new ArrayList<>();

    // Simple class to hold step data
    public static class NavStep {
        String instruction;
        String distanceText;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_directions, container, false);

        // Bind UI
        searchBox = view.findViewById(R.id.search_box);
        btnGo = view.findViewById(R.id.btn_go);
        directionsListCard = view.findViewById(R.id.directions_list_card);
        txtTotalInfo = view.findViewById(R.id.txt_total_info);
        recyclerSteps = view.findViewById(R.id.recycler_steps);

        // Setup List
        recyclerSteps.setLayoutManager(new LinearLayoutManager(getContext()));

        // Init Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.directions_map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Load Locations
        locations = JsonUtils.loadLocations(requireContext());
        if (locations != null) {
            ArrayAdapter<CampusLocation> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, locations);
            searchBox.setAdapter(adapter);
        }

        searchBox.setOnItemClickListener((parent, view1, position, id) -> {
            selectedDestination = (CampusLocation) parent.getItemAtPosition(position);
            // Reset UI
            directionsListCard.setVisibility(View.GONE);
            if (mMap != null) mMap.clear();
        });

        btnGo.setOnClickListener(v -> {
            if (selectedDestination != null) {
                searchBox.clearFocus();
                calculateRoute();
            } else {
                Toast.makeText(getContext(), "Select a destination first", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng campus = new LatLng(39.744, -105.003);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campus, 16));

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void calculateRoute() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng dest = new LatLng(selectedDestination.getLatitude(), selectedDestination.getLongitude());
                fetchRoute(origin, dest);
            } else {
                Toast.makeText(getContext(), "Waiting for location...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRoute(LatLng origin, LatLng dest) {
        String apiKey = BuildConfig.MAPS_API_KEY;
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                origin.latitude + "," + origin.longitude +
                "&destination=" + dest.latitude + "," + dest.longitude +
                "&mode=walking&key=" + apiKey;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    new Handler(Looper.getMainLooper()).post(() -> parseAndDisplay(json, origin, dest));
                }
            }
        });
    }

    private void parseAndDisplay(String jsonData, LatLng origin, LatLng dest) {
        try {
            JSONObject json = new JSONObject(jsonData);
            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() == 0) {
                Toast.makeText(getContext(), "No route found", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject route = routes.getJSONObject(0);

            // 1. Draw Path on Map
            String encodedPath = route.getJSONObject("overview_polyline").getString("points");
            List<LatLng> points = PolyUtil.decode(encodedPath);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(dest).title(selectedDestination.getName()));
            mMap.addPolyline(new PolylineOptions().addAll(points).width(15).color(Color.BLUE));

            // Zoom to fit
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origin);
            builder.include(dest);
            for(LatLng p : points) builder.include(p);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

            // 2. Parse Steps for List
            routeSteps.clear();
            JSONArray legs = route.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            JSONArray steps = leg.getJSONArray("steps");

            // Get Totals
            String totalTime = leg.getJSONObject("duration").getString("text");
            String totalDist = leg.getJSONObject("distance").getString("text");
            txtTotalInfo.setText("Total: " + totalTime + " (" + totalDist + ")");

            for (int i = 0; i < steps.length(); i++) {
                JSONObject step = steps.getJSONObject(i);
                NavStep navStep = new NavStep();
                // Html.fromHtml cleans up the "<b>Left</b>" tags from Google
                navStep.instruction = Html.fromHtml(step.getString("html_instructions"), Html.FROM_HTML_MODE_LEGACY).toString();
                navStep.distanceText = step.getJSONObject("distance").getString("text");
                routeSteps.add(navStep);
            }

            // Add "Arrived" step
            NavStep arrived = new NavStep();
            arrived.instruction = "Arrive at " + selectedDestination.getName();
            arrived.distanceText = "";
            routeSteps.add(arrived);

            // 3. Show List
            DirectionsAdapter adapter = new DirectionsAdapter(routeSteps);
            recyclerSteps.setAdapter(adapter);
            directionsListCard.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing route", Toast.LENGTH_SHORT).show();
        }
    }

    public void setDestinationFromMap(CampusLocation location) {
        this.selectedDestination = location;
        if (searchBox != null) {
            searchBox.setText(location.getName());
            searchBox.dismissDropDown();
            // Hide list until they press Go
            directionsListCard.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Press the Gold Arrow to view route", Toast.LENGTH_SHORT).show();
        }
    }
}