package com.example.csci_3310_nav; // Make sure this matches your actual package!

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // Initialize fragments once so they keep their state (and so we can pass data to them)
    private final LocationsMapFragment locationsFragment = new LocationsMapFragment();
    private final DirectionsFragment directionsFragment = new DirectionsFragment();
    private final AccessibilityFragment accessibilityFragment = new AccessibilityFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Handle Tab Selection
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_locations) {
                selectedFragment = locationsFragment;
            } else if (itemId == R.id.nav_directions) {
                selectedFragment = directionsFragment;
            } else if (itemId == R.id.nav_accessibility) {
                selectedFragment = accessibilityFragment;
            }

            if (selectedFragment != null) {
                // Replace the fragment container with the selected fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default selection (Locations) if app just started
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_locations);
        }
    }

    /**
     * This public method is called from LocationsMapFragment
     * when the user clicks "GO HERE" on a building card.
     */
    public void navigateToDirections(CampusLocation destination) {
        // 1. Switch the UI tab to Directions
        bottomNav.setSelectedItemId(R.id.nav_directions);

        // 2. Pass the data to the DirectionsFragment
        // We use .post() to ensure the fragment transaction is complete before we try to access the fragment's views
        bottomNav.post(() -> {
            if (directionsFragment != null && directionsFragment.isAdded()) {
                directionsFragment.setDestinationFromMap(destination);
            }
        });
    }
}