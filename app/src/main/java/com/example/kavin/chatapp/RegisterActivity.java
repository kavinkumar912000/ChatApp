package com.example.kavin.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static android.view.View.GONE;

public class RegisterActivity extends AppCompatActivity {
    private Button Createaccountbutton;
    private EditText useremail, userpassword;
    private TextView alreadyaccount;
    private FirebaseAuth mauth;
    private ProgressDialog loadingBar;
    private DatabaseReference rootref;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mauth = FirebaseAuth.getInstance();
        rootref =FirebaseDatabase.getInstance().getReference();

        InitializeFields();


        alreadyaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserloginActivity();
            }
        });

        Createaccountbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

    }


    private void InitializeFields() {
        Createaccountbutton = (Button) findViewById(R.id.register_button);
        useremail = (EditText) findViewById(R.id.register_email1);
        userpassword = (EditText) findViewById(R.id.register_password);
        alreadyaccount = (TextView) findViewById(R.id.alreaddy_account);
        loadingBar = new ProgressDialog(this) ;
        image=(ImageView)  findViewById(R.id.register_image);

    }

    private void SendUserloginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, loginActivity.class);
        startActivity(loginIntent);
    }

    private void CreateNewAccount() {
        final String email = useremail.getText().toString();
        String password = userpassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "enter email..!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "enter password..!", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating account for you..!");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mauth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful() )
                            {
                                mauth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(RegisterActivity.this,"Verification code sent Successfully!",Toast.LENGTH_LONG).show();
                                                    SendUserMainActivity();
                                                    loadingBar.dismiss();
                                                }
                                                else
                                                {
                                                    Toast.makeText(RegisterActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    String devicetoken = FirebaseInstanceId.getInstance().getToken();


                                    String currentuserId = mauth.getCurrentUser().getUid();
                                    rootref.child("users").child(currentuserId).setValue("");

                                    rootref.child("users").child(currentuserId).child("device_token")
                                            .setValue(devicetoken);


                                    loadingBar.dismiss();

                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error: "+message ,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserMainActivity() {
        Intent MainIntent = new Intent(RegisterActivity.this, loginActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}


