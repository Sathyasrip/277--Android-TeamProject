package com.example.teamproject.ui;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamproject.R;
import com.example.teamproject.model.ProfileSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NewRegistration extends AppCompatActivity {
    private ImageView GoBackIcon;
    private Button Registration;
    private EditText EmailAddress, UserName, Password, ConfirmPassword, FullName, Credentials;
    private TextView EmailError, UserNameError, PasswordError, ConfirmPasswordError;
    private String email_address, username, password, confirm_password, fullname, credentials;
    private String account_type = "standard";
    private ArrayList<String> user_emails = new ArrayList<String>();
    private ArrayList<String> user_logins = new ArrayList<String>();
    private Boolean email_error = false, duplicate_user = false, invalid_password = false,
            non_matching_passwords = false;
    private ArrayList<Boolean> filled_fields = new ArrayList<Boolean>();

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private void UpdateRegistrationButton() {
        Registration = (Button) findViewById(R.id.button_save_registration);

        // Check if all fields are filled, and then enable the Registration button accordingly.
        Boolean all_fields_filled = true;
        for (int i=0; i < filled_fields.size(); ++i) {
            if (!filled_fields.get(i)) {
                all_fields_filled = false;
            }
        }
        if (all_fields_filled) {
            Registration.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_registration);

        // Firebase authentication.
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Define all views.
        GoBackIcon = (ImageView) findViewById(R.id.image_registration_go_back);
        Registration = (Button) findViewById(R.id.button_save_registration);
        EmailAddress = (EditText) findViewById(R.id.editTextRegisterEmail);
        UserName = (EditText) findViewById(R.id.editTextRegisterUsername);
        Password = (EditText) findViewById(R.id.editTextRegisterPassword);
        ConfirmPassword = (EditText) findViewById(R.id.editTextRegisterConfirmPassword);
        FullName = (EditText) findViewById(R.id.editTextRegisterFullName);
        Credentials = (EditText) findViewById(R.id.editTextRegisterCredentials);
        EmailError = (TextView) findViewById(R.id.tv_email_failure);
        UserNameError = (TextView) findViewById(R.id.tv_username_failure);
        PasswordError = (TextView) findViewById(R.id.tv_password_failure);
        ConfirmPasswordError = (TextView) findViewById(R.id.tv_confirm_failure);

        /***************************************************************************************
         * TODO: Use an SQL Query or NoSQL Query to get a list of user logins & email accounts.
         **************************************************************************************/
        user_emails.add(LoginScreen.TestAccount[2]);
        user_logins.add(LoginScreen.TestAccount[0]);


        /**********************************************************************
         * TODO: Replace with LiveData instead.
         *********************************************************************/
        // Initialize all filled_field flags to false.
        for (int i=0; i < 6; ++i) {
            filled_fields.add(false);
        }

        /***************************************************
         * If the user presses the back icon, return to the
         * Login activity.
         **************************************************/
        GoBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the activity and go back to the Login Screen.
                Intent LoginIntent = new Intent(
                        NewRegistration.this,
                        LoginScreen.class);
                startActivity(LoginIntent);
                finish();
            }
        });

        /*******************************************************
         * Check if the user entered the following:
         * - Valid Email Address.
         * - Valid username (it is not taken already)
         * - Valid password (over 6 characters long)
         * - Confirmed password matches set password.
         *******************************************************/
        EmailAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost check that the text field has valid values.
                email_address = String.valueOf(EmailAddress.getText());

                if (!hasFocus) {
                    // Check all user emails to see if the email address specified is duplicate.
                    email_error = false;
                    for (int i=0; i < user_emails.size(); ++i) {
                        if (email_address.equals(user_emails.get(i))) {
                            email_error = true; // Only set true if there is a duplicate email.
                        }
                    }

                    // If a duplicate username exists, 'enable' the corresponding error message.
                    if (email_error) {
                        EmailError.setTextColor(Color.RED); // Red means duplicate.
                        EmailError.setText(getResources().getString(R.string.error_registration_email));
                        filled_fields.set(0, false);
                    } else {
                        EmailError.setText("");
                        if (!email_address.equals("")) {
                            filled_fields.set(0, true);
                        }
                    }

                    UpdateRegistrationButton();
                }
            }
        });
        UserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost check that the text field has valid values.
                username = String.valueOf(UserName.getText());

                if (!hasFocus) {
                    // Check all user_logins to see if the username specified is duplicate.
                    duplicate_user = false;
                    for (int i=0; i < user_logins.size(); ++i) {
                        if (username.equals(user_logins.get(i))) {
                            duplicate_user = true; // Only set true if there is a duplicate user.
                        }
                    }

                    // If a duplicate username exists, 'enable' the corresponding error message.
                    if (duplicate_user) {
                        UserNameError.setText(getResources().getString(R.string.error_registration_username));
                        filled_fields.set(1, false);
                    } else {
                        UserNameError.setText("");
                        if (!username.equals("")) {
                            filled_fields.set(1, true);
                        }
                    }

                    UpdateRegistrationButton();
                }
            }
        });
        Password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost check that the text field has valid values.
                password = String.valueOf(Password.getText());

                if (!hasFocus) {
                    // Check if the password meets the required minimum password requirements.
                    invalid_password = false;
                    if (password.length() < 6) {
                        invalid_password = true;
                    }

                    // If the password is invalid, 'enable' the corresponding error message.
                    if (invalid_password) {
                        PasswordError.setText(getResources().getString(R.string.error_registration_password));
                        filled_fields.set(2, false);
                    } else {
                        PasswordError.setText("");
                        if (!password.equals("")) {
                            filled_fields.set(2, true);
                        }
                    }

                    UpdateRegistrationButton();
                }
            }
        });
        ConfirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost check that the text field has valid values.
                password = String.valueOf(Password.getText());
                confirm_password = String.valueOf(ConfirmPassword.getText());

                if (!hasFocus) {
                    // Check if the password and confirm_password are equal, if not show error.
                    non_matching_passwords = false;
                    if (!password.equals(confirm_password)) {
                        non_matching_passwords = true;
                    }

                    // If the two password fields don't match 'enable' the error message.
                    if (non_matching_passwords) {
                        ConfirmPasswordError.setText(getResources().getString(R.string.error_registration_confirm_password));
                        filled_fields.set(3, false);
                    } else {
                        ConfirmPasswordError.setText("");
                        if (!confirm_password.equals("")) {
                            filled_fields.set(3, true);
                        }
                    }

                    UpdateRegistrationButton();
                }
            }
        });
        FullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost check that the text field has valid values.
                fullname = String.valueOf(FullName.getText());

                if (!hasFocus) {
                    if (!fullname.equals("")) {
                        filled_fields.set(4, true);
                    } else {
                        filled_fields.set(4, false);
                    }

                    UpdateRegistrationButton();
                }
            }
        });
        Credentials.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost check that the text field has valid values.
                credentials = String.valueOf(Credentials.getText());

                if (!hasFocus) {
                    if (!credentials.equals("")) {
                        filled_fields.set(5, true);
                    } else {
                        filled_fields.set(5, false);
                    }

                    UpdateRegistrationButton();
                }
            }
        });

        /**********************************************************************
         * Verify all fields are filled before allowing clicking registration
         *********************************************************************/
        // TODO: Use LiveData instead of the hackery for OnFocus in Edit Text.
        Registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do a firebase authentication.
                mAuth.createUserWithEmailAndPassword(email_address, password)
                        .addOnCompleteListener(NewRegistration.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(NewRegistration.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    String user_uuid = task.getResult().getUser().getUid();
                                    //Toast.makeText(NewRegistration.this, user_uuid, Toast.LENGTH_SHORT).show();
                                    mDatabase.child("user_profiles").child(user_uuid).child("account_type").setValue(account_type);
                                    mDatabase.child("user_profiles").child(user_uuid).child("credentials").setValue(credentials);
                                    mDatabase.child("user_profiles").child(user_uuid).child("email").setValue(email_address);
                                    mDatabase.child("user_profiles").child(user_uuid).child("full_name").setValue(fullname);
                                    mDatabase.child("user_profiles").child(user_uuid).child("picture").setValue("no_photo.png");
                                    mDatabase.child("user_profiles").child(user_uuid).child("username").setValue(username);
                                    mDatabase.child("user_profiles").child(user_uuid).child("theme_id").setValue((long) 0);

                                    // Test if sign out works.
                                    mAuth.signOut();

                                    // After the profile was successfully created, we should now leave the activity.
                                    Intent LoginIntent = new Intent(
                                            NewRegistration.this,
                                            LoginScreen.class);
                                    //Toast.makeText(NewRegistration.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                                    startActivity(LoginIntent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }
}