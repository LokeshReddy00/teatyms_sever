package com.buddies.teatymsclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class DashBoardActivity extends AppCompatActivity {


    private EditText pTitleEt,pDescriptionEt,pLink,source,image;
//    private ImageView pImageIv;
    private Button pUpload;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    FirebaseUser user;
    ActionBar actionBar;
    private RadioButton radioButton;

    private static  final int CAMERA_REQUEST_CODE = 200;
    private static  final int STORAGE_REQUEST_CODE = 300;
    private static  final int IMAGE_PICK_GALLERY_CODE = 400;
    private static  final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri image_uri;
    String link;

    ProgressDialog pd;
    private RadioButton News,life,ent,tech,food,story,poli,bus,edu,Trending,gen;

    String name, email, uid, dp,s,sour,img;


    String editTitle, editDescription, editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
//        pImageIv = findViewById(R.id.pImageIv);
        pTitleEt = findViewById(R.id.pTitleEt);
        pDescriptionEt = findViewById(R.id.pDescriptionEt);
        pLink = findViewById(R.id.pLink);
        pUpload = findViewById(R.id.pUpload);
        actionBar = getSupportActionBar();
        News = findViewById(R.id.News);
        life = findViewById(R.id.life);
        ent = findViewById(R.id.ent);
        tech = findViewById(R.id.tech);
        gen = findViewById(R.id.gen);
        food = findViewById(R.id.food);
        story = findViewById(R.id.story);
        image = findViewById(R.id.image);
        poli = findViewById(R.id.poli);
        bus = findViewById(R.id.bus);
        edu = findViewById(R.id.edu);
        source = findViewById(R.id.source);
        Trending = findViewById(R.id.Trending);
        actionBar.setSubtitle(email);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();


        Intent intent = getIntent();

        String action = intent.getAction();
        String type = intent.getType();

        if(Intent.ACTION_SEND.equals(action) && type!=null){
            if("text/plain".equals(type)){
                handelSendText(intent);
            }else if(type.startsWith("image")){
                handelSendImage(intent);
            }
        }

        final String isUpdateKey = ""+intent.getStringExtra("key");
        final String editPostId = ""+intent.getStringExtra("editPostId");

        if(isUpdateKey.equals("editPost")){
            actionBar.setTitle("Update Post");
            pUpload.setText("Update");
            loadPostData(editPostId);
        }else {

            actionBar.setTitle("Add New Post");
            pUpload.setText("Post");


        }

        email = user.getEmail();
        uid = user.getUid();
        name = user.getDisplayName();


        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){

                    name = ""+ds.child("name").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = ""+ds.child("profileImage").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//        pImageIv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showImagePickDialog();
//
//            }
//
//        });

        pUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = pTitleEt.getText().toString().trim();
                String description = pDescriptionEt.getText().toString().trim();
                link = pLink.getText().toString().trim();
                sour = source.getText().toString().trim();
                img = image.getText().toString().trim();


                if(TextUtils.isEmpty(title) ){
                    Toast.makeText(DashBoardActivity.this, "Enter title....", Toast.LENGTH_SHORT).show();
                    return;
                } if(TextUtils.isEmpty(sour) ){
                    Toast.makeText(DashBoardActivity.this, "Enter Source....", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(DashBoardActivity.this, "Enter description", Toast.LENGTH_SHORT).show();
                    return;
                } if(TextUtils.isEmpty(link)){
                    Toast.makeText(DashBoardActivity.this, "Enter Link", Toast.LENGTH_SHORT).show();
                    return;
                }if(TextUtils.isEmpty(img)){
                    Toast.makeText(DashBoardActivity.this, "Enter Link", Toast.LENGTH_SHORT).show();
                    return;
                }


                if(isUpdateKey.equals("editPost")){
//                    beginUpdate(title, description, editPostId);
                }else {
                    uploadData(title, description);

                }

//                if(image_uri==null){
//
//                    uploadData(title, description, "noImage");
//
//                }else {
//                    uploadData(title, description, String.valueOf(image_uri));
//                }

            }
        });
    }

    private void handelSendImage(Intent intent) {
        Uri imageUri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(imageUri !=null){
            image_uri = imageUri;
//            pImageIv.setImageURI(image_uri);
        }
    }

    private void handelSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if(sharedText!=null){
            pDescriptionEt.setText(sharedText);
        }
    }


