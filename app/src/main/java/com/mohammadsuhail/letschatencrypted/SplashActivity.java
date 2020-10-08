package com.mohammadsuhail.letschatencrypted;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIMEOUT = 4000;
    private Animation toptoBot,bottoTop,lefttoRight;
    private RelativeLayout relativeLayout;
    private TextView letsChat,myname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);

        toptoBot = AnimationUtils.loadAnimation(this,R.anim.top_bottom_animation);
        bottoTop = AnimationUtils.loadAnimation(this,R.anim.bottom_top_animation);
        lefttoRight = AnimationUtils.loadAnimation(this,R.anim.left_right_animation);

        relativeLayout = findViewById(R.id.toplines);
        letsChat = findViewById(R.id.letsChatid);
        myname = findViewById(R.id.mynameid);

        relativeLayout.setAnimation(toptoBot);
        letsChat.setAnimation(lefttoRight);
        myname.setAnimation(bottoTop);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SplashActivity.this, "Runnable Working", Toast.LENGTH_SHORT).show();
            }
        },SPLASH_TIMEOUT);

    }
}