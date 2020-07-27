package com.example.payapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Request extends AppCompatActivity {
    String his_id;
    TextView naam, amount;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    ImageView Back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();


        his_id = getIntent().getStringExtra("USERID");
        naam = findViewById(R.id.naav);
        amount = findViewById(R.id.amt);
        Back = findViewById(R.id.back);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        DatabaseReference reff = FirebaseDatabase.getInstance().getReference("Users").child(his_id).child("name");
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                naam.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reff2 = FirebaseDatabase.getInstance().
                getReference("Transactions").child("Request").child(firebaseUser.getUid()).child(his_id);
        reff2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                amount.setText("Amount to be paid by "+ naam.getText().toString() +" is "+dataSnapshot.child("amount").getValue().toString() + " ₹");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Request.this,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });



    }
}
