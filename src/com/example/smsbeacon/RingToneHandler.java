package com.example.smsbeacon;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class RingToneHandler {

	protected final  Ringtone mRingTone;
	protected final  Context mContext;
	protected FlashLightHandler mFlashH;
	
	protected int mInitVolume;
	
	protected Thread mAutoStop;
	
	protected class RingToneRunnable implements Runnable {
		protected int mMaxDuration;
		protected int mOriginalVolume;
		
		/**
		 * Create the class needed to stop automatically the ringtone.
		 * @param maxDuration
		 */
		public RingToneRunnable(int maxDuration) {
			mMaxDuration = maxDuration;
		}
		
		@Override
		public void run() {
			// RingToneHandler.this is a /copy/ of the parent class. We need to 
			// make all the work in this function
			RingToneHandler.this.startRingTone();
			try {
				Thread.sleep(mMaxDuration);
			} catch (InterruptedException e) {
				// stopRingTone() expect the thread to be in interrupted state
				Thread.currentThread().interrupt();  
			}
			stopRingTone();
		}
		
	}
	
	public RingToneHandler(Context c) {
		mContext = c;
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		mRingTone = RingtoneManager.getRingtone(c, notification);
	}

	/**
	 * Set the volume of the RingTone at the value "volume"
	 * @return the original volume
	 */
	public int setVolume(int volume) {
		AudioManager audioM = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		int originalVolume = audioM.getStreamVolume(AudioManager.STREAM_RING);
		audioM.setStreamVolume(
				   AudioManager.STREAM_RING,
				   audioM.getStreamMaxVolume(AudioManager.STREAM_RING),
				   AudioManager.FLAG_PLAY_SOUND
				);	
		return originalVolume;
	}
	
	/**
	 * Set the volume of the RingTone at its maximum.
	 * @return the original volume
	 */
	public int SetMaxVolume() {
		AudioManager audioM = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = audioM.getStreamMaxVolume(AudioManager.STREAM_RING);
		return setVolume(maxVolume);
	}		

	/**
	 * Start the ringtone.
	 * If stopRingTone is not called within maxDuratiionMs milliseconds, 
	 * the ringtone will stop
	 * @param maxDurationMs Maximum duration of the Ringtone
	 */
	public void startRingTone(int maxDurationMs, boolean useFlash) {
		if(useFlash)
			mFlashH = new FlashLightHandler();
		else
			mFlashH = null;
		// runRingTone is now aware of the element mFlashH
		Runnable runRingTone = new RingToneRunnable(maxDurationMs);		
		mAutoStop = new Thread(runRingTone);
		mAutoStop.start();
	}
	
	public void startRingTone() {
		if(mRingTone.isPlaying())
			throw new IllegalStateException("Ringtone already playing");
		mInitVolume = SetMaxVolume();
		mRingTone.play();
		mFlashH.startFlicker();
	}
	
	/**
	* Will stop the ringtone if already playing
	* No effect if it is not.
	* */
	public void stopRingTone() {
		if(mAutoStop != null && !mAutoStop.isAlive()) {
			// mAutoStop is null in the RingToneRunnable thread
			// same method from the copy of this instance. 
			mAutoStop.interrupt();
			mAutoStop = null;
			mFlashH = null;
		} else {
			mRingTone.stop();
			setVolume(mInitVolume);
			if(mFlashH != null)
				mFlashH.stopFlicker();
		}
	}	
}
