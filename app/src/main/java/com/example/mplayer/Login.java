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

public class Login extends AppCompatActivity implements View.OnClickListener{
    Button blogin;
    TextView tsign;
    EditText e1,e2;
    ProgressBar pbar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        e1=(EditText)findViewById(R.id.editText);
        e2=(EditText)findViewById(R.id.editText3);
        blogin=(Button)findViewById(R.id.button);
        tsign=(TextView)findViewById(R.id.textView5);
        pbar=(ProgressBar)findViewById(R.id.progress2);
        mAuth = FirebaseAuth.getInstance();

        blogin.setOnClickListener(this);
        tsign.setOnClickListener(this);
    }

    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, Homepage.class));
        }
    }

    public void userLogin()
    {
        String email=e1.getText().toString().trim();
        String pass=e2.getText().toString().trim();

        if (email.isEmpty())
        {
            e1.setError("Email required");
            e2.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            e1.setError("Please enter valid email id");
            e2.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            e1.setError("Password is required");
            e2.requestFocus();
            return;
        }
        if (pass.length()<6)
        {
            e1.setError("Minimum length of password should be 6");
            e2.requestFocus();
            return;
        }
        pbar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pbar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    finish();
                    Intent intent = new Intent(Login.this, Homepage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.textView5:
                finish();
                startActivity(new Intent(this, signup.class));
                break;
            case R.id.button:
                userLogin();
                break;
        }
    }

}