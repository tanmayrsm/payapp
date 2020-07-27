package com.example.payapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    TextView Hi;
    DatabaseReference databaseReference;
    CircleImageView dp;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();



        bottomNavigationView = findViewById(R.id.navigation_menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        dp = findViewById(R.id.dp_toolbar2);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if(firebaseUser.getUid()!=null){
            DatabaseReference ro = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("image");
            ro.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Glide.with(getApplicationContext()).load(dataSnapshot.getValue().toString()).into(dp);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EditProfile.class));
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    switch (menuItem.getItemId()){
                        case R.id.action_home :
                            selectedFragment = new HomeFragment();
                            break;
                        case  R.id.action_notifications :
                            selectedFragment = new NotificationsFragment();
                            break;
                        case R.id.action_history:
                            selectedFragment = new HistoryFragment();
                            break;
                        case R.id.action_logout :
                            if(firebaseUser != null){

                                final AlertDialog alertDialogCall = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialogCall.setCanceledOnTouchOutside(false);
                                alertDialogCall.setTitle("Alert");
                                alertDialogCall.setMessage("Are you sure you want to logout ");
                                alertDialogCall.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        auth.signOut();
                                        Paper.book().destroy();
                                        alertDialogCall.dismiss();
                                        Intent io = new Intent(MainActivity.this,Login.class);
                                        io.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(io);
                                        finish();
                                    }
                                });
                                alertDialogCall.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        alertDialogCall.dismiss();
                                    }
                                });
                                alertDialogCall.show();

                            }
                            selectedFragment = new HomeFragment();
                            break;

                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;

                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_logout){
            // do something

            if(firebaseUser != null){
                auth.signOut();
                Paper.book().destroy();

                Intent i = new Intent(MainActivity.this,Login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();

            }

        }
        else if(id == R.id.menu_pay){
            startActivity(new Intent(MainActivity.this, Pay.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
