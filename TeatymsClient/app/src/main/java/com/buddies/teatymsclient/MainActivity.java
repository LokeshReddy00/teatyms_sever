package com.buddies.teatymsclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    EditText passEt, emailEt;
    Button register_btn, login_btn;
    CheckBox checkBox;
    ProgressBar progressBar;
    TextView forgotPasswordTv;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEt = findViewById(R.id.login_email_et);
        passEt = findViewById(R.id.login_password_et);
        login_btn = findViewById(R.id.button_login);
        register_btn = findViewById(R.id.button_signup);
        checkBox = findViewById(R.id.login_checkbox);
        forgotPasswordTv = findViewById(R.id.forgotPasswordTv);
        progressBar = findViewById(R.id.progressbar_login);
        mAuth = FirebaseAuth.getInstance();

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    passEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    //    confirm_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {

                    passEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //  confirm_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEt.getText().toString();
                String pass = passEt.getText().toString();
                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(pass)){
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                               checkUserType();
                            }else {
                                String error = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, "Error :"+error, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });


                }else {
                    Toast.makeText(MainActivity.this, "please fill all fields", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void sendtoMain() {
        Intent intent = new Intent(MainActivity.this,DashBoardActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkUserType() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Data");
        ref.child("Users").child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String AccountType = ""+snapshot.child("AccountType").getValue();
                        if(AccountType.equals("poster")){
                            sendtoMain();
                        }else {
                            Toast.makeText(MainActivity.this, "Please contact with the Teatyms to get access", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user !=null){
            Intent intent = new Intent(MainActivity.this,DashBoardActivity.class);
            startActivity(intent);
            finish();
        }
    }
}