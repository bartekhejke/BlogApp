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

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmailText;
    private EditText registerPasswordText;
    private EditText registerConfirmPasswordText;
    private Button createAccountButton;
    private Button toLoginButton;
    private ProgressBar registerProgressBar;
    private TextView logoText;
    private TextView registerText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        //fonts
        Typeface logoFont = Typeface.createFromAsset(getAssets(), "fonts/BRUSHSCI.ttf");
        Typeface hintTextFont = Typeface.createFromAsset(getAssets(), "fonts/courier_new.ttf");
        Typeface buttonsFont = Typeface.createFromAsset(getAssets(), "fonts/CORBEL.TTF");

        logoText = (TextView) findViewById(R.id.logo2TextView);
        registerText = (TextView) findViewById(R.id.registerTextView);
        registerEmailText = (EditText) findViewById(R.id.registeremailEditText);
        registerPasswordText = (EditText) findViewById(R.id.registerpasswordEditText);
        registerConfirmPasswordText = (EditText) findViewById(R.id.registerpasswordConfirmEditText);
        createAccountButton = (Button) findViewById(R.id.createAccountButton);
        toLoginButton = (Button) findViewById(R.id.toLoginButton);
        registerProgressBar = (ProgressBar) findViewById(R.id.registerProgessBar);

        registerEmailText.setTypeface(hintTextFont);
        registerPasswordText.setTypeface(hintTextFont);
        registerConfirmPasswordText.setTypeface(hintTextFont);
        createAccountButton.setTypeface(buttonsFont);
        toLoginButton.setTypeface(buttonsFont);
        logoText.setTypeface(logoFont);
        registerText.setTypeface(buttonsFont);


        createAccountButton.setOnClickListener(new CreateNewAccount());
        toLoginButton.setOnClickListener(new toLoginActivity());

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
        Intent loginIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private class CreateNewAccount implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            String email = registerEmailText.getText().toString().trim();
            String password = registerPasswordText.getText().toString().trim();
            String confirmPassword = registerConfirmPasswordText.getText().toString().trim();

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)){
                if (password.equals(confirmPassword)){
                    registerProgressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){


                                Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                startActivity(setupIntent);
                                finish();

                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                            registerProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                } else {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.passwordsEquals), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class toLoginActivity implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }
}
