package com.mohammadsuhail.letschatencrypted;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.internalContactList;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.signedUser;

public class ContactsFragment extends Fragment {

    private static ProgressBar progressBar;
    private static ContactAdapter listAdapter;
    public static ArrayList<Contact> contacts = new ArrayList<>();
    private static RecyclerView recyclerView;

    public ContactsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressBar = getActivity().findViewById(R.id.toolbar_progress_bar);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)  {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (contacts.size() == 0) {
                progressBar.setVisibility(View.VISIBLE);
                if (isNetworkAvailable())
                    loadContacts();
                else {
                    Toast.makeText(getContext(), "Please connect to internet and restart your app", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

        }
    }

    static void loadContacts() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        for (Contact c : internalContactList) {
            String number = c.getNumber();
            String name = c.getName();

            if (isValid(number)) {
                if (number.equals(signedUser.getNumber()))
                    continue;
                final String finalNumber = number;
                final String finalName = name;
                root.child("Users").child(finalNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        Toast.makeText(getContext(), "Getting images", Toast.LENGTH_SHORT).show();
                        if (snapshot.getValue() != null) {
                            Contact contact;
                            if (snapshot.getChildrenCount() == 2) {

                                contact = snapshot.getValue(Contact.class);

                                contacts.add(new Contact(finalName, finalNumber, contact.getImage(),0));
                            } else {
                                contacts.add(new Contact(finalName, finalNumber));
                            }
                            listAdapter.notifyDataSetChanged();
                            if (progressBar.getVisibility() == View.VISIBLE)
                                progressBar.setVisibility(View.GONE);
                        }
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
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        initializeRecyclerView(view);
        return view;
    }

    static boolean isValid(String number) {
        return number != null && !number.contains(".") && !number.contains("#") && !number.contains("$") && !number.contains("[") && !number.contains("]");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void initializeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.myRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        listAdapter = new ContactAdapter(contacts, getActivity());
//            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(listAdapter);
    }

}