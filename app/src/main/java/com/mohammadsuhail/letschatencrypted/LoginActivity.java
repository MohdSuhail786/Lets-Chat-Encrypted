package com.mohammadsuhail.letschatencrypted;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;

public class LoginActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private Button button;
    private TextInputEditText textInputEditText,textInputEditText2;
    private TextInputLayout textInputLayout;
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

        ccp.registerCarrierNumberEditText(textInputEditText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean vfn = ccp.isValidFullNumber();
                boolean usn = (textInputEditText2.getText().toString().length() >= 4);
                if (vfn && usn){
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
    }

}