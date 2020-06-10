package com.example.mplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Emotion extends AppCompatActivity {
    String emotion;
    TextView tv;
    Button btplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion);
        tv = findViewById(R.id.tv);
        btplay=findViewById(R.id.play);
        Intent intent = getIntent();
        emotion = intent.getStringExtra("emotion");
        tv.setText(emotion);
        btplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Emotion.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}