package com.example.csci_3310_nav;

public class CampusLocation {
    private String id;
    private String name;
    private String category;
    private String address;
    private String description;
    private double latitude;
    private double longitude;
    private String website;

    // Getters
    public String getName() { return name; }
    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getWebsite() { return website; }
    public String getImageFileName() {
        if (id == null) return "ic_launcher.png";
        // Handle specific exceptions
        if (id.equals("LM")) return "Los_Molinos.png";
        if (id.equals("TPG")) return "Tivoli_Garage.png";
        if (id.equals("5G")) return "5th_Street_Garage.png";
        if (id.equals("7S")) return "7th_Street_Garage.png";

        // Default: Use building code (e.g., "AL.png")
        return id + ".png";
    }

    @Override
    public String toString() {
        return name + " (" + id + ")"; // Useful for the search dropdown
    }

}