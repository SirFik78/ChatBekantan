package com.project.android.bekantan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.project.android.bekantan.model.UserModel;
import com.project.android.bekantan.util.FirebaseUtil;

public class Username extends AppCompatActivity {
    UserModel userModel;
    EditText regis_username;
    Button submitBtn;
    ProgressBar progressBar;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        regis_username = findViewById(R.id.regis_username);
        submitBtn = findViewById(R.id.submit_button);
        progressBar = findViewById(R.id.regis_progress);

        phoneNumber = getIntent().getExtras().getString("phone");
        getUsername();

        submitBtn.setOnClickListener(v -> setUsername());
    }

    void setUsername() {
        setInProgress(true);
        String username = regis_username.getText().toString();
        if (username.isEmpty() || username.length() < 3) {
            regis_username.setError("Setidaknya lebih dari 3 karakter");
            setInProgress(false);
            return;
        }
        if (userModel != null) {
            userModel.setUsername(username);
            userModel.setPhone(phoneNumber);
            userModel.setStatus("active"); // Pastikan status diatur sebagai "active"
        } else {
            userModel = new UserModel(username, phoneNumber, Timestamp.now(), FirebaseUtil.cuurentUserId());
            userModel.setStatus("active"); // Pastikan status diatur sebagai "active"
        }

        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true); // Menandai pengguna sebagai sudah login
                editor.apply();
                Intent intent = new Intent(Username.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                regis_username.setError("Gagal menyimpan username. Silakan coba lagi.");
            }
        });
    }

    void getUsername() {
        setInProgress(true);
        FirebaseUtil.allUserCollectionReference()
                .whereEqualTo("status", "active")
                .whereEqualTo("userId", FirebaseUtil.cuurentUserId())
                .get()
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        userModel = task.getResult().toObjects(UserModel.class).get(0);
                        if (userModel != null) {
                            regis_username.setText(userModel.getUsername());
                        }
                    } else {
                        regis_username.setError("Gagal mengambil data username. Periksa koneksi Anda.");
                    }
                });
    }



    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(TextView.VISIBLE);
            submitBtn.setVisibility(TextView.GONE);
        } else {
            progressBar.setVisibility(TextView.GONE);
            submitBtn.setVisibility(TextView.VISIBLE);
        }
    }
}
