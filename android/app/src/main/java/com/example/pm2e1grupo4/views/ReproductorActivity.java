package com.example.pm2e1grupo4.views;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pm2e1grupo4.R;

public class ReproductorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor);

        String urlVideo = getIntent().getStringExtra("urlVideo");
        VideoView videoView = findViewById(R.id.videoViewFull);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        if (urlVideo != null) {
            videoView.setVideoURI(Uri.parse(urlVideo));
            videoView.start();
        }
    }
}