package com.mohammadsuhail.letschatencrypted;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.internal.firebase_auth.zzff;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.MultiFactor;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mohammadsuhail.letschatencrypted.LoginActivity.profileUri;

public class VerifyOTP extends AppCompatActivity {
    TextView textView;
    PinView pinView;
    String codeBySystem;
    ImageView crossImage;
    String phone_num;
    String user_name;
    Uri profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_o_t_p);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        rootRef = FirebaseDatabase.getInstance().getReference();
        pinView = findViewById(R.id.pinview);
        crossImage = findViewById(R.id.imageView2);
        textView = findViewById(R.id.phoneid);

        phone_num = getIntent().getStringExtra("phone_number");
        user_name = getIntent().getStringExtra("user_n");
        textView.setText(phone_num);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        profileImage = profileUri;
        sendVerificationCodeToUser(phone_num);
        crossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VerifyOTP.this, LoginActivity.class));
                finish();
            }
        });

    }

    private void sendVerificationCodeToUser(String phone_num) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone_num,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                pinView.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeBySystem = s;
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyOTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(user_name).build();
                            assert user != null;
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                if (profileImage != null) {
                                                    final StorageReference filePath = UserProfileImageRef.child(user.getPhoneNumber() + ".jpg");
                                                    final String[] downloadURL = new String[1];
                                                    filePath.putFile(profileImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                                                                Toast.makeText(VerifyOTP.this, "Upload successful", Toast.LENGTH_SHORT).show();
                                                                Toast.makeText(VerifyOTP.this, user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                                                rootRef.child("Users").child(Objects.requireNonNull(user.getPhoneNumber())).child("name").setValue(user.getDisplayName());

                                                                startActivity(new Intent(VerifyOTP.this, MainActivity.class));
                                                                finish();
                                                            } else {
                                                                Toast.makeText(VerifyOTP.this, "Profile not uploaded", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(VerifyOTP.this, user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                                    rootRef.child("Users").child(Objects.requireNonNull(user.getPhoneNumber())).setValue(user.getDisplayName());
                                                    startActivity(new Intent(VerifyOTP.this, MainActivity.class));
                                                    finish();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(VerifyOTP.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void callNextScreenFromOTP(View view) {
        String code = pinView.getText().toString();
        if (!code.isEmpty()) {
            verifyCode(code);
        }
    }
}