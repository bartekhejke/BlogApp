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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 100;
    private android.support.v7.widget.Toolbar postToolbar;
    private ImageView postImage;
    private EditText postDescription;
    private Button postButton;
    private ProgressBar postProgressBar;

    private Uri postImageUri = null;
    private String currentUserId;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postToolbar =  findViewById(R.id.newPostToolbar);
        setSupportActionBar(postToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.postPage));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postImage = (ImageView) findViewById(R.id.newPostImage);
        postDescription = (EditText) findViewById(R.id.newPostDescription);
        postButton = (Button) findViewById(R.id.addPostButton);
        postProgressBar = (ProgressBar) findViewById(R.id.newPostProgressBar);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        postImage.setOnClickListener(new AddPhotoToImageView());
        postButton.setOnClickListener(new AddPostToBlog());

    }

    private class AddPhotoToImageView implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(PostActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(PostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    Toast.makeText(PostActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                } else{
                    //image crop
                }
            } else {
                Toast.makeText(PostActivity.this, "Permission already.", Toast.LENGTH_SHORT).show();
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .start(PostActivity.this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                postImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private class AddPostToBlog implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final String description = postDescription.getText().toString().trim();

            if (!TextUtils.isEmpty(description) && postImageUri != null){
                postProgressBar.setVisibility(View.VISIBLE);

                String randomNames = UUID.randomUUID().toString();
                StorageReference filePath = storageReference.child("Post_images").child(randomNames+ ".jpg");
                filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            String downloadUri = task.getResult().getDownloadUrl().toString();

                            Map<String, Object> postMap = new HashMap<>();
                            postMap.put("image_url", downloadUri);
                            postMap.put("description", description);
                            postMap.put("user_id", currentUserId);
                            postMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(PostActivity.this, "Post was added.", Toast.LENGTH_SHORT).show();
                                        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();

                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(PostActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                    }

                                    postProgressBar.setVisibility(View.INVISIBLE);
                                }
                            });

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(PostActivity.this, "Error: "+ error, Toast.LENGTH_SHORT).show();
                            postProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }
    }
}
