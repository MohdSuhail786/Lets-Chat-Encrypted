package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadsuhail.letschatencrypted.LoginActivity.profileUri;
import static com.mohammadsuhail.letschatencrypted.MainActivity.unreadChat;
import static com.mohammadsuhail.letschatencrypted.SplashActivity.nnHashmap;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView userProfileImage;
    ImageView camera;
    TextView userNameProfile;
    TextView userNumProfile;
    DatabaseReference rootRef;
    private StorageReference UserProfileImageRef;
    private DatabaseHandler db;
    FirebaseUser user;

    private FirebaseAuth firebaseAuth;
    private ValueEventListener valueEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(10, 0, 0, 0);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        userNameProfile = findViewById(R.id.usernameProfile_id);
        userProfileImage = findViewById(R.id.userProfileImage_id);
        userNumProfile = findViewById(R.id.usernumProfile_id);
        camera = findViewById(R.id.camera_id);

        firebaseAuth = FirebaseAuth.getInstance();
        db = new DatabaseHandler(ProfileActivity.this);
        userNameProfile.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        userNumProfile.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        rootRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
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

        valueEventListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Message msg = s.getValue(Message.class);
                        Toast.makeText(ProfileActivity.this, "Profile ACTIVITY", Toast.LENGTH_SHORT).show();
                        assert msg != null;
                        String getName = nnHashmap.get(msg.getNumber());
                        if (getName == null) getName = msg.getNumber();
                        Chat chat = new Chat(getName, msg.getNumber());
                        db.deleteChat(chat);
                        db.addChat(chat);

                        db.addMessage(chat, msg);
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.EFFECT_DOUBLE_CLICK));
                        } else {
                            vibrator.vibrate(500);
                        }
                        unreadChat.put(chat.getNumber(),true);
                        ChatsFragment.updateList(chat);
                        addNotification(msg, getName);
                    }
                    rootRef.child("Chats").child(user.getPhoneNumber()).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        rootRef.child("Chats").child(user.getPhoneNumber()).addValueEventListener(valueEventListener);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(ProfileActivity.this);
            }
        });
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
                    final StorageReference filePath = UserProfileImageRef.child(user.getPhoneNumber() + ".jpg");
                    final String[] downloadURL = new String[1];
                    filePath.putFile(profileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        downloadURL[0] = task.getResult().toString();
                                        rootRef.child("Users").child(user.getPhoneNumber()).child("image").setValue(downloadURL[0]);
                                    }
                                });
                                Toast.makeText(ProfileActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(ProfileActivity.this, "Profile not uploaded", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        rootRef.child("Chats").child(user.getPhoneNumber()).removeEventListener(valueEventListener);
    }

    @Override
    public boolean onSupportNavigateUp(){
        startActivity(new Intent(this,MainActivity.class));
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this,MainActivity.class));
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
}