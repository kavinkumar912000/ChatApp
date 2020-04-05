package com.example.kavin.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forget_password extends AppCompatActivity {
    private EditText email;
    private Button forget_button;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        email=(EditText) findViewById(R.id.forget_email1);
        forget_button =(Button) findViewById(R.id.reset_button);
        mauth =FirebaseAuth.getInstance();

        forget_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = email.getText().toString().trim();


                if(useremail.equals(""))
                {
                    Toast.makeText(forget_password.this, "Enter Email..!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mauth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Intent i=new Intent(forget_password.this ,loginActivity.class);
                                startActivity(i);
                                Toast.makeText(forget_password.this, "Password Reset Email sent...!", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(forget_password.this, "Check your Email....!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}
