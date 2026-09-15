package com.example.appfirst;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtLicensePlate, edtPassword, edtConfirmPassword;
    private RadioGroup rgVehicleType;
    private Button btnRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtLicensePlate = findViewById(R.id.edtLicensePlate);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        rgVehicleType = findViewById(R.id.rgVehicleType);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String license = edtLicensePlate.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Họ tên, SĐT và Mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedVehicleId = rgVehicleType.getCheckedRadioButtonId();
        RadioButton rbSelected = findViewById(selectedVehicleId);
        String vehicleType = rbSelected != null ? rbSelected.getText().toString() : "Xe máy";

        String authEmail = phone.contains("@") ? phone : phone + "@cuuho.com";

        btnRegister.setEnabled(false);
        Toast.makeText(this, "Đang xử lý đăng ký...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(authEmail, password)
            .addOnCompleteListener(this, task -> {
                btnRegister.setEnabled(true);
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", userId);
                        userData.put("fullName", fullName);
                        userData.put("phone", phone);
                        userData.put("licensePlate", license);
                        userData.put("vehicleType", vehicleType);
                        userData.put("createdAt", System.currentTimeMillis());

                        mDatabase.child("users").child(userId).setValue(userData)
                            .addOnCompleteListener(dbTask -> {
                                Toast.makeText(RegisterActivity.this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                intent.putExtra("FULL_NAME", fullName);
                                intent.putExtra("PHONE", phone);
                                startActivity(intent);
                                finish();
                            });
                    }
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại";
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
    }
}