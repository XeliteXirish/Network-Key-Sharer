package com.XeliteXirish.NetworkKeySharer.ui.activities;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.XeliteXirish.NetworkKeySharer.R;

import org.wordpress.passcodelock.PasscodePreferenceFragment;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName) || PinLockFragment.class.getName().equals(fragmentName) || StyleFragment.class.getName().equals(fragmentName);
    }

    public static class PinLockFragment extends PasscodePreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_pin_lock);
            setHasOptionsMenu(true);
        }

        @Override
        public void onStart() {
            super.onStart();

            setPreferences(findPreference(getString(R.string.pref_key_passcode_toggle)),
                    findPreference(getString(R.string.pref_key_change_passcode)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class StyleFragment extends DialogFragment{

        public StyleFragment() {
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dialog_change_theme, container, false);

            final Spinner spinnerStyle;
            String[] themes = {"Light", "Dark"};
            Button buttonChangeTheme;

            spinnerStyle = (Spinner) rootView.findViewById(R.id.spinnerSelectTheme);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, themes);
            buttonChangeTheme = (Button) rootView.findViewById(R.id.buttonChangeTheme);
            spinnerStyle.setAdapter(adapter);

            buttonChangeTheme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (spinnerStyle.getSelectedItem().equals("Light")) {
                        //getActivity().getApplication().setTheme(R.style.AppThemeLight);
                        getActivity().recreate();

                    } else if (spinnerStyle.getSelectedItem().equals("Dark")) {
                        //getActivity().getApplication().setTheme(R.style.AppThemeDark);
                        getActivity().recreate();
                    }
                }
            });
            return rootView;
        }
    }
}
