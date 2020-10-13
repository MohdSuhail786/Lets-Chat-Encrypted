package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIMEOUT = 4000;
    private Animation toptoBot, bottoTop, lefttoRight;
    private RelativeLayout relativeLayout;
    private TextView letsChat, myname;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);

        toptoBot = AnimationUtils.loadAnimation(this, R.anim.top_bottom_animation);
        bottoTop = AnimationUtils.loadAnimation(this, R.anim.bottom_top_animation);
        lefttoRight = AnimationUtils.loadAnimation(this, R.anim.left_right_animation);

        relativeLayout = findViewById(R.id.toplines);
        letsChat = findViewById(R.id.letsChatid);
        myname = findViewById(R.id.mynameid);

        relativeLayout.setAnimation(toptoBot);
        letsChat.setAnimation(lefttoRight);
        myname.setAnimation(bottoTop);
        final Intent intent;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null) {
            intent = new Intent(this,LoginActivity.class);
        } else {
//            startReceivingMessagesFromFirebase();
            intent = new Intent(this,MainActivity.class);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIMEOUT);
    }

    private void startReceivingMessagesFromFirebase() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        final DatabaseHandler db = new DatabaseHandler(this);
        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot s:snapshot.getChildren()) {
                        Message msg = s.getValue(Message.class);
                        Toast.makeText(SplashActivity.this, "SPLASH ACTIVITY", Toast.LENGTH_SHORT).show();
                        db.addChat(new Chat("SUHAIL",msg.getNumber()));
                        db.addMessage(new Chat("SUHAIL",msg.getNumber()),msg);

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


}