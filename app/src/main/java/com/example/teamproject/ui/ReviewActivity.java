package com.example.teamproject.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import com.example.teamproject.R;
import com.example.teamproject.model.AppThemes;
import com.example.teamproject.model.ProfileSettings;
import com.example.teamproject.model.ReviewPageAdapter;
import com.google.android.material.tabs.TabLayout;

public class ReviewActivity extends AppCompatActivity {
    ProfileSettings CurrentUser;
    AppThemes AvailableThemes;
    int ThemeID;

    private ConstraintLayout ThisLayout;
    private Button SaveChanges;
    private Spinner version_dropdown;
    private String[] sample_versions = {"Version 1", "Version 2", "Version 3"};
    String dropdown_selection;
    int current_version = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_review);

        Intent i = getIntent();
        CurrentUser = (ProfileSettings) i.getSerializableExtra("UserProfile");

        /*****************************************************************
         *  Change the Background that was selected by the theme_dropdown
         *****************************************************************/
        ThisLayout = (ConstraintLayout) findViewById(R.id.startreview_layout);
        ThemeID = CurrentUser.Theme();
        AvailableThemes = new AppThemes(ThemeID);
        ThisLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), AvailableThemes.GetTheme()));

        /***************************************************************
         *  Configure the dropdown to show all versions of the Review
         ***************************************************************/
        version_dropdown = (Spinner) findViewById(R.id.version_dropdown);
        ArrayAdapter<String> adpter = new ArrayAdapter<String> (ReviewActivity.this, R.layout.review_spinner, sample_versions);
        version_dropdown.setAdapter(adpter);

        /**************************************************************************************
         *  After the user is done editing and reviewing comments, save the changes.
         *************************************************************************************/
        SaveChanges = (Button) findViewById(R.id.button_save_review);
        SaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Update Firebase entries and go back to the User Portal.
                Intent UserPortalIntent = new Intent(
                        ReviewActivity.this,
                        UserPortal.class);
                UserPortalIntent.putExtra("UserProfile", CurrentUser);
                startActivity(UserPortalIntent);
            }
        });

        /**************************************************************************************
         *  Below will obtain the current document version from the Spinner dropdown listener.
         *************************************************************************************/
        dropdown_selection = version_dropdown.getSelectedItem().toString();
        current_version = Integer.parseInt(dropdown_selection.replace("Version ", ""));

        // On pressing the drop down menu, return the current version
        version_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                dropdown_selection = version_dropdown.getSelectedItem().toString();
                current_version = Integer.parseInt(dropdown_selection.replace("Version ", ""));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                dropdown_selection = version_dropdown.getSelectedItem().toString();
                current_version = Integer.parseInt(dropdown_selection.replace("Version ", ""));
            }
        });

        /**************************************************************************************
         *  Load the ViewPager which loads up both of the tabs for this Activity.
         *************************************************************************************/
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.review_tabs);
        tabLayout.setupWithViewPager(viewPager);

        ReviewPageAdapter viewPagerAdapter = new ReviewPageAdapter(getSupportFragmentManager(),
                ReviewActivity.this);
        viewPager.setOffscreenPageLimit(2); // Required to refresh the tabs.
        viewPager.setAdapter(viewPagerAdapter);
    }

    public void SaveChangesDialog() {
        String title = "Cancel Edits";
        String dialog_message = "By continuing, your edits will be lost. Click Save Changes button to save your progress.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message).setTitle(title);

        // If the user chooses to press "Yes", only then, return to login screen.
        // TODO: Remove last user login entry from SQL Database.
        builder.setPositiveButton("Goto User Portal", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If the user is an elevated user/reviewer, you can see and click the icon.
                Intent UserPortalIntent = new Intent(
                        ReviewActivity.this,
                        UserPortal.class);
                startActivity(UserPortalIntent);
                finish();
            }
        });

        // If the user chooses "No", just dismiss the dialog box.
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Finally display the Alert Dialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        SaveChangesDialog();
    }}