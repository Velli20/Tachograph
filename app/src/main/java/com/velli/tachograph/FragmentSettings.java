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

package com.velli.tachograph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.prefs.Preferences;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.velli.tachograph.ExportEvents.OnFileSavedListener;
import com.velli.tachograph.filepicker.ActivityFilePicker;
import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.preferences.CustomCheckBoxPreference;
import com.velli.tachograph.preferences.CustomPreference;
import com.velli.tachograph.preferences.CustomRingtonePreference;
import com.velli.tachograph.preferences.PreferenceFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


public class FragmentSettings extends PreferenceFragment implements OnFileSavedListener, OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 0;
    public static final int EXPORT_REQUEST_CODE = 43;
    public static final int PICK_BACKUP_FILE_REQUEST_CODE = 47;
    public static final int BACKUP_REQUEST_CODE = 49;

	public static final String TAG = "SettingsFragment ";

    private CustomPreference mNotificationsVibrate;
    private CustomRingtonePreference mNotificationsSound;
    private CustomPreference mNotificationsDrive;
    private CustomPreference mNotificationsBreak;
    private CustomPreference mNotificationsDailyrest;
    private CustomPreference mNotificationsWeeklyrest;
    private SharedPreferences mPrefs;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		final Resources res = getActivity().getResources();

		final Preference mRemoveLogsPref = findPreference(res.getString(R.string.preference_key_remove_all_logs));
		final Preference mBackupPref = findPreference(res.getString(R.string.preference_key_backup));
		final Preference mRestorePref = findPreference(res.getString(R.string.preference_key_restore));
		final Preference mExportPref = findPreference(res.getString(R.string.preference_key_export));
		final Preference locationPref = findPreference(res.getString(R.string.preference_key_use_gps));

		mNotificationsVibrate = (CustomPreference) findPreference(res.getString(R.string.preference_key_notification_vibrate));
		mNotificationsSound = (CustomRingtonePreference) findPreference(res.getString(R.string.preference_key_notification_sound));
		mNotificationsBreak = (CustomPreference) findPreference(res.getString(R.string.preference_key_alarm_break_time_ending));
		mNotificationsDailyrest = (CustomPreference) findPreference(res.getString(R.string.preference_key_alarm_daily_rest_ending));
		mNotificationsDrive = (CustomPreference) findPreference(res.getString(R.string.preference_key_alarm_drive_time_ending));
		mNotificationsWeeklyrest = (CustomPreference) findPreference(res.getString(R.string.preference_key_alarm_weekly_rest_ending));

		mRemoveLogsPref.setOnPreferenceClickListener(this);
		mBackupPref.setOnPreferenceClickListener(this);
		mRestorePref.setOnPreferenceClickListener(this);
		mExportPref.setOnPreferenceClickListener(this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
			locationPref.setOnPreferenceChangeListener(this);
            int permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

            if(permission != PackageManager.PERMISSION_GRANTED && mPrefs.getBoolean(getString(R.string.preference_key_use_gps), false)) {
                mPrefs.edit().putBoolean(getString(R.string.preference_key_use_gps), false).apply();
                ((CustomCheckBoxPreference)findPreference(getString(R.string.preference_key_use_gps))).setChecked(false);
            }

		}


		setNotificationPreferencesEnabled(mPrefs.getBoolean(getResources().getString(R.string.preference_key_show_notifications), true));

		setSoundPrefSummary();
	}
    @Override
	public void onResume(){
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause(){
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if(key.equals(getString(R.string.preference_key_remove_all_logs))) {
            onRemoveAllLogs();
        } else if(key.equals(getString(R.string.preference_key_backup))) {
            Intent backup = new Intent(getActivity(), ActivityFilePicker.class);
            backup.putExtra(ActivityFilePicker.INTENT_EXTRA_FILE_EXTENSION, ".db");
            backup.putExtra(ActivityFilePicker.INTENT_EXTRA_MODE, ActivityFilePicker.MODE_CREATE_FILE);
            backup.putExtra(ActivityFilePicker.INTENT_EXTRA_FILENAME, getString(R.string.app_name) + "_" + DateCalculations.getFileDateName());

            startActivityForResult(backup, BACKUP_REQUEST_CODE);
        } else if(key.equals(getString(R.string.preference_key_restore))) {
            Intent restore = new Intent(getActivity(), ActivityFilePicker.class);
            restore.putExtra(ActivityFilePicker.INTENT_EXTRA_FILE_EXTENSION, ".db");
            restore.putExtra(ActivityFilePicker.INTENT_EXTRA_MODE, ActivityFilePicker.MODE_PICK_FILE);

            startActivityForResult(restore, PICK_BACKUP_FILE_REQUEST_CODE);
        } else if(key.equals(getString(R.string.preference_key_export))) {
            Intent i = new Intent(getActivity(), ActivityFilePicker.class);
            i.putExtra(ActivityFilePicker.INTENT_EXTRA_FILE_EXTENSION, ".xls");
            i.putExtra(ActivityFilePicker.INTENT_EXTRA_MODE, ActivityFilePicker.MODE_CREATE_FILE);
            i.putExtra(ActivityFilePicker.INTENT_EXTRA_FILENAME, getString(R.string.app_name));

            startActivityForResult(i, EXPORT_REQUEST_CODE);
        }


		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object o) {
		final String key = preference.getKey();


		if(key.equals(getString(R.string.preference_key_use_gps))) {
			int permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

			if (permission != PackageManager.PERMISSION_GRANTED) {

				requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                return false;
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
                ((CustomCheckBoxPreference)findPreference(getString(R.string.preference_key_use_gps))).setChecked(permissionGranted);

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
        if(data != null) {
            uri = data.getStringExtra(ActivityFilePicker.INTENT_EXTRA_FILEPATH);
        }
		if(requestCode == EXPORT_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			if(uri != null){
				File file = new File(uri);
				
				ExportEvents exportevents = new ExportEvents();
				exportevents.setFile(file);
                exportevents.setIncludeAutomaticallyCalculatedRestingEvents(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(getResources().getString(R.string.preference_key_count_daily_and_weekly_rest_automatically), false));
				exportevents.setOnFileSavedListener(this);
			    exportevents.write(getActivity().getApplicationContext());
			}
		} else if(requestCode == PICK_BACKUP_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(uri != null) {
                restore(new File(uri));
            }
        } else if(requestCode == BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(uri != null) {
                backup(new File(uri));
            }
        }
	}


    public void onRemoveAllLogs() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.action_title_remove_all_logs)
                .content(R.string.dialog_text_remove_all_logs)
                .theme(Theme.DARK)
                .positiveText(R.string.action_ok)
                .negativeText(R.string.action_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        getActivity().deleteDatabase(DataBaseHandler.DATABASE_NAME);
                        DataBaseHandler.getInstance().notifyCallbacks(DataBaseHandler.ACTION_DELETE_EVENT, -1);
                        Toast.makeText(getActivity(), getActivity().getText(R.string.action_all_files_removed), Toast.LENGTH_LONG).show();

                    }
                })
                .show();

    }

	
	public void backup(File file){
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
                    Toast.makeText(getActivity(), getActivity().getText(R.string.action_database_backup_complete), Toast.LENGTH_SHORT).show();
	            }
	        }
	    } catch (Exception e) {
	    }
	}
	

	public void restore(File fileToRestore){
		
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
		            Toast.makeText(getActivity(), getString(R.string.action_database_restore_complete), Toast.LENGTH_SHORT).show();
		            DataBaseHandler.getInstance().notifyCallbacks(0, -1);
		        }
		    }
		} catch (Exception e) {
		}
	}
	

	
	

	@Override
	public void onFileSaved(File file) {
		if(file.exists()){
			createFileSavedNotification(getActivity(), Uri.fromFile(file));
		}
	}


	
	public static void createFileSavedNotification(Context c, Uri fileuri){
		Intent intent = new Intent();
		
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, fileuri);
		intent.setType("application/vnd.ms-excel");
		
		PendingIntent resultPendingIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder noti = new NotificationCompat.Builder(c)
				.setContentTitle(c.getText(R.string.action_file_saved))
				.setSmallIcon(R.drawable.ic_action_share)
				.setColor(c.getResources().getColor(R.color.color_primary_400))
				.setTicker(c.getText(R.string.action_file_saved))
				.setContentIntent(resultPendingIntent);
		
		int mNotificationId = 120;
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr =  (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(mNotificationId, noti.build());
	}




	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		setSoundPrefSummary();
		setNotificationPreferencesEnabled(sharedPreferences.getBoolean(getResources().getString(R.string.preference_key_show_notifications), true));
	}
	

	private void setSoundPrefSummary() {
		if (mNotificationsSound != null) {
			Uri ringtoneUri = Uri.parse(mPrefs.getString(mNotificationsSound.getKey(), ""));
			Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
			if (mNotificationsSound != null && ringtone != null) {
				mNotificationsSound.setSummary(ringtone.getTitle(getActivity()));
			}

		}
	}
	
	private void setNotificationPreferencesEnabled(boolean enabled){
		mNotificationsVibrate.setEnabled(enabled);
		if(mNotificationsSound != null){
			mNotificationsSound.setEnabled(enabled);
		}
		mNotificationsBreak.setEnabled(enabled);
		mNotificationsDailyrest.setEnabled(enabled);
		mNotificationsDrive.setEnabled(enabled);
		mNotificationsWeeklyrest.setEnabled(enabled);
	}


}

