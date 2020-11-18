package com.example.teamproject.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.room.Delete;

import com.example.teamproject.R;
import com.example.teamproject.model.AppThemes;
import com.example.teamproject.model.ProfileSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class UserPortal extends AppCompatActivity {
    ProfileSettings CurrentUser;
    AppThemes AvailableThemes;
    int ThemeID;

    Boolean DebugMode = true;
    Boolean ElevatedUser = false; // Used to control what buttons are available or not?

    private ConstraintLayout ThisLayout;
    private Button StartReview, SelectPDF;
    private EditText ReviewTitle;
    private TextView SelectedTitle, SelectedVersion, SelectedPDF, AccountType;
    private ImageView ProfileIcon, GoBackIcon, DeleteReviewIcon;
    private Spinner review_title_chooser, review_version_chooser;
    private String[] existing_reviews, existing_versions;
    private String[] test_reviews = {"Stem Cell IEEE", "Part Datasheet", "COVID19 Report"};
    private String[] test_versions = {"1", "2", "3"};

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_portal);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        /**********************************************************************
        *  Retrieve the Intent containing the User Profile data from Firebase.
        **********************************************************************/
        Intent i = getIntent();
        CurrentUser = (ProfileSettings) i.getSerializableExtra("UserProfile");

        // TODO: Load the dropdown based on Firebase NoSQL Database entries.
        if (DebugMode) {
            existing_reviews = test_reviews;
            existing_versions = test_versions;
        }
        review_title_chooser = (Spinner) findViewById(R.id.review_title_chooser);
        review_version_chooser = (Spinner) findViewById(R.id.review_version_chooser);
        ArrayAdapter<String> adpter1 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_reviews);
        ArrayAdapter<String> adpter2 = new ArrayAdapter<String> (UserPortal.this, R.layout.review_spinner, existing_versions);
        review_title_chooser.setAdapter(adpter1);
        review_version_chooser.setAdapter(adpter2);

        // These are all the views
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

        /*****************************************************************
         *  Change the Background that was selected by the theme_dropdown
         *****************************************************************/
        ThemeID = CurrentUser.Theme();
        AvailableThemes = new AppThemes(ThemeID);
        ThisLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), AvailableThemes.GetTheme()));

        /*****************************
         * Load the profile picture.
         *****************************/
        //Toast.makeText(getApplicationContext(), CurrentUser.GetLocalProfilePicture(), Toast.LENGTH_SHORT).show();
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
                    startActivity(DeletingReviewIntent);
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
                    Toast.makeText(getApplicationContext(), "Select PDF", Toast.LENGTH_SHORT).show();
                }
            });

            /**************************************************************************
             * When the user chooses a project (new or existing) open ReviewActivity.
             *
             * NOTE: The data references must be updated and passed to the
             * ReviewActivity.
             *************************************************************************/
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
        }

        StartReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Launch ReviewActivity after adding in the appropriate extras.
                Intent ReviewIntent = new Intent(
                        UserPortal.this,
                        ReviewActivity.class);
                ReviewIntent.putExtra("UserProfile", CurrentUser);
                startActivity(ReviewIntent);
            }
        });
    }

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
        Picasso.get().load(image_url).fit().centerCrop().into(image_view);
    }

    @Override
    public void onBackPressed() {
        LogoutDialog();
    }
}