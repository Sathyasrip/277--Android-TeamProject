package com.example.teamproject.ui;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.teamproject.R;
import com.example.teamproject.model.ProfileSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginScreen extends AppCompatActivity {

    String[] AccountType = {"standard", "reviewer", "moderator"};
    static ProfileSettings CurrentUser = new ProfileSettings();

    private ProgressBar progressBar;
    private Button ButtonLogin;
    private TextView ClickToRegister;
    private EditText EditTextUserName, EditTextUserPassword;
    private String username = "", password = "", accountType = "", accountUser = "";

    // Firebase Authentication
    static String[] TestAccount = {"academia", "test1234", "academia.tester1@gmail.com"};
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        // Initialize the Firebase authenticator & Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            String user_uuid = mAuth.getCurrentUser().getUid();
            GetFirebaseUserProfile(user_uuid);

            //CurrentUser = GetTestUserProfile();
            // Pass the user profile to the User Portal activity to display some actions.
            //Intent LoginScreenIntent = new Intent(
            //        LoginScreen.this,
            //        UserPortal.class);
            //LoginScreenIntent.putExtra("UserProfile", CurrentUser);
            //Toast.makeText(LoginScreen.this, "Welcome Back! " + CurrentUser.FullName(), Toast.LENGTH_SHORT).show();
            //startActivity(LoginScreenIntent);
            //finish();
        }

        // Initialize all the views on the layout.
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ButtonLogin = (Button) findViewById(R.id.button_userlogin);
        ClickToRegister = (TextView) findViewById(R.id.tv_register_click);
        EditTextUserName = (EditText) findViewById(R.id.editTextUsername);
        EditTextUserPassword = (EditText) findViewById(R.id.editTextPassword);

        /*******************************************************
         * Check if the user entered the username and password.
         *******************************************************/
        EditTextUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost check that the text field has valid values.
                username = String.valueOf(EditTextUserName.getText());
                password = String.valueOf(EditTextUserPassword.getText());

                if (!hasFocus) {
                    if (username != "" && password != "") {
                        // Enable the Login Button.
                        ButtonLogin.setEnabled(true);
                    }
                }
            }
        });

        /********************************************************************
         * Check if the user used the test account, or administer account.
         * If user entered an invalid username & password, throw a toast.
         *******************************************************************/
        ButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*******************************************
                 * Validate the Username and password
                 *******************************************/
                username = String.valueOf(EditTextUserName.getText());
                password = String.valueOf(EditTextUserPassword.getText());

                if (username.equals("") || password.equals("")) {
                    // Invalid user, so throw a Toast message.
                    Toast.makeText(getApplicationContext(),
                            "Invalid Username and/or password, Try again!",
                            Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    //authenticate user
                    mAuth.signInWithEmailAndPassword(username, password)
                            .addOnCompleteListener(LoginScreen.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    progressBar.setVisibility(View.GONE);
                                    if (!task.isSuccessful()) {
                                        // there was an error
                                        if (password.length() < 6) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Invalid Username and/or password, Try again!",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(LoginScreen.this, "Authentication failed!", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        // Before logging in, load the profile.
                                        // TODO: Get the profile information from firebase.
                                        String user_uuid = mAuth.getCurrentUser().getUid();
                                        GetFirebaseUserProfile(user_uuid);
                                        //CurrentUser = GetTestUserProfile();
                                        // Pass the user profile to the User Portal activity to display some actions.
                                        //Intent LoginScreenIntent = new Intent(
                                        //        LoginScreen.this,
                                        //        UserPortal.class);
                                        //LoginScreenIntent.putExtra("UserProfile", CurrentUser);
                                        //Toast.makeText(LoginScreen.this, "Login Successful!", Toast.LENGTH_LONG).show();
                                        //startActivity(LoginScreenIntent);
                                        //finish();
                                    }
                                }
                            });
                }
            }
        });

        /****************************************************************
         * Registration Menu requires SQL Lite or NoSQL Cloud Firestore.
         ****************************************************************/
        ClickToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RegistrationIntent = new Intent(
                        LoginScreen.this,
                        NewRegistration.class);
                startActivity(RegistrationIntent);
            }
        });
    }

    public ProfileSettings GetTestUserProfile() {
        // Returns the Test User's profile.
        String firebase_auth_uuid = "mODpElIxyydsH28D9i48yKcvRLT2";
        String firebase_profile_picture_file = firebase_auth_uuid + ".jpg";
        String full_name = "Mary Sue";
        String email = "academia.tester1@gmail.com";
        String username = "marysue";
        String credentials = "Academia Reviewer Tester";
        String account_type = "standard";
        int theme_id = 1;
        ProfileSettings TestUser = new ProfileSettings(firebase_auth_uuid,
                firebase_profile_picture_file, full_name, email, username, credentials,
                account_type, theme_id);
        String ImageUrl = "https://i.pinimg.com/originals/96/f8/12/96f8124135db17dd063555a910e5e240.jpg";
        TestUser.UploadNewProfilePicture(ImageUrl); // Will be used to test web loading in Profile.

        return TestUser;
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

                ProfileSettings UserProfile = new ProfileSettings(user_uuid, firebase_profile_picture_file,
                        full_name, email, username, credentials, account_type, theme_id);

                // TODO: For now, just use the ImageURL from somewhere until downloading is figured out.
                String ImageUrl = "https://i.pinimg.com/originals/96/f8/12/96f8124135db17dd063555a910e5e240.jpg";
                UserProfile.UploadNewProfilePicture(ImageUrl);
                //CurrentUser = UserProfile;

                // Now go to the User Portal.
                Intent LoginScreenIntent = new Intent(
                        LoginScreen.this,
                        UserPortal.class);
                LoginScreenIntent.putExtra("UserProfile", UserProfile);
                Toast.makeText(LoginScreen.this, "Login Successful!", Toast.LENGTH_LONG).show();
                startActivity(LoginScreenIntent);
                finish();
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