package com.developer.tms.themeansquare;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.tms.themeansquare.Utility.AuthUser;
import com.developer.tms.themeansquare.Utility.DBHelper;
import com.developer.tms.themeansquare.Utility.WavAudioRecorder;
import com.developer.tms.themeansquare.speakerrecognition.SpeakerVerificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.verification.Verification;

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

public class VoiceVerification extends AppCompatActivity {

    SmoothProgressBar progressBar;
    DBHelper dbHelper;
    AuthUser authUser;
    File wavRecordFile;
    @BindView(R.id.down_curve)
    TextView downCurve;
    @BindView(R.id.headerLabel)
    TextView headerLabel;
    @BindView(R.id.voiceRecording)
    ImageView voiceRecording;
    @BindView(R.id.voiceRecordingLabel)
    TextView voiceRecordingLabel;
    @BindView(R.id.voiceMessagesLayout)
    LinearLayout voiceMessagesLayout;
    @BindView(R.id.recordingGIF)
    GifImageView recordingGIF;
    private WavAudioRecorder wavRecorder;
    String api_key = "e3dcbca68ac9484cad5e05d57d138393";
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_verification);
        ButterKnife.bind(this);
        String user_name = getIntent().getStringExtra("USER_NAME");

        dbHelper = new DBHelper(this);
        authUser = dbHelper.getUser(user_name);
        headerLabel.setText(authUser.getPhrase());
        //Creating a folder to store recorded files
        File path = new File(Environment.getExternalStorageDirectory().getPath() + "/vrify");
        Log.d("Profile", "onCreate: " + path);
        path.mkdirs();
        try {
            wavRecordFile = File.createTempFile("wavefile", ".wav", path);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }
        isAudioPermissionGranted();
        isStoragePermissionGranted();
        progressBar = (SmoothProgressBar) findViewById(R.id.progressbar);
    }

    @OnClick(R.id.voiceRecording)
    public void onViewClicked() {
        if (!isRecording) {
            isRecording = true;
            voiceRecording.setImageResource(R.drawable.stopaudio);
            voiceRecordingLabel.setText("Stop Recording");
            recordingGIF.setVisibility(View.VISIBLE);
            wavRecorder = WavAudioRecorder.getInstanse();
            wavRecorder.setOutputFile(wavRecordFile.getAbsolutePath());
            wavRecorder.prepare();
            wavRecorder.start();
        } else if (isRecording) {
            isRecording = false;
            voiceRecording.setImageResource(R.drawable.recordaudio);
            voiceRecordingLabel.setText("Start Recording");
            recordingGIF.setVisibility(View.GONE);
            wavRecorder.stop();
            VerificationTask verificationTask = new VerificationTask();
            verificationTask.execute();
        }
    }

    /*
    In this we are taking user recoded file and passing it to api with user unique.
    If it match with user enrolled voice attribute it will return ACCEPT otherwise RESJECT
     */
    private class VerificationTask extends AsyncTask<Void, Void, Void> {
        String verificationResult;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(wavRecordFile)));
                SpeakerVerificationRestClient restClient = new SpeakerVerificationRestClient(api_key);
                Verification verification = restClient.verify(dis, UUID.fromString(authUser.getUuid()));
                verificationResult = verification.result.toString();
            } catch (Throwable t) {
                Toast.makeText(VoiceVerification.this, "Something goes wrong, Try GAIN", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.progressiveStart();
        }

        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            voiceMessagesLayout.setVisibility(View.GONE);
            progressBar.progressiveStop();
            voiceRecordingLabel.setText("Your voice is " + verificationResult + "ED");
        }
    }

    public boolean isAudioPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 2);
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }
    }
}
