package com.example.payapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.payapp.Models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;


public class Register extends AppCompatActivity {
    private EditText Name, Email, Password;
    private Button Register;
    private CircleImageView Image;
    private TextView Loginn;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    private Uri mImageUri;
    private  String name,email,password;
    private StorageTask uploadTask;
    StorageReference storageRef;
    ProgressDialog loadingbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Name = findViewById(R.id.name);
        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        Register = findViewById(R.id.register_button);
        Image = findViewById(R.id.image);
        Loginn = findViewById(R.id.logiin);

        loadingbar = new ProgressDialog(this);
        loadingbar.setTitle("Registering");
        loadingbar.setMessage("Please wait...");
        loadingbar.setCanceledOnTouchOutside(false);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();



        storageRef = FirebaseStorage.getInstance().getReference("images");


        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");


        if(mImageUri!=null){
            Glide.with(getApplicationContext()).load(mImageUri).into(Image);
        }

        Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                        .start(Register.this);
            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = Name.getText().toString();
                email = Email.getText().toString();
                password = Password.getText().toString();
                loadingbar.show();

                if(mImageUri == null){
                    Toast.makeText(Register.this, "Please select image", Toast.LENGTH_SHORT).show();
                }
                else if(name.equals("") || email.equals("") || password.equals("")){
                    Toast.makeText(Register.this, "Enter all details", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
                else{
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                firebaseUser = auth.getCurrentUser();
                                String jam = "";
                                if(getFileExtension(mImageUri) == null)
                                    jam = "jpg";
                                else    jam = getFileExtension(mImageUri);
                                final StorageReference filereference = storageRef.child(System.currentTimeMillis() + "." +jam);
                                uploadTask = filereference.putFile(mImageUri);

                                uploadTask.continueWithTask(new Continuation() {
                                    @Override
                                    public Object then(@NonNull Task task) throws Exception {
                                        if(!task.isSuccessful()){
                                            throw task.getException();
                                        }
                                        return filereference.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            loadingbar.dismiss();
                                            Uri downloadUrl = task.getResult();
                                            String myUrl = downloadUrl.toString();

                                            User user = new User(name, myUrl, password, firebaseUser.getUid(),email);

                                            reference.child(firebaseUser.getUid()).setValue(user)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            startActivity(new Intent(Register.this, Login.class));
                                                        }
                                                    });

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Register.this, "Error", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }else{
                                String error2 = task.getException().getMessage();
                                Toast.makeText(Register.this, "something Fishy: "+error2, Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }

                        }
                    });
                }

            }
        });

        Loginn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                CropImage.ActivityResult result2 = CropImage.getActivityResult(data);
                mImageUri = result2.getUri();
                Glide.with(getApplicationContext()).load(mImageUri).into(Image);
                //uploadImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("Error crop:",error.toString());
            }
        }
    }
}
