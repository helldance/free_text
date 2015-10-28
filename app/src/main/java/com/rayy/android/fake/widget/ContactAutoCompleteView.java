package com.rayy.android.fake.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.rayy.android.fake.model.PhoneContact;

/**
 * @Author yangw
 * @Date 15/10/15 11:22 PM.
 */
public class ContactAutoCompleteView extends AutoCompleteTextView {
    public ContactAutoCompleteView(Context context) {
        super(context);
        init();
    }

    public ContactAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContactAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnItemClickListener(onItemClickListener);
        setThreshold(1);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            PhoneContact selectedContact = (PhoneContact)getAdapter().getItem(i);
            setText(selectedContact.name + " " + selectedContact.phone1);
            setSelection(getText().length());
        }
    };
}
