package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadsuhail.letschatencrypted.SplashActivity.nnHashmap;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.signedUser;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView userProfileImage;
    private ImageView camera;
    private TextView userNameProfile;
    private TextView userNumProfile;
    ValueEventListener valueEventListener;
    private DatabaseReference rootRef;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        addTitleBar();

        createHooks();

        updateUserProfile();

        camera.setOnClickListener(view ->
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(ProfileActivity.this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri profileUri = result.getUri();
                userProfileImage.setImageURI(profileUri);

                if (profileUri != null) {
                    final StorageReference filePath = UserProfileImageRef.child(signedUser.getNumber() + ".jpg");
                    filePath.putFile(profileUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateProfile(filePath);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Profile not uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void addNotification(Message msg, String name) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(name)
                        .setContentText(msg.getMessage());

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void addTitleBar() {
        Toolbar toolbar = findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(10, 0, 0, 0);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void createHooks() {
        userNameProfile = findViewById(R.id.usernameProfile_id);
        userProfileImage = findViewById(R.id.userProfileImage_id);
        userNumProfile = findViewById(R.id.usernumProfile_id);
        camera = findViewById(R.id.camera_id);
        rootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }

    private void updateUserProfile() {
        userNameProfile.setText(signedUser.getName());
        userNumProfile.setText(signedUser.getNumber());
        if (!signedUser.getImage().equals("nil"))
            Glide.with(this).load(Uri.parse(signedUser.getImage())).into(userProfileImage);
    }

    private void updateProfile(StorageReference filePath) {
        filePath.getDownloadUrl().addOnCompleteListener(task1 -> {
            final String downloadURL = task1.getResult().toString();
            rootRef.child("Users").child(signedUser.getNumber()).child("image").setValue(downloadURL);
            signedUser.setImage(downloadURL);
        });
        Toast.makeText(ProfileActivity.this, "Profile Imgage Changed", Toast.LENGTH_SHORT).show();
    }


    private void joinValueEventListener() {
        Log.i("xxxx","JOIN VALUEEVENT LISTENER");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.i("xxxx","CALLED");
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Message msg = s.getValue(Message.class);
                        assert msg != null;
                        msg.setStatus("FROM");
                        Log.i("xxxx", "PROFILEACTIVITY");
                        String getName = nnHashmap.get(msg.getNumber());
                        if (getName == null) getName = msg.getNumber();
                        Log.i("xxxx", getName);
                        Log.i("xxxx", msg.getMessage());
                        Log.i("xxxx",msg.getNumber());
                        DatabaseHandler db = new DatabaseHandler(ProfileActivity.this);
                        db.deleteChat(new Contact(getName,msg.getNumber(),msg.getSenderimage(),0));
                        db.addChat(new Contact(getName,msg.getNumber(),msg.getSenderimage(),1));
                        db.addMessage(new Contact(getName,msg.getNumber(),msg.getSenderimage(),1),msg);
                    }
                    rootRef.child("Chats").child(signedUser.getNumber()).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child("Chats").child(signedUser.getNumber()).addValueEventListener(valueEventListener);
    }
    public void removeValueEventListener() {
        FirebaseDatabase.getInstance().getReference().child("Chats").child(signedUser.getNumber()).removeEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        removeValueEventListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        joinValueEventListener();
    }

}