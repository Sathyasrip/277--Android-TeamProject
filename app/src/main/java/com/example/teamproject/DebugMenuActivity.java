package com.example.teamproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DebugMenuActivity extends AppCompatActivity {

    Button UserProfile, OfflineDemo, Logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_screen);

        UserProfile = (Button) findViewById(R.id.button_userprofile);
        OfflineDemo = (Button) findViewById(R.id.button_offline_demo);
        Logout = (Button) findViewById(R.id.button_logout);

        /********************************************************************
         * When the user presses any buttons, take them to the activity.
         *******************************************************************/
        UserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent UserProfileIntent = new Intent(
                        DebugMenuActivity.this,
                        UserProfileActivity.class);
                startActivity(UserProfileIntent);
            }
        });
        OfflineDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent OfflineDemoIntent = new Intent(
                        DebugMenuActivity.this,
                        LocalDemoActivity.class);
                startActivity(OfflineDemoIntent);
            }
        });
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LogoutIntent = new Intent(
                        DebugMenuActivity.this,
                        LoginActivity.class);
                startActivity(LogoutIntent);
            }
        });
    }
}