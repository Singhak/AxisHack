package com.developer.tms.themeansquare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.tms.themeansquare.Utility.AuthUser;
import com.developer.tms.themeansquare.Utility.DBHelper;
import com.developer.tms.themeansquare.Utility.WavAudioRecorder;
import com.developer.tms.themeansquare.speakerrecognition.SpeakerVerificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.verification.Enrollment;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import pl.droidsonroids.gif.GifImageView;

public class VoiceEnrollment extends Activity {
    Enrollment playTask;
    Button startRecordingButton, stopRecordingButton, startPlaybackButton,
            stopPlaybackButton;
    TextView statusText;
    String user_name = "";

    File recordingFile, finalFile;
    static int enroll_count = 0;

    boolean isRecording = false, isPlaying = false;

    //    int frequency = 11025, channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
//    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    @BindView(R.id.down_curve)
    TextView downCurve;
    @BindView(R.id.headerLabel)
    TextView headerLabel;
    @BindView(R.id.voiceRecordingOptionLabel)
    TextView voiceRecordingOptionLabel;
    @BindView(R.id.voiceRecordingOptions)
    ImageView voiceRecordingOptions;
    @BindView(R.id.success1)
    ImageView success1;
    @BindView(R.id.success2)
    ImageView success2;
    @BindView(R.id.success3)
    ImageView success3;
    @BindView(R.id.recordingImageGIF)
    GifImageView recordingImageGIF;
    @BindView(R.id.progressbar)
    SmoothProgressBar progressbar;

    private WavAudioRecorder wavRecorder;
    DBHelper dbHelper;
    AuthUser authUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_enrollment);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            user_name = extras.getString("USER_NAME");

        dbHelper = new DBHelper(this);
        authUser = dbHelper.getUser(user_name);

        enroll_count = authUser.getEnroll_count();
        headerLabel.setText(authUser.getPhrase());

        isStoragePermissionGranted();
        isAudioPermissionGranted();

        File path = new File(Environment.getExternalStorageDirectory().getPath() + "/vrify");
        Log.d("Profile", "onCreate: " + path);
        path.mkdirs();
        try {
            finalFile = File.createTempFile("wavefile", ".wav", path);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }
    }

    private class EnrollmentTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            progressbar.progressiveStop();
            progressbar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
            if (enroll_count == 1) {
                success1.setImageResource(R.drawable.green_dot);
            }
            if (enroll_count == 2) {
                success2.setImageResource(R.drawable.green_dot);
            }
            if (enroll_count == 3) {
                voiceRecordingOptions.setImageResource(R.drawable.recordaudio);
                voiceRecordingOptionLabel.setText("Success fully enrolled");
                success3.setImageResource(R.drawable.green_dot);
                authUser.setEnroll_count(enroll_count);
                dbHelper.updateUser(authUser);
                Handler h = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        Intent i = new Intent().setClass(VoiceEnrollment.this, VoiceVerification.class);
                        i.putExtra("USER_NAME", authUser.getUser_name());
                        startActivity(i);
                    }
                };

                h.sendEmptyMessageDelayed(0, 800);
            }
        }

        @Override
        protected void onPreExecute() {
            progressbar.setVisibility(View.VISIBLE);
            progressbar.progressiveStart();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(finalFile)));
                SpeakerVerificationRestClient restClient = new SpeakerVerificationRestClient("e3dcbca68ac9484cad5e05d57d138393");
                Enrollment enroll = restClient.enroll(dis, UUID.fromString(authUser.getUuid()));
                enroll_count = enroll.enrollmentsCount;
            } catch (Throwable t) {
                Toast.makeText(VoiceEnrollment.this, "Failed, Please Try again", Toast.LENGTH_LONG);
            }
            return null;
        }
    }

    @OnClick(R.id.voiceRecordingOptions)
    public void onViewClicked() {
        if (enroll_count != 3) {
            if (!isRecording) {
                isRecording = true;
                voiceRecordingOptions.setImageResource(R.drawable.stopaudio);
                voiceRecordingOptionLabel.setText("Stop Recording");
                recordingImageGIF.setVisibility(View.VISIBLE);
                wavRecorder = WavAudioRecorder.getInstanse();
                wavRecorder.setOutputFile(finalFile.getAbsolutePath());
                wavRecorder.prepare();
                wavRecorder.start();
            } else if (isRecording) {
                isRecording = false;
                voiceRecordingOptions.setImageResource(R.drawable.recordaudio);
                voiceRecordingOptionLabel.setText("Start Recording");
                recordingImageGIF.setVisibility(View.GONE);
                wavRecorder.stop();
                EnrollmentTask enrollmentTask = new EnrollmentTask();
                enrollmentTask.execute();
            }
        }
    }

    public boolean isAudioPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                Log.v("Profile", "Permission is granted");
                return true;
            } else {
                Log.v("Profile", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Profile", "Permission is granted");
            return true;
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Profile", "Permission is granted");
                return true;
            } else {
                Log.v("Profile", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Profile", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("Profile", "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

}

