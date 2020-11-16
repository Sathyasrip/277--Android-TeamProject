package com.example.teamproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamproject.R;

public class UserPortal extends AppCompatActivity {

    Boolean DebugMode = true;
    private Button StartReview, OpenDoc, SyncDoc;
    private EditText ReviewTitle;
    private Spinner review_chooser;
    private String[] existing_reviews;
    private String[] test_reviews = {"Stem Cell IEEE", "Part Datasheet", "COVID19 Report"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_screen);

        if (DebugMode) {
            existing_reviews = test_reviews;
        }

        // TODO: Load the spinner based on SQL or NoSQL Database entries.
        review_chooser = (Spinner) findViewById(R.id.review_chooser);
        ArrayAdapter<String> adpter = new ArrayAdapter<String> (UserPortal.this, R.layout.spinner, existing_reviews);
        review_chooser.setAdapter(adpter);

        // Initialize all the buttons and EditText.
        StartReview = (Button) findViewById(R.id.button_start_review);
        OpenDoc = (Button) findViewById(R.id.button_open_doc);
        SyncDoc = (Button) findViewById(R.id.button_upload_doc);  // not enabled
        ReviewTitle = (EditText) findViewById(R.id.editTextReviewTitle);  // not enabled

        /**************************************************************************
         * TODO: Sathya
         * These two buttons will interact with File Manager or FireBase to
         * download the PDF (as version 1) under main/res/raw/review_doc.pdf.
         *
         * When uploading to the Cloud or storing locally under main/res/raw,
         * any name may be used as long as it is added into some database to pull
         * from.
         *************************************************************************/
        OpenDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Open File Manager to choose the file (to upload/download).
            }
        });
        SyncDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Upload to FireBase or just download to main/res/raw folder.s
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