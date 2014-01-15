package com.example.zeebraapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import java.io.File;
import java.io.IOException;


@SuppressLint("HandlerLeak")
public class AudioRecordTest extends Activity
{
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    
    private Handler handler = null;
    
    private Boolean recording = false;
    


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        recording = true;
        handler.post(updateVisualizer);

    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        recording = false;
        handler.removeCallbacks(updateVisualizer); // stop updating GUI
        //visualizer.clear();

    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    public AudioRecordTest() {
    	

    	File folder = new File(Environment.getExternalStorageDirectory() + "/ZeebraaRecordings");
    	boolean success = true;
    	if (!folder.exists()) {
    	    success = folder.mkdir();
    	}
    	if (success) {
    	    // Do something on success
    	} else {
    	    // Do something else on failure 
    	}
    	
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZeebraaRecordings";
        mFileName += "/test.3gp";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
//		setContentView(R.layout.activity_audio_record_test);


        LinearLayout ll = new LinearLayout(this);
//		LinearLayout ll = (LinearLayout) findViewById(R.id.ll1);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));
        setContentView(ll);
        
//        visualizer = (VisualizerView) findViewById(R.id.visualizerView1);
//        visualizer = new VisualizerView(this);
//        ll.addView(visualizer,new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                1));

        
        handler = new Handler(){ 
            @Override public void handleMessage(Message msg) { 
                String mString=(String)msg.obj;
                Toast.makeText(getApplicationContext(), mString, Toast.LENGTH_SHORT).show();
             }
         };

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
    
    
    
    
    
	Runnable updateVisualizer = new Runnable() {
		@Override
		public void run() {
			if (recording) // if we are already recording
			{
				// get the current amplitude
//				int x = mRecorder.getMaxAmplitude();
//				Message msg=new Message();
//                msg.obj=Integer.toString(x);
//                handler.sendMessage(msg);
//                visualizer.addAmplitude(x); // update the VisualizeView
//                visualizer.invalidate();
                handler.postDelayed(this, 50);


//                 try {
//                     Thread.sleep(100);
//                 } 
//                 catch (InterruptedException e) {
//                    e.printStackTrace();
//                 }
//				System.out.println(x);
//				Toast.makeText(getApplicationContext(),
//		        		x,
//		        		Toast.LENGTH_SHORT).show();
				//visualizer.addAmplitude(x); // update the VisualizeView
				//visualizer.invalidate(); // refresh the VisualizerView
				//handler.postDelayed(this, 50); // update in 50 milliseconds
			} // end if
		} // end method run
	}; // end Runnable

    
    
    
    
    
    
    
    
    
    
    
}