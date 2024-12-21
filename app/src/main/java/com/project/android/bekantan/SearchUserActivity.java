package com.project.android.bekantan;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.project.android.bekantan.adapter.SearchUserRecyclerAdapter;
import com.project.android.bekantan.model.UserModel;
import com.project.android.bekantan.util.FirebaseUtil;

public class SearchUserActivity extends AppCompatActivity {

    EditText editText;
    ImageButton searchBtn;
    ImageButton backBtn;
    RecyclerView recyclerView;


    SearchUserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        editText = findViewById(R.id.search_input);
        backBtn = findViewById(R.id.back_btn);
        searchBtn = findViewById(R.id.search_btn_user);
        recyclerView = findViewById(R.id.search_item);

        editText.requestFocus();

        backBtn.setOnClickListener(view -> {
            onBackPressed();
        });

        searchBtn.setOnClickListener(view -> {
            String searchTerm = editText.getText().toString();
            if (searchTerm.isEmpty()){
                editText.setError("Invalid Username");
                return;
            }
            setupSearchRecyclerView(searchTerm);
        });

    }

    void setupSearchRecyclerView(String searchTerm) {
        String endTerm = searchTerm + "\uf8ff";
        Query query = FirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("username", searchTerm)
                .whereLessThanOrEqualTo("username", endTerm);
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        adapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}