package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Objects;
import static com.mohammadsuhail.letschatencrypted.ContactsFragment.contacts;
import static com.mohammadsuhail.letschatencrypted.ContactsFragment.loadContacts;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.signedUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseHandler db;
    private DatabaseReference root;
    private FirebaseUser user;
    private FloatingActionButton feedBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addToolBar();

        setViewPagerWithTabLayout();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        user = FirebaseAuth.getInstance().getCurrentUser();
        root = FirebaseDatabase.getInstance().getReference();
        db = new DatabaseHandler(MainActivity.this);


        FirebaseMessaging.getInstance().subscribeToTopic(user.getPhoneNumber().substring(2));

        feedBackBtn = findViewById(R.id.floatingActionButton);
        feedBackBtn.setOnClickListener(view -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:thesuhailansari786246@gmail.com" + "&subject=" + Uri.encode("Feedback from : "+signedUser.getName() +"( "+signedUser.getNumber() +" )") ));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                //TODO: Handle case where no email app is
                Toast.makeText(this, "No Email app is available", Toast.LENGTH_SHORT).show();
            }
        });
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
        if (item.getItemId() == R.id.refreshOption) {
            contacts.clear();
            loadContacts();
        }
        if (item.getItemId() == R.id.logoutOptions) {
            firebaseAuth.signOut();
            DatabaseHandler db = new DatabaseHandler(MainActivity.this);
            db.dropTables();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        if (item.getItemId() == R.id.aboutOption) {
            Snackbar.make(getCurrentFocus(), "Developed and maintained by MSAF", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        if (item.getItemId() == R.id.profileOption) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }
        return true;
    }

    private void addToolBar() {
        Toolbar toolbar = findViewById(R.id.mainToolBar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(10, 0, 0, 0);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lets Chat");
    }

    private void setViewPagerWithTabLayout() {
        ViewPager viewPager = findViewById(R.id.mainTabsPager);
        viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

}