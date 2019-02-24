package com.bartekhejke.blogapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
    private EditText loginemailText;
    private EditText loginpasswordText;
    private ProgressBar loginProgressBar;
    private TextView logoText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //fonts
        Typeface logoFont = Typeface.createFromAsset(getAssets(), "fonts/BRUSHSCI.ttf");
        Typeface hintTextFont = Typeface.createFromAsset(getAssets(), "fonts/courier_new.ttf");
        Typeface buttonsFont = Typeface.createFromAsset(getAssets(), "fonts/CORBEL.TTF");

        loginButton = (Button) findViewById(R.id.createAccountButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginemailText = (EditText) findViewById(R.id.registeremailEditText);
        loginpasswordText = (EditText) findViewById(R.id.registerpasswordEditText);
        loginProgressBar = (ProgressBar) findViewById(R.id.loginProgessBar);
        logoText = (TextView) findViewById(R.id.logo2TextView);

        logoText.setTypeface(logoFont);
        loginemailText.setTypeface(hintTextFont);
        loginpasswordText.setTypeface(hintTextFont);
        loginButton.setTypeface(buttonsFont);
        registerButton.setTypeface(buttonsFont);


        loginButton.setOnClickListener(new Login());
        registerButton.setOnClickListener(new Register());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            //user signed in
            sendToMain();
        } else {
            //no user signed in

        }
    }

    private void sendToMain() {
        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private class Login implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String loginEmail = loginemailText.getText().toString();
            String loginPassword = loginpasswordText.getText().toString();

            if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)){

                loginProgressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //log in
                            sendToMain();
                        } else {
                            //no log in
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                        loginProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }

    private class Register implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
            finish();
        }
    }
}
