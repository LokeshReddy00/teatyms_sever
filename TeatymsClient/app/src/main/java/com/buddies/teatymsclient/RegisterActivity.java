package com.buddies.teatymsclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {


    private ImageView profileIv;
    private EditText nameEt, phoneEt, emailEt, passwordEt;
    private Button registerBtn;
    private TextView seller;
    private android.widget.RadioGroup RadioGroup;
    private RadioButton radioButton;


    private static  final int CAMERA_REQUEST_CODE = 200;
    private static  final int STORAGE_REQUEST_CODE = 300;
    private static  final int IMAGE_PICK_GALLERY_CODE = 400;
    private static  final int IMAGE_PICK_CAMERA_CODE = 500;


    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri image_uri;



    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        profileIv = findViewById(R.id.shopping_profileIv);
        nameEt = findViewById(R.id.shopnameedt);
        phoneEt = findViewById(R.id.shopphoneedt);
        passwordEt = findViewById(R.id.shoppassword);
        emailEt = findViewById(R.id.shopemail);
        registerBtn = findViewById(R.id.register_Shopedt);
        seller = findViewById(R.id.seller);
        RadioGroup = findViewById(R.id.RadioGroup);


        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = firebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

        seller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });
    }

        private String fullname, phoneNumber, email,password;
        private int selectId;
        private void inputData() {

            selectId = RadioGroup.getCheckedRadioButtonId();
            radioButton = (RadioButton)findViewById(selectId);
            fullname = nameEt.getText().toString().trim();
            phoneNumber = phoneEt.getText().toString().trim();
            email = emailEt.getText().toString().trim();
            password = passwordEt.getText().toString().trim();

            if(radioButton.getText() == null){
                Toast.makeText(this, "Gender Required...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(fullname)) {
                Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Enter Phone Number...", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
                return;
            }
            if(password.length()<6){
                Toast.makeText(this, "Password must be atleast 6 characters long...", Toast.LENGTH_SHORT).show();
                return;
            }
            createaccount();
        }

        private void createaccount() {
            progressDialog.setMessage("Creating Account.....");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            savefirebasedata();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void savefirebasedata() {
            progressDialog.setMessage("Saving Account");

            final String timestamp = ""+System.currentTimeMillis();

            if(image_uri ==null){


                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("uid",""+firebaseAuth.getUid());
                hashMap.put("email",""+email);
                hashMap.put("name",""+fullname);
                hashMap.put("phone",""+phoneNumber);
                hashMap.put("timestamp",""+timestamp);
                hashMap.put("Gender",""+radioButton.getText().toString());
                hashMap.put("profileImage","");

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(firebaseAuth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterActivity.this,DashBoardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterActivity.this,DashBoardActivity.class));
                        finish();
                    }
                });
            }else {
                String filePathAndName = "profile_image/" + ""+firebaseAuth.getUid();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
                storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadImageUri = uriTask.getResult();

                        if(uriTask.isSuccessful()){



                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid",""+firebaseAuth.getUid());
                            hashMap.put("email",""+email);
                            hashMap.put("name",""+fullname);
                            hashMap.put("phone",""+phoneNumber);
                            hashMap.put("timestamp",""+timestamp);
                            hashMap.put("Gender",""+radioButton.getText().toString());
                            hashMap.put("profileImage",""+downloadImageUri);
//

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(firebaseAuth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    startActivity(new Intent(RegisterActivity.this,DashBoardActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    startActivity(new Intent(RegisterActivity.this,DashBoardActivity.class));
                                    finish();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


        private void showImagePickDialog() {
            String[] options = {"Camera", "Gallery"};


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        if(checkcameraPermission()){
                            pickFromCamera();
                        }else {
                            requestCamerPermission();
                        }
                    }else {
                        if(checkstoragePermission()){
                            pickFromGallery();
                        }else {
                            requestStoragePermission();
                        }
                    }
                }
            }).show();
        }

        private void pickFromGallery(){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
        }

        private void pickFromCamera(){
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image title");
            contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image description");


            image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

        }



        private boolean checkstoragePermission(){
            boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

            return result;
        }

        private void requestStoragePermission(){
            ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);

        }

        private boolean checkcameraPermission(){
            boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
            boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

            return result && result1;
        }
        private void requestCamerPermission(){
            ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUEST_CODE);

        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            switch (requestCode){
                case CAMERA_REQUEST_CODE:{
                    if(grantResults.length>0){
                        boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        if(cameraAccepted && storageAccepted){
                            pickFromCamera();
                        }else {
                            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
                case STORAGE_REQUEST_CODE:{
                    if(grantResults.length>0){
                        boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if(storageAccepted){
                            pickFromGallery();
                        }else {
                            Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            if (resultCode == RESULT_OK) {
                if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                    image_uri = data.getData();
                    profileIv.setImageURI(image_uri);
                } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                    profileIv.setImageURI(image_uri);
                }

                super.onActivityResult(requestCode, resultCode, data);
            }
        }

        @Override
        protected void onStart() {
            super.onStart();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user !=null){
                Intent intent = new Intent(RegisterActivity.this,DashBoardActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }