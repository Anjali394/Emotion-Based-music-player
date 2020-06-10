package com.example.mplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
     private boolean check=false;
     Uri uri;
     ListView listView;
     String  songName,songUrl;
     ImageView m;
    JcPlayerView jcPlayerView;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    ArrayList<String> arrayListSongname=new ArrayList<>();
    ArrayList<String> arrayListSongUrl=new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.myList);

        jcPlayerView=findViewById(R.id.jcplayer);
        retrivesong();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                jcPlayerView.playAudio(jcAudios.get(position));
                jcPlayerView.setVisibility(View.VISIBLE);
                jcPlayerView.createNotification();


            }
        });
    }

    private void retrivesong() {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Songs");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    Song songObj=ds.getValue(Song.class);
                    arrayListSongname.add(songObj.getSongName());
                    arrayListSongUrl.add(songObj.getSongUrl());
                    jcAudios.add(JcAudio.createFromURL(songObj.getSongName(),songObj.getSongUrl()));

                }
                arrayAdapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,arrayListSongname){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view=super.getView(position,convertView,parent);
                        TextView textView=view.findViewById(android.R.id.text1);
                        textView.setSingleLine(true);
                        textView.setMaxLines(1);
                        return view;
                    }
                };
                jcPlayerView.initPlaylist(jcAudios,null);
                listView.setAdapter(arrayAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.nav_upload)
        {
            if(validatePermission())
            {
                pickSong();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void pickSong() {
        Intent intent_up=new Intent();
        intent_up.setType("audio/+");
        intent_up.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_up,1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1)
        {
            if(resultCode==RESULT_OK)
            {
                uri=data.getData();
                Cursor cursor=getApplicationContext().getContentResolver()
                        .query(uri,null,null,null,null);
                int indexname=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                songName=cursor.getString(indexname);
                cursor.close();
                uploadSongtoDatabase();

            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadSongtoDatabase()
    {
        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Songs").child(uri.getLastPathSegment());
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.show();
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlSong=uriTask.getResult();
                songUrl=urlSong.toString();
                uploaddetailstodatabase();
                progressDialog.dismiss();




            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                int currentprogress=(int)progress;
                progressDialog.setMessage("Uploaded:"+currentprogress+"%");
            }
        });
    }

    private void uploaddetailstodatabase() {
        Song songobj=new Song(songName,songUrl);
        FirebaseDatabase.getInstance().getReference("Songs")
                .push().setValue(songobj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this,"Song Upladed",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validatePermission()
    {
        Dexter.withActivity(MainActivity.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                check=true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                  check=false;
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
              permissionToken.continuePermissionRequest();
            }
        }).check();
        return check;
    }
}