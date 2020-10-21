package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadsuhail.letschatencrypted.LoginActivity.profileUri;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView userProfileImage;
    TextView userNameProfile;
    TextView userNumProfile;
    DatabaseReference rootRef;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userNameProfile = findViewById(R.id.usernameProfile_id);
        userProfileImage = findViewById(R.id.userProfileImage_id);
        userNumProfile = findViewById(R.id.usernumProfile_id);

        userNameProfile.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        userNumProfile.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        rootRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        rootRef.child("Users").child(user.getPhoneNumber()).child("image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Picasso.get().load(snapshot.getValue().toString()).into(userProfileImage);
                    Toast.makeText(ProfileActivity.this, "" + snapshot.getValue(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ProfileActivity.this, "Default Image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this,MainActivity.class));
    }
}