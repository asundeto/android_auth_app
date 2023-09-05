package com.example.reglogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {

    private AppCompatButton mVerifyCodeBtn;
    private EditText otpEdit;
    private String OTP;
    private FirebaseAuth firebaseAuth;
    private TextView sendCodeAgainBtn;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mVerifyCodeBtn = findViewById(R.id.otp_send_btn);
        otpEdit = findViewById(R.id.otp_edit_text);

        sendCodeAgainBtn = findViewById(R.id.send_code_again_btn);


        firebaseAuth = FirebaseAuth.getInstance();

        OTP = getIntent().getStringExtra("auth");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        mVerifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verification_code = otpEdit.getText().toString();
                if(!verification_code.isEmpty()) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP, verification_code);
                    //save mobile to memory
                    MemoryData.saveLoged(phoneNumber, OtpActivity.this);
                    signIn(credential);
                } else {
                    Toast.makeText(OtpActivity.this, "Please enter OTP!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendCodeAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verification_code = otpEdit.getText().toString();
                if(!verification_code.isEmpty()) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP, verification_code);
                    //save mobile to memory
                    MemoryData.saveLoged(OTP, OtpActivity.this);
                    signIn(credential);
                } else {
                    Toast.makeText(OtpActivity.this, "Please enter OTP!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void signIn(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    sendToMain();
                } else {
                    Toast.makeText(OtpActivity.this, "Verification Failed!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        startActivity(new Intent(OtpActivity.this, MainActivity.class));
        finish();
    }
}