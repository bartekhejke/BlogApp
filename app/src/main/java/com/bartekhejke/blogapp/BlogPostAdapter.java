package com.bartekhejke.blogapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogPostAdapter extends RecyclerView.Adapter<BlogPostAdapter.ViewHolder> {

    public List<BlogPost> blogList;
    public Context context;

    private FirebaseFirestore firebaseFirestore;

    public BlogPostAdapter(List<BlogPost> blogList){
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
       context = parent.getContext();

       firebaseFirestore = FirebaseFirestore.getInstance();

       return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String description_data = blogList.get(position).getDescription();
        holder.setDesciprtionView(description_data);

        String post_image = blogList.get(position).getImage_url();
        holder.setPostImage(post_image);

        String user_id = blogList.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(userName, userImage);

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error: "+ error, Toast.LENGTH_SHORT).show();
                }
            }
        });


        long miliseconds = blogList.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(miliseconds)).toString();
        holder.setDate(dateString);

    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView desciprtionView;
        private ImageView postImage;
        private TextView postDate;
        private TextView postUserName;
        private CircleImageView postUserImage;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDesciprtionView(String descText){
            desciprtionView = mView.findViewById(R.id.post_description);
            desciprtionView.setText(descText);
        }

        public void setPostImage(String imageUri){
            postImage = mView.findViewById(R.id.post_image);
            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.default_post_image);
            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(imageUri).into(postImage);
        }

        public void setDate(String date){
            postDate = mView.findViewById(R.id.post_timestamp);
            postDate.setText(date);
        }

        public void setUserData(String userName, String userImage){
            postUserName = mView.findViewById(R.id.post_username);
            postUserName.setText(userName);

            postUserImage = mView.findViewById(R.id.post_user_image);
            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder (R.drawable.default_user_image);
            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(userImage).into(postUserImage);
        }
    }
}
