package com.example.teamproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamproject.R;

public class UserProfile extends AppCompatActivity {

    Button ButtonProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        ButtonProfile = (Button) findViewById(R.id.button_profile_save);
        /*****************************************************************************************
         * TODO: Depending on user changes from existing, change button text & update database.
         ****************************************************************************************/
        ButtonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // After getting the changes & updated the database, return to DebugMenu activity.
                Intent DebugMenuIntent = new Intent(
                        UserProfile.this,
                        AdministratorMenu.class);
                startActivity(DebugMenuIntent);
            }
        });
    }
}