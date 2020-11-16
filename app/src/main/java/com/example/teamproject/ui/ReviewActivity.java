package com.example.teamproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.teamproject.R;
import com.example.teamproject.model.ReviewPageAdapter;
import com.google.android.material.tabs.TabLayout;

public class ReviewActivity extends AppCompatActivity {

    private Button SaveChanges;
    private Spinner version_dropdown;
    private String[] sample_versions = {"Version 1", "Version 2", "Version 3"};
    String dropdown_selection;
    int current_version = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

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
                // TODO: Launch UserPortal after performing the edits.
                Intent DemoMenuIntent = new Intent(
                        ReviewActivity.this,
                        UserPortal.class);
                startActivity(DemoMenuIntent);
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
}