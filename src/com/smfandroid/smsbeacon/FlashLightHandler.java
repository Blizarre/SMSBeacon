package com.smfandroid.smsbeacon;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;

/**
 * State machine : Stopped -> ( Started with lamp Off <-> Started with lamp On ) -> Stopped  
 * Start the state machine with startFlicker(..).
 * Stop it with stopFlicker()
 * The automatic change of states is done in the FlickerRunnable class
 */
public class FlashLightHandler {
	private Camera mCamera;
	protected Thread mFlickerThread = null;
	
	private final String TAG = getClass().getName();

	protected enum State {
		STOPPED,
		STARTED_LAMP_ON,
		STARTED_LAMP_OFF,
		ERROR
	};
	
	protected State mState = State.STOPPED;
	
	/**
	 * The Runnable used to make the flashlight flicker.
	 * Will call start() -> lightOn() <-> lightOff() until interrupted.
	 * will then call stop().
	 *  
	 * @author Simon
	 *
	 */
	protected class FlickerRunnable implements Runnable {

		protected int mOnDurationMs;
		protected int mOffDurationMs;
		
		public FlickerRunnable(int onDurationMs, int offDurationMs) {
			mOnDurationMs = onDurationMs;
			mOffDurationMs = offDurationMs;
		}
		
		@Override
		public void run() {
			try {
				//
				// TODO: Make a real state machine, will be more robust
				// 
				FlashLightHandler.this.start();

				while(mState != State.ERROR) {
						Thread.sleep(mOffDurationMs);
						FlashLightHandler.this.lightOn();
						Thread.sleep(mOnDurationMs);
						FlashLightHandler.this.lightOff();
				}
				
			} catch (InterruptedException e) {
				// Someone asked to stop this thread. Clean up and stop 
				if(mState == State.STARTED_LAMP_ON)
					FlashLightHandler.this.lightOff(); // probably not necessary

				FlashLightHandler.this.stop();
			}
		}
	}

	public FlashLightHandler() {
	}
	
	/**
	 * Initialize the camera object 
	 */
	public void start() {
		Log.i(TAG, "Start the Flashlight.");

		if(mState != State.STOPPED) 
			throw new IllegalStateException("FlashLightHandler is already started");
		
		// Reset the camera
		mCamera = null;
		
		try
		{
			mCamera = Camera.open();
		}
		catch(Exception e)
		{
			Log.w(TAG, "Exception raised when trying to open the camera");
		}
		
		if(mCamera == null)
		{
			Log.w(TAG, "Couldn't get the handle to the camera.");
			mState = State.ERROR;
		}
		else
		{
			mState = State.STARTED_LAMP_OFF;
		}
	}

	/**
	 * Put the flash in TORCH mode
	 */
	public void lightOn() {
		if(mState != State.STARTED_LAMP_OFF) 
			throw new IllegalStateException("Camera is already stopped");
		
		Parameters params = mCamera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_TORCH);
		mCamera.setParameters(params);
		mCamera.startPreview();

		mState = State.STARTED_LAMP_ON;
	}
		
	/**
	 * Stop the flash
	 */
	public void lightOff() {
		
		if(mState != State.STARTED_LAMP_ON) 
			throw new IllegalStateException("Camera is already stopped");
		
		Parameters params = mCamera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_OFF);
		mCamera.setParameters(params);
		mCamera.stopPreview();
		
		mState = State.STARTED_LAMP_OFF;
	}

	/**
	 * Release the camera object
	 */
	public void stop() {
		Log.i(TAG, "Stopping the Flashlight.");

		if(mCamera != null)
		{
			Log.i(TAG, "Releasing Camera");
			mCamera.release();
		}
		
		mCamera = null;
		
		mState = State.STOPPED;
	}
	
	/**
	 * Return true if the phone has a camera flash
	 */
	public static boolean hasFlashLight(Context c) {
		PackageManager packM = c.getPackageManager();
		return  packM.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
	}
	
	
	public void startFlicker() {
		startFlicker(100, 500); // default values, nice stroboscopic effect 
	}

	/**
	 * Start the flickering if it has not been started
	 */
	public void startFlicker(int onDurationMs, int offDurationMs) {
		Runnable flickRun;
		if(mFlickerThread == null || !mFlickerThread.isAlive()) {
			flickRun = new FlickerRunnable(onDurationMs, offDurationMs);
			mFlickerThread = new Thread( flickRun );
			mFlickerThread.start();
		} else {
			throw new IllegalStateException("Flicker already started");
		}
	}
	
	/**
	 * Stop the flickering if it has been started
	 */
	public void stopFlicker() {
		if(mFlickerThread != null) 
			mFlickerThread.interrupt();
	}
	

}
