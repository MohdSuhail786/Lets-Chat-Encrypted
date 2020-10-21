package com.mohammadsuhail.letschatencrypted;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.mohammadsuhail.letschatencrypted.SplashActivity.contactsList;


public class ContactsFragment extends Fragment {


    private ProgressBar progressBar;
    private ContactAdapter listAdapter;
    private static ArrayList<Contact> contacts = new ArrayList<>();
    private RecyclerView recyclerView;

    public ContactsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressBar = getActivity().findViewById(R.id.toolbar_progress_bar);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (contacts.size() == 0)progressBar.setVisibility(View.VISIBLE);
            recyclerView = getActivity().findViewById(R.id.myRecyclerView);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            listAdapter = new ContactAdapter(contacts, getActivity().getApplicationContext());
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
            recyclerView.setAdapter(listAdapter);
            if (contacts.size() == 0) {
                if (isNetworkAvailable())
                loadContacts();
                else{
                    Toast.makeText(getContext(), "Please connect to internet and restart your app", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

        }
    }

    private void loadContacts() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        for (Contact c : contactsList) {
            String number = c.getNumber();
            String name = c.getName();
            if (isValid(number)) {

                if (number.equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                    continue;
                final String finalNumber = number;
                final String finalName = name;
                root.child("Users").child(finalNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            contacts.add(new Contact(finalName, finalNumber));
                            listAdapter.notifyDataSetChanged();
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    boolean isValid(String number) {
        return number != null && !number.contains(".") && !number.contains("#") && !number.contains("$") && !number.contains("[") && !number.contains("]");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}