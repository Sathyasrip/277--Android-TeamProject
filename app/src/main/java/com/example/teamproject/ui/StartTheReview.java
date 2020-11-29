package com.example.teamproject.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import com.example.teamproject.R;
import com.example.teamproject.model.AppThemes;
import com.example.teamproject.model.CommentsListAdapter;
import com.example.teamproject.model.FirebaseReview;
import com.example.teamproject.model.FirebaseReviewVersion;
import com.example.teamproject.model.ProfileSettings;
import com.example.teamproject.model.ReviewPageAdapter;
import com.example.teamproject.model.SingleComment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pdftron.fdf.FDFDoc;
import com.pdftron.pdf.PDFDoc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StartTheReview extends AppCompatActivity {
    private static final String TAG = "StartTheReview";
    ProfileSettings CurrentUser;
    String logged_in_username;
    AppThemes AvailableThemes;
    int ThemeID;

    // Used to select the tab automatically when refreshing the View Pager.
    TabLayout tabLayout;
    ViewPager viewPager;
    Boolean version_changed = false;

    // Used for all the main views in the Activity.
    private ConstraintLayout ThisLayout;
    private EditText StartReviewTitle;
    private Button RowComment, SaveChanges;
    private Spinner version_dropdown;
    FirebaseReview SelectedReview;
    ArrayList<FirebaseReviewVersion> ReviewVersions;
    String dropdown_selection, selected_version;
    int current_dropdown_position = 0, current_tab_selection = 0, current_version = 0;

    // The data used for updating firebase (on edits) and displaying comments.
    Context CommentsContext;
    View CommentsView;
    ListView CommentsListView;
    List<SingleComment> listOfComments = new ArrayList<SingleComment>();
    ArrayList<SingleComment> DeletedComments = new ArrayList<SingleComment>();
    ArrayList<SingleComment> UpdatedComments = new ArrayList<SingleComment>();
    ArrayList<SingleComment> NewComments = new ArrayList<SingleComment>();

    // Firebase
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;

    // Used for PDFTron.
    PDFDoc CurrentPDFDoc;
    String CacheDirectory, FirebaseAnnotationFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_review);

        // Display the cache directory
        /**********************************************************************
         *  Obtain the Cache Directory for this app, to be used as a PDF Cache.
         **********************************************************************/
        CacheDirectory = getApplicationContext().getCacheDir().toString();

        /***************************
         *  Firebase Authentication.
         ***************************/
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        /**********************************************************************
         *  Load the Profile settings and the basic Firebase Review info.
         **********************************************************************/
        Intent i = getIntent();
        CurrentUser = (ProfileSettings) i.getSerializableExtra("UserProfile");
        SelectedReview = (FirebaseReview) i.getSerializableExtra("ReviewContainer");
        selected_version = (String) i.getSerializableExtra("ReviewVersion");
        logged_in_username = CurrentUser.Username();

        Log.i(TAG, "Logged In: " + logged_in_username);
        Log.i(TAG, "Review UUID: " + SelectedReview.UUID());
        Log.i(TAG, "Review Title: " + SelectedReview.ReviewTitle());
        Log.i(TAG, "Review Owner: " + SelectedReview.Owner());
        Log.i(TAG, "Selected Review Version: " + selected_version);

        /************************************************************************
         *  Update the list of Versions to appear in the Version select spinner.
         ***********************************************************************/
        ArrayList<FirebaseReviewVersion> ReviewVersions = SelectedReview.getAllVersions();
        ArrayList<String> available_versions = new ArrayList<String>();
        for (int idx = 0; idx < ReviewVersions.size(); ++idx) {
            String new_version = "Version " + ReviewVersions.get(idx).Version();
            available_versions.add(new_version);
            Log.d(TAG, "Found New Version: " + new_version);

            if (selected_version.equals(ReviewVersions.get(idx).Version())) {
                current_dropdown_position = idx;
                Log.d(TAG, "The version found is the version selected = " + String.valueOf(idx));

                // Additionally, update the annotations filename. It will determine if annotations file exists or not.
                FirebaseAnnotationFilename = SelectedReview.getAllVersions().get(idx).AnnotationFile().replace("/annotations/", "");
                Log.d(TAG, "The annotations file listed on Firebase currently is: " + FirebaseAnnotationFilename);
            }
        }
        version_dropdown = (Spinner) findViewById(R.id.version_dropdown);
        ArrayAdapter<String> adpter = new ArrayAdapter<String> (this, R.layout.review_spinner, available_versions);
        version_dropdown.setAdapter(adpter);

        /*****************************************************************
         *  Change the Background that was selected by the theme_dropdown
         *****************************************************************/
        ThisLayout = (ConstraintLayout) findViewById(R.id.startreview_layout);
        ThemeID = CurrentUser.Theme();
        AvailableThemes = new AppThemes(ThemeID);
        ThisLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), AvailableThemes.GetTheme()));

        /**********************************************************************
         *  If the account type is Standard, disable the EditText.
         **********************************************************************/
         StartReviewTitle = (EditText) findViewById(R.id.editTextStartReviewTitle);
         StartReviewTitle.setText(SelectedReview.ReviewTitle());
         if (CurrentUser.AccountType().equals("standard") || !CurrentUser.Username().equals(SelectedReview.Owner())) {
             StartReviewTitle.setEnabled(false);
         }

        /**************************************************************************************
         *  After the user is done editing and reviewing comments, save the changes.
         *************************************************************************************/
        SaveChanges = (Button) findViewById(R.id.button_save_review);
        SaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Now update the title in firebase if changed in this activity.
                String new_review_title = StartReviewTitle.getText().toString();
                if (CurrentUser.Username().equals(SelectedReview.Owner()) && !new_review_title.isEmpty()) {
                    mDatabaseReference.child("open_reviews").child(SelectedReview.UUID()).child("title").setValue(new_review_title);
                    Log.d(TAG, "SaveChanges: Updated Review Title in database to: " + new_review_title);
                }

                // Delete, Update or Add new comments to Firebase.
                Log.d(TAG, "SaveChanges: Updating Firebase comments (add, edit, delete).");
                UpdateFirebaseComments(SelectedReview.UUID(), String.valueOf(current_version),
                        NewComments, UpdatedComments, DeletedComments);

                // Upload the annotations to firebase.
                Log.d(TAG, "SaveChanges: Saving Annotations to Firebase Storage.");
                ExportAnnotationsToFile(SelectedReview.UUID(), String.valueOf(current_version));
            }
        });

        /**************************************************************************************
         *  Below will obtain the current document version from the Spinner dropdown listener.
         *************************************************************************************/
        // Set the selected item to some value.
        version_dropdown.setSelection(current_dropdown_position);  // Selects the correct version.
        dropdown_selection = version_dropdown.getSelectedItem().toString();
        current_version = Integer.parseInt(dropdown_selection.replace("Version ", ""));

        // On pressing the drop down menu, return the current version
        version_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                dropdown_selection = version_dropdown.getSelectedItem().toString();
                int selected_version = Integer.parseInt(dropdown_selection.replace("Version ", ""));
                if (selected_version != current_version) {
                    ChangeVersionsDialog(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                ; // Do nothing.
            }
        });

        /********************************
         *  Loads the tabs and fragments.
         ********************************/
        LoadReviewVersion();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!version_changed) {
                    current_tab_selection = (int) tab.getPosition();
                    //Toast.makeText(getApplicationContext(), String.valueOf(current_tab_selection), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    /************************************************************************
     *  Updates all the data structures used to view the PDF when selecting
     *  the VIEW tab as well as load up all the comments.
     ************************************************************************/
    public void LoadReview(String version) {
        // PDF will not change,
    }

    /************************************************************************
     *  Load the ViewPager which loads up all of the tabs for this Activity.
     ************************************************************************/
    public void LoadReviewVersion() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.review_tabs);
        tabLayout.setupWithViewPager(viewPager);

        ReviewPageAdapter viewPagerAdapter = new ReviewPageAdapter(getSupportFragmentManager(),
                StartTheReview.this);
        viewPager.setOffscreenPageLimit(2); // Required to refresh the tabs.
        viewPager.setAdapter(viewPagerAdapter);
    }

    /*******************************************************
     *  Changes the tab when given the TabLayout page index
     *******************************************************/
    void selectTab(int pageIndex){
        tabLayout.setScrollPosition(pageIndex,0f,true);
        viewPager.setCurrentItem(pageIndex);
    }

    public void ChangeVersionsDialog(int position) {
        String title = "Changing Review Version";
        String dialog_message = "If you change the version, you will lose all your changes...";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message).setTitle(title);

        // TODO: The dropdown selection will modify the version data from Firebase.
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Reset all the comment-related lists.
                NewComments = new ArrayList<SingleComment>();
                UpdatedComments = new ArrayList<SingleComment>();
                listOfComments = new ArrayList<SingleComment>();

                dropdown_selection = version_dropdown.getSelectedItem().toString();
                current_version = Integer.parseInt(dropdown_selection.replace("Version ", ""));
                current_dropdown_position = version_dropdown.getSelectedItemPosition();
                version_changed = true;
                LoadReviewVersion();
                selectTab(current_tab_selection);
                version_changed = false;
            }
        });

        // If the user chooses "No", just dismiss the dialog box.
        builder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                version_dropdown.setSelection(current_dropdown_position);
                dialog.dismiss();
            }
        });

        // Finally display the Alert Dialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void SaveChangesDialog() {
        String title = "Cancel Edits";
        String dialog_message = "By continuing, your edits will be lost. Click Save Changes button to save your progress.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message).setTitle(title);

        // If the user chooses "No", just dismiss the dialog box.
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // If the user chooses to press "Yes", only then, return to login screen.
        // TODO: Remove last user login entry from SQL Database.
        builder.setPositiveButton("Goto User Portal", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ReturnToPreviousActivity();
            }
        });

        // Finally display the Alert Dialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**************************************************
     *  Comments Tab: Button Click Handler Functions.
     **************************************************/
    public void EditCommentDialog(final String firebase_comment_number, final String commenter_fullname,
                                  final String commenter_username, final String comment) {
        String title = commenter_username + "'s Comment";
        String updated_comment = "";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText editable_comment_view = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editable_comment_view.setText(comment);
        editable_comment_view.setLayoutParams(lp);
        builder.setView(editable_comment_view);

        // TODO: Removing the Delete option as it will not sync with the View PDF Tab.
        // If the user chooses "Cancel", just dismiss the dialog box.
        //builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // If the user chooses "Delete", the comment in question is deleted.
        //builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
        //    public void onClick(DialogInterface dialog, int id) {
        //        String delete_comment = editable_comment_view.getText().toString();

        //        // Add a new entry into DeletedComments array.
        //        String empty_date = "";
        //        SingleComment del_comment = new SingleComment(
        //                firebase_comment_number, commenter_fullname, "",
        //                delete_comment, empty_date);
        //        del_comment.setCreation_date();
        //        DeletedComments.add(del_comment);

        //        // Delete the comment in the comment list array by comparing the comment #.
        //        for (int i = 0; i < listOfComments.size(); ++i) {
        //            if (listOfComments.get(i).CommentNumber().equals(firebase_comment_number)) {
        //                listOfComments.remove(i);
        //                break;
        //            }
        //        }

        //       // Finally, reload the List ArrayAdapter..
        //       CommentsListAdapter adapter = new CommentsListAdapter(CommentsContext, R.layout.display_comment, commenter_username, listOfComments);
        //       CommentsListView.setAdapter(adapter);

        //        dialog.dismiss();
        //    }
        //});

        // If the user chooses "Modify", The user's text is modified.
        builder.setPositiveButton("Modify", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String updated_comment = editable_comment_view.getText().toString();
                RowComment.setText(updated_comment);

                // Only if the comment has changed from the original, then modify comments array.
                if (!updated_comment.equals(comment)) {
                    // Check if existing entry exists in the comments array, update the entry.
                    Boolean updated_comment_exists = false;
                    for (int i = 0; i < UpdatedComments.size(); ++i) {
                        if (UpdatedComments.get(i).CommentNumber().equals(firebase_comment_number)) {
                            updated_comment_exists = true;
                            UpdatedComments.get(i).setComment(updated_comment);
                        }
                    }

                    // If instead, their is not existing comment payload, add it to the comments array.
                    if (!updated_comment_exists) {
                        String empty_date = "";
                        SingleComment new_comment = new SingleComment(
                                firebase_comment_number, commenter_fullname, commenter_username,
                                updated_comment, empty_date);
                        new_comment.setCreation_date();
                        UpdatedComments.add(new_comment);
                    }
                }
            }
        });

        // Finally display the Alert Dialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void ViewCommentDetailsDialog(String commenter_username, String comment) {
        String title = commenter_username + "'s Comment";
        String dialog_message = comment;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message).setTitle(title);

        // If the user chooses "No", just dismiss the dialog box.
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Finally display the Alert Dialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void ButtonClickHandler(View view) {
        //get the row the clicked button is in
        ConstraintLayout RowLayout = (ConstraintLayout) view.getParent();
        RowComment = (Button) RowLayout.getChildAt(0);
        TextView RowFirebaseCommentNumber = (TextView) RowLayout.getChildAt(2);
        TextView RowFullName = (TextView) RowLayout.getChildAt(3);
        TextView RowUsername = (TextView) RowLayout.getChildAt(4);

        // If the comment username is the same as the logged in user, allow editing the comment.
        if (RowUsername.getText().toString().equals("@" + CurrentUser.Username())) {
            String firebase_comment_number = RowFirebaseCommentNumber.getText().toString();
            EditCommentDialog(firebase_comment_number, RowFullName.getText().toString(), RowUsername.getText().toString(), RowComment.getText().toString());
        } else {
            ViewCommentDetailsDialog(RowUsername.getText().toString(), RowComment.getText().toString());
        }
        RowLayout.refreshDrawableState();
    }
    /*********************************************
     *  Update Firebase entries for the comments.
     *********************************************/
    public void UpdateFirebaseComments(String uuid, String version, List<SingleComment> new_comments, List<SingleComment> updated_comments, List<SingleComment> deleted_comments) {
        // Update the comments in firebase.
        String comment_username = CurrentUser.Username();
        String comment_fullname = CurrentUser.FullName();
        DatabaseReference comments_db = mDatabaseReference.child("open_reviews").child(uuid).child("versions").child(version).child("comments");

        /*******************
         *  Delete comments.
         ******************/
        for (int idx = 0; idx < deleted_comments.size(); ++idx) {
            SingleComment deleted_comment = deleted_comments.get(idx);
            String comment_number = deleted_comment.CommentNumber();

            // Updating the new entries is exactly like making a new entry.
            comments_db.child(comment_number).removeValue(); // No listener needed really.
            Log.d(TAG, "UpdateFirebaseComments: DeleteComment: removed comment # " + comment_number + " for Review Version=" + version);
        }
        /*******************
         *  Update comments.
         ******************/
        for (int idx = 0; idx < updated_comments.size(); ++idx) {
            SingleComment updated_comment = new_comments.get(idx);
            String comment_number = updated_comment.CommentNumber();
            String comment_details = updated_comment.Comment();
            String comment_timestamp = updated_comment.CreationDate();
            String annotation_id = updated_comment.AnnotationID();

            // Updating the new entries is exactly like making a new entry.
            comments_db.child(comment_number).child("details").setValue(comment_details);
            comments_db.child(comment_number).child("full_name").setValue(comment_fullname);
            comments_db.child(comment_number).child("timestamp").setValue(comment_timestamp);
            comments_db.child(comment_number).child("username").setValue(comment_username);
            comments_db.child(comment_number).child("annotation_id").setValue(annotation_id);
            Log.d(TAG, "UpdateFirebaseComments: Updated: updated comment # " + comment_number + " to Review Version=" + version);
        }
        /*******************
         *  New comments.
         ******************/
        for (int idx = 0; idx < new_comments.size(); ++idx) {
            SingleComment new_comment = new_comments.get(idx);
            String comment_number = new_comment.CommentNumber();
            String comment_details = new_comment.Comment();
            String comment_timestamp = new_comment.CreationDate();
            String annotation_id = new_comment.AnnotationID();

            // Add the new entries.
            comments_db.child(comment_number).child("details").setValue(comment_details);
            comments_db.child(comment_number).child("full_name").setValue(comment_fullname);
            comments_db.child(comment_number).child("timestamp").setValue(comment_timestamp);
            comments_db.child(comment_number).child("username").setValue(comment_username);
            comments_db.child(comment_number).child("annotation_id").setValue(annotation_id);
            Log.d(TAG, "UpdateFirebaseComments: NewComment: added comment # " + comment_number + " to Review Version=" + version);
        }
    }

    /****************************************************
     *  Exports the annotations from the PDF to Firebase.
     ****************************************************/
    public void ExportAnnotationsToFile(final String uuid, final String version) {
        String cachePDF_filename = CacheDirectory + "/" + "upload.pdf";
        String cache_annotation_file = CacheDirectory + "/" + "upload.xfdf";
        Log.e(TAG, "ExportAnnotationsToFile: Cache File: " + cachePDF_filename);
        Log.e(TAG, "ExportAnnotationsToFile: Cache XFDF File: " + cache_annotation_file);

        // TODO: Currently this does work. Now just need to save to firebase.
        try {
            // Export the Annotations on the ViewPDF fragment and save it to a cache XFDF file.
            FDFDoc doc_fields = CurrentPDFDoc.fdfExtract(PDFDoc.e_annots_only);
            doc_fields.saveAsXFDF(cache_annotation_file);
            Log.w(TAG, "ExportAnnotationsToFile: Saved existing annotations to the cache XFDF file.");

            // Upload the cache XFDF file to firebase storage and update the firebase database entry.
            UpdateFirebaseAnnotations(cache_annotation_file, uuid, version);
        } catch (Exception e) {
            Log.e(TAG, "ExportAnnotationsToFile: Could not export annotations to a cache file!");
            e.getStackTrace();

            Log.w(TAG, "ExportAnnotationsToFile: Returning to the previous activity.");
            ReturnToPreviousActivity();
        }
    }
    public void UpdateFirebaseAnnotations(final String existing_annotation_file, final String uuid, final String version) {
        // Specify the new location in firebase to upload the .xfdf file to.
        final String new_annotations_file = uuid + "_v" + version + ".xfdf";
        String firebase_annotations_file = "/annotations/" + new_annotations_file;

        // Try to open the Uri of the .xfdf file and upload it to firebase.
        try {
            Uri file = Uri.fromFile(new File(existing_annotation_file));
            Log.d(TAG, "ExportAnnotationsToFile: The Uri from the xfdf file has been successfully obtained.");
            mStorageReference.child(firebase_annotations_file).putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Upon uploading the .xfdf file to firebase storage, update the annotation entry in the firebase database.
                            Log.d(TAG, "ExportAnnotationsToFile: Successfully uploaded the xfdf file to firebase!");
                            mDatabaseReference.child("open_reviews").child(uuid).child("versions").child(version).child("annotations").setValue(new_annotations_file);
                            Log.d(TAG, "ExportAnnotationsToFile: Updated the xfdf file entry on the firebase database.");

                            // Change this variable to reflect the changes, so it can be viewed in the ViewPDF Fragment.
                            FirebaseAnnotationFilename = new_annotations_file;
                            Log.d(TAG, "ExportAnnotationsToFile: The annotations file on firebase is now: " + FirebaseAnnotationFilename);

                            // After the upload is complete, return to the previous activity.
                            Log.w(TAG, "ExportAnnotationsToFile: Returning to the previous activity.");
                            ReturnToPreviousActivity();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "ExportAnnotationsToFile: Could not upload the cached XFDF file to Firebase Storage!");
            Log.e(TAG, "ExportAnnotationsToFile: No annotation database entry was updated as well.");
            e.printStackTrace();

            Log.w(TAG, "ExportAnnotationsToFile: Returning to the previous activity.");
            ReturnToPreviousActivity();
        }
    }

    /**********************************************
     *  Function to return to UserPortal activity.
     **********************************************/
    public void ReturnToPreviousActivity() {
        Intent UserPortalIntent = new Intent(
                StartTheReview.this,
                UserPortal.class);
        UserPortalIntent.putExtra("UserProfile", CurrentUser);
        startActivity(UserPortalIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        SaveChangesDialog();
    }}