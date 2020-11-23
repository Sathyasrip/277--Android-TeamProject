package com.example.teamproject.ui;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.teamproject.R;
import com.example.teamproject.model.ProfileSettings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Initialize the Firebase authenticator & Database
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

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
                        if(mAuth.getCurrentUser() != null) {
                            String user_uuid = mAuth.getCurrentUser().getUid();
                            GetFirebaseUserProfile(user_uuid);
                        } else {
                            // If no last user was detected, go to the login screen.
                            startActivity(new Intent(SplashScreen.this, LoginScreen.class));
                            finish();
                        }

                    }
                }, 3000);
    }

    public void GetFirebaseUserProfile(final String user_uuid) {
        DatabaseReference user_profiles_db = FirebaseDatabase.getInstance().getReference("user_profiles");
        user_profiles_db.child(user_uuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Ordered same way as in the database.
                String account_type = "", credentials = "", email = "", full_name = "",
                        firebase_profile_picture_file = "";
                int theme_id = 0;
                String username = "";

                // TODO: Fix FirebaseUserProfile.class to work DataSnapshot without crashing.
                int db_item_count = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (db_item_count == 0) {
                        String str_item = data.getValue(String.class);
                        account_type = str_item;
                    } else if (db_item_count == 1) {
                        String str_item = data.getValue(String.class);
                        credentials = str_item;
                    } else if (db_item_count == 2) {
                        String str_item = data.getValue(String.class);
                        email = str_item;
                    } else if (db_item_count == 3) {
                        String str_item = data.getValue(String.class);
                        full_name = str_item;
                    } else if (db_item_count == 4) {
                        String str_item = data.getValue(String.class);
                        firebase_profile_picture_file = str_item;
                    } else if (db_item_count == 5) {
                        long long_item = data.getValue(long.class);
                        theme_id = (int) long_item;
                    } else if (db_item_count == 6) {
                        String str_item = data.getValue(String.class);
                        username = str_item;
                    }
                    db_item_count++;
                }

                final ProfileSettings UserProfile = new ProfileSettings(user_uuid, firebase_profile_picture_file,
                        full_name, email, username, credentials, account_type, theme_id);

                // Get the DownloadURL
                String firebase_profile_pic_link = UserProfile.GetExistingFirebaseProfilePicture();
                //Toast.makeText(getApplicationContext(), firebase_profile_pic_link, Toast.LENGTH_SHORT).show();
                storageRef.child(firebase_profile_pic_link).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                {
                    @Override
                    public void onSuccess(Uri downloadUrl)
                    {
                        String ImageUrl = UserProfile.GetFirebasePublicUrl(downloadUrl.getEncodedPath());
                        UserProfile.UploadNewProfilePicture(ImageUrl);
                        //Toast.makeText(getApplicationContext(), ImageUrl, Toast.LENGTH_LONG).show();

                        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ImageUrl));
                        //startActivity(browserIntent);

                        //Toast.makeText(getApplicationContext(), UserProfile.GetLocalProfilePicture(), Toast.LENGTH_LONG).show();
                        Intent LoginScreenIntent = new Intent(
                                SplashScreen.this,
                                UserPortal.class);
                        LoginScreenIntent.putExtra("UserProfile", UserProfile);
                        //Toast.makeText(SplashScreen.this, "Login Successful!", Toast.LENGTH_LONG).show();
                        startActivity(LoginScreenIntent);
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value.
                Toast.makeText(getApplicationContext(), "Unable to Read from the Database!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}