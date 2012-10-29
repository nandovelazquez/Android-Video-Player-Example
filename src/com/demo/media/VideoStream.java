package com.demo.media;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.demo.videoplayer.R;

public class VideoStream implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, OnSeekBarChangeListener, 
									OnSeekCompleteListener {

	private int STATUS = 0;
	private final int STATUS_STOPED = 1;
	private final int STATUS_PLAYING = 2;
	private final int STATUS_PAUSED = 3;
	
	private Context ctx;
	private WakeLock wakeLock;
	private MediaPlayer mPlayer;
	private SeekBar seekBar = null;
	private SurfaceView surfaceView;
	private TextView lblCurrentPosition = null;
	private TextView lblDuration = null;
	private Timer timer = null;
	
	public VideoStream(Context ctx) {
		this.ctx = ctx;
		
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnPreparedListener(this);
		
		PowerManager powerManager = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
	    wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyMediaPlayer");
	}
	
	/**
	 * Sets up the surface dimensions to display
	 * the video on it.
	 */
	private void setUpVideoDimensions() {
		// Get the dimensions of the video
		int videoWidth = mPlayer.getVideoWidth();
		int videoHeight = mPlayer.getVideoHeight();
		float videoProportion = (float) videoWidth / (float) videoHeight;
	
		// Get the width of the screen
		int screenWidth = ((Activity) ctx).getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = ((Activity) ctx).getWindowManager().getDefaultDisplay().getHeight();
		float screenProportion = (float) screenWidth / (float) screenHeight;
	
		// Get the SurfaceView layout parameters
		android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
	
		if (videoProportion > screenProportion) {
			lp.width = screenWidth;
			lp.height = (int) ((float) screenWidth / videoProportion);
		} else {
			lp.width = (int) (videoProportion * (float) screenHeight);
			lp.height = screenHeight;
		}
	
		// Commit the layout parameters
		surfaceView.setLayoutParams(lp);
	}
	
	/**
	 * Pause the video playback.
	 */
	public void pause() {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
			STATUS = STATUS_PAUSED;
			
			wakeLockRelease();
		}
	}
	
	/**
	 * Start the video playback.
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	public void play() throws IllegalStateException, IOException {
		
		if (STATUS != STATUS_PLAYING) {
			wakeLockAcquire();
			
			if (STATUS == STATUS_PAUSED )
				mPlayer.start();
			else {
				mPlayer.prepare();
				mPlayer.start();
			}
			
			STATUS = STATUS_PLAYING;
		}
	}
	
	/**
	 * Sets up the video source.
	 * @param source - The video address
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void setUpVideoFrom(String source) throws IllegalArgumentException, IllegalStateException, IOException {
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
//		if (source.contains("http"))
			mPlayer.setDataSource(source);
//		else {
//			Uri uri = Uri.parse(source);
//			mPlayer.setDataSource(ctx, uri);
//		}
			
	}
	
	/**
	 * Release the video object.
	 * This will stops the playback and release the memory used.
	 */
	public void release() {
		reset();
			
		mPlayer.release();
		mPlayer = null;
	}
	
	/**
	 * Reset the seekbar.
	 */
	private void reset() {
		if (seekBar != null) {
			seekBar.setProgress(0);
			timer.cancel();
			lblCurrentPosition.setText(ctx.getResources().getString(R.string.empty_message));
		}
	}
	
	/**
	 * Set up the surface to display the video on it.
	 * @param holder - The surface to display the video.
	 */
	public void setDisplay(SurfaceView surfaceView, SurfaceHolder holder) {
		this.surfaceView = surfaceView;
		mPlayer.setDisplay(holder);
	}
	
	/**
	 * Set up a listener to execute when the video is ready to playback.
	 * @param listener - The Listener.
	 */
	public void setOnPrepared(MediaPlayer.OnPreparedListener listener){
		mPlayer.setOnPreparedListener(listener);
	}
	
	/**
	 * Sets up a seekbar and two labels to display the video progress.
	 * @param seekBar
	 * @param lblCurrentPosition
	 * @param lblDuration
	 */
	public void setSeekBar(SeekBar seekBar, TextView lblCurrentPosition, TextView lblDuration) {
		this.seekBar = seekBar;
		this.lblCurrentPosition = lblCurrentPosition;
		this.lblDuration = lblDuration;
		
		seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgress(0);
	}
	
	/**
	 * Stop the video playback.
	 */
	public void stop(){
		if (STATUS != STATUS_STOPED) {
			mPlayer.stop();
			STATUS = STATUS_STOPED;
			
			reset();
			wakeLockRelease();
		}
	}

	/**
	 * Get a string with the video's duration.
	 * The format of the string is hh:mm:ss 
	 * @param sec - The seconds to convert.
	 * @return A string formated.
	 */
	private String getDurationInSeconds(int sec){
		sec = sec / 1000;
		int hours = sec / 3600; 
		int minutes = (sec / 60) - (hours * 60);
		int seconds = sec - (hours * 3600) - (minutes * 60) ;
		String formatted = String.format("%d:%02d:%02d", hours, minutes, seconds);
		
		return formatted;
	}
	
	/**
	 * Set the current position of the video in the seekbar
	 * @param progress - The seconds to seek the bar
	 */
	private void setCurrentPosition(int progress){
		lblCurrentPosition.setText(getDurationInSeconds(progress));
	}
	
	/**
	 * Acquire wakelock the screen.
	 */
	private void wakeLockAcquire() {
		wakeLock.acquire();
	}
	
	/**
	 * Release the wakelock.
	 */
	private void wakeLockRelease() {
		wakeLock.release();
	}

	/**
	 * Update the seekbar while the video is playing.
	 */
	private void updateMediaProgress() {
		timer = new Timer("progress Updater");
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				((Activity) ctx).runOnUiThread(new Runnable() {
					public void run() {
						seekBar.setProgress(mPlayer.getCurrentPosition());
						setCurrentPosition(mPlayer.getCurrentPosition());
					}
				});
			}
		}, 0, 1000);
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		stop();
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		
		if (seekBar != null) {
			mPlayer.setOnSeekCompleteListener(this);
			
			int duration = (int) mp.getDuration();
			seekBar.setMax(duration);
			lblDuration.setText(getDurationInSeconds(duration));
			
			updateMediaProgress();
		}
		
		setUpVideoDimensions();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		setCurrentPosition(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mPlayer.seekTo(seekBar.getProgress() );
	}
	
	@Override
	public void onSeekComplete(MediaPlayer mp) {

	}


}
