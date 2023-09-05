package com.example.reglogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {

    private EditText countryCodeEdit, phoneNumberEdit;
    private AppCompatButton signUpBtn;
    private FirebaseAuth auth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String phoneNumber;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://reglogapp-d59fc-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final TextView goToSignInBtn = findViewById(R.id.go_to_sign_in_btn);
        phoneNumberEdit = findViewById(R.id.phone_number_edit);
        countryCodeEdit = findViewById(R.id.country_code);
        final EditText passwordEdit = findViewById(R.id.r_password);
        final EditText passwordEdit2 = findViewById(R.id.r_password_repeat);
        signUpBtn = findViewById(R.id.sign_up_btn);

        auth = FirebaseAuth.getInstance();

        final ImageView blockBtn = findViewById(R.id.pass_block_btn);
        final ImageView openBtn = findViewById(R.id.pass_open_btn);


        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBtn.setVisibility(View.VISIBLE);
                blockBtn.setVisibility(View.GONE);
                passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordEdit2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBtn.setVisibility(View.GONE);
                blockBtn.setVisibility(View.VISIBLE);
                passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                passwordEdit2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            }
        });



        //ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.show();

                String country_code = countryCodeEdit.getText().toString();
                String mobileStr = phoneNumberEdit.getText().toString();

                if(country_code.isEmpty()) {
                    country_code = "7";
                }

                phoneNumber = "+" + country_code + "" + mobileStr;

                final String passwordStr = passwordEdit.getText().toString();
                final String passwordStr2 = passwordEdit2.getText().toString();


                if (mobileStr.isEmpty() || passwordStr.isEmpty() || passwordStr2.isEmpty()) {
                    Toast.makeText(Register.this, "All fields Required!!!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else {

                    int length = mobileStr.length();     /////////////////////////////
                    int check = 0;                                              /////////////////////////////
                    char[] array = mobileStr.toCharArray();         /////////////////////////////
                    for (int i = 0; i < length; i++) {              /////////////////////////////
                        for (int j = 31; j < 48; j++) {         /////////////////////////////
                            if (array[i] == j) {
                                check = 1;
                                break;
                            }
                        }
                        for (int j = 58; j < 65; j++) {
                            if (array[i] == j) {
                                check = 1;
                                break;
                            }
                        }
                        for (int j = 91; j < 97; j++) {
                            if (array[i] == j) {
                                check = 1;
                                break;
                            }
                        }
                    }
                    if (check == 1) {
                        Toast.makeText(Register.this, "Mobile have incorrect symbols!!!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        if (!passwordStr.equals(passwordStr2)) {
                            Toast.makeText(Register.this, "Passwords are not the same, try again!!!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        } else {
                            int passwordLength = passwordStr.length();
                            if (passwordLength < 5) {
                                Toast.makeText(Register.this, "Password must not be less than 5 symbols, try again!!!", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {

                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        progressDialog.dismiss();

                                        if(snapshot.child("users").hasChild(phoneNumber)){

                                            Toast.makeText(Register.this, "Mobile number already exists", Toast.LENGTH_SHORT).show();
                                        }
                                        else {

                                            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                                                    .setPhoneNumber(phoneNumber)
                                                    .setTimeout(10L, TimeUnit.SECONDS)
                                                    .setActivity(Register.this)
                                                    .setCallbacks(mCallBacks)
                                                    .build();

                                            PhoneAuthProvider.verifyPhoneNumber(options);

                                            String userCountStr = snapshot.child("userCount").getValue().toString();
                                            int userCountInt = Integer.parseInt(userCountStr);
                                            userCountInt += 1;

                                            databaseReference.child("users").child(phoneNumber).child("userId").setValue(userCountInt);
                                            databaseReference.child("users").child(phoneNumber).child("Password").setValue(MemoryData.passSecurity(passwordStr, Register.this));
                                            databaseReference.child("userCount").setValue(userCountInt);

                                            Toast.makeText(Register.this, "Success!!!", Toast.LENGTH_SHORT).show();

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                    }
                                });

                            }
                        }
                    }
                }
            }
        });
        ///////////////////////////////////////////////
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(Register.this, "Failed!!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                Toast.makeText(Register.this, "Success!!!", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent otpIntent = new Intent(Register.this, OtpActivity.class);
                        otpIntent.putExtra("auth", s);
                        otpIntent.putExtra("phoneNumber", phoneNumber);
                        startActivity(otpIntent);
                        finish();
                    }
                },1000);
            }
        };


        goToSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpToLogin();
            }
        });
    }

    public void signUpToLogin() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            sendToMain();
        }
    }
    private void sendToMain() {
        Intent mainIntent = new Intent(Register.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
    private void signIn(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    sendToMain();
                } else {
                    Toast.makeText(Register.this, "Failed!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}