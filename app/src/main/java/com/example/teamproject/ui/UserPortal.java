package com.example.teamproject.ui;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Delete;

import com.example.teamproject.R;

import org.w3c.dom.Text;

public class UserPortal extends AppCompatActivity {

    Boolean DebugMode = true;
    Boolean ElevatedUser = false; // Used to control what buttons are available or not?
    private Button StartReview, SelectPDF;
    private EditText ReviewTitle;
    private TextView SelectedTitle, SelectedVersion;
    private ImageView ProfileIcon, GoBackIcon, DeleteReviewIcon;
    private Spinner review_title_chooser, review_version_chooser;
    private String[] existing_reviews, existing_versions;
    private String[] test_reviews = {"Stem Cell IEEE", "Part Datasheet", "COVID19 Report"};
    private String[] test_versions = {"1", "2", "3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_portal);

        if (DebugMode) {
            existing_reviews = test_reviews;
            existing_versions = test_versions;
        }

        // TODO: Load the dropdown based on Firebase NoSQL Database entries.
        review_title_chooser = (Spinner) findViewById(R.id.review_title_chooser);
        review_version_chooser = (Spinner) findViewById(R.id.review_version_chooser);
        ArrayAdapter<String> adpter1 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_reviews);
        ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_versions);
        review_title_chooser.setAdapter(adpter1);
        review_version_chooser.setAdapter(adpter2);

        // These are the 3 image views that will lead to some new activity.
        GoBackIcon = (ImageView) findViewById(R.id.image_userportal_go_back);
        DeleteReviewIcon = (ImageView) findViewById(R.id.image_userportal_delete_review);
        ProfileIcon = (ImageView) findViewById(R.id.image_userportal_profile);

        // These are all the other views for controlling whether to make a new review or not.
        SelectPDF = (Button) findViewById(R.id.button_choose_pdf);
        ReviewTitle = (EditText) findViewById(R.id.editTextReviewTitle);
        SelectedTitle = (TextView) findViewById(R.id.tv_selected_review_title);
        SelectedVersion = (TextView) findViewById(R.id.tv_selected_review_version);
        StartReview = (Button) findViewById(R.id.button_start_review);

        /**************************************************************************
         * The following OnClickListeners take the user to a specified activity
         * to do something.
         *************************************************************************/
        GoBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the user is an admin, return to the Admin menu after updating changes.
                Intent PreviousActivityIntent = new Intent(
                        UserPortal.this,
                        LoginScreen.class);
                startActivity(PreviousActivityIntent);
                //finish();
            }
        });
        DeleteReviewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the user is an elevated user/reviewer, you can see and click the icon.
                Intent DeletingReviewIntent = new Intent(
                        UserPortal.this,
                        DeletingReview.class);
                startActivity(DeletingReviewIntent);
            }
        });
        ProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The profile icon should be up to date with what is on Firebase and User Profile.
                Intent ProfileActivityIntent = new Intent(
                        UserPortal.this,
                        UserProfile.class);
                startActivity(ProfileActivityIntent);
            }
        });
        /**************************************************************************
         * TODO: Sathya
         * These two buttons will interact with File Manager or FireBase to
         * download the PDF (as version 1) under main/res/raw/review_doc.pdf.
         *
         * When uploading to the Cloud or storing locally under main/res/raw,
         * any name may be used as long as it is added into some database to pull
         * from.
         *************************************************************************/
        SelectPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Open File Manager to choose the file (to upload/download).
            }
        });

        /**************************************************************************
         * When the user chooses a project (new or existing) open ReviewActivity.
         *
         * NOTE: The data references must be updated and passed to the
         * ReviewActivity.
         *************************************************************************/
        StartReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Launch ReviewActivity after adding in the appropriate extras.
                Intent ReviewIntent = new Intent(
                        UserPortal.this,
                        ReviewActivity.class);
                startActivity(ReviewIntent);
            }
        });
    }
}