package com.mohammadsuhail.letschatencrypted;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {

    private ImageView camera;
    private CountryCodePicker ccp;
    private Button button;
    private TextInputEditText phonenumber, username;
    private CircleImageView userProfileImage;
    private Uri profileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_login);

        createHooks();

        button.setOnClickListener(view -> {
            boolean validNum = ccp.isValidFullNumber();
            boolean validName = (username.getText().toString().length() >= 4);

            if (validName && validNum) {
                Intent intent = new Intent(LoginActivity.this, VerifyOTP.class);
                intent.putExtra("phone_number", ccp.getFullNumberWithPlus());
                intent.putExtra("user_n", username.getText().toString());
                if (profileUri != null)
                    intent.putExtra("profile_uri", profileUri.toString());
                else intent.putExtra("profile_uri","nil");
                startActivity(intent);
                finish();
            } else {
                if (!validNum) phonenumber.setError("Incorrect phone number");
                if (!validName) username.setError("Minimum four char required");
            }
        });

        camera.setOnClickListener(view -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(LoginActivity.this));
    }

    private void createHooks() {
        ccp = findViewById(R.id.ccp_id);
        phonenumber = findViewById(R.id.phonenumber_id);
        username = findViewById(R.id.username_id);
        camera = findViewById(R.id.cameraid);
        button = findViewById(R.id.go_id);
        userProfileImage = findViewById(R.id.userprofile_id);
        ccp.registerCarrierNumberEditText(phonenumber);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    profileUri = result.getUri();
                    Log.i("xxxx", profileUri.toString());
                    userProfileImage.setImageURI(profileUri);
                } else {
                }
            }
        }

    }
}