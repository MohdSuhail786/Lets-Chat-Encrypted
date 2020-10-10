package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Objects;

public class ChatBox extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);
        Toolbar toolbar = findViewById(R.id.chatboxtoolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher_round);
        toolbar.setPadding(0,4,0,4);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String name = getIntent().getStringExtra("name");
        String number = getIntent().getStringExtra("number");
        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        this.overridePendingTransition(R.anim.enter_activity,
                R.anim.exit_activity);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.enter_activity,
                R.anim.exit_activity);
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