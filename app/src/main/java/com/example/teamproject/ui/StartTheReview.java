package com.example.teamproject.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import com.example.teamproject.R;
import com.example.teamproject.model.AppThemes;
import com.example.teamproject.model.ProfileSettings;
import com.example.teamproject.model.ReviewPageAdapter;
import com.example.teamproject.model.SingleComment;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;

public class StartTheReview extends AppCompatActivity {
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
    private String[] sample_versions = {"Version 1", "Version 2", "Version 3"};
    String dropdown_selection;
    int current_dropdown_position = 0, current_tab_selection = 0, current_version = 0;

    // The data used for updating firebase (on edits).
    ArrayList<SingleComment> UpdatedComments = new ArrayList<SingleComment>();
    ArrayList<SingleComment> NewComments = new ArrayList<SingleComment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_review);

        /**********************************************************************
         *  Controls what comments can be deleted as well as Theme Background
         **********************************************************************/
        Intent i = getIntent();
        CurrentUser = (ProfileSettings) i.getSerializableExtra("UserProfile");
        logged_in_username = CurrentUser.Username();

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
         if (CurrentUser.AccountType().equals("standard")) {
             StartReviewTitle.setEnabled(false);
         }

        /***************************************************************
         *  Configure the dropdown to show all versions of the Review
         ***************************************************************/
        version_dropdown = (Spinner) findViewById(R.id.version_dropdown);
        ArrayAdapter<String> adpter = new ArrayAdapter<String> (this, R.layout.review_spinner, sample_versions);
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
                        StartTheReview.this,
                        UserPortal.class);
                UserPortalIntent.putExtra("UserProfile", CurrentUser);
                startActivity(UserPortalIntent);
            }
        });

        /**************************************************************************************
         *  Below will obtain the current document version from the Spinner dropdown listener.
         *************************************************************************************/
        // Set the selected item to some value.
        version_dropdown.setSelection(current_dropdown_position);  // Example: Version 1
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

        // If the user chooses to press "Yes", only then, return to login screen.
        // TODO: Remove last user login entry from SQL Database.
        builder.setPositiveButton("Goto User Portal", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If the user is an elevated user/reviewer, you can see and click the icon.
                Intent UserPortalIntent = new Intent(
                        StartTheReview.this,
                        UserPortal.class);
                UserPortalIntent.putExtra("UserProfile", CurrentUser);
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

    /**************************************************
     *  Comments Tab: Button Click Handler Functions.
     **************************************************/
    public void EditCommentDialog(final int comment_number, final String commenter_fullname,
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

        // If the user chooses "No", just dismiss the dialog box.
        builder.setPositiveButton("Modify", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String updated_comment = editable_comment_view.getText().toString();
                RowComment.setText(updated_comment);

                // Only if the comment has changed from the original, then modify comments array.
                if (!updated_comment.equals(comment)) {
                    // Check if existing entry exists in the comments array, update the entry.
                    Boolean updated_comment_exists = false;
                    for (int i = 0; i < UpdatedComments.size(); ++i) {
                        if (String.valueOf(comment_number) == UpdatedComments.get(i).CommentNumber()) {
                            updated_comment_exists = true;
                            UpdatedComments.get(i).setComment(updated_comment);
                            //Toast.makeText(getApplicationContext(), "Updated: " + UpdatedComments.get(i).Comment(), Toast.LENGTH_LONG).show();
                        }
                    }

                    // If instead, their is not existing comment payload, add it to the comments array.
                    if (!updated_comment_exists) {
                        String empty_date = "";
                        SingleComment new_comment = new SingleComment(
                                String.valueOf(comment_number),commenter_fullname, commenter_username,
                                updated_comment, empty_date);
                        new_comment.setCreation_date();
                        UpdatedComments.add(new_comment);
                        //Toast.makeText(getApplicationContext(), "New: " + UpdatedComments.get(UpdatedComments.size() - 1).Comment(), Toast.LENGTH_LONG).show();
                    }
                }
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
        TextView RowCommentNumber = (TextView) RowLayout.getChildAt(1);
        TextView RowFullName = (TextView) RowLayout.getChildAt(2);
        TextView RowUsername = (TextView) RowLayout.getChildAt(3);

        // If the comment username is the same as the logged in user, allow editing the comment.
        if (RowUsername.getText().toString().equals("@" + CurrentUser.Username())) {
            String number_string = RowCommentNumber.getText().toString().replace("#","").trim();
            int firebase_comment_index = Integer.parseInt(number_string);
            EditCommentDialog(firebase_comment_index, RowFullName.getText().toString(), RowUsername.getText().toString(), RowComment.getText().toString());
        } else {
            ViewCommentDetailsDialog(RowUsername.getText().toString(), RowComment.getText().toString());
        }
        RowLayout.refreshDrawableState();
    }

    @Override
    public void onBackPressed() {
        SaveChangesDialog();
    }}