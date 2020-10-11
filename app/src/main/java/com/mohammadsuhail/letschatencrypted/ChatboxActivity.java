package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ChatboxActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private Chat currentChat;
    private ImageButton sendBtn;
    private EditText message;
    private RecyclerView messageRecyclerView;
    private MessageListAdapter messageListAdapter;
    private static ArrayList<Message> messageList= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Toolbar toolbar = findViewById(R.id.chatboxtoolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher_round);
        toolbar.setPadding(0,4,0,4);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        currentChat = new Chat(getIntent().getStringExtra("name"),getIntent().getStringExtra("number"));
        db = new DatabaseHandler(this);

        addRecentMessagesToScreen();

        Objects.requireNonNull(getSupportActionBar()).setTitle(currentChat.getName());
        this.overridePendingTransition(R.anim.enter_activity, R.anim.exit_activity);

        sendBtn = findViewById(R.id.sendButton);
        message = findViewById(R.id.editText);
        messageRecyclerView = findViewById(R.id.recyclerviewMessageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageRecyclerView.addItemDecoration(new DividerItemDecoration(messageRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        messageListAdapter = new MessageListAdapter(this,messageList);
        messageRecyclerView.setAdapter(messageListAdapter);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = message.getText().toString();
                message.setText("");
                String currentTime = getCurrentTime();
                if(!msg.isEmpty()) {
                    Message newMessage = new Message(msg,currentTime,"TO",currentChat.getNumber());
                    db.addMessage(currentChat,newMessage);
                    addToTop(currentChat);
                    messageList.add(newMessage);
                    messageListAdapter.notifyDataSetChanged();
                    messageRecyclerView.smoothScrollToPosition(messageList.size()-1);
                }
            }
        });

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
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
        if(item.getItemId() == R.id.settingsOption) {

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

}