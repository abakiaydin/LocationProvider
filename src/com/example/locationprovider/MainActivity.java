package com.example.locationprovider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	public TextView mConnectionStatus;
	public TextView mAppStatus;
	
	private ServiceMessageReceiver mMessageReceiver;
	
	private EditText mPauseInterval;
	private EditText mSendInterval;
	private Intent mRequestIntent;
	
	private ProgressBar mActivityIndicator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mConnectionStatus = (TextView) findViewById(R.id.connection_status);
		mAppStatus = (TextView) findViewById(R.id.app_status);
		mPauseInterval = (EditText) findViewById(R.id.pause_value);
		mSendInterval = (EditText) findViewById(R.id.send_interval_value);
		
		mActivityIndicator = (ProgressBar) findViewById(R.id.testing_activity_indicator);
		
		mMessageReceiver = new ServiceMessageReceiver();
		
		IntentFilter filter = new IntentFilter(LocationUtils.ACTION_SERVICE_MESSAGE);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
		
		mRequestIntent = new Intent(this, SendMockLocationService.class);

	}
	
	/**
     * Respond when Run Once is clicked. Start a one-time mock location test run.
     * @param v The View that was clicked
     */
    public void onStartOnceButtonClick(View v) {

        // Verify the input values and put them into global variables
        if (getInputValues()) {
            // Notify SendMockLocationService to loop once through the mock locations
            mRequestIntent.setAction(LocationUtils.ACTION_START_ONCE);

            // Set the app status field in the UI
            mAppStatus.setText(R.string.testing_started);

            // Turn on the activity indicator, to show that testing is running
            mActivityIndicator.setVisibility(View.VISIBLE);

            // Start SendMockLocationService
            startService(mRequestIntent);
        }
    }

    
    /**
     * Respond when Run Continuously is clicked. Start a continuous mock location test run.
     * Mock locations are sent indefinitely, until the tester clicks Stop Continuous Run.
     * @param v The View that was clicked
     */
    public void onStartContinuousButtonClick(View v) {

        // Verify the input values and put them into global variables
        if (getInputValues()) {
            // Notify SendMockLocationService to loop indefinitely through the mock locations
            mRequestIntent.setAction(LocationUtils.ACTION_START_CONTINUOUS);

            // Set the app status field in the UI
            mAppStatus.setText(R.string.testing_started);

            // Turn on the activity indicator, to show that testing is running
            mActivityIndicator.setVisibility(View.VISIBLE);

            // Start SendMockLocationService
            startService(mRequestIntent);
        }
    }
    
    /**
     * Respond when Stop Test is clicked. Stop the current mock location test run. If the user
     * requested a one-time run with a short pause interval and fast send interval, this
     * request may have no effect, because the Service will have already stopped.
     * @param v The View that was clicked
     */
    public void onStopButtonClick(View v) {

        // Stop SendMockLocationService
        mRequestIntent.setAction(LocationUtils.ACTION_STOP_TEST);

        // If SendMockLocationService is running
        if (null != startService(mRequestIntent)) {

            // Update app status to show that a request was sent to stop the Service
            mAppStatus.setText(R.string.stop_service);

        } else {

            // Update app status to show that the Service isn't running
            mAppStatus.setText(R.string.no_service);
        }

        // Update connection status to show that the connection was destroyed
        mConnectionStatus.setText(R.string.disconnected);

        // Turn off the activity indicator
        mActivityIndicator.setVisibility(View.GONE);
    }
    
    private class ServiceMessageReceiver extends BroadcastReceiver {
    	/*
         * Invoked when a broadcast Intent from SendMockLocationService arrives
         *
         * context is the Context of the app
         * intent is the Intent object that triggered the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get the message code from the incoming Intent
            int code1 = intent.getIntExtra(LocationUtils.KEY_EXTRA_CODE1, 0);
            int code2 = intent.getIntExtra(LocationUtils.KEY_EXTRA_CODE2, 0);

            // Choose the action, based on the message code
            switch (code1) {
                /*
                 * SendMockLocationService reported that the location client is connected. Update
                 * the app status reporting field in the UI.
                 */
                case LocationUtils.CODE_CONNECTED:
                    mConnectionStatus.setText(R.string.connected);
                    break;

                /*
                 * SendMockLocationService reported that the location client disconnected. This
                 * happens if Location Services drops the connection. Update the app status and the
                 * connection status reporting fields in the UI.
                 */
                case LocationUtils.CODE_DISCONNECTED:
                    mConnectionStatus.setText(R.string.disconnected);
                    mAppStatus.setText(R.string.notification_content_test_stop);
                    break;

                /*
                 * SendMockLocationService reported that an attempt to connect to Location
                 * Services failed. Testing can't continue. The Service has already stopped itself.
                 * Update the connection status reporting field and include the error code.
                 * Also update the app status field
                 */
                case LocationUtils.CODE_CONNECTION_FAILED:
                    mActivityIndicator.setVisibility(View.GONE);
                    mConnectionStatus.setText(
                            context.getString(R.string.connection_failure, code2));
                    mAppStatus.setText(R.string.location_test_finish);
                    break;

                /*
                 * SendMockLocationService reported that the tester requested a test, but a test
                 * is already underway. Update the app status reporting field.
                 */
                case LocationUtils.CODE_IN_TEST:
                    mAppStatus.setText(R.string.not_continuous_test);
                    break;

                /*
                 * SendMockLocationService reported that the test run finished. Turn off the
                 * progress indicator, update the app status reporting field and the connection
                 * status reporting field. Since this message can only occur if
                 * SendMockLocationService disconnected the client, the connection status is
                 * "disconnected".
                 */
                case LocationUtils.CODE_TEST_FINISHED:
                    mActivityIndicator.setVisibility(View.GONE);
                    mAppStatus.setText(context.getText(R.string.location_test_finish));
                    mConnectionStatus.setText(R.string.disconnected);
                    break;

                /*
                 * SendMockLocationService reported that the tester interrupted the test.
                 * Turn off the activity indicator and update the app status reporting field.
                 */
                case LocationUtils.CODE_TEST_STOPPED:
                    mActivityIndicator.setVisibility(View.GONE);
                    mAppStatus.setText(R.string.test_interrupted);
                    break;

                /*
                 * An unknown broadcast Intent was received. Log an error.
                 */
                default:
                    Log.e(LocationUtils.APPTAG, getString(R.string.invalid_broadcast_code));
                    break;
            }
        }
    }
    
    /**
     * Verify the pause interval and send interval from the UI. If they're correct, store
     * them in the Intent that's used to start SendMockLocationService
     * @return true if all the input values are correct; otherwise false
     */
    public boolean getInputValues() {

        // Get the values from the UI
        String pauseIntervalText = mPauseInterval.getText().toString();
        String sendIntervalText = mSendInterval.getText().toString();

        if (TextUtils.isEmpty(pauseIntervalText)) {

            // Report that the pause interval is empty
            mAppStatus.setText(R.string.pause_interval_empty);
            return false;
        } else if (Integer.valueOf(pauseIntervalText) <= 0) {

            // Report that the pause interval is not a positive number
            mAppStatus.setText(R.string.pause_interval_not_positive);
            return false;
        }

        if (TextUtils.isEmpty(sendIntervalText)) {

            mAppStatus.setText(R.string.send_entry_empty);
            return false;
        } else if (Integer.valueOf(sendIntervalText) <= 0) {

            // Report that the send interval is not a positive number
            mAppStatus.setText(R.string.send_interval_not_positive);
            return false;
        }

        int pauseValue = Integer.valueOf(pauseIntervalText);
        int sendValue = Integer.valueOf(sendIntervalText);

        mRequestIntent.putExtra(LocationUtils.EXTRA_PAUSE_VALUE, pauseValue);
        mRequestIntent.putExtra(LocationUtils.EXTRA_SEND_INTERVAL, sendValue);

        return true;
    }
	

}