//    private void beginUpdate(String title, String description, String editPostId, String cat) {
//        pd.setMessage("Updating Post...");
//        pd.show();
//
//        if(!editImage.equals("noImage")){
//            updateWasWithImage(title, description, editPostId, cat);
//        }else  if(pImageIv.getDrawable() != null){
//            updateWasWithoutImage(title, description, editPostId, cat);
//
//        }else {
//
//            updatewithoutImage(title, description, editPostId, cat);
//
//        }
//
//    }

    private void updatewithoutImage(String title, String description, String editPostId, String cat) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescription", description);
        hashMap.put("pImage", "noImage");
        hashMap.put("cat",""+radioButton.getText().toString());
        hashMap.put("pLikes", "0");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(DashBoardActivity.this, "Update....", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(DashBoardActivity.this, "Failed....", Toast.LENGTH_SHORT).show();
            }
        });


    }

//    private void updateWasWithoutImage(String s, final String title, final String description, final String editPostId) {
//
//        StorageReference mPitureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
//        mPitureRef.delete()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                        final String timeStamp = String.valueOf(System.currentTimeMillis());
//                        String filePathAndName = "Posts/" + "post_" + timeStamp;
//
//
//                        Bitmap bitmap = ((BitmapDrawable) pImageIv.getDrawable()).getBitmap();
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                        byte[] data = baos.toByteArray();
//
//                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
//                        ref.putBytes(data)
//                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                                        while (!uriTask.isSuccessful()) ;
//
//                                        String downloadUri = uriTask.getResult().toString();
//                                        if (uriTask.isSuccessful()) {
//
//                                            HashMap<String, Object> hashMap = new HashMap<>();
//                                            hashMap.put("uid", uid);
//                                            hashMap.put("uName", name);
//                                            hashMap.put("uEmail", email);
//                                            hashMap.put("uDp", dp);
//                                            hashMap.put("pTitle", title);
//                                            hashMap.put("pDescription", description);
//                                            hashMap.put("pImage", downloadUri);
//                                            hashMap.put("cat", ""+radioButton.getText().toString());
//                                            hashMap.put("pLikes", "0");
//
//                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
//                                            ref.child(editPostId).updateChildren(hashMap)
//                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//                                                            pd.dismiss();
//                                                            Toast.makeText(DashBoardActivity.this, "Update....", Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    }).addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    pd.dismiss();
//                                                    Toast.makeText(DashBoardActivity.this, "Failed....", Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//
//                                        }
//
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                pd.dismiss();
//                                Toast.makeText(DashBoardActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
//
//                    }
//                });
//    }

