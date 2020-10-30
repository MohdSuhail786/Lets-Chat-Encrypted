package com.mohammadsuhail.letschatencrypted;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {
    private ArrayList<Contact> contactsList;
    private Context context;

    // Counstructor for the Class
    public ContactAdapter(ArrayList<Contact> contactsList, Context context) {
        this.contactsList = contactsList;
        this.context = context;
    }

    // This method creates views for the RecyclerView by inflating the layout
    // Into the viewHolders which helps to display the items in the RecyclerView
    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        // Inflate the layout view you have created for the list rows here
        View view = layoutInflater.inflate(R.layout.contact_item_view, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public int getItemCount() {
        return contactsList == null ? 0 : contactsList.size();
    }

    // This method is called when binding the data to the views being created in RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, final int position) {
        final Contact contact = contactsList.get(position);

        // Set the data to the views here
        holder.setContactName(contact.getName());
        holder.setContactNumber(contact.getNumber());
        if (!contact.getImage().equals("nil")) {
            holder.setProfile(contact.getImage());
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatboxActivity.class);
                intent.putExtra("name", contact.getName());
                intent.putExtra("number", contact.getNumber());
                intent.putExtra("profileurl", contact.getImage());
                view.getContext().startActivity(intent);
            }

        });
        // You can set click listners to indvidual items in the viewholder here
        // make sure you pass down the listner or make the Data members of the viewHolder public
    }

    // This is your ViewHolder class that helps to populate data to the view
    public class ContactHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private TextView txtNumber;
        private CircleImageView profile;

        public ContactHolder(View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txt_name);
            txtNumber = itemView.findViewById(R.id.txt_number);
            profile = itemView.findViewById(R.id.profileID);
        }

        public void setContactName(String name) {
            txtName.setText(name);
        }

        public void setContactNumber(String number) {
            txtNumber.setText(number);
        }

        public void setProfile(String url) {
            if (!url.equals("")) {
                Glide.with(context).load(Uri.parse(url)).into(profile);
//                Picasso.get().load(url).into(profile);
            }
//                profile.setImageResource(R.drawable.ic_baseline_account_circle_24);
        }
    }
}
