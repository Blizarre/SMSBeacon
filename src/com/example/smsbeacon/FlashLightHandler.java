package com.example.smsbeacon;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

public class FlashLightHandler {
	private final Camera mCamera;
	protected Thread mFlickerThread = null;
	protected boolean mIsStarted;

	
	/**
	 * The Runnable used to make the flashlight flicker 
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
			Thread t = Thread.currentThread();
			while(!t.isInterrupted()) {
				try {
					Thread.sleep(mOffDurationMs);
					FlashLightHandler.this.start();
					Thread.sleep(mOnDurationMs);
					FlashLightHandler.this.stop();
				} catch (InterruptedException e) {
					// Someone asked nicely to stop this thread. Exit nicely
					FlashLightHandler.this.stop();
					break;
				}
			}
		}
	}
	
	
	public static boolean hasFlashLight(Context c) {
		PackageManager packM = c.getPackageManager();
		return  packM.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
	}
	
	public FlashLightHandler() {
		mCamera = Camera.open();     
	}
	
	public void start() {
		if(mIsStarted) 
			throw new IllegalStateException("Camera is already started");

		Parameters params = mCamera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_TORCH);
		mCamera.setParameters(params);
		mCamera.startPreview();
		mIsStarted = true;
	}

	public void stop() {
		// Needed because the thread can be interrupted in an on or off state
		// enable us to be less conservative about the use of the stop  
		if(!mIsStarted) 
			return;

		mIsStarted = false;
        mCamera.stopPreview();
	}
	
	public void startFlicker() {
		startFlicker(100, 500); // default values, nice stroboscopic effect 
	}
	
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
	
	public void release() {
		mCamera.release();
	}
}
