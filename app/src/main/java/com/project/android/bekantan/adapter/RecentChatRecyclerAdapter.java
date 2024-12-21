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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.project.android.bekantan.ChatActivity;
import com.project.android.bekantan.R;
import com.project.android.bekantan.model.ChatroomModel;
import com.project.android.bekantan.model.UserModel;
import com.project.android.bekantan.util.AndroidUtil;
import com.project.android.bekantan.util.FirebaseUtil;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    Context context;
    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean lastMessageSendByMe = model.getLastMessageSenderId().equals(FirebaseUtil.cuurentUserId());
                UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                String username = otherUserModel.getUsername();
                if (otherUserModel.getUserId().equals(FirebaseUtil.cuurentUserId())) {
                    username += " (You)";
                }
                holder.usernameText.setText(username);

                if (lastMessageSendByMe) {
                    holder.lastMessageText.setText("You: " + model.getLastMessage());
                } else {
                    holder.lastMessageText.setText(model.getLastMessage());
                }

                holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(context, ChatActivity.class);
                    AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                });
            }
        });
    }




    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder{
        TextView lastMessageText;
        TextView usernameText;
        TextView lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView){
            super(itemView);
            usernameText = itemView.findViewById(R.id.name_chat_text);
            lastMessageText = itemView.findViewById(R.id.phone_chat_text);
            lastMessageTime = itemView.findViewById(R.id.time_message_text);
            profilePic = itemView.findViewById(R.id.profile_pic_img_user);
        }
    }
}
