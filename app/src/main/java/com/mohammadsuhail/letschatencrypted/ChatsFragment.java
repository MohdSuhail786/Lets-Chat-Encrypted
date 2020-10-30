package com.mohammadsuhail.letschatencrypted;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.nnHashmap;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.signedUser;


public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatAdapter listAdapter;
    private ValueEventListener valueEventListener;
    public ChatsFragment() {
    }

    static ArrayList<Contact> chatlist = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        getChatList(view);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initializeRecyclerView(View view) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView = view.findViewById(R.id.chatRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        listAdapter = new ChatAdapter(chatlist, getContext());
        recyclerView.setAdapter(listAdapter);
    }

    private void getChatList(View view) {
        initializeRecyclerView(view);
    }

    private void joinValueEventListener() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.i("xxxx","CALLED");
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Message msg = s.getValue(Message.class);
                        assert msg != null;
                        msg.setStatus("FROM");
                        Log.i("xxxx", "CHATSFRAGMENT");
                        String getName = nnHashmap.get(msg.getNumber());
                        if (getName == null) getName = msg.getNumber();
                        Log.i("xxxx", getName);
                        Log.i("xxxx", msg.getMessage());
                        Log.i("xxxx",msg.getNumber());
                        DatabaseHandler db = new DatabaseHandler(getContext());
                        db.deleteChat(new Contact(getName,msg.getNumber(),msg.getSenderimage(),0));
                        db.addChat(new Contact(getName,msg.getNumber(),msg.getSenderimage(),1));
                        db.addMessage(new Contact(getName,msg.getNumber(),msg.getSenderimage(),1),msg);
                        populateRecyclerView();

                    }
                    rootRef.child("Chats").child(signedUser.getNumber()).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child("Chats").child(signedUser.getNumber()).addValueEventListener(valueEventListener);
    }

    public void removeValueEventListener() {
        FirebaseDatabase.getInstance().getReference().child("Chats").child(signedUser.getNumber()).removeEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        removeValueEventListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        joinValueEventListener();
        populateRecyclerView();
    }

    private void populateRecyclerView() {
        DatabaseHandler db = new DatabaseHandler(getContext());
        chatlist = db.getAllChats();
        listAdapter = new ChatAdapter(chatlist,getContext());
        recyclerView.setAdapter(listAdapter);
    }
}