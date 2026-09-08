package com.example.appfirst;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button btnPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        btnPlay = findViewById(R.id.btnPlay);

        // Tạo thanh điều khiển tua / tạm dừng video
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Bắt sự kiện bấm nút phát video
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Đang tải video...", Toast.LENGTH_SHORT).show();

                String videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                Uri uri = Uri.parse(videoUrl);
                videoView.setVideoURI(uri);

                // Tự động phát khi tải xong dữ liệu đệm
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        videoView.start();
                    }
                });

                // Báo lỗi nếu mạng gặp sự cố
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(MainActivity.this, "Không thể tải video, kiểm tra kết nối mạng!", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        });
    }
}