package com.example.appfirst;

import android.graphics.Color;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RequestsFragment extends Fragment {

    private LinearLayout llRequestList;
    private TextView tvEmptyRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        llRequestList = view.findViewById(R.id.llRequestList);
        tvEmptyRequests = view.findViewById(R.id.tvEmptyRequests);

        loadRescueRequests();

        return view;
    }

    private void loadRescueRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            tvEmptyRequests.setText("Vui lòng đăng nhập để xem lịch sử yêu cầu.");
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("rescue_requests");

        requestsRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                llRequestList.removeAllViews();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    tvEmptyRequests.setVisibility(View.VISIBLE);
                    tvEmptyRequests.setText("Bạn chưa có yêu cầu cứu hộ nào.");
                    return;
                }

                tvEmptyRequests.setVisibility(View.GONE);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                for (DataSnapshot child : snapshot.getChildren()) {
                    String serviceType = child.child("serviceType").getValue(String.class);
                    String location = child.child("location").getValue(String.class);
                    String status = child.child("status").getValue(String.class);
                    Long timestamp = child.child("timestamp").getValue(Long.class);

                    View itemCard = LayoutInflater.from(getContext()).inflate(R.layout.item_rescue_request, llRequestList, false);

                    TextView tvItemTitle = itemCard.findViewById(R.id.tvItemTitle);
                    TextView tvItemLocation = itemCard.findViewById(R.id.tvItemLocation);
                    TextView tvItemTime = itemCard.findViewById(R.id.tvItemTime);
                    TextView tvItemStatus = itemCard.findViewById(R.id.tvItemStatus);

                    tvItemTitle.setText(serviceType != null ? serviceType : "Cứu hộ");
                    tvItemLocation.setText("📍 Vị trí: " + (location != null ? location : "Không xác định"));
                    tvItemTime.setText("🕒 " + (timestamp != null ? sdf.format(new Date(timestamp)) : ""));

                    if ("PENDING".equals(status)) {
                        tvItemStatus.setText("Đang chờ tiếp nhận");
                        tvItemStatus.setTextColor(Color.parseColor("#E65100"));
                    } else if ("IN_PROGRESS".equals(status)) {
                        tvItemStatus.setText("Đội cứu hộ đang đến");
                        tvItemStatus.setTextColor(Color.parseColor("#1976D2"));
                    } else if ("COMPLETED".equals(status)) {
                        tvItemStatus.setText("Đã hoàn thành");
                        tvItemStatus.setTextColor(Color.parseColor("#388E3C"));
                    } else {
                        tvItemStatus.setText(status != null ? status : "Đang xử lý");
                        tvItemStatus.setTextColor(Color.parseColor("#E65100"));
                    }

                    llRequestList.addView(itemCard, 0); // Hiển thị yêu cầu mới nhất lên đầu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    tvEmptyRequests.setText("Không thể tải danh sách: " + error.getMessage());
                }
            }
        });
    }
}