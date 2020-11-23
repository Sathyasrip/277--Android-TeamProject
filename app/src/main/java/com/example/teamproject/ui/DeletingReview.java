package com.example.teamproject.ui;

import com.example.teamproject.R;
import com.example.teamproject.model.AppThemes;
import com.example.teamproject.model.ProfileSettings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

public class DeletingReview extends AppCompatActivity {
    ProfileSettings CurrentUser;
    AppThemes AvailableThemes;
    int ThemeID;

    // Views for the Activity layout.
    private ConstraintLayout ThisLayout;
    private ImageView GoBackIcon;
    private Button DeleteReview;
    private Switch PurgeEntireReview;

    // Used for the Spinner views.
    private Spinner review_title_chooser, review_version_chooser;
    private String[] existing_reviews, available_versions;
    private String[] test_reviews = {"Stem Cell IEEE", "Part Datasheet", "COVID19 Report"};
    private String[] test_versions = {"1", "2", "3"};

    // Used for updating Firebase.
    Boolean PurgeEntireReviewFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deleting_review);

        /*************************
         *  Link up all the views
         *************************/
        ThisLayout = (ConstraintLayout) findViewById(R.id.deletereview_layout);
        GoBackIcon = (ImageView) findViewById(R.id.image_deletereview_go_back);
        PurgeEntireReview = (Switch) findViewById(R.id.switch_delete_whole_review);
        DeleteReview = (Button) findViewById(R.id.button_delete_review);
        review_title_chooser = (Spinner) findViewById(R.id.deletereview_review_title_chooser);
        review_version_chooser = (Spinner) findViewById(R.id.deletereview_review_version_chooser);

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
        // TODO: Load the dropdown based on Firebase NoSQL Database entries, not these test versions.
        existing_reviews = test_reviews;
        available_versions = test_versions;

        // Can be freely choosen by the user.
        ArrayAdapter<String> adpter1 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, existing_reviews);
        review_title_chooser.setAdapter(adpter1);
        review_title_chooser.setEnabled(true);

        // By default, the versions can be selected.
        PurgeEntireReviewFlag = false;
        ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, available_versions);
        review_version_chooser.setAdapter(adpter2);
        review_version_chooser.setEnabled(true);

        // However, if the switch is enabled, you cannot select the version.
        PurgeEntireReview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    PurgeEntireReviewFlag = true;
                    String[] no_reviews = new String[0];
                    ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, no_reviews);
                    review_version_chooser.setAdapter(adpter2);
                    review_version_chooser.setEnabled(false);
                } else {
                    PurgeEntireReviewFlag = false;
                    ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (DeletingReview.this, R.layout.review_spinner, available_versions);
                    review_version_chooser.setAdapter(adpter2);
                    review_version_chooser.setEnabled(true);
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
                // TODO: Do the firebase entry deletion here.

                // Return to the User Portal Activity.
                Intent PreviousActivityIntent = new Intent(
                        DeletingReview.this,
                        UserPortal.class);
                PreviousActivityIntent.putExtra("UserProfile", CurrentUser);
                startActivity(PreviousActivityIntent);
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
}