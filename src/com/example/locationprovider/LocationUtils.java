package com.example.locationprovider;

public final class LocationUtils {
    // Debugging tag for the application
    public static final String APPTAG = "Location Mock Tester";

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = new String();

    // Conversion factor for boot time
    public static final long NANOSECONDS_PER_MILLISECOND = 1000000;

    // Conversion factor for time values
    public static final long MILLISECONDS_PER_SECOND = 1000;

    // Conversion factor for time values
    public static final long NANOSECONDS_PER_SECOND =
                    NANOSECONDS_PER_MILLISECOND * MILLISECONDS_PER_SECOND;

    /*
     * Action values sent by Intent from the main activity to the service
     */
    // Request a one-time test
    public static final String ACTION_START_ONCE =
            "com.example.locationprovider.ACTION_START_ONCE";

    // Request continuous testing
    public static final String ACTION_START_CONTINUOUS =
                    "com.example.locationprovider.ACTION_START_CONTINUOUS";

    // Stop a continuous test
    public static final String ACTION_STOP_TEST =
                    "com.example.locationprovider.ACTION_STOP_TEST";

    /*
     * Extended data keys for the broadcast Intent sent from the service to the main activity.
     * Key1 is the base connection message.
     * Key2 is extra data or error codes.
     */
    public static final String KEY_EXTRA_CODE1 =
            "com.example.locationprovider.KEY_EXTRA_CODE1";

    public static final String KEY_EXTRA_CODE2 =
            "com.example.locationprovider.KEY_EXTRA_CODE2";

    /*
     * Codes for communicating status back to the main activity
     */

    // The location client is disconnected
    public static final int CODE_DISCONNECTED = 0;

    // The location client is connected
    public static final int CODE_CONNECTED = 1;

    // The client failed to connect to Location Services
    public static final int CODE_CONNECTION_FAILED = -1;

    // Report in the broadcast Intent that the test finished
    public static final int CODE_TEST_FINISHED = 3;

    /*
     * Report in the broadcast Intent that the activity requested the start to a test, but a
     * test is already underway
     *
     */
    public static final int CODE_IN_TEST = -2;

    // The test was interrupted by clicking "Stop testing"
    public static final int CODE_TEST_STOPPED = -3;

    // The name used for all mock locations
    public static final String LOCATION_PROVIDER = "fused";

    // An array of latitudes for constructing test data
    public static final double[] WAYPOINTS_LAT = {
    34.413963,
    37.422,
    47.641839,
    29.817178,
    25.782324,
    41.8337329,
    42.3133734,
    42.755942
    };

    // An array of longitudes for constructing test data
    public static final double[] WAYPOINTS_LNG = {
	-119.848947,
	-122.084058,
	-122.140746,
	-95.4012915,
	-80.2310801,
	-87.7321555,
	-71.057157,
	-75.8092041
	};

    // An array of accuracy values for constructing test data
    public static final float[] WAYPOINTS_ACCURACY = {
        1.0f,
        1.12f,
        1.5f,
        1.7f,
        1.12f,
        1.0f,
        1.12f,
        1.7f
    };

    // Mark the broadcast Intent with an action
    public static final String ACTION_SERVICE_MESSAGE =
            "com.example.locationprovider.ACTION_SERVICE_MESSAGE";

    /*
     * Key for extended data in the Activity's outgoing Intent that records the type of test
     * requested.
     */
    public static final String EXTRA_TEST_ACTION =
            "com.example.locationprovider.EXTRA_TEST_ACTION";

    /*
     * Key for extended data in the Activity's outgoing Intent that records the requested pause
     * value.
     */
    public static final String EXTRA_PAUSE_VALUE =
            "com.example.locationprovider.EXTRA_PAUSE_VALUE";

    /*
     * Key for extended data in the Activity's outgoing Intent that records the requested interval
     * for mock locations sent to Location Services.
     */
    public static final String EXTRA_SEND_INTERVAL =
            "com.example.locationprovider.EXTRA_SEND_INTERVAL";
}
