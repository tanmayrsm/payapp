package com.example.payapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.payapp.Models.Prevalent;
import com.example.payapp.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class Login extends AppCompatActivity {
    private EditText Email,Password;
    private TextView Reg;
    String email,password;
    private Button Login;
    ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        Email = findViewById(R.id.login_email);
        Password = findViewById(R.id.login_password);
        Login = findViewById(R.id.login_button);
        Reg = findViewById(R.id.regiister);
        Reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        loadingbar = new ProgressDialog(this);
        loadingbar.setTitle("Logging In");
        loadingbar.setMessage("Please wait...");
        loadingbar.setCanceledOnTouchOutside(false);


        Paper.init(this);
        String emailUser = Paper.book().read(Prevalent.UserEmailKey__);
        String passo = Paper.book().read(Prevalent.UserPasswordKey__);

        if(emailUser!=null && passo!=null){
            Log.e("haaa",emailUser+" "+passo);
            AllowAccess(emailUser ,passo);
        }


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingbar.show();
                email = Email.getText().toString();
                password = Password.getText().toString();
                if(!email.equals("") || !password.equals("")){
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if(!email.equals("") && !password.equals("")){
                        auth.signInWithEmailAndPassword(email,password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            Paper.book().write(Prevalent.UserEmailKey__ , email);
                                            Paper.book().write(Prevalent.UserPasswordKey__ ,password);
                                            loadingbar.dismiss();
                                            Intent i = new Intent(Login.this ,MainActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(i);
                                            finish();

                                        }
                                        else{
                                            String error2 = task.getException().getMessage();
                                            Toast.makeText(Login.this, "something Fishy: "+error2, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }else{
                    Toast.makeText(Login.this, "Enter all details", Toast.LENGTH_SHORT).show();
                }
                loadingbar.dismiss();
            }
        });
    }



    private void AllowAccess(final String emailUser, final String passo) {
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference("Users");
        loadingbar.show();

        rootref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dss : dataSnapshot.getChildren()) {

                    User user = dss.getValue(User.class);
                    Log.e("Ha",user.getEmail()+" "+emailUser);
                    if(user.getEmail()!=null)
                    if(user.getEmail().equals(emailUser) && user.getPassword().equals(passo)){

                        Paper.book().write(Prevalent.UserEmailKey__ , emailUser);
                        Paper.book().write(Prevalent.UserPasswordKey__ ,passo);
                        loadingbar.dismiss();

                        //loadingbar.dismiss();
                        Intent i = new Intent(Login.this,MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                }
                loadingbar.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //loadingbar.dismiss();
                Toast.makeText(Login.this, "Session destroyed, please Login again", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
