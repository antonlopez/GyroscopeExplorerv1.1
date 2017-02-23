package com.kircherelectronics.bluetoothgyro.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.SwitchPreference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.kircherelectronics.com.gyroscopeexplorer.R;

/*
 * Gyroscope Explorer
 * Copyright (C) 2013-2015, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Configuration activity.
 */
public class ConfigActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener
{

	// Preference keys for smoothing filters
	public static final String MEAN_FILTER_SMOOTHING_ENABLED_KEY = "mean_filter_smoothing_enabled_preference";
	public static final String MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY = "mean_filter_smoothing_time_constant_preference";
	
	public static final String CALIBRATED_GYROSCOPE_ENABLED_KEY = "calibrated_gyroscope_preference";

	public static final String IMUOCF_ORIENTATION_ENABLED_KEY = "imuocf_orienation_enabled_preference";
	public static final String IMUOCF_ORIENTATION_COEFF_KEY = "imuocf_orienation_coeff_preference";

	public static final String IMUOCF_ROTATION_MATRIX_ENABLED_KEY = "imuocf_rotation_matrix_enabled_preference";
	public static final String IMUOCF_ROTATION_MATRIX_COEFF_KEY = "imuocf_rotation_matrix_coeff_preference";

	public static final String IMUOCF_QUATERNION_ENABLED_KEY = "imuocf_quaternion_enabled_preference";
	public static final String IMUOCF_QUATERNION_COEFF_KEY = "imuocf_quaternion_coeff_preference";

	public static final String IMUOKF_QUATERNION_ENABLED_KEY = "imuokf_quaternion_enabled_preference";

	private SwitchPreference spImuOCfOrientation;
	private SwitchPreference spImuOCfRotationMatrix;
	private SwitchPreference spImuOCfQuaternion;
	private SwitchPreference spImuOKfQuaternion;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/*
		 * Read preferences resources available at res/xml/preferences.xml
		 */
		addPreferencesFromResource(R.xml.preferences);

		spImuOCfOrientation = (SwitchPreference) findPreference(IMUOCF_ORIENTATION_ENABLED_KEY);

		spImuOCfRotationMatrix = (SwitchPreference) findPreference(IMUOCF_ROTATION_MATRIX_ENABLED_KEY);

		spImuOCfQuaternion = (SwitchPreference) findPreference(IMUOCF_QUATERNION_ENABLED_KEY);

		spImuOKfQuaternion = (SwitchPreference) findPreference(IMUOKF_QUATERNION_ENABLED_KEY);

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{

		if (key.equals(IMUOCF_ORIENTATION_ENABLED_KEY))
		{
			if (sharedPreferences.getBoolean(key, false))
			{
				Editor edit = sharedPreferences.edit();

				edit.putBoolean(IMUOCF_ROTATION_MATRIX_ENABLED_KEY, false);
				edit.putBoolean(IMUOCF_QUATERNION_ENABLED_KEY, false);
				edit.putBoolean(IMUOKF_QUATERNION_ENABLED_KEY, false);

				edit.apply();

				spImuOCfRotationMatrix.setChecked(false);
				spImuOCfQuaternion.setChecked(false);
				spImuOKfQuaternion.setChecked(false);
			}
		}

		if (key.equals(IMUOCF_ROTATION_MATRIX_ENABLED_KEY))
		{
			if (sharedPreferences.getBoolean(key, false))
			{
				Editor edit = sharedPreferences.edit();

				edit.putBoolean(IMUOCF_ORIENTATION_ENABLED_KEY, false);
				edit.putBoolean(IMUOCF_QUATERNION_ENABLED_KEY, false);
				edit.putBoolean(IMUOKF_QUATERNION_ENABLED_KEY, false);

				edit.apply();

				spImuOCfOrientation.setChecked(false);
				spImuOCfQuaternion.setChecked(false);
				spImuOKfQuaternion.setChecked(false);

			}
		}

		if (key.equals(IMUOKF_QUATERNION_ENABLED_KEY))
		{
			if (sharedPreferences.getBoolean(key, false))
			{
				Editor edit = sharedPreferences.edit();

				edit.putBoolean(IMUOCF_ORIENTATION_ENABLED_KEY, false);
				edit.putBoolean(IMUOCF_ROTATION_MATRIX_ENABLED_KEY, false);
				edit.putBoolean(IMUOCF_QUATERNION_ENABLED_KEY, false);

				edit.apply();

				spImuOCfOrientation.setChecked(false);
				spImuOCfRotationMatrix.setChecked(false);
				spImuOCfQuaternion.setChecked(false);
			}
		}

		if (key.equals(IMUOCF_QUATERNION_ENABLED_KEY))
		{
			if (sharedPreferences.getBoolean(key, false))
			{
				Editor edit = sharedPreferences.edit();

				edit.putBoolean(IMUOCF_ORIENTATION_ENABLED_KEY, false);
				edit.putBoolean(IMUOCF_ROTATION_MATRIX_ENABLED_KEY, false);
				edit.putBoolean(IMUOKF_QUATERNION_ENABLED_KEY, false);

				edit.apply();

				spImuOCfOrientation.setChecked(false);
				spImuOCfRotationMatrix.setChecked(false);
				spImuOKfQuaternion.setChecked(false);
			}
		}

		if (key.equals(IMUOCF_ORIENTATION_COEFF_KEY))
		{
			if (Double.valueOf(sharedPreferences.getString(key, "0.5")) > 1)
			{
				sharedPreferences.edit().putString(key, "0.5").apply();

				((EditTextPreference) findPreference(IMUOCF_ORIENTATION_COEFF_KEY))
						.setText("0.5");

				Toast.makeText(
						getApplicationContext(),
						"Whoa! The filter constant must be less than or equal to 1",
						Toast.LENGTH_LONG).show();
			}
		}

		if (key.equals(IMUOCF_ROTATION_MATRIX_COEFF_KEY))
		{
			if (Double.valueOf(sharedPreferences.getString(key, "0.5")) > 1)
			{
				sharedPreferences.edit().putString(key, "0.5").apply();

				((EditTextPreference) findPreference(IMUOCF_ROTATION_MATRIX_COEFF_KEY))
						.setText("0.5");

				Toast.makeText(
						getApplicationContext(),
						"Whoa! The filter constant must be less than or equal to 1",
						Toast.LENGTH_LONG).show();
			}
		}

		if (key.equals(IMUOCF_QUATERNION_COEFF_KEY))
		{
			if (Double.valueOf(sharedPreferences.getString(key, "0.5")) > 1)
			{
				sharedPreferences.edit().putString(key, "0.5").apply();

				((EditTextPreference) findPreference(IMUOCF_QUATERNION_COEFF_KEY))
						.setText("0.5");

				Toast.makeText(
						getApplicationContext(),
						"Whoa! The filter constant must be less than or equal to 1",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}