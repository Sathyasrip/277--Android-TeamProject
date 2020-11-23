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
import android.view.ViewGroup;
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
import com.example.teamproject.model.FirebaseReview;
import com.example.teamproject.model.FirebaseReviewVersion;
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

import java.util.ArrayList;
import java.util.UUID;

public class UserPortal extends AppCompatActivity {
    ProfileSettings CurrentUser;
    AppThemes AvailableThemes;
    int ThemeID;
    private static final String TAG = "UserPortal";

    // UI based variables.
    private ConstraintLayout ThisLayout;
    private Button StartReview, SelectPDF;
    private EditText ReviewTitle;
    private TextView SelectedTitle, SelectedVersion, SelectedPDF, AccountType, UploadStatus;
    private ImageView ProfileIcon, GoBackIcon, DeleteReviewIcon;
    private Spinner review_title_chooser, review_version_chooser;
    private ArrayList<String> existing_reviews = new ArrayList<String>(); // empty
    private ArrayList<String> existing_versions = new ArrayList<String>();; // empty
    ArrayList<FirebaseReview> AllReviews = new ArrayList<FirebaseReview>(); // Additional information about each review.
    private String current_review, current_version, current_review_uuid, review_owner; // These are configured by user choosing the dropdown.
    private ArrayList<String> test_reviews = new ArrayList<String>() {{
        add("");
        add("Stem Cell IEEE");
        add("Part Datasheet");
        add("COVID19 Report");
    }};
    private ArrayList<String> test_versions = new ArrayList<String>() {{
        add("");
        add("1");
        add("2");
        add("3");
    }};
    Boolean[] existing_review_selected_flags = {false, false};

