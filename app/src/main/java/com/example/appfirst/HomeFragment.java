package com.example.appfirst;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class HomeFragment extends Fragment {

    private TextView tvWelcome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView tvHotline = view.findViewById(R.id.tvHotline);
        LinearLayout btnEmergencyRescue = view.findViewById(R.id.btnEmergencyRescue);
        LinearLayout btnServiceTire = view.findViewById(R.id.btnServiceTire);
        LinearLayout btnServiceBattery = view.findViewById(R.id.btnServiceBattery);
        LinearLayout btnServiceFuel = view.findViewById(R.id.btnServiceFuel);
        LinearLayout btnServiceTowing = view.findViewById(R.id.btnServiceTowing);

        loadUserData();

        if (tvHotline != null) {
            tvHotline.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:0898212031"));
                startActivity(dialIntent);
            });
        }

        btnEmergencyRescue.setOnClickListener(v -> openRequestRescue("Khẩn cấp / Tai nạn"));
        btnServiceTire.setOnClickListener(v -> openRequestRescue("Vá / Thay Lốp"));
        btnServiceBattery.setOnClickListener(v -> openRequestRescue("Kích Bình Ắc Quy"));
        btnServiceFuel.setOnClickListener(v -> openRequestRescue("Giao Nhiên Liệu"));
        btnServiceTowing.setOnClickListener(v -> openRequestRescue("Xe Kéo / Cẩu"));

        return view;
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isAdded() && snapshot.exists() && snapshot.hasChild("fullName")) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null && !fullName.isEmpty()) {
                            tvWelcome.setText("Xin chào, " + fullName + "!");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void openRequestRescue(String serviceName) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), RequestRescueActivity.class);
            intent.putExtra("SERVICE_NAME", serviceName);
            startActivity(intent);
        }
    }
}