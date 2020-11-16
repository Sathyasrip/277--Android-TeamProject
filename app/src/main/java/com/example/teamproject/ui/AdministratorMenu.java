package com.example.teamproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.teamproject.R;

public class AdministratorMenu extends AppCompatActivity {

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
                //Intent UserProfileIntent = new Intent(
                //        AdministratorMenu.this,
                //        UserProfile.class);
                //startActivity(UserProfileIntent);
            }
        });
        OfflineDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent OfflineDemoIntent = new Intent(
                //        AdministratorMenu.this,
                //        UserPortal.class);
                //startActivity(OfflineDemoIntent);
            }
        });
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LogoutIntent = new Intent(
                        AdministratorMenu.this,
                        LoginScreen.class);
                startActivity(LogoutIntent);
            }
        });
    }
}