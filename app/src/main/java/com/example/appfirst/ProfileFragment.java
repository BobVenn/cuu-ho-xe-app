package com.example.appfirst;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText edtProfileFullName, edtProfilePhone, edtProfileLicense;
    private RadioGroup rgProfileVehicleType;
    private RadioButton rbProfileMotorbike, rbProfileCar, rbProfileTruck;
    private Button btnUpdateProfile, btnProfileLogout;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        edtProfileFullName = view.findViewById(R.id.edtProfileFullName);
        edtProfilePhone = view.findViewById(R.id.edtProfilePhone);
        edtProfileLicense = view.findViewById(R.id.edtProfileLicense);
        rgProfileVehicleType = view.findViewById(R.id.rgProfileVehicleType);
        rbProfileMotorbike = view.findViewById(R.id.rbProfileMotorbike);
        rbProfileCar = view.findViewById(R.id.rbProfileCar);
        rbProfileTruck = view.findViewById(R.id.rbProfileTruck);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnProfileLogout = view.findViewById(R.id.btnProfileLogout);

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            mUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            loadProfileData();
        }

        btnUpdateProfile.setOnClickListener(v -> updateProfileData());

        btnProfileLogout.setOnClickListener(v -> {
            mAuth.signOut();
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    private void loadProfileData() {
        if (mUserRef == null) return;

        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || !snapshot.exists()) return;

                String fullName = snapshot.child("fullName").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String license = snapshot.child("licensePlate").getValue(String.class);
                String vehicleType = snapshot.child("vehicleType").getValue(String.class);

                edtProfileFullName.setText(fullName != null ? fullName : "");
                edtProfilePhone.setText(phone != null ? phone : "");
                edtProfileLicense.setText(license != null ? license : "");

                if ("Ô tô".equalsIgnoreCase(vehicleType)) {
                    rbProfileCar.setChecked(true);
                } else if ("Xe tải".equalsIgnoreCase(vehicleType)) {
                    rbProfileTruck.setChecked(true);
                } else {
                    rbProfileMotorbike.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateProfileData() {
        if (mUserRef == null) return;

        String fullName = edtProfileFullName.getText().toString().trim();
        String license = edtProfileLicense.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập Họ và tên!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = rgProfileVehicleType.getCheckedRadioButtonId();
        RadioButton rbSelected = getView() != null ? getView().findViewById(selectedId) : null;
        String vehicleType = rbSelected != null ? rbSelected.getText().toString() : "Xe máy";

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("licensePlate", license);
        updates.put("vehicleType", vehicleType);

        btnUpdateProfile.setEnabled(false);
        mUserRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (isAdded()) {
                btnUpdateProfile.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}