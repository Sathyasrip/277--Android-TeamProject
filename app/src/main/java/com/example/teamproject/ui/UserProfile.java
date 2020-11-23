package com.example.teamproject.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.teamproject.R;
import com.example.teamproject.model.AppThemes;
import com.example.teamproject.model.CircularImageTransform;
import com.example.teamproject.model.ProfileSettings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UserProfile extends AppCompatActivity {
    ProfileSettings CurrentUser;
    AppThemes AvailableThemes;
    int ThemeID;
    private static final String TAG = "UserProfile";

    private ConstraintLayout ThisLayout;
    private ImageView GoBackIcon, ProfilePicture;
    private TextView Username, Email, AccountType;
    private EditText FullName, Credentials;
    Button ButtonProfile;
    private Spinner theme_dropdown;
    private String[] available_themes;
    String dropdown_selection, full_name, credentials;

    // Firebase-related
    private FirebaseAuth mAuth;
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;
    Boolean new_photo_selected = false;
    final static int PICK_IMAGE = 1034;
    Uri new_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        /***************************
         *  Firebase Authentication.
         ***************************/
        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        /*************************
         *  Link up all the views
         *************************/
        ThisLayout = (ConstraintLayout) findViewById(R.id.userprofile_layout);
        GoBackIcon = (ImageView) findViewById(R.id.image_profile_go_back);
        ProfilePicture = (ImageView) findViewById(R.id.image_userprofile_photo);
        FullName = (EditText) findViewById(R.id.editTextProfileName);
        Username = (TextView) findViewById(R.id.tv_profile_username);
        Email = (TextView) findViewById(R.id.tv_profile_email);
        Credentials = (EditText) findViewById(R.id.editTextProfileCredentials);
        AccountType = (TextView) findViewById(R.id.tv_profile_account);
        ButtonProfile = (Button) findViewById(R.id.button_profile_save);

        /**********************************************************************
         *  Retrieve the Intent containing the User Profile data from Firebase.
         **********************************************************************/
        Intent i = getIntent();
        CurrentUser = (ProfileSettings) i.getSerializableExtra("UserProfile");

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

        /************************************************
         *  Store the original full name and credentials.
         ************************************************/
        full_name = CurrentUser.FullName();
        credentials = CurrentUser.Credentials();

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

        /*****************************************************************
         * The user can edit the following: Full Name and Credentials.
         ****************************************************************/
        FullName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    full_name = FullName.getText().toString();
                    return true;
                }
                return false;
            }
        });
        Credentials.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    credentials = Credentials.getText().toString();
                    return true;
                }
                return false;
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
                ButtonProfile.setEnabled(false);
                Log.d(TAG, "Profile Save Button disabled, due to upload.");
                // Apply any changes that the user made. (Full Name, Credentials, and BG Theme)
                full_name = FullName.getText().toString();
                credentials = Credentials.getText().toString();

                // Update Firebase entries with the changed values.
                updateProfileEntries(full_name, credentials, ThemeID, CurrentUser);

                // Do the profile photo upload into firebase.
                if (new_photo_selected) {
                    uploadNewPhoto(new_photo);
                } else {
                    // Refresh Firebase profile & return to the User Portal Activity.
                    GetFirebaseUserProfile(CurrentUser.UUID());
                }
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
                getImage();
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

    /****************************************************************
     *  Uploads the file to Firebase Storage.
     *  firebase_file_location: /profile_pictures/filename.png
     ***************************************************************/
    private void uploadFirebaseFile(Uri data, String firebase_file_location, final ProfileSettings current_user, final Boolean new_entry_flag) {
        // Upload the file to the Firebase.
        StorageReference sRef = mStorageReference.child(firebase_file_location);
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload was a success!
                        // If required, update the database entry.s
                        if (new_entry_flag) {
                            mDatabaseReference.child("user_profiles").child(current_user.UUID()).child("picture").setValue(current_user.GetFirebaseProfilePictureName());
                            Log.d(TAG, "Firebase user profile database entry updated with picture=" + current_user.GetFirebaseProfilePictureName());
                        }
                        // Refresh Firebase profile & return to the User Portal Activity.
                        GetFirebaseUserProfile(current_user.UUID());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //UploadStatus.setText((int) progress + "% Uploading...");
                    }
                });

    }

    /*************************************************************************
     *  Deletes the file & then Replaces a new file to the Firebase Storage.
     *  firebase_file_location: /profile_pictures/filename.png
     ************************************************************************/
    private void deleteAndReplaceFirebaseFile(final Uri new_photo, String old_firebase_file_location,
                                              final String new_firebase_file_location, final ProfileSettings current_user, final Boolean new_entry_flag) {
        // Upload the file to the Firebase.
        StorageReference sRef = mStorageReference.child(old_firebase_file_location);
        sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {
                                                   uploadFirebaseFile(new_photo, new_firebase_file_location, current_user, new_entry_flag);
                                               }
                                           });
    }

    /**************************************************************************************
     *  Updates the Profile Photo with user selected one, and then adds entry to database.
     *************************************************************************************/
    private void uploadNewPhoto(Uri photo) {
        ProfileSettings current_user = CurrentUser;

        // Check if the new filename extension for firebase is new or the same.
        String no_photo_firebase_location = "/profile_pictures/no_photo.png";
        if (current_user.GetExistingFirebaseProfilePicture().equals(no_photo_firebase_location) && !current_user.GetFirebaseProfilePicture().isEmpty()) {
            // Since the user profile never had a custom photo to begin with, upload a new one.
            Log.d(TAG, "New photo, uploading new file and database entry.");
            uploadFirebaseFile(photo, current_user.GetFirebaseProfilePicture(), current_user, true);
        } else if (current_user.GetFirebaseProfilePicture().equals(current_user.GetExistingFirebaseProfilePicture())) {
            // The new photo and the old one have the same extension, do an upload only.
            Log.d(TAG, "Same Extension. Just upload new file.");
            uploadFirebaseFile(photo, current_user.GetFirebaseProfilePicture(), current_user, false);
        } else if (current_user.GetFirebaseProfilePicture().equals("")) {
            // This should never happen! A new photo flag should lock out this combination.
            Log.d(TAG, "No new photo selected.");

            // Refresh Firebase profile & return to the User Portal Activity.
            GetFirebaseUserProfile(current_user.UUID());
        } else {
            // if the extension for the new photo and the old one are different,
            // delete the previous profile pic file and then do an upload of the new file.
            Log.d(TAG, "Different Extension. Delete previous file, upload new file and database.");
            deleteAndReplaceFirebaseFile(photo, current_user.GetExistingFirebaseProfilePicture(),
                    current_user.GetFirebaseProfilePicture(), current_user, true);
        }
    }

    /********************************************************************
     *  Update the changes made by the user in the profile to firebase.
     *******************************************************************/
    private void updateProfileEntries(String full_name, String credentials, int theme_id, ProfileSettings current_user) {
        mDatabaseReference.child("user_profiles").child(current_user.UUID()).child("full_name").setValue(full_name);
        mDatabaseReference.child("user_profiles").child(current_user.UUID()).child("credentials").setValue(credentials);
        mDatabaseReference.child("user_profiles").child(current_user.UUID()).child("theme_id").setValue((long) theme_id);

        Log.d(TAG, "General User profile database entries successfully updated.");
    }

    /***********************************************
     *  Uses file manager to choose a local photo.
     ***********************************************/
    private void getImage() {
        // First check permissions for the file manager.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        // Create an intent to choose the file.
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    /***************************************************
     *  After getting the photo, just change the photo.
     ***************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // When the user chooses the file
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // if a file is selected
            if (data.getData() != null) {
                new_photo = data.getData();

                // Use picasso to display the new image using the Uri of the new photo.
                LoadProfilePictureUri(ProfilePicture, new_photo);

                // Extracts the extension of the picture and generates expected profile pic name.
                CurrentUser.SetFirebaseProfilePictureNameUri(this, new_photo);

                // Setting new_photo_selected flag to true will enable upload of the new pic.
                new_photo_selected = true;

                // Do something for the upload
            } else{
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void GetFirebaseUserProfile(final String user_uuid) {
        DatabaseReference user_profiles_db = FirebaseDatabase.getInstance().getReference("user_profiles");
        Log.d(TAG, "Firebase User Profile pull: Obtaining User=" + user_uuid + " User Profile...");
        user_profiles_db.child(user_uuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Firebase User Profile pull: Successfully found user data!");
                // Ordered same way as in the database.
                String account_type = "", credentials = "", email = "", full_name = "",
                        firebase_profile_picture_file = "";
                int theme_id = 0;
                String username = "";

                // Pull up all the children and get their values.
                account_type = dataSnapshot.child("account_type").getValue(String.class);
                credentials = dataSnapshot.child("credentials").getValue(String.class);
                email = dataSnapshot.child("email").getValue(String.class);
                full_name = dataSnapshot.child("full_name").getValue(String.class);
                firebase_profile_picture_file = dataSnapshot.child("picture").getValue(String.class);
                username = dataSnapshot.child("username").getValue(String.class);
                long theme_id_lng = dataSnapshot.child("theme_id").getValue(long.class);
                theme_id = (int) theme_id_lng;
                Log.d(TAG, "Firebase User Profile pull: Successfully pulled all data!");

                final ProfileSettings UserProfile = new ProfileSettings(user_uuid, firebase_profile_picture_file,
                        full_name, email, username, credentials, account_type, theme_id);

                // Get the Download URL for the Profile picture.
                String firebase_profile_pic_link = UserProfile.GetExistingFirebaseProfilePicture();
                mStorageReference.child(firebase_profile_pic_link).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                {
                    @Override
                    public void onSuccess(Uri downloadUrl)
                    {
                        String ImageUrl = UserProfile.GetFirebasePublicUrl(downloadUrl.getEncodedPath());
                        UserProfile.UploadNewProfilePicture(ImageUrl);
                        Log.d(TAG, "Firebase Profile picture location successfully updated!");
                        Intent LoginScreenIntent = new Intent(
                                UserProfile.this,
                                UserPortal.class);
                        LoginScreenIntent.putExtra("UserProfile", UserProfile);
                        startActivity(LoginScreenIntent);
                        Log.d(TAG, "Now going into User Portal Activity.");
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

    /***************************************************************
     * Use Picasso to load the image from a URL into the ImageView.
     ***************************************************************/
    public void LoadProfilePicture(ImageView image_view, String image_url) {
        Picasso.get().load(image_url).transform(new CircularImageTransform()).fit().into(image_view);
        Log.d(TAG, "Loaded Web URL Photo");
    }
    public void LoadProfilePictureUri(ImageView image_view, Uri image_uri) {
        Picasso.get().load(image_uri).transform(new CircularImageTransform()).fit().into(image_view);
        Log.d(TAG, "Loaded Local Uri Photo");
    }
}