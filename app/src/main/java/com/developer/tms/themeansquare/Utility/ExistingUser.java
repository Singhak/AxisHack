package com.developer.tms.themeansquare.Utility;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.tms.themeansquare.R;
import com.developer.tms.themeansquare.VoiceEnrollment;
import com.developer.tms.themeansquare.VoiceVerification;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExistingUser extends AppCompatActivity {

    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.proceed)
    TextView proceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.existing_user);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.proceed)
    public void onViewClicked() {
        String user_name = username.getText().toString();
        if (user_name.isEmpty()) {
            Toast.makeText(ExistingUser.this, "Enter profile Name", Toast.LENGTH_LONG).show();
        }
        else{
            DBHelper dbHelper = new DBHelper(ExistingUser.this);
            AuthUser user = dbHelper.getUser(user_name);
            if (user != null) {
                Intent i;
                if (user.getEnroll_count() == 3) {
                    i = new Intent(ExistingUser.this, VoiceVerification.class);
                } else {
                    i = new Intent(ExistingUser.this, VoiceEnrollment.class);
                }
                i.putExtra("USER_NAME", user_name);
                startActivity(i);
            } else {
                Toast.makeText(ExistingUser.this, "This user does not exist", Toast.LENGTH_LONG).show();
            }
        }
    }
}
