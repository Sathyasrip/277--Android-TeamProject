package com.example.teamproject;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RegistrationActivity extends AppCompatActivity {

    private Button Registration;
    private EditText UserName, Password, ConfirmPassword, FullName, Credentials;
    private TextView UserNameError, PasswordError, ConfirmPasswordError;
    private String username, password, confirm_password, fullname, credentials;
    private ArrayList<String> user_logins = new ArrayList<String>();
    private Boolean duplicate_user = false, invalid_password = false,
            non_matching_passwords = false;
    private ArrayList<Boolean> filled_fields = new ArrayList<Boolean>();


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
        setContentView(R.layout.registration);

        Registration = (Button) findViewById(R.id.button_save_registration);
        UserName = (EditText) findViewById(R.id.editTextRegisterUsername);
        Password = (EditText) findViewById(R.id.editTextRegisterPassword);
        ConfirmPassword = (EditText) findViewById(R.id.editTextRegisterConfirmPassword);
        FullName = (EditText) findViewById(R.id.editTextRegisterFullName);
        Credentials = (EditText) findViewById(R.id.editTextRegisterCredentials);
        UserNameError = (TextView) findViewById(R.id.tv_username_failure);
        PasswordError = (TextView) findViewById(R.id.tv_password_failure);
        ConfirmPasswordError = (TextView) findViewById(R.id.tv_confirm_failure);

        /**********************************************************************
         * TODO: Use an SQL Query or NoSQL Query to get a list of user logins.
         *********************************************************************/
        user_logins.add(LoginActivity.TestAccount[0]);
        user_logins.add(LoginActivity.AdminAccount[0]);


        /**********************************************************************
         * TODO: Replace with LiveData instead.
         *********************************************************************/
        // Initialize all filled_field flags to false.
        for (int i=0; i < 5; ++i) {
            filled_fields.add(false);
        }

        /*******************************************************
         * Check if the user entered the following:
         * - Valid username (it is not taken already)
         * - Valid password (over 6 characters long)
         * - Confirmed password matches set password.
         *******************************************************/
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
                        UserNameError.setTextColor(Color.RED); // Red means duplicate.
                        filled_fields.set(0, false);
                    } else {
                        UserNameError.setTextColor(Color.WHITE); // White means disabled.
                        if (!username.equals("")) {
                            filled_fields.set(0, true);
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
                        PasswordError.setTextColor(Color.RED); // Red means invalid password.
                        filled_fields.set(1, false);
                    } else {
                        PasswordError.setTextColor(Color.WHITE); // White means disabled.
                        if (!password.equals("")) {
                            filled_fields.set(1, true);
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
                        ConfirmPasswordError.setTextColor(Color.RED); // Red means no match.
                        filled_fields.set(2, false);
                    } else {
                        ConfirmPasswordError.setTextColor(Color.WHITE); // White means disabled.
                        if (!confirm_password.equals("")) {
                            filled_fields.set(2, true);
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
                        filled_fields.set(3, true);
                    } else {
                        filled_fields.set(3, false);
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
                        filled_fields.set(4, true);
                    } else {
                        filled_fields.set(4, false);
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
                // TODO: Save the data provided in the EditText to the database & return to login.
                Intent LoginIntent = new Intent(
                        RegistrationActivity.this,
                        LoginActivity.class);
                startActivity(LoginIntent);
            }
        });
    }
}