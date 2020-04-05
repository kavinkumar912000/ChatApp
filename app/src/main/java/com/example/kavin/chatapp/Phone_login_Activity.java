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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Phone_login_Activity extends AppCompatActivity {

    private Button sendvercode,verifybutton;
    private EditText inputphonenumber,inputverification;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mverificationid;
    private ProgressDialog loadingBar;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login_);

        mAuth =FirebaseAuth.getInstance();

        sendvercode =(Button) findViewById(R.id.send_verification_code);
        verifybutton =(Button) findViewById(R.id.verify_button);
        inputphonenumber =(EditText) findViewById(R.id.phone_number);
        inputverification =(EditText) findViewById(R.id.verification_code);
        loadingBar =new ProgressDialog(this);

        sendvercode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


              String phonenumber = inputphonenumber.getText().toString();
              if (TextUtils.isEmpty(phonenumber))
              {
                  Toast.makeText(Phone_login_Activity.this,"Enter phone number first...!",Toast.LENGTH_SHORT).show();
              }
              else
              {
                  loadingBar.setTitle("phone verification");
                  loadingBar.setMessage("please wait, while we are verifying your phone number....!");
                  loadingBar.setCanceledOnTouchOutside(false);
                  loadingBar.show();
                  PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenumber, 60, TimeUnit.SECONDS, Phone_login_Activity.this, callbacks);
              }
            }
        });

        verifybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendvercode.setVisibility(View.INVISIBLE);
                inputphonenumber.setVisibility(View.INVISIBLE);

                String verificationcode = inputverification.getText().toString();

                if(TextUtils.isEmpty(verificationcode))
                {
                    Toast.makeText(Phone_login_Activity.this,"please write verification code first..!",Toast.LENGTH_SHORT).show();
                }
                else
                {

                    loadingBar.setTitle("code verification");
                    loadingBar.setMessage("please wait, while we are verify...!");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mverificationid, verificationcode);
                            signInWithPhoneAuthCredential(credential);

                }
            }
        });

        callbacks =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                 signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(Phone_login_Activity.this,"Invalid phone number, please enter valid phone number with your country code...!",Toast.LENGTH_SHORT).show();
                sendvercode.setVisibility(View.VISIBLE);
                inputphonenumber.setVisibility(View.VISIBLE);

                verifybutton.setVisibility(View.INVISIBLE);
                inputverification.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCodeSent(String verificationId,PhoneAuthProvider.ForceResendingToken token){
                loadingBar.dismiss();
                mverificationid =verificationId ;
                mResendToken =token ;
                Toast.makeText(Phone_login_Activity.this,"code has been sent,please checkout...!",Toast.LENGTH_SHORT).show();
                sendvercode.setVisibility(View.INVISIBLE);
                inputphonenumber.setVisibility(View.INVISIBLE);

                verifybutton.setVisibility(View.VISIBLE);
                inputverification.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Phone_login_Activity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                           loadingBar.dismiss();
                            Toast.makeText(Phone_login_Activity.this,"congragulations, you are logged in successfully...!",Toast.LENGTH_SHORT).show();
                            sendusertomainactivity();
                        }
                        else
                        {
                          String msg=task.getException().toString();
                            Toast.makeText(Phone_login_Activity.this,"Error:" +msg,Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void sendusertomainactivity()
    {
        Intent mainintent=new Intent(Phone_login_Activity.this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}
