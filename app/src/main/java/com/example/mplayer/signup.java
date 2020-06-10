package com.example.mplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class signup extends AppCompatActivity implements View.OnClickListener {

    Button bsign;
    TextView tlog;
    EditText ename,eemail,epass,ephone;
    ProgressBar pbar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ename=(EditText)findViewById(R.id.editText2);
        eemail=(EditText)findViewById(R.id.editText4);
        epass=(EditText)findViewById(R.id.editText7);
        ephone=(EditText)findViewById(R.id.editText5);
        bsign=(Button)findViewById(R.id.button2);
        tlog=(TextView)findViewById(R.id.textView7);
        pbar=(ProgressBar)findViewById(R.id.progress);
        mAuth = FirebaseAuth.getInstance();

        bsign.setOnClickListener(this);
        tlog.setOnClickListener(this);
    }

    public void registerUser()
    {
        String email=eemail.getText().toString().trim();
        String pass=epass.getText().toString().trim();

        if (email.isEmpty())
        {
            eemail.setError("Email required");
            eemail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            eemail.setError("Please enter valid email id");
            eemail.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            epass.setError("Password is required");
            epass.requestFocus();
            return;
        }
        if (pass.length()<6)
        {
            epass.setError("Minimum length of password should be 6");
            epass.requestFocus();
            return;
        }
        pbar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pbar.setVisibility(View.GONE);
                if (task.isSuccessful())
                {
                    finish();
                    startActivity(new Intent(signup.this, Homepage.class));

                }
                else
                {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.textView7:
                startActivity(new Intent(this, Login.class));
                break;
            case R.id.button2:
                registerUser();
                break;
        }
    }
}