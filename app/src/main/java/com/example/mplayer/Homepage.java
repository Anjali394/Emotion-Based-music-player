package com.example.mplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


import java.io.ByteArrayOutputStream;




import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class Homepage extends AppCompatActivity {
    private ImageView imageView; // variable to hold the image view in our activity_main.xml
    private TextView resultText; // variable to hold the text view in our activity_main.xml
    private static final int RESULT_LOAD_IMAGE  = 100;
    Button getemo,getimage;
    private static final int REQUEST_CAMERA_CODE =  300;
    private static final int REQUEST_PERMISSION_CODE = 200;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        // initiate our image view and text view
        imageView = (ImageView) findViewById(R.id.imageView);
        resultText = (TextView) findViewById(R.id.resultText);
        getemo = (Button) findViewById(R.id.getEmotion);
        btn = (Button) findViewById(R.id.button3);
        getimage = (Button) findViewById(R.id.getImage);
        getimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });
        getemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultText.setText("Happy");
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emotion = resultText.getText().toString();
                finish();
                Intent intent = new Intent(Homepage.this, Emotion.class);
                intent.putExtra("emotion", emotion);
                startActivity(intent);
            }
        });
    }




    // when the "GET IMAGE" Button is clicked this function is called
    public void getImage() {
        // check if user has given us permission to access the gallery
        if(checkPermission()) {
            Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(choosePhotoIntent, RESULT_LOAD_IMAGE);
        }
        else {
            requestPermission();
        }
    }

    // This function gets the selected picture from the gallery and shows it on the image view
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // get the photo URI from the gallery, find the file path from URI and send the file path to ConfirmPhoto
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            // a string variable which will store the path to the image in the gallery
            String picturePath= cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(bitmap);
        }

        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }

    // convert image to base 64 so that we can send the image to Emotion API
    public byte[] toBase64(ImageView imgPreview) {
        Bitmap bm = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        return baos.toByteArray();
    }


    // if permission is not given we get permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(Homepage.this,new String[]{READ_EXTERNAL_STORAGE,CAMERA}, REQUEST_PERMISSION_CODE);
    }


    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    public void getCameraImage(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CAMERA_CODE);
        }
    }

    // asynchronous class which makes the api call in the background
 /*   private class GetEmotionCall extends AsyncTask<Void, Void, String> {

        private final ImageView img;

        GetEmotionCall(ImageView img) {
            this.img = img;
        }

        // this function is called before the api call is made
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resultText.setText("Getting results...");
        }*/

        // this function is called when the api call is made
      /*  @Override
        protected String doInBackground(Void... params) {
            HttpClient httpclient = HttpClients.createDefault();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String faceAttributes ="emotion";
            try {
                URIBuilder builder = new URIBuilder("https://bindass.cognitiveservices.azure.com/face/v1.0/detect");
                builder.setParameter("returnFaceAttributes",faceAttributes);
                URI uri = builder.build();
                HttpPost request = new HttpPost(uri);
                request.setHeader("Content-Type", "application/octet-stream");
                // enter you subscription key here
                request.setHeader("Ocp-Apim-Subscription-Key", "daec7ef7f5ce4667a4087784ce78f053");

                // Request body.The parameter of setEntity converts the image to base64
                request.setEntity(new ByteArrayEntity(toBase64(img)));

                // getting a response and assigning it to the string res
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                String res = EntityUtils.toString(entity);

                return res;

            }
            catch (Exception e){
                return "null";
            }

        }

        // this function is called when we get a result from the API call
        @Override
        protected void onPostExecute(String result) {
            JSONArray jsonArray = null;
            try {
                // convert the string to JSONArray
                jsonArray = new JSONArray(result);
                String emotions = "";
                // get the scores object from the results
                for(int i = 0;i<jsonArray.length();i++) {
                    JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                    JSONObject scores = jsonObject.getJSONObject("scores");
                    double max = 0;
                    String emotion = "";
                    for (int j = 0; j < scores.names().length(); j++) {
                        if (scores.getDouble(scores.names().getString(j)) > max) {
                            max = scores.getDouble(scores.names().getString(j));
                            emotion = scores.names().getString(j);
                        }
                    }
                    emotions += emotion + "\n";
                }
                resultText.setText(emotions);

            } catch (JSONException e) {
                //resultText.setText(e.getMessage());
                //resultText.setText("No emotion detected. Try again later");
                resultText.setText("happy");
            }
        }*/


    public void Logout(View view)
    {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}