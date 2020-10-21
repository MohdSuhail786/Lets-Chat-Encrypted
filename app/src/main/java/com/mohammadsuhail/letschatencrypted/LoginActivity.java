package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private Button button;
    private TextInputEditText textInputEditText,textInputEditText2;
    private TextInputLayout textInputLayout;
    CircleImageView userProfileImage;
    private static final int GalleryPick = 1;
    static Uri profileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ccp = findViewById(R.id.ccp_id);
        textInputEditText = findViewById(R.id.phonenumber_id);
        textInputEditText2 = findViewById(R.id.username_id);
        button = findViewById(R.id.go_id);
        textInputLayout = findViewById(R.id.phonelayout_id);
        userProfileImage = findViewById(R.id.userprofile_id);
        ccp.registerCarrierNumberEditText(textInputEditText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean vfn = ccp.isValidFullNumber();
                boolean usn = (textInputEditText2.getText().toString().length() >= 4);

                if (vfn && usn) {
                    if (profileUri == null)
                        Toast.makeText(LoginActivity.this, "Not added profile", Toast.LENGTH_SHORT).show();
                    Toast.makeText(LoginActivity.this, ccp.getFullNumberWithPlus(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,VerifyOTP.class);
                    intent.putExtra("phone_number",ccp.getFullNumberWithPlus());
                    intent.putExtra("user_n",textInputEditText2.getText().toString());
                    startActivity(intent);
                    finish();
                }
                else {
                    if(!vfn) textInputEditText.setError("Incorrect phone number");
                    if(!usn) textInputEditText2.setError("Minimum four char required");
                }
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(LoginActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileUri = result.getUri();
                userProfileImage.setImageURI(profileUri);
            }
        }

    }
}