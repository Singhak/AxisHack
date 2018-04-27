package com.developer.tms.themeansquare;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.tms.themeansquare.Utility.AuthUser;
import com.developer.tms.themeansquare.Utility.DBHelper;
import com.developer.tms.themeansquare.speakerrecognition.SpeakerVerificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.verification.CreateProfileResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class CreateUserProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String selected_phrase = "";
    String user_name = "";
    DBHelper dbHelper = null;

    @BindView(R.id.notesSpinner)
    AppCompatSpinner notesSpinner;
    @BindView(R.id.voiceTextET)
    EditText voiceTextET;
    @BindView(R.id.down_curve)
    TextView downCurve;
    @BindView(R.id.proceedButton)
    TextView proceedButton;
    private AuthUser authUser;

    @BindView(R.id.progressbar)
    SmoothProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_layout);
        ButterKnife.bind(this);
        notesSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0)
            selected_phrase = parent.getItemAtPosition(position).toString();
//        Toast.makeText(parent.getContext(), "Selected: " + selected_phrase, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @OnClick(R.id.proceedButton)
    public void onViewClicked() {
        user_name = voiceTextET.getText().toString();
        if (notesSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(CreateUserProfile.this, "Please select phrase for voice recognition", Toast.LENGTH_LONG).show();
        } else if (user_name.isEmpty()) {
            Toast.makeText(CreateUserProfile.this, "Enter profile name", Toast.LENGTH_LONG).show();
        } else if (selected_phrase.isEmpty()) {
            Toast.makeText(CreateUserProfile.this, "Select one phrase to proceed", Toast.LENGTH_LONG).show();
        } else {
            dbHelper = new DBHelper(CreateUserProfile.this);
            AuthUser user = dbHelper.getUser(user_name);
            if (user != null) {
                Toast.makeText(CreateUserProfile.this, "User name is already taken, choose different", Toast.LENGTH_LONG).show();
            } else {
                new CreateProfileTask().execute();
            }
        }
    }

    private class CreateProfileTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressbar.setVisibility(View.VISIBLE);
            progressbar.progressiveStart();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String uuid = "";
            SpeakerVerificationRestClient verificationRestClient = new SpeakerVerificationRestClient("e3dcbca68ac9484cad5e05d57d138393");
            try {
                CreateProfileResponse profile = verificationRestClient.createProfile("en-us");
                uuid = profile.verificationProfileId.toString();
                Log.d("Profile = ", profile.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return uuid;
        }

        protected void onPostExecute(String uuid) {

            progressbar.progressiveStop();
            progressbar.setVisibility(View.GONE);

            if (!uuid.isEmpty()) {
                authUser = new AuthUser(user_name, uuid, selected_phrase, 0);
                dbHelper.addUser(authUser);
                Toast.makeText(CreateUserProfile.this, "Your profile is created successfully", Toast.LENGTH_LONG).show();

                Handler h = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        Intent i = new Intent().setClass(CreateUserProfile.this, VoiceEnrollment.class);
                        i.putExtra("USER_NAME", authUser.getUser_name());
                        startActivity(i);
                    }
                };

                h.sendEmptyMessageDelayed(0, 800);
            }
        }

    }

}
