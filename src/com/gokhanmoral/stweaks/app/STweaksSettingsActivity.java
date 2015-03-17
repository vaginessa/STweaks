package com.gokhanmoral.stweaks.app;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class STweaksSettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new STweaksPreferenceFragment()).commit();
    }

    public static class STweaksPreferenceFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.stweaks_settings);
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

            if (key.equals("pref_launcher_icon")) {
                String icon = prefs.getString("pref_launcher_icon", "");
                if (icon.equals("material")) {
                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-Material"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-NoIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-Stock"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                Toast.makeText(getActivity(), R.string.change_icon,
                        Toast.LENGTH_LONG).show();
                } else if (icon.equals("stock")) {
                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-Stock"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-NoIcon"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-Material"),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    Toast.makeText(getActivity(), R.string.change_icon,
                            Toast.LENGTH_LONG).show();
                } else if (icon.equals("noicon")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.icon_prompt)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Handle Ok
                                    getActivity().getPackageManager().setComponentEnabledSetting(
                                                new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-NoIcon"),
                                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                                    getActivity().getPackageManager().setComponentEnabledSetting(
                                                new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-Material"),
                                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                                    getActivity().getPackageManager().setComponentEnabledSetting(
                                                new ComponentName("com.gokhanmoral.stweaks.app", "com.gokhanmoral.stweaks.app.MainActivity-Stock"),
                                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                                    Toast.makeText(getActivity(), R.string.no_icon,
                                                Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Handle Cancel
                                }
                            })
                            .setTitle(R.string.warning)
                            .create()
                            .show();
                }
            }
        }
    }
}
