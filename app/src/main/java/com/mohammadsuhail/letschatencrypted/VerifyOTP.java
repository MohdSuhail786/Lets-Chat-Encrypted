package com.mohammadsuhail.letschatencrypted;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mohammadsuhail.letschatencrypted.SplashActivity.signedUser;


public class VerifyOTP extends AppCompatActivity {

    private TextView phoneNumber;
    private Button verifyButton;
    private PinView pinView;
    private String codeFromServer, phone_num, user_name;
    private ImageView crossImage;
    private Uri profileUri;
    private DatabaseReference rootRef;
    private StorageReference userProfileImageRef;
    private FirebaseUser user;
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_verify_o_t_p);

        createHooks();

        phoneNumber.setText(phone_num);

        sendVerificationCodeToUser(phone_num);

        crossImage.setOnClickListener(view -> {
            startActivity(new Intent(VerifyOTP.this, LoginActivity.class));
            finish();
        });

        verifyButton.setOnClickListener(view -> {
            String code = pinView.getText().toString();
            if (!code.isEmpty()) {
                verifyCode(code);
            }
        });
    }

    private void sendVerificationCodeToUser(String phone_num) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone_num,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeFromServer = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                pinView.setText(code);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyOTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String codeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeFromServer, codeByUser);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.show();
                        updateName();
                    } else {
                        Toast.makeText(VerifyOTP.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createHooks() {
        phone_num = getIntent().getStringExtra("phone_number");
        user_name = getIntent().getStringExtra("user_n");
        if (!getIntent().getStringExtra("profile_uri").equals("nil")) {
            profileUri = Uri.parse(getIntent().getStringExtra("profile_uri"));
        } else profileUri = null;
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        pinView = findViewById(R.id.pinview);
        crossImage = findViewById(R.id.imageView2);
        verifyButton = findViewById(R.id.verifyBtnid);
        phoneNumber = findViewById(R.id.phoneid);
        initializeProgressbar();
    }

    private void updateName() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(user_name).build();
        assert user != null;
        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                rootRef.child("Users").child(Objects.requireNonNull(user.getPhoneNumber())).child("name").setValue(user.getDisplayName());
                updateProfileImage();
            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileImage() {
        if (profileUri != null) {
            StorageReference filePath = userProfileImageRef.child(user.getPhoneNumber() + ".jpg");
            filePath.putFile(profileUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    filePath.getDownloadUrl().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            final String downloadURL = task1.getResult().toString();
                            rootRef.child("Users").child(user.getPhoneNumber()).child("image").setValue(downloadURL);
                            Log.i("xxxx", "SUHAIL");
                            progressBar.dismiss();
                            startActivity(new Intent(VerifyOTP.this, SplashActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(VerifyOTP.this, "Profile not uploaded", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            rootRef.child("Users").child(user.getPhoneNumber()).child("image").setValue("nil").addOnCompleteListener(task -> {
                progressBar.dismiss();
                startActivity(new Intent(VerifyOTP.this, SplashActivity.class));
                finish();
            });
        }
    }

    void initializeProgressbar() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);//you can cancel it by pressing back button
        progressBar.setMessage("Wait ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
}