package com.example.locationprovider;

public class TestLocation {
    // Member fields
    public final double Latitude;
    public final double Longitude;
    public final float Accuracy;
    public final String Id;

    /**
     * Primary constructor. Create an object for a set of test location settings
     * @param id Identifies this location. Used as the test Location object's provider
     * @param latitude The test location's latitude
     * @param longitude The test location's longitude
     * @param accuracy The accuracy of the test location data
     */
    public TestLocation(String id, double latitude, double longitude, float accuracy) {

        Id = id;
        Latitude = latitude;
        Longitude = longitude;
        Accuracy = accuracy;
    }
    /**
     * Default constructor. Initialize everything to reasonable values.
     */
    public TestLocation() {

        Id = "test";
        Latitude = 34.413739;
        Longitude = -119.841148;
        Accuracy = 3.0f;
    }
}
