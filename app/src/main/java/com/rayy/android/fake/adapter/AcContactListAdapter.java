package com.rayy.android.fake.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.rayy.android.fake.R;
import com.rayy.android.fake.model.PhoneContact;
import com.rayy.android.fake.util.MultiPathImageLoader;
import com.rayy.android.fake.widget.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yangw
 * @Date 15/10/15 11:14 PM.
 */
public class AcContactListAdapter extends BaseAdapter implements Filterable {
    private List<PhoneContact> contacts;
    private List<PhoneContact> phoneContacts;
    private Context context;

    public AcContactListAdapter(Context context, List<PhoneContact> contacts) {
        this.contacts = this.phoneContacts = contacts;
        this.context = context;
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int i) {
        return contacts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return Long.parseLong(contacts.get(i).contactId);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.contact_item, null);
        }

        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        TextView phoneView = (TextView) view.findViewById(R.id.contact_phone);
        CircleImageView avatarView = (CircleImageView) view.findViewById(R.id.contact_avatar);

        PhoneContact contact = (PhoneContact) getItem(i);

        nameView.setText(contact.name);
        phoneView.setText(contact.phone1);
        MultiPathImageLoader.loadImage(avatarView, contact.avatar);

        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults oReturn = new FilterResults();
                final List<PhoneContact> results = new ArrayList<PhoneContact>();

                if (charSequence != null) {
                    if (phoneContacts != null && phoneContacts.size() > 0) {
                        for (final PhoneContact contact : phoneContacts) {
                            if (contact.phone1.contains(charSequence) || contact.name.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                                results.add(contact);
                            }
                        }
                    }

                    oReturn.values = results;
                }

                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contacts = (ArrayList<PhoneContact>) filterResults.values;

                if (contacts == null) {
                    contacts = new ArrayList<PhoneContact>();
                }

                notifyDataSetChanged();
            }
        };
    }
}
