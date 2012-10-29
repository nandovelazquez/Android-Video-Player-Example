package com.demo.videoplayer;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.demo.media.VideoStream;

public class MainActivity extends Activity implements OnClickListener, SurfaceHolder.Callback {

	private String urlVideo = "http://www.pocketjourney.com/downloads/pj/video/famous.3gp";
	private VideoStream player;
	private SurfaceView surface;
	private SurfaceHolder sHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_demo);
       
        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        Button btnPause = (Button) findViewById(R.id.btnPause);
        Button btnStop = (Button) findViewById(R.id.btnStop);
        surface = (SurfaceView) findViewById(R.id.surfaceView);
        
        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        
		sHolder = surface.getHolder();
		sHolder.addCallback(this);
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		try {
			player = new VideoStream(this);
			player.setUpVideoFrom(urlVideo);
			player.setDisplay(surface, sHolder);
			player.setSeekBar((SeekBar) findViewById(R.id.seekBar), 
					(TextView) findViewById(R.id.lblCurrentPosition), 
					(TextView) findViewById(R.id.lblDuration));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    }

    @Override
    public void onDestroy(){
    	super.onDestroy();
    	player.release();
    	player = null;
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	player.pause();
    }
    
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.btnPlay:
			try {
				player.play();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case R.id.btnPause:
			player.pause();
			break;
		case R.id.btnStop:
			player.stop();
			break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
 
}
