package com.example.payapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.payapp.Models.Paid;
import com.example.payapp.Models.Transactions;
import com.example.payapp.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Transfer extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    String his_id, my_id;
    TextView Name, Email;
    Spinner sp;
    CircleImageView im;
    ImageView Back;
    Button Done;
    EditText amt, Desc;
    int index;
    String[] typoo = {"Request", "Grant"};
    String typee = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        Name = findViewById(R.id.names);
        Email = findViewById(R.id.emails);
        sp = findViewById(R.id.spin);
        im = findViewById(R.id.image);
        Done = findViewById(R.id.done);
        amt = findViewById(R.id.amount);
        Desc = findViewById(R.id.desc);
        Back = findViewById(R.id.back);

        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        his_id = getIntent().getStringExtra("USERID");
        my_id = firebaseUser.getUid();

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Transfer.this ,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(his_id);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Name.setText(dataSnapshot.child("name").getValue().toString());
                Email.setText(dataSnapshot.child("email").getValue().toString());
                Glide.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).into(im);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = sp.getSelectedItem().toString();
                index = getIndexOf(typoo,text);
                typee = typoo[index];

                if (amt.getText().toString().equals(""))
                    Toast.makeText(Transfer.this, "Please enter some amount", Toast.LENGTH_SHORT).show();
                else if(typee.equals("Request") && Desc.getText().toString().equals("")){
                    Toast.makeText(Transfer.this, "Enter the description", Toast.LENGTH_SHORT).show();
                }
                else{

                    final AlertDialog alertDialogCall = new AlertDialog.Builder(Transfer.this).create();
                    alertDialogCall.setCanceledOnTouchOutside(false);
                    alertDialogCall.setTitle("Alert");
                    alertDialogCall.setMessage("Are you sure you want to "  +typee+" : "+amt.getText().toString()+" to "+ Name.getText().toString());
                    alertDialogCall.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(typee.equals("Request")){
                                Transactions transactions = new Transactions(his_id,typee,amt.getText().toString(),Desc.getText().toString());
                                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Transactions");

                                ref2.child("Request").child(my_id).child(his_id).setValue(transactions)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Transactions");
                                                Transactions transactions2 = new Transactions(my_id,typee,amt.getText().toString(),Desc.getText().toString());

                                                ref2.child("Grant").child(his_id).child(my_id).setValue(transactions2)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                final DatabaseReference df = FirebaseDatabase.getInstance().getReference().child("History");
                                                                final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                                                final String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                                                                final String key = currentDate + " " + currentTime;

                                                                Paid p = new Paid(amt.getText().toString(),Desc.getText().toString(),his_id,currentDate + " " + currentTime,"given","no");
                                                                df.child(my_id).child(key).setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        //given matlb de
                                                                        DatabaseReference dataRe2 = FirebaseDatabase.getInstance().getReference().child("Notifications");
                                                                        Paid p3 = new Paid(amt.getText().toString(),Desc.getText().toString(),my_id,currentDate + " " + currentTime,"given","no");
                                                                        dataRe2.child(his_id).child(key).setValue(p3).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(!task.isSuccessful())
                                                                                    Toast.makeText(Transfer.this, "Notification not sent to him", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                            }
                                        });
                                Toast.makeText(Transfer.this, typee + " done", Toast.LENGTH_SHORT).show();
                                alertDialogCall.dismiss();
                                startActivity(new Intent(Transfer.this, MainActivity.class));
                            }
                            else{
                                final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child("History");

                                final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                final String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                                final String key = currentDate + " " + currentTime;


                                Paid p = new Paid(amt.getText().toString(),Desc.getText().toString(),his_id,currentDate + " " + currentTime,"received","");
                                dataRef.child(my_id).child(key).setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Paid p2 = new Paid(amt.getText().toString(),Desc.getText().toString(),my_id,currentDate + " " + currentTime,"received","");
                                        dataRef.child(his_id).child(key).setValue(p2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    Toast.makeText(Transfer.this, "Payment successfully done", Toast.LENGTH_SHORT).show();


                                                        // TODO - add in notification list for receiver..that he received some money
                                                        final DatabaseReference dataRef2 = FirebaseDatabase.getInstance().getReference().child("Notifications");
                                                        Paid p3 = new Paid(amt.getText().toString(),Desc.getText().toString(),my_id,currentDate + " " + currentTime,"received","no");
                                                        dataRef2.child(his_id).child(key).setValue(p3).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(!task.isSuccessful())
                                                                    Toast.makeText(Transfer.this, "Notification not sent to him", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });


                                                }
                                                else
                                                    Toast.makeText(Transfer.this, "Unsuccessful payment", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });


//                                ref2.child("Request").child(my_id).child(his_id).setValue(transactions)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Transactions");
//                                                Transactions transactions2 = new Transactions(my_id,typee,amt.getText().toString(),Desc.getText().toString());
//
//                                                ref2.child("Grant").child(his_id).child(my_id).setValue(transactions2)
//                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//
//                                                            }
//                                                        });
//                                            }
//                                        });
                                Toast.makeText(Transfer.this, typee + " done", Toast.LENGTH_SHORT).show();
                                alertDialogCall.dismiss();
                                Intent i = new Intent(Transfer.this ,MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            }

                        }
                    });
                    alertDialogCall.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialogCall.dismiss();
                        }
                    });
                    alertDialogCall.show();
                }
            }
        });

    }

    public static int getIndexOf(String[] strings, String item) {
        for (int i = 0; i < strings.length; i++) {
            if (item.equals(strings[i])) return i;
        }
        return -1;
    }

}
