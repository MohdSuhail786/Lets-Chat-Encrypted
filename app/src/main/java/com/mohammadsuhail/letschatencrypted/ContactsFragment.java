package com.mohammadsuhail.letschatencrypted;

import android.database.Cursor;
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

import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONTACTS_LOADER_ID = 1;
    Map<String, Boolean> namePhoneMap = new HashMap<String, Boolean>();
    private MyAdapter listAdapter;
    private ArrayList<Contact> contactsList = new ArrayList<>();
    private RecyclerView recyclerView;

    public ContactsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == CONTACTS_LOADER_ID) {
            return contactsLoader();
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        recyclerView = getActivity().findViewById(R.id.myRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        listAdapter = new MyAdapter(contactsList,getActivity().getApplicationContext());
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(listAdapter);

        contactsFromCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    private  Loader<Cursor> contactsLoader() {
        Uri contactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // The content URI of the phone contacts
        String selection = null;                                 //Selection criteria
        String[] selectionArgs = {};                             //Selection criteria
        String sortOrder = null;                                 //The sort order for the returned rows

        return new CursorLoader(Objects.requireNonNull(getActivity()).getApplicationContext(), contactsUri, null, selection, selectionArgs, sortOrder);
    }

    private void contactsFromCursor(final Cursor cursor) {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                 if(isValid(number)) {
                     number = number.replaceAll("\\s","");
                     final String finalNumber = number;
                     final String finalName = name;
                     root.child("Users").child(finalNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null) {
//                                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), finalNumber, Toast.LENGTH_SHORT).show();
                                if(!namePhoneMap.containsKey(finalNumber)){
                                    contactsList.add(new Contact(finalName,finalNumber));
                                    listAdapter.notifyDataSetChanged();
                                }
                                namePhoneMap.put(finalNumber,true);
                            }
                         }
                         @Override
                         public void onCancelled(@NonNull DatabaseError error) {

                         }

                     });
                 }
            } while (cursor.moveToNext());
        }
    }
    boolean isValid(String number) {
        return number!=null && !number.contains(".") && !number.contains("#") && !number.contains("$") && !number.contains("[") && !number.contains("]");
    }
}