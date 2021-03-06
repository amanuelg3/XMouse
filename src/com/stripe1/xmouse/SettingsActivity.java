package com.stripe1.xmouse;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.stripe1.xmouse.util.DatabaseHandler;
import com.stripe1.xmouse.util.FileDialog;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = true;
	final Context ctx = this;
	static int selectedHostIndex=0;
	CharSequence[] hostsNames;
	CharSequence[] hostsValues;
	ListPreference listPrefHost;
	ListPreference listPrefKeys;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			//getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		
		
		addPreferencesFromResource(R.xml.pref_general);
		// List preference under the category
		
		// Add 'data and sync' preferences, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_data_sync);
		getPreferenceScreen().addPreference(fakeHeader);
		
		ArrayList<ArrayList<String>> hosts = MainActivity.db.listAll(DatabaseHandler.HOST_TABLE_NAME,new String[]{"Alias","Host","Username","Port","Password","id"});
		hostsNames = new CharSequence[hosts.size()];
		hostsValues = new CharSequence[hosts.size()];
		
		for(int i=0;i<hosts.size();i++){
			
			String desc = hosts.get(i).get(0)+" ["+hosts.get(i).get(2)+"@"+hosts.get(i).get(1)+":"+hosts.get(i).get(3)+"] id="+hosts.get(i).get(5);
			String val = hosts.get(i).get(5);
			
			hostsNames[i]=desc;
			hostsValues[i]=val;
			//Log.d("setting", "-"+hosts.get(i).get(0)+"-");
		}
		//Log.d("setting", "-"+hosts.size()+"-");
		if(hosts.size()!=0){
			listPrefHost = new ListPreference(this);
			listPrefHost.setKey("hostPreferenceList"); //Refer to get the pref value
			listPrefHost.setDialogTitle("Select Host"); 
			listPrefHost.setTitle("Host Computer");
	        //listPref.setSummary("Select Host");
			listPrefHost.setEntries(hostsNames);
			listPrefHost.setEntryValues(hostsValues);
			listPrefHost.setDefaultValue(hostsValues[0]);
	        
	        getPreferenceScreen().addPreference(listPrefHost);
	        
	        bindPreferenceSummaryToValue(findPreference("hostPreferenceList"));
	        
	        Preference pref = new Preference(this);
			pref.setTitle("- Remove selected Host");
	        pref.setSummary("");
	        pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

			         builder.setMessage("Really delete Host?\n\n"+hostsNames[selectedHostIndex]);
			         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {   }
				         });	
			         builder.setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {

			           public void onClick(DialogInterface dialog, int id) {
			        	   
			        	   if(hostsValues[selectedHostIndex].toString().equals("")){
			        		   
			        		   Toast.makeText(getApplicationContext(), "Nothing happened", Toast.LENGTH_SHORT).show();
			        	   }else{
				        	   int r = Integer.valueOf(hostsValues[selectedHostIndex].toString());
				        	   
				        	   int rowsAffected = MainActivity.db.deleteRow(DatabaseHandler.HOST_TABLE_NAME,r);
				        	   
				        	   Toast.makeText(getApplicationContext(), rowsAffected+" Record(s) affected", Toast.LENGTH_SHORT).show();
				        	   
				        	   hostsNames[selectedHostIndex]="Recently Deleted";
				        	   hostsValues[selectedHostIndex]="";
				        	   
				        	   listPrefHost.setEntries(hostsNames);
				        	   listPrefHost.setEntryValues(hostsValues);
				        	   listPrefHost.setSummary("Select Host");
			        	   }
			           }

			         });

			         builder.show();
					return false;
				}
	        	
	        });
	        
	        getPreferenceScreen().addPreference(pref);
	        
		}
		Preference pref = new Preference(this);
		pref.setTitle("+ Add new Host");
        pref.setSummary("");
        Intent newHostActivity = new Intent(SettingsActivity.this,NewHostSettingsActivity.class);
        pref.setIntent(newHostActivity);
        getPreferenceScreen().addPreference(pref);
	        
		
        fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle("Authentication");
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_auth);
		//addPreferencesFromResource(R.xml.ssh_conn);
		final Preference myPref = (Preference) findPreference("pref_addkeybutton");
		
		
		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
             public boolean onPreferenceClick(Preference preference) {
                 //open browser or intent here
            	 
            	 //File mPath = new File(Environment.getExternalStorageDirectory(), null);
            	 FileDialog fileDialog = new FileDialog(SettingsActivity.this, null);
                 //fileDialog.setFileEndsWith(".txt");
            	 //fileDialog.setSelectDirectoryOption(true);
                 fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                     public void fileSelected(File file) {
                         Log.d(getClass().getName(), "selected file " + file.toString());
                         //SharedPreferences settings = getSharedPreferences(String n, MODE_PRIVATE);
                         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                         SharedPreferences.Editor editor = settings.edit();
                         editor.putString("pref_addkeybutton", file.toString());
                         editor.commit();
                         myPref.setSummary(file.toString());
                     }
                 });
                 //fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
                 //  public void directorySelected(File directory) {
                 //      Log.d(getClass().getName(), "selected dir " + directory.toString());
                 //  }
                 //});
                 //fileDialog.setSelectDirectoryOption(false);
                 fileDialog.showDialog();
            	 
            	 return true;
             }
         });
		
		//listPrefKeys = (ListPreference)this.findPreference("pref_usekeyauth_list");
		//listPrefKeys = new ListPreference(this);
		//listPrefKeys.setKey("pref_usekeyauth_list"); //Refer to get the pref value
		//listPrefKeys.setDependency("pref_usekeyauth");
		//listPrefKeys.setDialogTitle("Select Key"); 
		//listPrefKeys.setTitle("Selected Key");
		//listPrefKeys.setEnabled(false);
		
		// Add 'notifications' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_notifications);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_notification);
		
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle("Keyboard");
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_keyboard);
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		
		
		
		//bindPreferenceSummaryToValue(findPreference("setting_pass"));
		//bindPreferenceSummaryToValue(findPreference("autologin_checkbox"));
		bindPreferenceSummaryToValue(findPreference("pref_addkeybutton"));
		bindPreferenceSummaryToValue(findPreference("sensitivity_list"));
		bindPreferenceSummaryToValue(findPreference("setting_xdotool_initial"));
		
		//bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
		//bindPreferenceSummaryToValue(findPreference("sync_frequency"));
		
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				
				if(preference.getKey().equals("hostPreferenceList")){
				
					selectedHostIndex = index;
					//Log.d("onPreferenceChange","new index"+index);
				}
				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index]: null);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("example_text"));
			bindPreferenceSummaryToValue(findPreference("example_list"));
		}
	}

	/**
	 * This fragment shows notification preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends
			PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
		}
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DataSyncPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.ssh_conn);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("sync_frequency"));
		}
	}
}
