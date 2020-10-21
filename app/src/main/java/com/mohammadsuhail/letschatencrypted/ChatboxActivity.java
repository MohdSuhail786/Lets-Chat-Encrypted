package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;


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

import static com.mohammadsuhail.letschatencrypted.MainActivity.unreadChat;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.nnHashmap;

public class ChatboxActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private Chat currentChat;
    private ImageButton sendBtn;
    private EditText message;
    private RecyclerView messageRecyclerView;
    private MessageListAdapter messageListAdapter;
    private static ArrayList<Message> messageList = new ArrayList<>();
    private DatabaseReference root;
    private FirebaseUser user;
    private ValueEventListener valueEventListener;
    private RequestQueue requestQueue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private CircleImageView profileimg;
    private TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Toolbar toolbar = findViewById(R.id.chatboxtoolbar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0, 4, 0, 4);
        profileimg = findViewById(R.id.chatboxProfileid);
        username = findViewById(R.id.chatboxusernameid);
        String urlofimage = getIntent().getStringExtra("profileurl");
        if (urlofimage == null) profileimg.setImageResource(R.drawable.ic_baseline_account_circle_24_white);
        else Picasso.get().load(urlofimage).into(profileimg);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        requestQueue = Volley.newRequestQueue(this);
        root = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        currentChat = new Chat(getIntent().getStringExtra("name"), getIntent().getStringExtra("number"));
        db = new DatabaseHandler(this);
        username.setText(currentChat.getName());

        addRecentMessagesToScreen();

        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        this.overridePendingTransition(R.anim.enter_activity, R.anim.exit_activity);

        sendBtn = findViewById(R.id.sendButton);
        message = findViewById(R.id.editText);
        messageRecyclerView = findViewById(R.id.recyclerviewMessageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageRecyclerView.addItemDecoration(new DividerItemDecoration(messageRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        messageListAdapter = new MessageListAdapter(this, messageList);
        messageRecyclerView.setAdapter(messageListAdapter);

        FirebaseMessaging.getInstance().subscribeToTopic(user.getPhoneNumber().substring(2));
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkAvailable()) {
                    String msg = message.getText().toString();
                    message.setText("");
                    String currentTime = getCurrentTime();
                    if (!msg.isEmpty()) {
                        Message newMessage = new Message(msg, currentTime, "TO", currentChat.getNumber());
                        db.addMessage(currentChat, newMessage);
                        addToTop(currentChat);
                        messageList.add(newMessage);
                        messageListAdapter.notifyDataSetChanged();
                        messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                        sendMessageToFirebase(newMessage);
                        try {
                            sendNotificationToUser(currentChat.getNumber(), newMessage.getMessage());

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ChatboxActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Toast.makeText(ChatboxActivity.this, "Please connnect to internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Message msg = s.getValue(Message.class);
                        Toast.makeText(ChatboxActivity.this, "CHATBOX ACTIVITY", Toast.LENGTH_SHORT).show();
                        String getName = nnHashmap.get(msg.getNumber());
                        if (getName == null) getName = msg.getNumber();
                        Chat chat = new Chat(getName, msg.getNumber());
                        db.addChat(chat);
                        db.addMessage(chat, msg);
                        if (msg.getNumber().equals(currentChat.getNumber())) {
                            messageList.add(msg);
                            messageListAdapter.notifyDataSetChanged();
                            messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                        } else {
                            unreadChat.put(chat.getNumber(), true);

                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.EFFECT_DOUBLE_CLICK));
                            } else {
                                vibrator.vibrate(500);
                            }
                            addNotification(msg, getName);
                        }
                    }
                    root.child("Chats").child(user.getPhoneNumber()).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        root.child("Chats").child(user.getPhoneNumber()).addValueEventListener(valueEventListener);
    }

    private void addNotification(Message msg, String name) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(name)
                        .setContentText(msg.getMessage());

        Intent notificationIntent = new Intent(this, ChatboxActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void sendNotificationToUser(String number, String message) throws JSONException {
        JSONObject mainObj = new JSONObject();
        mainObj.put("to", "/topics/" + number.substring(2));
        JSONObject notificationObj = new JSONObject();
        notificationObj.put("title", user.getDisplayName());
        notificationObj.put("body", message);
        mainObj.put("notification", notificationObj);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, mainObj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(ChatboxActivity.this, "DONE", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatboxActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
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

    @Override
    protected void onStop() {
        super.onStop();
        root.child("Chats").child(user.getPhoneNumber()).removeEventListener(valueEventListener);
    }


    private void sendMessageToFirebase(Message newMessage) {
        Message myMessage = new Message(newMessage.getMessage(), newMessage.getTime(), "FROM", user.getPhoneNumber());
        root.child("Chats").child(currentChat.getNumber()).push().setValue(myMessage);
    }

    private void addRecentMessagesToScreen() {
        messageList = db.getMessages(currentChat);
//        messageListAdapter.notifyDataSetChanged();
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
    }

    private void addToTop(Chat chat) {
        db.deleteChat(chat);
        db.addChat(chat);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.enter_activity, R.anim.exit_activity);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        if (item.getItemId() == R.id.settingsOption) {

        }
        if (item.getItemId() == R.id.logoutOptions) {
        }
        if (item.getItemId() == R.id.aboutOption) {

        }
        if (item.getItemId() == R.id.profileOption) {
            Toast.makeText(this, "Loading your Profile ", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}