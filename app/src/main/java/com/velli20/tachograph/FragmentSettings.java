/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) [2017] [velli20]
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package com.velli20.tachograph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.velli20.tachograph.ExportEvents.OnFileSavedListener;
import com.velli20.tachograph.database.DataBaseHandlerConstants;
import com.velli20.tachograph.filepicker.ActivityFilePicker;
import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.preferences.CustomCheckBoxPreference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;


public class FragmentSettings extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 0;
    public static final int EXPORT_REQUEST_CODE = 43;
    public static final int PICK_BACKUP_FILE_REQUEST_CODE = 47;
    public static final int BACKUP_REQUEST_CODE = 49;

    public static final String TAG = "SettingsFragment ";

    private SharedPreferences mPrefs;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final Resources res = getActivity().getResources();

        final Preference mRemoveLogsPref = findPreference(res.getString(R.string.preference_key_remove_all_logs));
        final Preference mBackupPref = findPreference(res.getString(R.string.preference_key_backup));
        final Preference mRestorePref = findPreference(res.getString(R.string.preference_key_restore));
        final Preference mExportPref = findPreference(res.getString(R.string.preference_key_export));
        final Preference locationPref = findPreference(res.getString(R.string.preference_key_use_gps));
        final Preference viewSourceCode = findPreference(getString(R.string.preference_key_view_source_code));

        mRemoveLogsPref.setOnPreferenceClickListener(this);
        mBackupPref.setOnPreferenceClickListener(this);
        mRestorePref.setOnPreferenceClickListener(this);
        mExportPref.setOnPreferenceClickListener(this);
        viewSourceCode.setOnPreferenceClickListener(this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            locationPref.setOnPreferenceChangeListener(this);
            int permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

            if (permission != PackageManager.PERMISSION_GRANTED && mPrefs.getBoolean(getString(R.string.preference_key_use_gps), false)) {
                mPrefs.edit().putBoolean(getString(R.string.preference_key_use_gps), false).apply();
                ((CustomCheckBoxPreference) findPreference(getString(R.string.preference_key_use_gps))).setChecked(false);
            }

        }


    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals(getString(R.string.preference_key_remove_all_logs))) {
            onRemoveAllLogs();
        } else if (key.equals(getString(R.string.preference_key_backup))) {
            Intent backup = new Intent(getActivity(), ActivityFilePicker.class);
            backup.putExtra(ActivityFilePicker.INTENT_EXTRA_FILE_EXTENSION, ".db");
            backup.putExtra(ActivityFilePicker.INTENT_EXTRA_MODE, ActivityFilePicker.MODE_CREATE_FILE);
            backup.putExtra(ActivityFilePicker.INTENT_EXTRA_FILENAME, getString(R.string.app_name) + "_" + DateUtils.getFileDateName());

            startActivityForResult(backup, BACKUP_REQUEST_CODE);
        } else if (key.equals(getString(R.string.preference_key_restore))) {
            Intent restore = new Intent(getActivity(), ActivityFilePicker.class);
            restore.putExtra(ActivityFilePicker.INTENT_EXTRA_FILE_EXTENSION, ".db");
            restore.putExtra(ActivityFilePicker.INTENT_EXTRA_MODE, ActivityFilePicker.MODE_PICK_FILE);

            startActivityForResult(restore, PICK_BACKUP_FILE_REQUEST_CODE);
        } else if (key.equals(getString(R.string.preference_key_export))) {
            Intent i = new Intent(getActivity(), ActivityFilePicker.class);
            i.putExtra(ActivityFilePicker.INTENT_EXTRA_FILE_EXTENSION, ".xls");
            i.putExtra(ActivityFilePicker.INTENT_EXTRA_MODE, ActivityFilePicker.MODE_CREATE_FILE);
            i.putExtra(ActivityFilePicker.INTENT_EXTRA_FILENAME, getString(R.string.app_name) + "_" + DateUtils.getFileDateName());

            startActivityForResult(i, EXPORT_REQUEST_CODE);
        } else if (key.equals(getString(R.string.preference_key_view_source_code))) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://github.com/Velli20/Tachograph"));
            startActivity(i);
        }


        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        final String key = preference.getKey();


        if (key.equals(getString(R.string.preference_key_use_gps))) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                int permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);

                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION:
                boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                mPrefs.edit().putBoolean(getString(R.string.preference_key_use_gps), permissionGranted).apply();
                ((CustomCheckBoxPreference) findPreference(getString(R.string.preference_key_use_gps))).setChecked(permissionGranted);

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String uri = null;
        if (data != null) {
            uri = data.getStringExtra(ActivityFilePicker.INTENT_EXTRA_FILEPATH);
        }
        if (requestCode == EXPORT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                File file = new File(uri);

                ExportEvents exportevents = new ExportEvents();
                exportevents.setFile(file);
                exportevents.write(getActivity().getApplicationContext());
                exportevents.setOnFileSavedListener(new OnFileSavedListener() {
                    @Override
                    public void onFileSaved(File file) {
                        if (file.exists()) {
                            createFileSavedNotification(getActivity(), file);
                        }
                    }
                });
            }
        } else if (requestCode == PICK_BACKUP_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                restore(new File(uri));
            }
        } else if (requestCode == BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                backup(new File(uri));
            }
        }
    }


    public void onRemoveAllLogs() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_title_remove_all_logs)
                .content(R.string.dialog_text_remove_all_logs)
                .theme(Theme.DARK)
                .positiveText(R.string.action_ok)
                .negativeText(R.string.action_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        getActivity().deleteDatabase(DataBaseHandler.DATABASE_NAME);
                        DataBaseHandler.getInstance().notifyCallbacks(DataBaseHandlerConstants.DATABASE_ACTION_DELETE_ALL_DATA, -1);
                        Toast.makeText(getActivity(), getActivity().getText(R.string.notification_all_events_deleted), Toast.LENGTH_LONG).show();

                    }
                })
                .show();

    }


    public void backup(File file) {
        DataBaseHandler db = DataBaseHandler.getInstance();

        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {

                final File currentDB = getActivity().getDatabasePath(db.getDatabaseName());


                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(file).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getActivity(), getActivity().getText(R.string.notification_log_backup_complete), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ignored) {
        }
    }


    public void restore(File fileToRestore) {

        final DataBaseHandler db = DataBaseHandler.getInstance();
        try {
            final File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                final File currentDB = getActivity().getDatabasePath(db.getDatabaseName());

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(fileToRestore).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getActivity(), getString(R.string.notification_log_restored), Toast.LENGTH_SHORT).show();
                    DataBaseHandler.getInstance().notifyCallbacks(0, -1);
                }
            }
        } catch (Exception ignored) {
        }
    }


    public static void createFileSavedNotification(Context c, File file) {
        if (file == null || c == null || !file.exists()) {
            return;
        }
        Intent intent = new Intent();
        int notificationId = 120;

        intent.setAction(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(c, "com.velli.tachograph.fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.ms-excel");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
        }


        PendingIntent resultPendingIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(c)
                .setContentTitle(c.getText(R.string.notification_file_saved))
                .setSmallIcon(R.drawable.ic_action_share)
                .setColor(c.getResources().getColor(R.color.color_primary))
                .setTicker(c.getText(R.string.notification_file_saved))
                .setContentIntent(resultPendingIntent);

        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId, notification.build());
    }


}