    // Firebase
    private FirebaseAuth mAuth;
    final static int PICK_PDF_CODE = 2342;
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;
    Uri pdf_file;
    String review_title="", review_version="1", review_uuid;
    Boolean[] required_upload_flags = {false, false, false};
    Boolean new_review_flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_portal);

        /***************************
         *  Firebase Authentication.
         ***************************/
        mAuth = FirebaseAuth.getInstance();
        //getting firebase objects
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        /*************************
         *  Link up all the views
         *************************/
        ThisLayout = (ConstraintLayout) findViewById(R.id.userportal_layout);
        GoBackIcon = (ImageView) findViewById(R.id.image_userportal_go_back);
        DeleteReviewIcon = (ImageView) findViewById(R.id.image_userportal_delete_review);
        ProfileIcon = (ImageView) findViewById(R.id.image_userportal_profile);
        AccountType = (TextView) findViewById(R.id.tv_userportal_account);
        SelectPDF = (Button) findViewById(R.id.button_choose_pdf);
        SelectedPDF = (TextView) findViewById(R.id.tv_pdf_path);
        ReviewTitle = (EditText) findViewById(R.id.editTextReviewTitle);
        SelectedTitle = (TextView) findViewById(R.id.tv_selected_review_title);
        SelectedVersion = (TextView) findViewById(R.id.tv_selected_review_version);
        StartReview = (Button) findViewById(R.id.button_start_review);
        UploadStatus = (TextView) findViewById(R.id.tv_upload_status);

        StartReview.setEnabled(false);
        /**********************************************************************
        *  Retrieve the Intent containing the User Profile data from Firebase.
        **********************************************************************/
        Intent i = getIntent();
        CurrentUser = (ProfileSettings) i.getSerializableExtra("UserProfile");

        /************************
         *  Update the spinners
         ************************/
        RetrieveReviews();

        /*****************************************************************
         *  Change the Background that was selected by the theme_dropdown
         *****************************************************************/
        ThemeID = CurrentUser.Theme();
        AvailableThemes = new AppThemes(ThemeID);
        ThisLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), AvailableThemes.GetTheme()));

        /*****************************
         * Load the profile picture.
         *****************************/
        LoadProfilePicture(ProfileIcon, CurrentUser.GetLocalProfilePicture());

        /*****************************
         * Load the Account Type info.
         *****************************/
        AccountType.setText(CurrentUser.AccountType());

        /**************************************************************************
         * The following OnClickListeners take the user to a specified activity
         * to do something.
         *************************************************************************/
        GoBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutDialog();
            }
        });
        ProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The profile icon should be up to date with what is on Firebase and User Profile.
                Intent ProfileActivityIntent = new Intent(
                        UserPortal.this,
                        UserProfile.class);
                ProfileActivityIntent.putExtra("UserProfile", CurrentUser);
                startActivity(ProfileActivityIntent);
            }
        });

        if (CurrentUser.AccountType().equals("reviewer")) {
            /***********************************************************************************
             * If Reviewers click this button, they can delete any existing Reviews or Versions.
             ***********************************************************************************/
            DeleteReviewIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If the user is an elevated user/reviewer, you can see and click the icon.
                    Intent DeletingReviewIntent = new Intent(
                            UserPortal.this,
                            DeletingReview.class);
                    DeletingReviewIntent.putExtra("UserProfile", CurrentUser);
                    startActivity(DeletingReviewIntent);
                }
            });

            /**************************************************************************
             * Clicking SelectPDF will pull the file chooser and update the text view.
             *************************************************************************/
            SelectPDF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPDF();
                }
            });

            /*****************************************************************
             * When the User successfully types something into the EditText,
             * Generate the UUID for the Review as well.
             ****************************************************************/
            ReviewTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                                      @Override
                                                      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                                          if (actionId == EditorInfo.IME_ACTION_DONE) {
                                                              review_title = ReviewTitle.getText().toString();
                                                              review_uuid = UUID.randomUUID().toString().replace("-","");
                                                              Toast.makeText(getApplicationContext(), "UUID: " + review_uuid, Toast.LENGTH_SHORT).show();

                                                              // Change the title and version text views.
                                                              SelectedTitle.setText(review_title);
                                                              SelectedVersion.setText("1");

                                                              // Enable the Start Review button if all criteria is met.
                                                              required_upload_flags[1] = true;
                                                              if(required_upload_flags[0] && required_upload_flags[1]) {
                                                                  StartReview.setEnabled(true);
                                                              }

                                                              return true;
                                                          }
                                                          return false;
                                                      }
                                                  });
        } else {
            // If the user is a standard user, don't show any of the views for making/editing Reviews.
            TextView StartReviewHeader = (TextView) findViewById(R.id.tv_new_review);
            TextView ReviewTitleHeader = (TextView) findViewById(R.id.tv_review_title_text);

            ((ViewGroup) DeleteReviewIcon.getParent()).removeView(DeleteReviewIcon);
            ((ViewGroup) StartReviewHeader.getParent()).removeView(StartReviewHeader);
            ((ViewGroup) ReviewTitleHeader.getParent()).removeView(ReviewTitleHeader);
            ((ViewGroup) SelectPDF.getParent()).removeView(SelectPDF);
            ((ViewGroup) SelectedPDF.getParent()).removeView(SelectedPDF);
            ((ViewGroup) ReviewTitle.getParent()).removeView(ReviewTitle);

            // Now, enable the Start Review Button.
        }

        StartReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: firebase
                if (CurrentUser.AccountType().equals("reviewer")) {
                    // Ensure all required fields are set, before attempting to upload the file.
                    if(required_upload_flags[0] && required_upload_flags[1]) {
                        uploadReview(pdf_file, review_uuid, review_version, new_review_flag);
                        Toast.makeText(getApplicationContext(), "Check Firebase!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please input all required fields!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent ReviewIntent = new Intent(
                            UserPortal.this,
                            StartTheReview.class);
                    ReviewIntent.putExtra("UserProfile", CurrentUser);
                    startActivity(ReviewIntent);
                }
            }
        });
    }

    private void getPDF() {
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
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // When the user chooses the file
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // if a file is selected
            if (data.getData() != null) {
                pdf_file = data.getData();
                SelectedPDF.setText(pdf_file.getPath());

                required_upload_flags[0] = true;
                if(required_upload_flags[0] && required_upload_flags[1]) {
                    StartReview.setEnabled(true);
                }
            } else{
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /******************************************
     *  Uploads the file to Firebase Storage.
     *****************************************/
    private void uploadFile(Uri data, String review_uuid, String version) {
        // Upload the file to the Firebase.
        StorageReference sRef = mStorageReference.child("pdfs/" + review_uuid + "_v" + version + ".pdf");
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        UploadStatus.setText("File Uploaded Successfully");
                        required_upload_flags[2] = true;

                        // Now make a whole list of entries in Firebase for the review.
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
                        UploadStatus.setText((int) progress + "% Uploading...");
                    }
                });

    }

    /***************************************************************
     *  Uploads Realtime Database entries and uploads the PDF file.
     ***************************************************************/
    private void uploadReview(Uri data, String review_uuid, String version, Boolean new_review) {
        String firebase_pdf_file = review_uuid + "_v" + version + ".pdf";

        // Uploads the PDF on Firebase storage.
        uploadFile(data, review_uuid, version);

        if (new_review) {
            // Adds all the required first-time entries into the database.
            mDatabaseReference.child("open_reviews").child(review_uuid).child("latest_version").setValue((long) Integer.parseInt(version));
            mDatabaseReference.child("open_reviews").child(review_uuid).child("owner").setValue(CurrentUser.Username());
            mDatabaseReference.child("open_reviews").child(review_uuid).child("title").setValue(review_title);
            mDatabaseReference.child("open_reviews").child(review_uuid).child("read_only").setValue(false);
            mDatabaseReference.child("open_reviews").child(review_uuid).child("viewable").setValue(true);
        }
        // Now just add the new version entry into the database.
        mDatabaseReference.child("open_reviews").child(review_uuid).child("versions").child(version).child("pdf").setValue(firebase_pdf_file);
        mDatabaseReference.child("open_reviews").child(review_uuid).child("versions").child(version).child("annotations").setValue("none");
    }

    /***********************
     *  Logout Dialog box.
     ***********************/
    public void LogoutDialog() {
        String title = "Account Logout";
        String dialog_message = "Do you want to logout?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message).setTitle(title);

        // If the user chooses to press "Yes", only then, return to login screen.
        // TODO: Remove last user login entry from SQL Database.
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If the user is an elevated user/reviewer, you can see and click the icon.
                mAuth.signOut();
                Intent LogoutIntent = new Intent(
                        UserPortal.this,
                        LoginScreen.class);
                startActivity(LogoutIntent);
                finish();
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
        Picasso.get().load(image_url).transform(new CircularImageTransform()).fit().into(image_view);
    }

    /**************************************
     * Returns Firebase review versions.
     *************************************/
    public void RetrieveReviews() {
        // Define the spinners.
        review_title_chooser = (Spinner) findViewById(R.id.review_title_chooser);
        review_version_chooser = (Spinner) findViewById(R.id.review_version_chooser);

        // TODO: Read back all the firebase review versions.
        //existing_reviews = test_reviews;
        //existing_versions = test_versions;
        Log.d(TAG, "Preparing Firebase Review pull.");
        PullFirebaseReviews();

        // On pressing the drop down menu, return the current review title
        review_title_chooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Once the review is selected, updated the views below.
                SelectedTitle.setText("");
                current_review = review_title_chooser.getSelectedItem().toString();
                Log.d(TAG, "Review Title Selection: Current Review Title: " + current_review);

                if (!current_review.isEmpty()) {
                    existing_versions = new ArrayList<String>();
                    existing_versions.add("");

                    // Obtain the current review uuid (used to differentiate the uuid).
                    current_review_uuid = AllReviews.get(position - 1).UUID();
                    review_owner = AllReviews.get(position -1).Owner();
                    Log.d(TAG, "Review Title Selection: Current Review UUID: " + current_review_uuid);
                    Log.d(TAG, "Review Title Selection: Current Review Owner: " + review_owner);
                    Log.d(TAG, "Review Title Selection: Logged in User: " + CurrentUser.Username());

                    // TODO: Generate the list of versions based on entries in AllReviews.
                    FirebaseReview current_review = AllReviews.get(position - 1);
                    ArrayList<FirebaseReviewVersion> current_review_versions = current_review.getAllVersions();
                    for (int i = 0; i < current_review_versions.size(); ++i) {
                        String version = current_review_versions.get(i).Version();
                        existing_versions.add(version);
                        Log.d(TAG, "Review Title Selection: Found version: " + version);
                    }
                    existing_review_selected_flags[0] = true;
                } else {
                    existing_versions = new ArrayList<String>();
                    existing_versions.add(""); // Required for button enabled flag
                    existing_review_selected_flags[0] = false;
                }
                ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_versions);
                review_version_chooser.setAdapter(adpter2);
                Log.d(TAG, "Review Title Selection: Review Version spinner successfully updated");

                if(existing_review_selected_flags[0] && existing_review_selected_flags[1]) {
                    SelectedTitle.setText(current_review);
                    SelectedVersion.setText(current_version);

                    StartReview.setEnabled(true);
                } else {
                    SelectedTitle.setText("");
                    SelectedVersion.setText("");
                    StartReview.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing.
            }
        });
        // On pressing the drop down menu, return the current review version
        review_version_chooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Once the review is selected, updated the views below.
                SelectedVersion.setText("");
                current_version = review_version_chooser.getSelectedItem().toString();
                Log.d(TAG, "Review Version Selection: Current Version: " + current_version);

                if (!current_version.isEmpty()) {
                    existing_review_selected_flags[1] = true;
                } else {
                    existing_review_selected_flags[1] = false;
                }
                if(existing_review_selected_flags[0] && existing_review_selected_flags[1]) {
                    SelectedTitle.setText(current_review);
                    SelectedVersion.setText(current_version);

                    StartReview.setEnabled(true);
                } else {
                    SelectedTitle.setText("");
                    SelectedVersion.setText("");
                    StartReview.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing.
            }
        });
    }

    /********************************************************************
     * Retrieves Firebase entries and returns an ArrayList of entries.
     *******************************************************************/
    public void PullFirebaseReviews() {
        // open_reviews contains all the reviews to be played with in StartTheReview Activity.
        final DatabaseReference reviews_db = FirebaseDatabase.getInstance().getReference("open_reviews");
        reviews_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Initialize the String ArrayLists.
                existing_reviews = new ArrayList<String>();
                existing_versions = new ArrayList<String>();

                // Add the 'empty' entries to the String ArrayLists.
                existing_reviews.add(""); // Required for button enabled flag.
                existing_versions.add(""); // Required for button enabled flag.

                Log.d(TAG, "Firebase Reviews Pull: Access Success! Grabbing Children...");
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    // These children are essentially the UUIDs representing the Reviews.
                    String current_uuid = data.getKey();
                    String owner = data.child("owner").getValue(String.class);
                    long latest_version = data.child("latest_version").getValue(long.class);
                    Boolean read_only = data.child("read_only").getValue(Boolean.class);
                    String review_title = data.child("title").getValue(String.class);
                    Boolean viewable = data.child("viewable").getValue(Boolean.class);

                    //TODO: Check if viewable flag is true. If false, skip the review.
                    //if (!viewable) {
                    //Log.d(TAG, "Firebase Reviews Pull: Review=" + current_uuid + " is not viewable. Skipping...");
                    //    continue;
                    //}

                    FirebaseReview review = new FirebaseReview(current_uuid, owner, latest_version,
                            review_title, read_only, viewable);
                    Log.d(TAG, "Firebase Reviews Pull: Child, uuid:" + review.UUID());
                    Log.d(TAG, "Firebase Reviews Pull: Child, owner:" + review.Owner());
                    Log.d(TAG, "Firebase Reviews Pull: Child, latest_version:" + String.valueOf(review.LatestVersion()));

                    // Get more information inside "versions"
                    for (DataSnapshot nested_data : data.child("versions").getChildren()) {
                        String version_number = nested_data.getKey();
                        String version_pdf = nested_data.child("pdf").getValue(String.class);
                        String version_annotation;

                        FirebaseReviewVersion version = new FirebaseReviewVersion(version_number, version_pdf);
                        review.addVersionNumber(version_number);
                        Log.d(TAG, "Firebase Reviews Pull: Child, Version:" + version.Version());
                        Log.d(TAG, "Firebase Reviews Pull: Child, pdf file:" + version.PDF());
                        try {
                            version_annotation = nested_data.child("annotations").getValue(String.class);
                            Log.d(TAG, "Firebase Reviews Pull: Child, annotation file:" + version_annotation);
                        } catch (Exception e){
                            version_annotation = "";
                            Log.d(TAG, "Firebase Reviews Pull: Child, annotation file - not available!");
                        }
                        version.setAnnotationFile(version_annotation);
                        review.addVersion(version);
                        Log.d(TAG, "Firebase Reviews Pull: Child, Added Version inside Review.");
                        // TODO: Grab all the comments for StartTheReview activity?
                    }
                    AllReviews.add(review);  // Will be used to pass to next activity.
                    existing_reviews.add(review_title); // This will update the drop down list.
                    Log.d(TAG, "Firebase Reviews Pull: Child, Added Review == Title:" + review.ReviewTitle());
                }

                // Finally, update the spinners with the new data.
                ArrayAdapter<String> adpter1 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_reviews);
                review_title_chooser.setAdapter(adpter1);
                Log.d(TAG, "Firebase Reviews Pull: Review Title spinner successfully updated!");

                ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_versions);
                review_version_chooser.setAdapter(adpter2);
                Log.d(TAG, "Firebase Reviews Pull: Review Version spinner successfully updated!");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If there is nothing in the database, or some error occurs, just return empty.

                // Initialize the String ArrayLists.
                existing_reviews = new ArrayList<String>();
                existing_versions = new ArrayList<String>();

                // Add the 'empty' entries to the String ArrayLists.
                existing_reviews.add(""); // Required for button enabled flag.
                existing_versions.add(""); // Required for button enabled flag.

                // Finally, update the spinners with the new data.
                ArrayAdapter<String> adpter1 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_reviews);
                review_title_chooser.setAdapter(adpter1);
                Log.d(TAG, "Firebase Reviews Pull: Review Title spinner successfully updated!");

                ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_versions);
                review_version_chooser.setAdapter(adpter2);
                Log.d(TAG, "Firebase Reviews Pull: Review Version spinner successfully updated!");

                // Add the 'empty' entries to the String ArrayLists.
                Log.d(TAG, "Firebase Reviews Pull: No entries found (or error).");
            }
        });
        Log.d(TAG, "Reviews Size: " + String.valueOf(existing_reviews.size()));
    }

    public void onBackPressed() {
        LogoutDialog();
    }
}