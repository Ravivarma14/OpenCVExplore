package com.example.opencvcopy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    CardView fromCamera;
    CardView fromGallery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromCamera=findViewById(R.id.fromCamera);
        fromGallery=findViewById(R.id.fromGallery);

        fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // to call implicit intent to camera intent
                Intent intent=new Intent(MainActivity.this,CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        fromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // to call explicit intent to select image
            }
        });
    }
}