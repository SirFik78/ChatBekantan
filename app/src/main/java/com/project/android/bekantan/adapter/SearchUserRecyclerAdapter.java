package com.project.android.bekantan.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.android.bekantan.ChatActivity;
import com.project.android.bekantan.R;
import com.project.android.bekantan.model.UserModel;
import com.project.android.bekantan.util.AndroidUtil;
import com.project.android.bekantan.util.FirebaseUtil;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    Context context;
    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        holder.usernameText.setText(model.getUsername());
        holder.phoneText.setText(model.getPhone());
        if (model.getUserId().equals(FirebaseUtil.cuurentUserId())){
            holder.usernameText.setText(model.getUsername()+ "(You)");
        }
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context , ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent,model);
            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }



    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row,parent,false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView){
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_img_user);
        }
    }
}
