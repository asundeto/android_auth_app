package com.example.reglogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GravityCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;


public class Login extends AppCompatActivity {
    private EditText mobileEdit, countryCodeEdit;
    private String phoneNumber;
    boolean langBtnBool = true;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://reglogapp-d59fc-default-rtdb.firebaseio.com/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        countryCodeEdit = findViewById(R.id.country_code);
        mobileEdit = findViewById(R.id.phone_number_edit);
        final EditText passwordEdit = findViewById(R.id.l_password);
        final AppCompatButton signInBtn = findViewById(R.id.sign_in_btn);
        final TextView goToSignUpBtn = findViewById(R.id.go_to_sign_up_btn);

        final ImageView blockBtn = findViewById(R.id.pass_block_btn);
        final ImageView openBtn = findViewById(R.id.pass_open_btn);

        final LinearLayout langBtn = findViewById(R.id.lang_btn);
        final NavigationView langMenu = findViewById(R.id.lang_menu);

        langBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (langBtnBool) {
                    langMenu.setVisibility(View.VISIBLE);
                    langBtnBool = false;
                } else {
                    langMenu.setVisibility(View.GONE);
                    langBtnBool = true;
                }

            }
        });

        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBtn.setVisibility(View.VISIBLE);
                blockBtn.setVisibility(View.GONE);
                passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBtn.setVisibility(View.GONE);
                blockBtn.setVisibility(View.VISIBLE);
                passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            }
        });


        //ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.show();

                final String mobileStr = mobileEdit.getText().toString();
                final String passwordStr = passwordEdit.getText().toString();
                String countryCodeStr = countryCodeEdit.getText().toString();

                if (countryCodeStr.isEmpty()) {
                    countryCodeStr = "7";
                }

                phoneNumber = "+" + countryCodeStr + mobileStr;

                if (mobileStr.isEmpty() || passwordStr.isEmpty()) {
                    Toast.makeText(Login.this, "All fields Required!!!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //Algoritm to change EMAIL
                            if (!snapshot.child("users").hasChild(phoneNumber)) {
                                Toast.makeText(Login.this, "User is not registered!!!", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                            else {
                                final String userPassword = snapshot.child("users").child(phoneNumber).child("Password").getValue(String.class);
                                if (!userPassword.equals(MemoryData.passSecurity(passwordStr, Login.this))) {
                                    Toast.makeText(Login.this, "Wrong user name or password!!!", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                                else {
                                    Toast.makeText(Login.this, "Success!!!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                    //save mobile to memory
                                    MemoryData.saveLoged(phoneNumber, Login.this);

                                    progressDialog.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });


        goToSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignUp();
            }
        });
    }

    public void goToSignUp() {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //check if user already logged in
        String checkLoged = MemoryData.getLoged(this);
        if (!checkLoged.equals("unloged")){
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}