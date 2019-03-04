package com.bartekhejke.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {

    private Toolbar setupToolbar;
    private CircleImageView setupImage;
    private EditText setupName;
    private Button setupConfirmButton;
    private ProgressBar setupProgressBar;

    private Uri setupImageUri = null;

    private boolean isChanged = false;

    private String userId;

    FirebaseAuth mAuth;
    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupProgressBar = (ProgressBar) findViewById(R.id.setupProgressBar);
        setupImage = (CircleImageView) findViewById(R.id.setupImage);
        setupName = (EditText) findViewById(R.id.setupNameEditText);
        setupConfirmButton = (Button) findViewById(R.id.setupSaveButton);

        setupProgressBar.setVisibility(View.INVISIBLE);

        setupToolbar = (Toolbar) findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account settings");
        setupToolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.colorAccent));

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = mAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        setupImageUri = Uri.parse(image);

                        setupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.blank_profile_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);


                    }

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setupImage.setOnClickListener(new ChangeSetupImage());
        setupConfirmButton.setOnClickListener(new SaveSetupChanges());
    }

    private class ChangeSetupImage implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    Toast.makeText(SetupActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                } else{
                    //image crop
                }
            } else {
                Toast.makeText(SetupActivity.this, "Permission already.", Toast.LENGTH_SHORT).show();
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SetupActivity.this);
            }


        }
    }


    private class SaveSetupChanges implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final String userName = setupName.getText().toString().trim();

            if (!TextUtils.isEmpty(userName) && setupImageUri != null) {
                setupProgressBar.setVisibility(View.VISIBLE);

                if (isChanged) {
                    userId = mAuth.getCurrentUser().getUid();

                    StorageReference imagePath = storageReference.child("profile_images").child(userId + ".jpg");
                    imagePath.putFile(setupImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                setupProgressBar.setVisibility(View.INVISIBLE);
                                storeFirestoreUpdate(task, userName);

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Image Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    storeFirestoreUpdate(null, userName);
                }
            }
        }
    }

    private void storeFirestoreUpdate(@NonNull Task<UploadTask.TaskSnapshot> task, String userName) {
        Uri downloadUri;
        if (task != null) {
            downloadUri = task.getResult().getDownloadUrl();
        } else {
            downloadUri = setupImageUri;

        }

        Map<String,String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", downloadUri.toString());

        firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(SetupActivity.this, "The image is uploaded, user settings saved.", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Error: "+ error, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                setupImageUri = result.getUri();
                setupImage.setImageURI(setupImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
