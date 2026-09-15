package com.example.appfirst;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText edtPhone, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserDataAndNavigate(currentUser.getUid());
        }
    }

    private void loginUser() {
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Vui lòng nhập đầy đủ Số điện thoại và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        String authEmail = phone.contains("@") ? phone : phone + "@cuuho.com";

        btnLogin.setEnabled(false);
        Toast.makeText(this, "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(authEmail, password)
            .addOnCompleteListener(this, task -> {
                btnLogin.setEnabled(true);
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        Toast.makeText(MainActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        fetchUserDataAndNavigate(user.getUid());
                    }
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Sai thông tin đăng nhập";
                    Toast.makeText(MainActivity.this, "Đăng nhập thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void fetchUserDataAndNavigate(String userId) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = "Khách hàng";
                String phone = "";
                if (snapshot.exists()) {
                    if (snapshot.hasChild("fullName")) {
                        fullName = snapshot.child("fullName").getValue(String.class);
                    }
                    if (snapshot.hasChild("phone")) {
                        phone = snapshot.child("phone").getValue(String.class);
                    }
                }

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("FULL_NAME", fullName);
                intent.putExtra("PHONE", phone);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("FULL_NAME", "Khách hàng");
                startActivity(intent);
                finish();
            }
        });
    }
}