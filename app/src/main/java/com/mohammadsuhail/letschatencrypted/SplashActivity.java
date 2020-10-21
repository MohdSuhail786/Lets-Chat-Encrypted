package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {


    static ArrayList<Contact> contactsList = new ArrayList<>();
    static HashMap<String,String> nnHashmap = new HashMap<>();
    private static final int READ_CONTACT_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);

        Animation toptoBot = AnimationUtils.loadAnimation(this, R.anim.top_bottom_animation);
        Animation bottoTop = AnimationUtils.loadAnimation(this, R.anim.bottom_top_animation);
        Animation lefttoRight = AnimationUtils.loadAnimation(this, R.anim.left_right_animation);

        RelativeLayout relativeLayout = findViewById(R.id.toplines);
        TextView letsChat = findViewById(R.id.letsChatid);
        TextView myname = findViewById(R.id.mynameid);

        relativeLayout.setAnimation(toptoBot);
        letsChat.setAnimation(lefttoRight);
        myname.setAnimation(bottoTop);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null) {
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        } else {
            checkPermission(Manifest.permission.READ_CONTACTS, READ_CONTACT_PERMISSION);
        }

    }


    void getAllContacts() {
        long startnow = android.os.SystemClock.uptimeMillis();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,   ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.Contacts._ID}, selection, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        assert cursor != null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            number = number.replaceAll("\\s", "");
            if (number.length() == 11 && number.startsWith("0")) number = number.substring(1);
            if (number.length() == 10) number += "+91";
            if (!nnHashmap.containsKey(number)) {
                nnHashmap.put(number,name);
                contactsList.add(new Contact(name,number));
            }
            cursor.moveToNext();
        }
        cursor.close();
        startActivity(new Intent(SplashActivity.this,MainActivity.class));
        finish();
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

}