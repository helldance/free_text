/**
 * 
 */
package com.rayy.android.fake.diag;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

/**
 * @author RAY
 *
 */
public class DatePickerDialogFragment extends DialogFragment {
	private Fragment mFragment;
	private int mYear, mMonth, mDay;

	public DatePickerDialogFragment(){}
	
    public DatePickerDialogFragment(Fragment callback) {
        mFragment = callback;
    }

     public Dialog onCreateDialog(Bundle savedInstanceState) {
    	final Calendar c = Calendar.getInstance();
 		mYear = c.get(Calendar.YEAR);
 		mMonth = c.get(Calendar.MONTH);
 		mDay = c.get(Calendar.DAY_OF_MONTH);
         
 		return new DatePickerDialog(getActivity(), (OnDateSetListener) mFragment, mYear, mMonth, mDay);
     }
}
