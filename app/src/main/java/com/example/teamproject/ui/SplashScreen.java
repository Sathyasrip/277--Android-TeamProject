package com.example.teamproject.ui;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.teamproject.R;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Hide the navigation bar to load as full screen.
        // Source: https://developer.android.com/training/system-ui/navigation
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Use the Timer delay for 3 seconds before jumping to the main activity.
        new Timer().schedule(
                new TimerTask(){

                    @Override
                    public void run(){
                        // Login into the Login Activity (if last login doesn't exist or invalid)
                        // Login into User Portal (if last login was successful)
                        startActivity(new Intent(SplashScreen.this, LoginScreen.class));

                        // Terminate this activity because its a splash screen.
                        finish();
                    }
                }, 3000);
    }
}