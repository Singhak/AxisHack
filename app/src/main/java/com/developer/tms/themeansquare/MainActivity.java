package com.developer.tms.themeansquare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.developer.tms.themeansquare.Utility.ExistingUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.newUser)
    TextView newUser;
    @BindView(R.id.existingUser)
    TextView existingUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.newUser, R.id.existingUser})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.newUser:
                Intent newUserIntent = new Intent(MainActivity.this, CreateUserProfile.class);
                startActivity(newUserIntent);
                break;
            case R.id.existingUser:
                Intent existingUserIntent = new Intent(MainActivity.this, ExistingUser.class);
                startActivity(existingUserIntent);
                break;
        }
    }
}
