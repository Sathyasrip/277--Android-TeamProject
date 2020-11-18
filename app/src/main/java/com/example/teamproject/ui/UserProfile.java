package com.example.teamproject.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.teamproject.R;
import com.example.teamproject.model.AppThemes;
import com.example.teamproject.model.ProfileSettings;
import com.squareup.picasso.Picasso;

public class UserProfile extends AppCompatActivity {
    ProfileSettings CurrentUser;
    AppThemes AvailableThemes;
    int ThemeID;

    private ConstraintLayout ThisLayout;
    private ImageView GoBackIcon, ProfilePicture;
    private TextView Username, Email, AccountType;
    private EditText FullName, Credentials;
    Button ButtonProfile;
    private Spinner theme_dropdown;
    private String[] available_themes;
    String dropdown_selection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        /**********************************************************************
        *  Retrieve the Intent containing the User Profile data from Firebase.
        **********************************************************************/
        Intent i = getIntent();
        CurrentUser = (ProfileSettings) i.getSerializableExtra("UserProfile");

        // Load all the correct views.
        ThisLayout = (ConstraintLayout) findViewById(R.id.userprofile_layout);
        GoBackIcon = (ImageView) findViewById(R.id.image_profile_go_back);
        ProfilePicture = (ImageView) findViewById(R.id.image_userprofile_photo);
        FullName = (EditText) findViewById(R.id.editTextProfileName);
        Username = (TextView) findViewById(R.id.tv_profile_username);
        Email = (TextView) findViewById(R.id.tv_profile_email);
        Credentials = (EditText) findViewById(R.id.editTextProfileCredentials);
        AccountType = (TextView) findViewById(R.id.tv_profile_account);
        ButtonProfile = (Button) findViewById(R.id.button_profile_save);

        /******************************************
        *  Load the Existing User Profile Photo.
        ******************************************/
        // TODO: Replace with firebase logic.
        LoadProfilePicture(ProfilePicture, CurrentUser.GetLocalProfilePicture());

        /***********************************************************************************
         *  If the user presses on the picture, load a Alert Dialog asking to change photo.
         ***********************************************************************************/
        ProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeProfilePictureDialog();
            }
        });

        /******************************************
        *  Load all the TextViews and EditTexts.
        ******************************************/
        FullName.setText(CurrentUser.FullName());
        Username.setText(CurrentUser.Username());
        Email.setText(CurrentUser.Email());
        Credentials.setText(CurrentUser.Credentials());
        AccountType.setText(CurrentUser.AccountType());

        /***************************************************************
        *  Configure the dropdown to show the various themes
        ***************************************************************/
        ThemeID = CurrentUser.Theme();
        AvailableThemes = new AppThemes(ThemeID);
        available_themes = AvailableThemes.AvailableThemeNames();

        theme_dropdown = (Spinner) findViewById(R.id.profile_theme_dropdown);
        ArrayAdapter<String> adpter = new ArrayAdapter<String> (UserProfile.this, R.layout.theme_spinner, available_themes);
        theme_dropdown.setAdapter(adpter);

        // Load the theme as described in the user profile.
        theme_dropdown.setSelection(ThemeID);

        /*****************************************************************
         *  Change the Background that was selected by the theme_dropdown
         *****************************************************************/
        ThisLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), AvailableThemes.GetTheme()));

        /*********************************************************************
        *  Change the Background that was selected by user using the Spinner.
        **********************************************************************/
        theme_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ThemeID = position;
                CurrentUser.SetTheme(ThemeID);
                AvailableThemes.SetTheme(ThemeID);
                ThisLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), AvailableThemes.GetTheme()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                ; // Do nothing. Only react, when the user changes the version.
            }
        });

        /*************************************************************************
         * When user presses the back button image, go back to the previous menu.
         *************************************************************************/
        GoBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the user is an admin, return to the Admin menu after updating changes.
                Intent PreviousActivityIntent = new Intent(
                        UserProfile.this,
                        UserPortal.class);
                PreviousActivityIntent.putExtra("UserProfile", CurrentUser);
                startActivity(PreviousActivityIntent);
                finish();
            }
        });

        /*****************************************************************************************
         * TODO: Depending on user changes from existing, change button text & update database.
         ****************************************************************************************/
        ButtonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: When saving changes, update entries in Firebase, then go back.
                Intent PreviousActivityIntent = new Intent(
                        UserProfile.this,
                        UserPortal.class);
                PreviousActivityIntent.putExtra("UserProfile", CurrentUser);
                startActivity(PreviousActivityIntent);
                finish();
            }
        });
    }

    public void ChangeProfilePictureDialog() {
        String title = "Change Profile Picture";
        String dialog_message = "Would you like to change the profile picture?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message).setTitle(title);

        // If the user chooses to press "Yes", let the user select the photo & update view.
        // TODO: Don't update entry in Firebase unless user chooses 'Save Changes'
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "Photo Upload Action!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // If the user chooses "No", just dismiss the dialog box.
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Finally display the Alert Dialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /***************************************************************
     * Use Picasso to load the image from a URL into the ImageView.
     ***************************************************************/
    public void LoadProfilePicture(ImageView image_view, String image_url) {
        Picasso.get().load(image_url).fit().centerCrop().into(image_view);
    }
}