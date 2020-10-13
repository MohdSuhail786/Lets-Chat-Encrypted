package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener valueEventListener;
    private static final int READ_CONTACT_PERMISSION = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        Toolbar toolbar = findViewById(R.id.mainToolBar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(10,0,0,0);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lets Chat");
        ViewPager viewPager = findViewById(R.id.mainTabsPager);
        viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);
        checkPermission(Manifest.permission.READ_CONTACTS, READ_CONTACT_PERMISSION);
        receiveMessagesFromFirebase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
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
        if(item.getItemId() == R.id.settingsOption) {

        }
        if (item.getItemId() == R.id.logoutOptions) {
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
        if (item.getItemId() == R.id.aboutOption) {

        }
        if (item.getItemId() == R.id.profileOption) {
            Toast.makeText(this, "Loading your Profile ", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void receiveMessagesFromFirebase() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        final DatabaseHandler db = new DatabaseHandler(this);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot s:snapshot.getChildren()) {
                        Message msg = s.getValue(Message.class);
                        Toast.makeText(MainActivity.this, "MainActivity", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();/*
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        root.child("Chats").child(user.getPhoneNumber()).removeEventListener(valueEventListener);*/
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "READ CONTACT permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}