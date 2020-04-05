package com.example.kavin.chatapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class loginActivity extends AppCompatActivity {
    private Button loginbutton;
    private Button phoneloginbutton;
    private EditText useremail;
    private EditText  userpassword;
    private TextView neednewaccount;
    private TextView forgetpassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private DatabaseReference usersref;
    private String st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersref =FirebaseDatabase.getInstance().getReference().child("users");

        InitializeFields();

        neednewaccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SendUserRegisterActivity();
            }
        });

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             AllowUsertoLogin();
            }
        });

        phoneloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneloginintent= new Intent(loginActivity.this,Phone_login_Activity.class);
                startActivity(phoneloginintent);
            }
        });

        forgetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(loginActivity.this,forget_password.class);
                startActivity(i);
                finish();
            }
        });
    }



    @SuppressLint("WrongViewCast")
    private void InitializeFields()
    {
        loginbutton = (Button) findViewById(R.id.login_button);
        phoneloginbutton =(Button) findViewById(R.id.phone_login_button);
        useremail =(EditText) findViewById(R.id.login_email1);
        userpassword =(EditText) findViewById(R.id.login_password);
        neednewaccount =(TextView) findViewById(R.id.need_new_account);
        forgetpassword =(TextView) findViewById(R.id.forget_password);
        loadingBar =new ProgressDialog(this);
    }


    private void SendUserMainActivity() {
        Intent MainIntent = new Intent(loginActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
    private void SendUserRegisterActivity()
    {
        Intent registerIntent= new Intent(loginActivity .this,RegisterActivity.class);
        startActivity(registerIntent);
    }
    private void AllowUsertoLogin()
    {
        String email = useremail.getText().toString();
        String password = userpassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "enter email..!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "enter password..!", Toast.LENGTH_SHORT).show();
        } else
            {
                loadingBar.setTitle("Sign in");
                loadingBar.setMessage("Please wait...!");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            if(mAuth.getCurrentUser().isEmailVerified()) {
                            String currentuserid =mAuth.getCurrentUser().getUid();
                            String devicetoken =FirebaseInstanceId.getInstance().getToken();




                                usersref.child(currentuserid).child("device_token")
                                        .setValue(devicetoken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    SendUserMainActivity();
                                                    Toast.makeText(loginActivity.this, "you are logged in successfully..!", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();

                                                }
                                            }
                                        });
                            }
                            else
                            {
                                Toast.makeText(loginActivity.this,"Please Verify Email!",Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }



                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(loginActivity.this,"Error: "+message ,Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
    }

}
