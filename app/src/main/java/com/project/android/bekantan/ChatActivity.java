package com.project.android.bekantan;

import static com.project.android.bekantan.util.FirebaseUtil.cuurentUserId;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.project.android.bekantan.adapter.ChatRecyclerAdapter;
import com.project.android.bekantan.adapter.SearchUserRecyclerAdapter;
import com.project.android.bekantan.model.ChatMessageModel;
import com.project.android.bekantan.model.ChatroomModel;
import com.project.android.bekantan.model.UserModel;
import com.project.android.bekantan.util.AndroidUtil;
import com.project.android.bekantan.util.FirebaseUtil;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {
    ChatRecyclerAdapter adapter;
    ImageButton backBtn;
    String chatroomId;
    ChatroomModel chatroomModel;
    EditText messageInput;
    UserModel otUser;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageButton sendbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        otherUsername = findViewById(R.id.UserProfile);
        backBtn = findViewById(R.id.back_btn);
        messageInput = findViewById(R.id.chat_input); // Pastikan ID sesuai dengan EditText
        recyclerView = findViewById(R.id.chatRecycler);
        sendbtn = findViewById(R.id.send_massage_btn);
        otUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(cuurentUserId(),otUser.getUserId());
        backBtn.setOnClickListener(view -> {
            onBackPressed();

        });

        sendbtn.setOnClickListener(view -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMassageToUser(message);
        });

        otherUsername.setText(otUser.getUsername());

        getOrCreateChatroomModel();
        setupChatRecyclerAdapter();
    }
    void setupChatRecyclerAdapter(){

        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void sendMassageToUser(String message){

        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.cuurentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message,FirebaseUtil.cuurentUserId(),Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){
                    messageInput.setText("");
                }
            }
        });
    }
    void getOrCreateChatroomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel==null){
                    chatroomModel = new ChatroomModel(
                        chatroomId,
                            Arrays.asList(FirebaseUtil.cuurentUserId(),otUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );

                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }
}
