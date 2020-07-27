package com.example.payapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.payapp.Models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    ImageView back;TextView save;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    EditText name, email;
    CircleImageView dp;
    TextView chng;
    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageRef;
    String pass;
    boolean dog = false;
    ProgressDialog loadingbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        back = findViewById(R.id.close);
        save = findViewById(R.id.save);
        name = findViewById(R.id.name);
        email = findViewById(R.id.username);
        dp = findViewById(R.id.image_profile);
        chng = findViewById(R.id.tv_change);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("images");
        loadingbar = new ProgressDialog(this);
        loadingbar.setTitle("Logging In");
        loadingbar.setMessage("Please wait...");
        loadingbar.setCanceledOnTouchOutside(false);

        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditProfile.this, MainActivity.class));
            }
        });

        DatabaseReference reff = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        if(firebaseUser.getUid()!=null){
            reff.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    email.setText(dataSnapshot.child("email").getValue().toString());
                    name.setText(dataSnapshot.child("name").getValue().toString());
                    Glide.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).into(dp);
                    pass = dataSnapshot.child("password").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        chng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dog = true;
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                        .start(EditProfile.this);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingbar.show();
                final String name1 = name.getText().toString();
                final String email1 = email.getText().toString();

                if(name1.equals("") || email1.equals("")){
                    Toast.makeText(EditProfile.this, "Enter all details", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(dog){
                        firebaseUser = auth.getCurrentUser();
                        String jam = "";
                        if (getFileExtension(mImageUri) == null)
                            jam = "jpg";
                        else jam = getFileExtension(mImageUri);
                        final StorageReference filereference = storageRef.child(System.currentTimeMillis() + "." + jam);
                        uploadTask = filereference.putFile(mImageUri);

                        uploadTask.continueWithTask(new Continuation() {
                            @Override
                            public Object then(@NonNull Task task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return filereference.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUrl = task.getResult();
                                    String myUrl = downloadUrl.toString();

                                    User user = new User(name1, myUrl, pass, firebaseUser.getUid(), email1);

                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                    reference.child(firebaseUser.getUid()).setValue(user)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    loadingbar.dismiss();
                                                    startActivity(new Intent(EditProfile.this, MainActivity.class));
                                                }
                                            });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfile.this, "Error", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                                }
                        });
                    }
                    else{
                        HashMap<String ,Object> m =new HashMap<>();
                        m.put("name",name1);
                        m.put("email",email1);
                        DatabaseReference rf = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
                        if(firebaseUser.getUid()!=null){
                            rf.updateChildren(m);
                        }
                        loadingbar.dismiss();
                    }
                }
            }


        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)     {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                CropImage.ActivityResult result2 = CropImage.getActivityResult(data);
                mImageUri = result2.getUri();
                Glide.with(getApplicationContext()).load(mImageUri).into(dp);
                //uploadImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("Error crop:",error.toString());
            }
        }
    }
}
