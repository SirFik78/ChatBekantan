package com.project.android.bekantan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.project.android.bekantan.model.UserModel;
import com.project.android.bekantan.util.FirebaseUtil;

public class ProfileFragment extends Fragment {

    // Variabel untuk elemen UI
    ProgressBar progressBar;
    ImageView profileImage;
    EditText username;
    TextView phoneNumber;
    Button Updatebtn;
    TextView logoutButton;
    UserModel AnakIlang;
    TextView deleteaccBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Hubungkan elemen UI dengan findViewById
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.profileImage);
        progressBar = view.findViewById(R.id.regis_progress2);
        username = view.findViewById(R.id.username);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        Updatebtn = view.findViewById(R.id.Updatebtn);
        Updatebtn.setOnClickListener(view1 -> updateUsername());

        logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(view1 ->
                showConfirmationDialog("Are you sure you want to logout?", this::logout)
        );

        deleteaccBtn = view.findViewById(R.id.deleteButton);
        deleteaccBtn.setOnClickListener(view1 ->
                showConfirmationDialog("Are you sure you want to delete your account?", this::deleteAccount)
        );

        getUserData();
        return view;
    }

    // Method untuk menampilkan dialog konfirmasi
    private void showConfirmationDialog(String message, Runnable onConfirm) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    void getUserData() {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            AnakIlang = task.getResult().toObject(UserModel.class);
            username.setText(AnakIlang.getUsername());
            phoneNumber.setText(AnakIlang.getPhone());
        });
    }

    private void logout() {
        setInProgress(true);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getActivity(), Register.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        setInProgress(false); // Sembunyikan progress bar sebelum menutup aktivitas
        getActivity().finish();
    }

    // Fungsi untuk mengupdate username
    private void updateUsername() {
        setInProgress(true);
        String newUsername = username.getText().toString().trim();

        if (newUsername.isEmpty()) {
            username.setError("Username tidak boleh kosong");
            setInProgress(false); // Sembunyikan progress bar jika validasi gagal
            return;
        }

        FirebaseUtil.currentUserDetails().update("username", newUsername)
                .addOnCompleteListener(task -> {
                    setInProgress(false); // Sembunyikan progress bar setelah selesai
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Username berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        AnakIlang.setUsername(newUsername);
                    } else {
                        Toast.makeText(getActivity(), "Gagal memperbarui username", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void deleteAccount() {
        setInProgress(true);
        if (AnakIlang == null || AnakIlang.getUserId() == null) {
            Toast.makeText(getActivity(), "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
            setInProgress(false); // Sembunyikan progress bar jika data tidak ada
            return;
        }

        FirebaseUtil.currentUserDetails()
                .update("status", "non-active")
                .addOnCompleteListener(task -> {
                    setInProgress(false); // Sembunyikan progress bar setelah selesai
                    if (task.isSuccessful()) {
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", getActivity().MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        Toast.makeText(getActivity(), "Akun berhasil dinonaktifkan", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), Register.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Gagal menonaktifkan akun", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            Updatebtn.setEnabled(false); // Nonaktifkan tombol saat dalam proses
            logoutButton.setEnabled(false);
            deleteaccBtn.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            Updatebtn.setEnabled(true); // Aktifkan kembali tombol setelah selesai
            logoutButton.setEnabled(true);
            deleteaccBtn.setEnabled(true);
        }
    }
}