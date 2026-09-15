package com.example.appfirst;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RequestRescueActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQ_CODE = 1001;

    private Spinner spServiceType;
    private EditText edtLocation, edtContactPhone, edtNote;
    private Button btnGetLocation, btnSubmitRescue, btnCancel;

    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_rescue);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        spServiceType = findViewById(R.id.spServiceType);
        edtLocation = findViewById(R.id.edtLocation);
        edtContactPhone = findViewById(R.id.edtContactPhone);
        edtNote = findViewById(R.id.edtNote);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnSubmitRescue = findViewById(R.id.btnSubmitRescue);
        btnCancel = findViewById(R.id.btnCancel);

        String[] services = {
            "Khẩn cấp / Tai nạn",
            "Vá / Thay Lốp",
            "Kích Bình Ắc Quy",
            "Giao Nhiên Liệu",
            "Xe Kéo / Cẩu",
            "Sửa chữa tại chỗ"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, services);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spServiceType.setAdapter(adapter);

        String selectedService = getIntent().getStringExtra("SERVICE_NAME");
        if (selectedService != null) {
            for (int i = 0; i < services.length; i++) {
                if (services[i].equalsIgnoreCase(selectedService)) {
                    spServiceType.setSelection(i);
                    break;
                }
            }
        }

        btnGetLocation.setOnClickListener(v -> checkPermissionAndGetLocation());
        btnSubmitRescue.setOnClickListener(v -> submitRescueRequest());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void checkPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQ_CODE);
        } else {
            getCurrentDeviceLocation();
        }
    }

    private void getCurrentDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Đang định vị GPS thời gian thực...", Toast.LENGTH_SHORT).show();
        btnGetLocation.setEnabled(false);

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
            .addOnSuccessListener(this, location -> {
                btnGetLocation.setEnabled(true);
                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    updateAddressFromCoordinates(currentLatitude, currentLongitude);
                } else {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(RequestRescueActivity.this, lastLocation -> {
                        if (lastLocation != null) {
                            currentLatitude = lastLocation.getLatitude();
                            currentLongitude = lastLocation.getLongitude();
                            updateAddressFromCoordinates(currentLatitude, currentLongitude);
                        } else {
                            currentLatitude = 21.028511;
                            currentLongitude = 105.854167;
                            edtLocation.setText("Vị trí GPS mẫu: 21.0285, 105.8542 (Hà Nội)");
                            Toast.makeText(RequestRescueActivity.this, "Đã lấy tọa độ vị trí thành công!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            })
            .addOnFailureListener(this, e -> {
                btnGetLocation.setEnabled(true);
                Toast.makeText(RequestRescueActivity.this, "Lỗi quét GPS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateAddressFromCoordinates(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                edtLocation.setText(fullAddress);
                Toast.makeText(this, "Đã cập nhật địa chỉ GPS thành công!", Toast.LENGTH_SHORT).show();
            } else {
                edtLocation.setText("Tọa độ GPS: " + lat + ", " + lng);
                Toast.makeText(this, "Đã lấy tọa độ GPS thành công!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            edtLocation.setText("Tọa độ GPS: " + lat + ", " + lng);
            Toast.makeText(this, "Đã lấy tọa độ GPS thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentDeviceLocation();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền vị trí để ứng dụng tự lấy địa chỉ!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void submitRescueRequest() {
        String location = edtLocation.getText().toString().trim();
        String phone = edtContactPhone.getText().toString().trim();
        String note = edtNote.getText().toString().trim();
        String serviceName = spServiceType.getSelectedItem().toString();

        if (location.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập vị trí và số điện thoại liên hệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitRescue.setEnabled(false);
        Toast.makeText(this, "Đang gửi yêu cầu tới Firebase...", Toast.LENGTH_SHORT).show();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "ANONYMOUS";

        String requestId = mDatabase.child("rescue_requests").push().getKey();

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("requestId", requestId);
        requestData.put("userId", userId);
        requestData.put("serviceType", serviceName);
        requestData.put("location", location);
        requestData.put("latitude", currentLatitude);
        requestData.put("longitude", currentLongitude);
        requestData.put("contactPhone", phone);
        requestData.put("note", note);
        requestData.put("status", "PENDING");
        requestData.put("timestamp", System.currentTimeMillis());

        if (requestId != null) {
            mDatabase.child("rescue_requests").child(requestId).setValue(requestData)
                .addOnCompleteListener(task -> {
                    btnSubmitRescue.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(RequestRescueActivity.this, "Gửi yêu cầu thành công! Đội cứu hộ đang tiếp nhận.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(RequestRescueActivity.this, "Gửi yêu cầu thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            btnSubmitRescue.setEnabled(true);
        }
    }
}