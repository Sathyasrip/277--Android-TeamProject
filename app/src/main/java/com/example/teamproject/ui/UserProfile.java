package com.example.teamproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamproject.R;

public class UserProfile extends AppCompatActivity {

    private ImageView GoBackIcon;
    Button ButtonProfile;
    Boolean AdminMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        GoBackIcon = (ImageView) findViewById(R.id.image_profile_go_back);
        ButtonProfile = (Button) findViewById(R.id.button_profile_save);

        /*************************************************************************
         * When user presses the back button image, go back to the previous menu.
         *************************************************************************/
        GoBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AdminMode) {
                    // If any other user other than admin, do Firebase update and go back.
                    Intent PreviousActivityIntent = new Intent(
                            UserProfile.this,
                            UserPortal.class);
                    startActivity(PreviousActivityIntent);
                } else {
                    // If the user is an admin, return to the Admin menu after updating changes.
                    Intent PreviousActivityIntent = new Intent(
                            UserProfile.this,
                            AdministratorMenu.class);
                    startActivity(PreviousActivityIntent);
                }
                finish();
            }
        });

        /*****************************************************************************************
         * TODO: Depending on user changes from existing, change button text & update database.
         ****************************************************************************************/
        ButtonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AdminMode) {
                    // If any other user other than admin, do Firebase update and go back.
                    Intent PreviousActivityIntent = new Intent(
                            UserProfile.this,
                            UserPortal.class);
                    startActivity(PreviousActivityIntent);
                } else {
                    // If the user is an admin, return to the Admin menu after updating changes.
                    Intent PreviousActivityIntent = new Intent(
                            UserProfile.this,
                            AdministratorMenu.class);
                    startActivity(PreviousActivityIntent);
                }
                finish();
            }
        });
    }
}