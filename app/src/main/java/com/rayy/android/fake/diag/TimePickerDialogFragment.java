/**
 * 
 */
package com.rayy.android.fake.diag;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

/**
 * @author RAY
 *
 */
public class TimePickerDialogFragment extends DialogFragment {
	private Fragment mFragment;
	private int mHour, mMin;
	
	public TimePickerDialogFragment  (){}

    public TimePickerDialogFragment(Fragment callback) {
        mFragment = callback;
    }

     public Dialog onCreateDialog(Bundle savedInstanceState) {
    	final Calendar c = Calendar.getInstance();
    	mHour = c.get(Calendar.HOUR_OF_DAY);
		mMin = c.get(Calendar.MINUTE);
         
 		return new TimePickerDialog(getActivity(), (OnTimeSetListener) mFragment, mHour, mMin, true);
     }
}
