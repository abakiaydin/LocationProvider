package com.example.locationprovider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

public class SendMockLocationService extends Service implements 
	ConnectionCallbacks, OnConnectionFailedListener  {

	private class TestParam {
		public final String TestAction;
		public final int TestPause;
		public final int InjectionPause;
		
		public TestParam(String action, int testPause, int injectionPause) {
			TestAction = action;
			TestPause = testPause;
			InjectionPause = injectionPause;
		}
	}
	
	LocationClient mLocationClient;
	HandlerThread mWorkThread;
	
	private boolean mTestStarted;
	
	private LocalBroadcastManager mLocalBroadcastManager;
	private Looper mUpdateLooper;
	
	private UpdateHandler mUpdateHandler;
	
	private TestLocation[] mLocationArray;
	
	private int mPauseInterval;
	private int mInjectionInterval;
	private String mTestRequest;

	public class UpdateHandler extends Handler {
		public UpdateHandler(Looper inputLooper) {
			super(inputLooper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			boolean testOnce = false;
			
			Location mockLocation = new Location(LocationUtils.LOCATION_PROVIDER);
			
			long elapsedTimeNanos;
			long currentTime;
			
			TestParam params = (TestParam) msg.obj;
			String action = params.TestAction;
			int pauseInterval = params.TestPause;
			int injectionInterval = params.InjectionPause;
			
			if (TextUtils.equals(action, LocationUtils.ACTION_START_ONCE)) {
				testOnce = true;
			}
			
			if (!mTestStarted) {
				mTestStarted = true;
				mLocationClient.setMockMode(true);
				removeNotification();
				postNotification(getString(R.string.notification_content_test_running));
				
				try {
					Thread.sleep((long) (pauseInterval * 1000));
				} catch (InterruptedException e) {
					return;
				}
				
				elapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
				currentTime = System.currentTimeMillis();
				
				do {
					for (int index = 0; index < mLocationArray.length; index++) {
						mockLocation.setElapsedRealtimeNanos(elapsedTimeNanos);
						mockLocation.setTime(currentTime);
						
						mockLocation.setAccuracy(mLocationArray[index].Accuracy);
						mockLocation.setLatitude(mLocationArray[index].Latitude);
						mockLocation.setLongitude(mLocationArray[index].Longitude);
						
						mLocationClient.setMockLocation(mockLocation);
						
						try {
							Thread.sleep((long) (injectionInterval * 1000));
						} catch (InterruptedException e) {
							return;
						}
						
						elapsedTimeNanos += (long) injectionInterval * LocationUtils.NANOSECONDS_PER_SECOND;
						currentTime += injectionInterval * LocationUtils.MILLISECONDS_PER_SECOND;
					}
				} while (!testOnce);
				
				mLocationClient.setMockMode(false);
				mTestStarted = false;
				
				removeNotification();
				sendBroadcastMessage(LocationUtils.CODE_TEST_FINISHED, 0);
				
				stopSelf();
			} else {
				sendBroadcastMessage(LocationUtils.CODE_IN_TEST, 0);
			}
		}
	}
	
	@Override
	public void onCreate() {
		mLocationArray = buildTestLocationArray(LocationUtils.WAYPOINTS_LAT, LocationUtils.WAYPOINTS_LNG, LocationUtils.WAYPOINTS_ACCURACY);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mWorkThread = new HandlerThread("UpdateThread", Process.THREAD_PRIORITY_BACKGROUND);
		
		mWorkThread.start();
		
		mUpdateLooper = mWorkThread.getLooper();
		
		mUpdateHandler = new UpdateHandler(mUpdateLooper);
		
		mTestStarted = false;
	}
	
	private void postNotification(String contentText) {
		NotificationManager notificaitonManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		Notification.Builder builder;
		
		String contentTitle = this.getString(R.string.notification_title_test_start);
		
		builder = new Notification.Builder(this)
					.setAutoCancel(false)
					.setSmallIcon(R.drawable.ic_notify)
					.setContentTitle(contentTitle)
					.setContentText(contentText);
		
		notificaitonManager.notify(0, builder.build());
	}
	
	private void removeNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent startIntent, int flags, int startId) {
		mTestRequest = startIntent.getAction();
		
		if (
	           (TextUtils.equals(mTestRequest, LocationUtils.ACTION_START_ONCE))
	           ||
	           (TextUtils.equals(mTestRequest, LocationUtils.ACTION_START_CONTINUOUS))
	        ) {
			
			mPauseInterval = startIntent.getIntExtra(LocationUtils.EXTRA_PAUSE_VALUE, 2);
			mInjectionInterval = startIntent.getIntExtra(LocationUtils.EXTRA_SEND_INTERVAL, 1);
			
			postNotification(getString(R.string.notification_content_test_start));
			
			mLocationClient = new LocationClient(this,this,this);
			
			mLocationClient.connect();
		} else if (TextUtils.equals(mTestRequest, LocationUtils.ACTION_STOP_TEST)) {
			removeNotification();
			sendBroadcastMessage(LocationUtils.CODE_TEST_STOPPED, 0);
			stopSelf();
		}
		
		return Service.START_STICKY;
	}

	private TestLocation[] buildTestLocationArray(double[] lat_array, double[] lng_array, float[] accuracy_array) {
		TestLocation[] location_array = new TestLocation[lat_array.length];
		
		for (int index = 0; index < lat_array.length; index++) {
			location_array[index] = new TestLocation(Integer.toString(index), lat_array[index], lng_array[index], accuracy_array[index]);
		}
		return location_array;
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		sendBroadcastMessage(LocationUtils.CODE_CONNECTION_FAILED, result.getErrorCode());
		stopSelf();
	}
	
	private void sendBroadcastMessage(int code1, int code2) {
		Intent sendIntent = new Intent(LocationUtils.ACTION_SERVICE_MESSAGE);
		sendIntent.putExtra(LocationUtils.KEY_EXTRA_CODE1, code1);
		sendIntent.putExtra(LocationUtils.KEY_EXTRA_CODE2, code2);
		mLocalBroadcastManager.sendBroadcastSync(sendIntent);
	}
	
	@Override
	public void onConnected(Bundle arg0) {
		sendBroadcastMessage(LocationUtils.CODE_CONNECTED, 0);
		mUpdateLooper = mWorkThread.getLooper();
		mUpdateHandler = new UpdateHandler(mUpdateLooper);
		
		Message msg = mUpdateHandler.obtainMessage();
		TestParam testParams = new TestParam(mTestRequest, mPauseInterval, mInjectionInterval);
		msg.obj = testParams;
		
		mUpdateHandler.sendMessage(msg);
		
	}
	
	@Override
	public void onDisconnected() {
		if (mTestStarted) {
			sendBroadcastMessage(LocationUtils.CODE_DISCONNECTED, LocationUtils.CODE_TEST_STOPPED);
		}
	}
}
