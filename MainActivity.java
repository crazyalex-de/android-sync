package com.cade.cade_vidsync;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.util.Calendar;
public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private Uri videoUri;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_DATE_SETTINGS = 123;
    private static int playCount;

    private static boolean fileExists(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Uri convertFilePathToUri(String filePath) {
        File videoFile = new File(filePath);
        return Uri.fromFile(videoFile);
    }

    public void showTimeset()
    {
        Intent intent2 = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent2.setAction(Settings.ACTION_DATE_SETTINGS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent2.setAction("android.settings.DATE_SETTINGS");
        } else {
            intent2.setAction(Settings.ACTION_SETTINGS);
        }

        startActivityForResult(intent2, REQUEST_DATE_SETTINGS);
    }

    public void checkTimesetAtStart()
    {
        long elapsedTime = SystemClock.elapsedRealtime();
        long uptimeInSeconds = elapsedTime / 1000; // Convert milliseconds to seconds
        long minutes = uptimeInSeconds / 60;

        if(minutes<5) {
            showTimeset();
        }
    }

    public void runVideoOnScreen(Uri myUri)
    {
        videoView = findViewById(R.id.videoView);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        if(myUri == null) {
            String videoFilename = "cade_autostart.mp4";
            String moviesDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
            String videoFilePath = moviesDirectoryPath + "/" + videoFilename;
            if (fileExists(videoFilePath)) {
                Log.d("STATE", "LÄUFT");
                videoUri = convertFilePathToUri(videoFilePath);
                // Check for and request the storage permission if needed
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    // Permission already granted, proceed with your logic
                }
            } else {
                //Select Video
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, 1);
            }
        }
        else
        {
            videoUri = myUri;
        }

        playCount = 0;
        initializeVideoView();
        scheduleNextFullMinuteCheck();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkTimesetAtStart();
        runVideoOnScreen(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save your variables to the bundle
        outState.putParcelable("Uri", videoUri);
        // Add more variables as needed
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        videoUri = savedInstanceState.getParcelable("Uri");
        runVideoOnScreen(videoUri);
    }

    private boolean isNextMinute() {

        Calendar currentTime = Calendar.getInstance();
        int currentSeconds = currentTime.get(Calendar.SECOND);

        // Calculate the remaining seconds until the next minute at 00
        int secondsUntilNextMinute = 60 - currentSeconds;

        if(secondsUntilNextMinute < 2)
            return true;
        else
            return false;
    }

    private void scheduleNextFullMinuteCheck() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // Check if the current time is at the next full minute
                if (isNextMinute() && videoUri != null) {
                    videoView.start();

                } else {
                    handler.postDelayed(this, 10);
                }
            }
        }, 10); // Initial delay before the first check
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            videoUri = data.getData();

            initializeVideoView();
        }
    }

    private void initializeVideoView() {
        videoView = findViewById(R.id.videoView);
        videoView.setMediaController(null);
        videoView.setVideoURI(videoUri);

        // Loop Video
        videoView.setOnCompletionListener(mp -> {
            playCount++;

            if(playCount < 10){
                videoView.start();
            }
            else {
                scheduleNextFullMinuteCheck();
                playCount = 0;
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Close the app when the back button is pressed
        finish();
    }
}



