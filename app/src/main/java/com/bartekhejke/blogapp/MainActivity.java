package com.bartekhejke.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;

    private FloatingActionButton addNewPostButton;
    private BottomNavigationView mainBottomNavigationView;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    private String currentUserId;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        addNewPostButton = findViewById(R.id.floatingActionButton);
        mainBottomNavigationView = findViewById(R.id.bottomNavigation);

        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        mainToolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.colorAccent));

        addNewPostButton.setOnClickListener(new ToAddNewPost());
        mainBottomNavigationView.setOnNavigationItemSelectedListener(new SetBottomMenuAction());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            //user signed in
            currentUserId = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().exists()){
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: "+ error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

    private class SetBottomMenuAction implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()){
                case R.id.home_bottom_menu:
                    replaceFragment(homeFragment);
                    return true;
                case R.id.notification_bottom_menu:
                    replaceFragment(notificationFragment);
                    return true;
                case R.id.account_bottom_menu:
                    replaceFragment(accountFragment);
                    return true;
                    default:
                        return false;
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFragmentContainer,fragment);
        fragmentTransaction.commit();
    }
}
