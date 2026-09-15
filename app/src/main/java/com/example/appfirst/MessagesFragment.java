package com.example.appfirst;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessagesFragment extends Fragment {

    private LinearLayout llChatList;
    private ScrollView svChatScroll;
    private TextView tvEmptyMessages;
    private EditText edtChatMessage;
    private Button btnSendMessage;

    private DatabaseReference mChatRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        llChatList = view.findViewById(R.id.llChatList);
        svChatScroll = view.findViewById(R.id.svChatScroll);
        tvEmptyMessages = view.findViewById(R.id.tvEmptyMessages);
        edtChatMessage = view.findViewById(R.id.edtChatMessage);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            mChatRef = FirebaseDatabase.getInstance().getReference("chats").child(currentUserId).child("messages");
            loadChatMessages();
        } else {
            tvEmptyMessages.setText("Vui lòng đăng nhập để chat với tổng đài.");
        }

        btnSendMessage.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void loadChatMessages() {
        if (mChatRef == null) return;

        mChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                llChatList.removeAllViews();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    tvEmptyMessages.setVisibility(View.VISIBLE);
                    sendSystemWelcomeMessage();
                    return;
                }

                tvEmptyMessages.setVisibility(View.GONE);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

                for (DataSnapshot child : snapshot.getChildren()) {
                    String text = child.child("text").getValue(String.class);
                    String senderId = child.child("senderId").getValue(String.class);
                    String senderName = child.child("senderName").getValue(String.class);
                    Long timestamp = child.child("timestamp").getValue(Long.class);

                    boolean isMe = currentUserId != null && currentUserId.equals(senderId);

                    View chatItem = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_message, llChatList, false);
                    View llSentMessage = chatItem.findViewById(R.id.llSentMessage);
                    View llReceivedMessage = chatItem.findViewById(R.id.llReceivedMessage);

                    String timeStr = timestamp != null ? sdf.format(new Date(timestamp)) : "";

                    if (isMe) {
                        llSentMessage.setVisibility(View.VISIBLE);
                        llReceivedMessage.setVisibility(View.GONE);

                        TextView tvSentText = chatItem.findViewById(R.id.tvSentText);
                        TextView tvSentTime = chatItem.findViewById(R.id.tvSentTime);

                        tvSentText.setText(text != null ? text : "");
                        tvSentTime.setText(timeStr);
                    } else {
                        llReceivedMessage.setVisibility(View.VISIBLE);
                        llSentMessage.setVisibility(View.GONE);

                        TextView tvSenderName = chatItem.findViewById(R.id.tvSenderName);
                        TextView tvReceivedText = chatItem.findViewById(R.id.tvReceivedText);
                        TextView tvReceivedTime = chatItem.findViewById(R.id.tvReceivedTime);

                        tvSenderName.setText(senderName != null ? senderName : "Tổng đài Cứu hộ 24/7");
                        tvReceivedText.setText(text != null ? text : "");
                        tvReceivedTime.setText(timeStr);
                    }

                    llChatList.addView(chatItem);
                }

                svChatScroll.post(() -> svChatScroll.fullScroll(View.FOCUS_DOWN));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendSystemWelcomeMessage() {
        if (mChatRef == null) return;
        String msgId = mChatRef.push().getKey();
        if (msgId == null) return;

        Map<String, Object> welcomeMsg = new HashMap<>();
        welcomeMsg.put("messageId", msgId);
        welcomeMsg.put("senderId", "SUPPORT_SYSTEM");
        welcomeMsg.put("senderName", "Tổng đài Cứu hộ 24/7");
        welcomeMsg.put("text", "Xin chào! Tổng đài Cứu hộ Xe 24/7 sẵn sàng hỗ trợ bạn. Bạn gặp sự cố gì cần tư vấn ạ?");
        welcomeMsg.put("timestamp", System.currentTimeMillis());

        mChatRef.child(msgId).setValue(welcomeMsg);
    }

    private void sendMessage() {
        String text = edtChatMessage.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập nội dung tin nhắn!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mChatRef == null) return;

        String messageId = mChatRef.push().getKey();
        if (messageId == null) return;

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("messageId", messageId);
        msgMap.put("senderId", currentUserId);
        msgMap.put("senderName", "Bạn");
        msgMap.put("text", text);
        msgMap.put("timestamp", System.currentTimeMillis());

        edtChatMessage.setText("");

        mChatRef.child(messageId).setValue(msgMap).addOnCompleteListener(task -> {
            if (isAdded() && task.isSuccessful()) {
                simulateSupportReplyLater();
            }
        });
    }

    private void simulateSupportReplyLater() {
        if (mChatRef == null) return;
        llChatList.postDelayed(() -> {
            if (!isAdded()) return;
            String autoReplyId = mChatRef.push().getKey();
            if (autoReplyId != null) {
                Map<String, Object> replyMap = new HashMap<>();
                replyMap.put("messageId", autoReplyId);
                replyMap.put("senderId", "SUPPORT_SYSTEM");
                replyMap.put("senderName", "Đội Cứu hộ Khẩn cấp");
                replyMap.put("text", "Đội ngũ kỹ thuật đã nhận tin nhắn của bạn và đang kiểm tra thông tin. Chúng tôi sẽ liên hệ lại ngay!");
                replyMap.put("timestamp", System.currentTimeMillis());
                mChatRef.child(autoReplyId).setValue(replyMap);
            }
        }, 2000);
    }
}