//    private void updateWasWithImage(String s, final String title, final String description, final String editPostId) {
//
//        StorageReference mPitureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
//        mPitureRef.delete()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                        final String timeStamp = String.valueOf(System.currentTimeMillis());
//                        String filePathAndName = "Posts/" + "post_" + timeStamp;
//                        Bitmap bitmap = ((BitmapDrawable) pImageIv.getDrawable()).getBitmap();
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                        byte[] data = baos.toByteArray();
//                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
//                        ref.putBytes(data)
//                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                                        while (!uriTask.isSuccessful()) ;
//
//                                        String downloadUri = uriTask.getResult().toString();
//                                        if (uriTask.isSuccessful()) {
//
//                                            HashMap<String, Object> hashMap = new HashMap<>();
//                                            hashMap.put("uid", uid);
//                                            hashMap.put("uName", name);
//                                            hashMap.put("uEmail", email);
//                                            hashMap.put("uDp", dp);
//                                            hashMap.put("pTitle", title);
//                                            hashMap.put("pDescription", description);
//                                            hashMap.put("pImage", downloadUri);
//                                            hashMap.put("cat", ""+radioButton.getText().toString());
//                                            hashMap.put("pLikes", "0");
//
//                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
//                                            ref.child(editPostId).updateChildren(hashMap)
//                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//                                                            pd.dismiss();
//                                                            Toast.makeText(DashBoardActivity.this, "Update....", Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    }).addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    pd.dismiss();
//                                                    Toast.makeText(DashBoardActivity.this, "Failed....", Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//
//                                        }
//
//
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                pd.dismiss();
//                                Toast.makeText(DashBoardActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                pd.dismiss();
//                Toast.makeText(DashBoardActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//
//            }
//        });
//    }


    private void loadPostData(String editPostId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query fQuery = reference.orderByChild("pId").equalTo(editPostId);
        fQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescription").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    pTitleEt.setText(editTitle);
                    pDescriptionEt.setText(editDescription);

                    if(!editImage.equals("noImage")){
                        try {
//                            Picasso.get().load(editImage).into(pImageIv);
                        }catch (Exception e){

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void uploadData( final String title, final String description) {

        pd.setMessage("Publishing post...");
        pd.show();

//        final String timeStamp = String.valueOf(System.currentTimeMillis());

        final String timeStamp1 = String.valueOf(System.currentTimeMillis());

        Calendar cdate = Calendar.getInstance();

        SimpleDateFormat currentdates = new SimpleDateFormat("dd-MMMM-yyyy");

        final String savedate = currentdates.format(cdate.getTime());

        Calendar ctime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");

        final String savetime = currenttime.format(ctime.getTime());

        String timeStamp = "32" + timeStamp1 + savedate + ":" + savetime;
        String date = savedate;

        String filePathAndName = "Posts/" + "post_"+ timeStamp;



//        if(pImageIv.getDrawable() != null){
//
//            Bitmap bitmap = ((BitmapDrawable)pImageIv.getDrawable()).getBitmap();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100,baos);
//            byte[] data = baos.toByteArray();
//
//
//            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
//            ref.putBytes(data)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                            while ((!uriTask.isSuccessful()));
//
//                            String downloadUri = uriTask.getResult().toString();
//                            if(uriTask.isSuccessful()){
//
//
//                                HashMap<String, Object> hashMap = new HashMap<>();
//                                hashMap.put("uid",uid);
//                                hashMap.put("uName",name);
//                                hashMap.put("uEmail",email);
//                                hashMap.put("uDp",dp);
//                                hashMap.put("pId", timeStamp);
//                                hashMap.put("pTitle",title);
//                                hashMap.put("pDescription", description);
//                                hashMap.put("pImage",downloadUri);
//                                hashMap.put("pTime",timeStamp);
//                                hashMap.put("cat",s);
//                                hashMap.put("pLink",link);
//
//                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(s);
//                                reference.child(timeStamp).setValue(hashMap);
//
//
//                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
//                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        pd.dismiss();
//                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
//                                        pTitleEt.setText("");
//                                        pDescriptionEt.setText("");
//                                        image_uri = null;
//                                        pImageIv.setImageURI(null);
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        pd.dismiss();
//
//                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                            }
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    pd.dismiss();
//                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                }
//            });
//
//        }else {
//            HashMap<String, Object> hashMap = new HashMap<>();
//            hashMap.put("uid",uid);
//            hashMap.put("uName",name);
//            hashMap.put("uDp",dp);
//            hashMap.put("pId", timeStamp);
//            hashMap.put("pTitle",title);
//            hashMap.put("pDescription", description);
//            hashMap.put("pImage","noImage");
//            hashMap.put("pTime",timeStamp);
//            hashMap.put("cat",s);
//            hashMap.put("pLink",link);
//
//            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(s);
//            reference1.child(timeStamp).setValue(hashMap);
//
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
//            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    pd.dismiss();
//                    Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
//
//                    pTitleEt.setText("");
//                    pDescriptionEt.setText("");
//                    image_uri = null;
//                    pImageIv.setImageURI(null);
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    pd.dismiss();
//
//                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        }

//        Bitmap bitmap = ((BitmapDrawable)pImageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100,baos);
        byte[] data = baos.toByteArray();

        String downloadUri = img;

        if(News.isChecked()){



            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));


                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("cat","FNews");
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("FNews");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                        News.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        News.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(gen.isChecked()){



            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("cat","FNews");
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("uid",uid);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                        News.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        News.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(life.isChecked()){


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("cat","life");
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("life");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                        life.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        life.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(ent.isChecked()){



            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("cat","entertainment");
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ent");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        ent.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        ent.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(tech.isChecked()){


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("cat","technical");
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tech");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "News published", Toast.LENGTH_SHORT).show();
                                        tech.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        tech.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(food.isChecked()){


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("cat","food");
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("food");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        food.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        food.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(story.isChecked()){


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("cat","story");
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("story");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                       story.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        story.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(poli.isChecked()){


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("cat","politics");
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("poli");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        poli.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        poli.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(bus.isChecked()){


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("cat","business");
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("bus");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        bus.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        bus.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(edu.isChecked()){


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("cat","education");
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("edu");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                    edu.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        edu.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(Trending.isChecked()){


            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while ((!uriTask.isSuccessful()));

//                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){


                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", timeStamp);
                                hashMap.put("title",title);
                                hashMap.put("desc", description);
                                hashMap.put("image",downloadUri);
                                hashMap.put("time",date);
                                hashMap.put("web",link);
                                hashMap.put("source",sour);
                                hashMap.put("cat","Trend");
                                hashMap.put("uid",uid);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("trend");
                                reference.child(timeStamp).setValue(hashMap);


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(DashBoardActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                       Trending.setChecked(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Trending.setChecked(false);
                                        Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DashBoardActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
                    if(!checkcameraPermission()){
                        requestCamerPermission();
                    }else {
                        pickFromCamera();
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

    private void pickFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image description");


        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }


    private boolean checkstoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
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
    public boolean onSupportNavigateUp() {

        onBackPressed();

        return super.onSupportNavigateUp();
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
                        Toast.makeText(this, "Camera & Storage both permissions is required", Toast.LENGTH_SHORT).show();
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
//                pImageIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
//                pImageIv.setImageURI(image_uri);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}