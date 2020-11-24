package com.example.teamproject.ui;

import com.example.teamproject.R;
import com.example.teamproject.model.AppThemes;
import com.example.teamproject.model.FirebaseReview;
import com.example.teamproject.model.FirebaseReviewVersion;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;

public class DeletingReview extends AppCompatActivity {
    ProfileSettings CurrentUser;
    AppThemes AvailableThemes;
    int ThemeID;
    private static final String TAG = "DeletingReview";

    // Views for the Activity layout.
    private ConstraintLayout ThisLayout;
    private ImageView GoBackIcon;
    private Button DeleteReview;
    private Switch PurgeEntireReview;

    // Used for the Spinner views.
    private Spinner review_title_chooser, review_version_chooser;
    private ArrayList<String> existing_reviews = new ArrayList<String>(); // empty
    private ArrayList<String> existing_versions = new ArrayList<String>();; // empty
    ArrayList<FirebaseReview> AllReviews = new ArrayList<FirebaseReview>(); // Additional information about each review.
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

    // Firebase
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;
    private String review_uuid, review_version;
    private long latest_version; // This is decremented by 1 if the selected review for deletion is equal to this number.
    private int review_index = 0, num_versions = 0;
    private Boolean PurgeEntireReviewFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deleting_review);

        /*************************
         *  Initialize Firebase.
         *************************/
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        /*************************
         *  Link up all the views
         *************************/
        ThisLayout = (ConstraintLayout) findViewById(R.id.deletereview_layout);
        GoBackIcon = (ImageView) findViewById(R.id.image_deletereview_go_back);
        PurgeEntireReview = (Switch) findViewById(R.id.switch_delete_whole_review);
        DeleteReview = (Button) findViewById(R.id.button_delete_review);
        review_title_chooser = (Spinner) findViewById(R.id.deletereview_review_title_chooser);
        review_version_chooser = (Spinner) findViewById(R.id.deletereview_review_version_chooser);

        DeleteReview.setEnabled(false);
        /**********************************************************************
         *  Retrieve the Intent containing the User Profile data from Firebase.
         **********************************************************************/
        Intent i = getIntent();
        CurrentUser = (ProfileSettings) i.getSerializableExtra("UserProfile");

        /*****************************************************************
         *  Change the Background that was selected by the theme_dropdown
         *****************************************************************/
        ThemeID = CurrentUser.Theme();
        AvailableThemes = new AppThemes(ThemeID);
        ThisLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), AvailableThemes.GetTheme()));

        /************************
         *  Update the spinners
         ************************/
        PullFirebaseReviews();
        SpinnerListeners();

        // However, if the switch is enabled, you cannot select the version.
        PurgeEntireReview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    PurgeEntireReviewFlag = true;
                    ArrayList<String> no_reviews = new ArrayList<String>();
                    ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, no_reviews);
                    review_version_chooser.setAdapter(adpter2);
                    review_version_chooser.setEnabled(false);

                    // Check if the selected review is empty. If it is, disable the button.
                    if (review_title_chooser.getSelectedItem().toString().isEmpty()) {
                        DeleteReview.setEnabled(false);
                    } else {
                        DeleteReview.setEnabled(true);
                    }
                } else {
                    PurgeEntireReviewFlag = false;
                    ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, existing_versions);
                    review_version_chooser.setAdapter(adpter2);
                    review_version_chooser.setEnabled(true);

                    // Check if the selected review is empty. If it is, disable the button.
                    if (!review_title_chooser.getSelectedItem().toString().isEmpty() && !review_version_chooser.getSelectedItem().toString().isEmpty()) {
                        DeleteReview.setEnabled(true);
                    } else {
                        DeleteReview.setEnabled(false);
                    }

                }
            }
        });

        /**************************************************************************
         * The following OnClickListeners take the user to a specified activity
         * to do something.
         *************************************************************************/
        GoBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to the User Portal Activity.
                Intent PreviousActivityIntent = new Intent(
                        DeletingReview.this,
                        UserPortal.class);
                PreviousActivityIntent.putExtra("UserProfile", CurrentUser);
                startActivity(PreviousActivityIntent);
            }
        });
        DeleteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmDeletionDialog();
            }
        });
    }

    public void ConfirmDeletionDialog() {
        String title = "Deleting a Review";
        String dialog_message = "Are you sure you want to delete this review? You will be unable to access the Review version or the Review entirely...";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message).setTitle(title);

        // If the user chooses to press "Yes", only then, return to the user portal
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DeleteFileAndUpdateDatabase(review_index, review_uuid, review_version, PurgeEntireReviewFlag);
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

    /********************************************************************
     * Retrieves Firebase entries and updates global ArrayLists
     *******************************************************************/
    public void SpinnerListeners() {
        // When selecting the Review title, update version entries if Purge flag is not set.
        review_title_chooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String current_title = review_title_chooser.getSelectedItem().toString();
                existing_versions = new ArrayList<String>();
                existing_versions.add("");

                // If the Purge flag is false, fill all the versions.
                if (!current_title.isEmpty() && !PurgeEntireReviewFlag) {
                    Log.d(TAG, "ReviewTitle Selection: Purge flag is NOT set, and Review was selected.");
                    // update the review_uuid.
                    review_uuid = AllReviews.get(position -1).UUID();
                    latest_version = AllReviews.get(position - 1).LatestVersion();
                    Log.d(TAG, "ReviewTitle Selection: Review UUID=" + review_uuid);
                    Log.d(TAG, "ReviewTitle Selection: Latest Version=" + String.valueOf(latest_version));

                    Log.d(TAG, "ReviewTitle Selection: User has selected a review to delete, pull version entries.");
                    FirebaseReview CurrentReview = AllReviews.get(position - 1);
                    ArrayList<FirebaseReviewVersion> current_review_versions = CurrentReview.getAllVersions();
                    num_versions = current_review_versions.size();
                    Log.d(TAG, "ReviewTitle Selection: Number of Versions=" + String.valueOf(num_versions));

                    for (int i = 0; i < current_review_versions.size(); ++i) {
                        String version = current_review_versions.get(i).Version();
                        existing_versions.add(version);
                        Log.d(TAG, "ReviewTitle Selection: OK, Found review version: " + version);
                    }
                } else if (!current_title.isEmpty() && PurgeEntireReviewFlag) {
                    review_uuid = AllReviews.get(position -1).UUID();
                    latest_version = AllReviews.get(position - 1).LatestVersion();

                    Log.d(TAG, "ReviewTitle Selection: Purge flag is set, and Review was selected. Enable button.");
                    review_version = "1";  // We don't actually need this as we are purging the whole review.

                    Log.d(TAG, "ReviewTitle Selection: Review UUID=" + review_uuid);
                    Log.d(TAG, "ReviewTitle Selection: Review Version=" + review_version);
                    DeleteReview.setEnabled(true); // Values are ready to be pulled.
                } else {
                    DeleteReview.setEnabled(false);
                }

                ArrayAdapter<String> adpter2 = new ArrayAdapter<String>(DeletingReview.this, R.layout.review_spinner, existing_versions);
                review_version_chooser.setAdapter(adpter2);
                Log.d(TAG, "ReviewTitle Selection: ReviewVersion spinner values updated!");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ; // Do nothing.
            }
        });
        review_version_chooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String version = review_version_chooser.getSelectedItem().toString();

                if (!version.isEmpty() && !PurgeEntireReviewFlag) {
                    Log.d(TAG, "ReviewVersion Selection: Purge flag is NOT set, Version is selected. Enable button.");
                    review_version = version;
                    Log.d(TAG, "ReviewVersion Selection: Review Version=" + review_version);
                    DeleteReview.setEnabled(true);
                } else if (version.isEmpty() && PurgeEntireReviewFlag) {
                    Log.d(TAG, "ReviewVersion Selection: Purge flag is set, Version is not selected. Enable button.");
                    review_version = "1";
                    Log.d(TAG, "ReviewVersion Selection: Review Version=" + review_version);
                    DeleteReview.setEnabled(true);
                } else {
                    Log.d(TAG, "ReviewVersion Selection: No acceptable combination found. Disable button.");
                    DeleteReview.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ; // Do nothing.
            }
        });
    }
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
                    if (read_only || !owner.equals(CurrentUser.Username())) {
                        Log.d(TAG, "Firebase Reviews Pull: Review=" + current_uuid + " is not viewable. Skipping...");
                        continue;
                    }

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
                    }
                    AllReviews.add(review);  // Will be used to pass to next activity.
                    existing_reviews.add(review_title); // This will update the drop down list.
                    Log.d(TAG, "Firebase Reviews Pull: Child, Added Review == Title:" + review.ReviewTitle());
                }

                // Finally, update the spinners with the new data.
                ArrayAdapter<String> adpter1 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, existing_reviews);
                review_title_chooser.setAdapter(adpter1);
                Log.d(TAG, "Firebase Reviews Pull: Review Title spinner successfully updated!");

                ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, existing_versions);
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
                ArrayAdapter<String> adpter1 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, existing_reviews);
                review_title_chooser.setAdapter(adpter1);
                Log.d(TAG, "Firebase Reviews Pull: Review Title spinner successfully updated!");

                ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, existing_versions);
                review_version_chooser.setAdapter(adpter2);
                Log.d(TAG, "Firebase Reviews Pull: Review Version spinner successfully updated!");

                // Add the 'empty' entries to the String ArrayLists.
                Log.d(TAG, "Firebase Reviews Pull: No entries found (or error).");
            }
        });
        Log.d(TAG, "Reviews Size: " + String.valueOf(existing_reviews.size()));
    }

    public void PurgeDatabaseEntries(String review_uuid, String review_version, int num_versions, Boolean purge_review) {
        // Removes the entire database from the top.
        if (purge_review) {
            Log.d(TAG, "PurgeDatabase: Removing tree, because we want to purge the entire review.");
            mDatabaseReference.child("open_reviews").child(review_uuid).removeValue();
        } else {
            if (num_versions == 1) {
                Log.d(TAG, "PurgeDatabase: There's only 1 version and you want to delete it. Removing tree.");
                mDatabaseReference.child("open_reviews").child(review_uuid).removeValue();
            } else if (num_versions > 1) {
                if (String.valueOf(latest_version).equals(review_version)) {
                    Log.d(TAG, "PurgeDatabase: This version was the latest, so decrementing.");
                    latest_version = latest_version - 1; // Decrement by 1.
                    mDatabaseReference.child("open_reviews").child(review_uuid).child("latest_version").setValue(latest_version);
                }
                Log.d(TAG, "PurgeDatabase: Removing the version entry from the database.");
                mDatabaseReference.child("open_reviews").child(review_uuid).child("versions").child(review_version).removeValue();
            }
        }
    }

    public void DeleteFileAndUpdateDatabase(int index, final String review_uuid, final String review_version, final Boolean purge_review) {
        final ArrayList<FirebaseReviewVersion> versions = AllReviews.get(index).getAllVersions();

        if (purge_review) {
            PurgeDatabaseEntries(review_uuid, review_version, versions.size(), purge_review);

            for (int i = 0; i < versions.size(); ++i) {
                String firebase_pdf_file = versions.get(i).PDF();

                final int counter = i;
                StorageReference sRef = mStorageReference.child(firebase_pdf_file);
                sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (counter == (versions.size() - 1)) {
                            Log.d(TAG, "PurgeDatabase: Finished deleting all the PDFs. Launching Activity.");
                            DeleteReview.setEnabled(false);
                            ReturnToUserProfile();
                        }
                    }
                });
            }
        } else {
            // If only an individual entry is to be deleted, just remove just that one file.
            for (int i = 0; i < versions.size(); ++i) {
                if (versions.get(i).Version() == review_version) {
                    PurgeDatabaseEntries(review_uuid, review_version, versions.size(), purge_review);
                    String firebase_pdf_file = versions.get(i).PDF();

                    StorageReference sRef = mStorageReference.child(firebase_pdf_file);
                    sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "PurgeDatabase: Finished deleting the single PDF. Launching Activity.");
                            DeleteReview.setEnabled(false);
                            ReturnToUserProfile();
                        }
                    });
                    break;
                }
            }
        }
    }

    public void ReturnToUserProfile() {
        DeleteReview.setEnabled(false);

        // Return to the User Portal Activity.
        Intent PreviousActivityIntent = new Intent(
                DeletingReview.this,
                UserPortal.class);
        PreviousActivityIntent.putExtra("UserProfile", CurrentUser);
        startActivity(PreviousActivityIntent);
        finish();
    }
}