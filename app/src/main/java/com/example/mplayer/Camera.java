package com.example.mplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Camera extends AppCompatActivity {
    Button btc;
    ImageView img;
    private static final int PERMISSION_CODE = 101;
    private static final int CAMERA_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        btc = findViewById(R.id.buttoncam);
        img = findViewById(R.id.imageView3);

        btc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        //permission not granted request it
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup for runtime permission
                        requestPermissions(permission, PERMISSION_CODE);

                    } else {
                        //Permission is granted
                        openCamera();
                    }
                } else {
                    //system is less than sdk version
                    openCamera();
                }
            }
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_CODE);
    }

    //handle result of picked image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //called when image is captured
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAMERA_CODE) {
            //set image in image view
            //img.setImageURI(imageUri);
            Bitmap captureImage=(Bitmap)data.getExtras().get("data");
            img.setImageBitmap(captureImage);
        }
    }

    //handle result of runtime permission
    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    openCamera();
                } else {
                    //permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }
}