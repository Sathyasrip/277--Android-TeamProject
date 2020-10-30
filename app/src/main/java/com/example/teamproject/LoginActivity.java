package com.example.teamproject;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_ACCOUNT_USERNAME = "com.example.teamproject.ACCOUNT_USER";
    public static final String EXTRA_ACCOUNT_TYPE = "com.example.teamproject.ACCOUNT_TYPE";

    private Button ButtonLogin, ButtonRegister;
    private EditText EditTextUserName, EditTextUserPassword;
    static String[] TestAccount = {"test", "test1234"};
    static String[] AdminAccount = {"admin", "project11"};
    String[] AccountType = {"standard", "admin"};
    private String username = "", password = "",
            accountType = "unknown", accountUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        ButtonLogin = (Button) findViewById(R.id.button_userlogin);
        ButtonRegister = (Button) findViewById(R.id.button_userregister);
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

                if (username.equals(TestAccount[0]) && password.equals(TestAccount[1])) {
                    accountType = AccountType[0];
                    accountUser = username;
                } else if (username.equals(AdminAccount[0]) && password.equals(AdminAccount[1])) {
                    accountType = AccountType[1];
                    accountUser = username;
                } else {
                    // Invalid user, so throw a Toast message.
                    Toast.makeText(getApplicationContext(),
                            "Invalid Username and/or password, Try again!",
                            Toast.LENGTH_LONG).show();
                }
                // Finally, update the next activity's bundle and start the activity.
                if (accountType != "unknown") {
                    Intent DebugScreenIntent = new Intent(
                            LoginActivity.this,
                            DebugMenuActivity.class);
                    // Update the EXTRAS on this activity to be used by the next activity.
                    DebugScreenIntent.putExtra(EXTRA_ACCOUNT_USERNAME, accountUser);
                    DebugScreenIntent.putExtra(EXTRA_ACCOUNT_TYPE, accountType);
                    startActivity(DebugScreenIntent);
                }
            }
        });

        /****************************************************************
         * Registration Menu requires SQL Lite or NoSQL Cloud Firestore.
         ****************************************************************/
        ButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RegistrationIntent = new Intent(
                        LoginActivity.this,
                        RegistrationActivity.class);
                startActivity(RegistrationIntent);
            }
        });
    }
}