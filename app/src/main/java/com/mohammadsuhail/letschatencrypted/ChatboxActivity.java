package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadsuhail.letschatencrypted.SplashActivity.nnHashmap;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.signedUser;

public class ChatboxActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private ImageButton sendBtn;
    private EditText message;
    private RecyclerView messageRecyclerView;
    private MessageListAdapter messageListAdapter;
    private ValueEventListener valueEventListener;
    public static ArrayList<Message> messageList = new ArrayList<>();
    private DatabaseReference root;
    private RequestQueue requestQueue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private CircleImageView profileimg;
    private TextView username;
    private Contact receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_chat_box);
        this.overridePendingTransition(R.anim.enter_activity, R.anim.exit_activity);
        FirebaseMessaging.getInstance().subscribeToTopic(signedUser.getNumber().substring(2));

        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_screen_background));

        addToolbar();

        createHooks();

        updateReceiverInfo();

        addRecentMessagesToScreen();

        sendBtn.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                String msg = message.getText().toString();
                message.setText("");
                String currentTime = getCurrentTime();
                if (!msg.isEmpty()) {

                    Message simpleMessage = new Message(msg, currentTime, "TO", signedUser.getNumber(), signedUser.getImage());

                    try {
                        msg = AES.encrypt(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Message encryptedMessage = new Message(msg, currentTime, "TO", signedUser.getNumber(), signedUser.getImage());
                    db.addMessage(receiver, encryptedMessage);

                    addToTop(receiver);
                    messageList.add(simpleMessage);
                    messageListAdapter.notifyDataSetChanged();
                    messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);

                    sendMessageToFirebase(encryptedMessage);

                    try {
                        sendNotificationToUser(receiver.getNumber(), encryptedMessage.getMessage());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ChatboxActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(ChatboxActivity.this, "Please connect to internet", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void sendNotificationToUser(String number, String message) throws JSONException {
        JSONObject mainObj = new JSONObject();
        mainObj.put("to", "/topics/" + number.substring(2));
        JSONObject notificationObj = new JSONObject();
        notificationObj.put("title", signedUser.getName());
        notificationObj.put("body", message);
        mainObj.put("notification", notificationObj);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, mainObj, response ->
        {
        }, error -> Toast.makeText(ChatboxActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                header.put("content-type", "application/json");
                header.put("authorization", "key=AAAAPDK3g7E:APA91bHJ5lWCTQJ7QEMtLvoeekA5f12dbIqJb4WPQC36eW0IVx4smXEa6LmbIgMfpfi5xRtZudvahUBOX3pXhAI13TQK64laqsXHHW-nZeesaxVVD6r_j8vNB6kF-Gth9EdGLssdnoKh");
                return header;
            }
        };
        requestQueue.add(request);
    }

    private void sendMessageToFirebase(Message encryptedMessage) {
        root.child("Chats").child(receiver.getNumber()).push().setValue(encryptedMessage);
    }

    private void addRecentMessagesToScreen() {
        messageList = db.getMessages(receiver);
        initializeRecyclerView();
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
    }

    private void addToTop(Contact contact) {
        db.deleteChat(contact);
        db.addChat(contact);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chatbox_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        if (item.getItemId() == R.id.logoutOptions) {
            FirebaseAuth.getInstance().signOut();
            DatabaseHandler db = new DatabaseHandler(ChatboxActivity.this);
            db.dropTables();
            startActivity(new Intent(ChatboxActivity.this, LoginActivity.class));
            finish();
        }
        if (item.getItemId() == R.id.aboutOption) {
            Snackbar.make(getCurrentFocus(), "Developed and maintained by MSAF", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        if (item.getItemId() == R.id.profileOption) {
            startActivity(new Intent(ChatboxActivity.this, ProfileActivity.class));
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void addToolbar() {
        Toolbar toolbar = findViewById(R.id.chatboxtoolbar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0, 4, 0, 4);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
    }

    private void createHooks() {
        profileimg = findViewById(R.id.chatboxProfileid);
        username = findViewById(R.id.chatboxusernameid);
        receiver = new Contact(getIntent().getStringExtra("name"), getIntent().getStringExtra("number"),
                getIntent().getStringExtra("profileurl"), 0);
        requestQueue = Volley.newRequestQueue(this);
        root = FirebaseDatabase.getInstance().getReference();
        db = new DatabaseHandler(this);

        sendBtn = findViewById(R.id.sendButton);
        message = findViewById(R.id.editText);
        messageRecyclerView = findViewById(R.id.recyclerviewMessageList);
    }

    private void updateReceiverInfo() {
        if (receiver.getImage() == null)
            profileimg.setImageResource(R.drawable.ic_baseline_account_circle_24_white);
        else
            Glide.with(this).load(Uri.parse(receiver.getImage())).into(profileimg);
        username.setText(receiver.getName());
    }

    private void initializeRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageListAdapter = new MessageListAdapter(this, messageList);
        messageRecyclerView.setAdapter(messageListAdapter);
    }

    private void joinValueEventListener() {
        Log.i("xxxx", "JOIN VALUEEVENT LISTENER");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.i("xxxx", "CALLED");
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Message msg = s.getValue(Message.class);
                        assert msg != null;
                        msg.setStatus("FROM");
                        Log.i("xxxx", "CHATBOXACTIVITY");
                        String getName = nnHashmap.get(msg.getNumber());
                        if (getName == null) getName = msg.getNumber();
                        Log.i("xxxx", getName);
                        Log.i("xxxx", msg.getMessage());
                        Log.i("xxxx", msg.getNumber());
                        DatabaseHandler db = new DatabaseHandler(ChatboxActivity.this);
                        db.deleteChat(new Contact(getName, msg.getNumber(), msg.getSenderimage(), 0));


                        if (receiver.getNumber().equals(msg.getNumber())) {
                            db.addChat(new Contact(getName, msg.getNumber(), msg.getSenderimage(), 0));
                            db.addMessage(new Contact(getName, msg.getNumber(), msg.getSenderimage(), 0), msg);
                            try {
                                msg.setMessage(AES.decrypt(msg.getMessage()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            messageList.add(msg);
                            messageListAdapter.notifyDataSetChanged();
                            messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                        } else {
                            db.addChat(new Contact(getName, msg.getNumber(), msg.getSenderimage(), 1));
                            db.addMessage(new Contact(getName, msg.getNumber(), msg.getSenderimage(), 1), msg);
                        }
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
    }

}