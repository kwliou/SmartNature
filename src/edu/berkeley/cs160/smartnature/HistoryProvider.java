package edu.berkeley.cs160.smartnature;

import android.content.SearchRecentSuggestionsProvider;

public class HistoryProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "edu.berkeley.cs160.smartnature";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public HistoryProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
