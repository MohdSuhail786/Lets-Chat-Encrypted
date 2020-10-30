package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mohammadsuhail.letschatencrypted.ChatboxActivity.messageList;

public class SplashActivity extends AppCompatActivity {

    private Animation toptoBot, bottoTop, lefttoRight;
    private RelativeLayout relativeLayout;
    private TextView letsChat, myname;
    private FirebaseUser firebaseUser;
    private static ValueEventListener valueEventListener;
    static ArrayList<Contact> internalContactList = new ArrayList<>();
    static HashMap<String, String> nnHashmap = new HashMap<>();
    private static final int READ_CONTACT_PERMISSION = 100;
    public static SignedUser signedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);

        initializeAnimations();

        createHooks();

        setAnimations();

        if (isNetworkAvailable()) {
            if (firebaseUser == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                checkPermission(Manifest.permission.READ_CONTACTS, READ_CONTACT_PERMISSION);
            }
        } else {
            Toast.makeText(this, "Network not available. Connect to internet and reopen your app", Toast.LENGTH_SHORT).show();
        }

    }

    void getAllContacts() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, null, selection, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        assert cursor != null;
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            number = number.replaceAll("\\s", "");
            if (number.length() == 11 && number.startsWith("0")) number = number.substring(1);
            if (number.length() == 10) number += "+91";
            if (!nnHashmap.containsKey(number)) {
                nnHashmap.put(number, name);
                internalContactList.add(new Contact(name, number));
            }
//            Log.i("xxxx","Reading Cursor");
        }
        cursor.close();
        setSignedUser();
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {
            getAllContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllContacts();
            } else {
                checkPermission(Manifest.permission.READ_CONTACTS, READ_CONTACT_PERMISSION);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initializeAnimations() {
        toptoBot = AnimationUtils.loadAnimation(this, R.anim.top_bottom_animation);
        bottoTop = AnimationUtils.loadAnimation(this, R.anim.bottom_top_animation);
        lefttoRight = AnimationUtils.loadAnimation(this, R.anim.left_right_animation);
    }

    private void createHooks() {
        relativeLayout = findViewById(R.id.toplines);
        letsChat = findViewById(R.id.letsChatid);
        myname = findViewById(R.id.mynameid);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setAnimations() {
        relativeLayout.setAnimation(toptoBot);
        letsChat.setAnimation(lefttoRight);
        myname.setAnimation(bottoTop);
    }

    public void setSignedUser() {
        Log.i("xxxx", "Set Signed User");
        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getPhoneNumber()).child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String signedUserName = firebaseUser.getDisplayName();
                String signedUserNumber = firebaseUser.getPhoneNumber();
                String myProfileUrl = "nil";
                if (snapshot.exists()) {
                    myProfileUrl = snapshot.getValue().toString();
                }
                signedUser = new SignedUser(signedUserName, signedUserNumber, myProfileUrl);
//                joinValueEventListener();
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}