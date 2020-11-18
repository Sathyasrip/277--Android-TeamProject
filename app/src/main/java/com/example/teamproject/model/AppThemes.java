package com.example.teamproject.model;
import com.example.teamproject.R;

public class AppThemes {
    private String[] available_themes = {"Night Sky", "Mountain and Field"};
    private int [] theme_resources = {R.drawable.nightsky_theme, R.drawable.mountain_theme};
    private int theme_id;

    public AppThemes(int theme_id) {
        this.theme_id = theme_id;
    }

    public void SetTheme(int theme_id) {
        this.theme_id = theme_id;
    }

    public int GetTheme() {
        if (0 < this.theme_id && this.theme_id < theme_resources.length) {
            return this.theme_resources[this.theme_id];
        }
        else {
            // By default, return the first theme.
            return this.theme_resources[0];
        }
    }

    public String[] AvailableThemeNames() {
        return this.available_themes;
    }
}
