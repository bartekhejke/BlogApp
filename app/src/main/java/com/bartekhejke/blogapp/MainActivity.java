package com.bartekhejke.blogapp;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;

    private FloatingActionButton addNewPostButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        addNewPostButton = findViewById(R.id.floatingActionButton);

        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        mainToolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.colorAccent));

        addNewPostButton.setOnClickListener(new ToAddNewPost());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null){
            //user signed in
        } else {
            //no user signed in
            sendToLogin();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:
                logOut();
                return true;
            case R.id.action_settings:
                openSettingsAccountPage();
                return true;
            default:
                return false;
        }
    }

    private void openSettingsAccountPage() {
        Intent accountSettingIntent = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(accountSettingIntent);
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private class ToAddNewPost implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent newPostPageIntent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(newPostPageIntent);
        }
    }
}
