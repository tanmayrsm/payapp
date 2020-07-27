package com.example.payapp.Adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.payapp.Grant;
import com.example.payapp.HomeFragment;
import com.example.payapp.Login;
import com.example.payapp.MainActivity;
import com.example.payapp.Models.User;
import com.example.payapp.R;
import com.example.payapp.Request;
import com.example.payapp.Transfer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewholder> {

    Activity context;
    ArrayList<User> userArrayList;
    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    boolean busy  = false;
    String uska_id;

    DatabaseReference reference3 = FirebaseDatabase.getInstance().getReference().child("Calls");
    DatabaseReference ref_call_user = FirebaseDatabase.getInstance().getReference().child("Calls");


    public AllUsersAdapter(Activity context ,ArrayList<User> userArrayList){
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @Override
    public AllUsersViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users,parent,false);
        AllUsersViewholder allUsersAdapter = new AllUsersViewholder(view);

        return allUsersAdapter;
    }

    @Override
    public void onBindViewHolder(AllUsersViewholder holder, int position) {
        User user = userArrayList.get(position);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        holder.user_ka_naam.setText(user.getName());
        if(user.getImage()!=null)
            Glide.with(context).load(user.getImage()).into(holder.prof_image);
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class AllUsersViewholder extends RecyclerView.ViewHolder{
        TextView user_ka_naam;

        String my_name ,my_id;

        CircleImageView prof_image;
        //ImageView onlinu;
        LinearLayout vu;

        public AllUsersViewholder(View itemView) {
            super(itemView);
            user_ka_naam = itemView.findViewById(R.id.itemName);
            prof_image = itemView.findViewById(R.id.itemImage);
            vu = itemView.findViewById(R.id.view_him);


            vu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    User user = userArrayList.get(getAdapterPosition());
                    auth = FirebaseAuth.getInstance();
                    firebaseUser = auth.getCurrentUser();

                    DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Transactions");
                    // check if he has any pending history with user
                    reff.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = userArrayList.get(getAdapterPosition());
                            auth = FirebaseAuth.getInstance();
                            firebaseUser = auth.getCurrentUser();

                            if(dataSnapshot.child("Request").child(firebaseUser.getUid()).child(user.getUserid()).exists()){
                                Intent i = new Intent(context , Request.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.putExtra("USERID", user.getUserid());
                                context.startActivity(i);
                                context.finish();

                            }
                            else if(dataSnapshot.child("Grant").child(firebaseUser.getUid()).child(user.getUserid()).exists()){
                                Intent i = new Intent(context, Grant.class);
                                i.putExtra("USERID", user.getUserid());
                                context.startActivity(i);
                            }else{
                                Intent i = new Intent(context, Transfer.class);
                                i.putExtra("USERID", user.getUserid());
                                context.startActivity(i);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    //reff.child("Request").child(firebaseUser.getUid()).child(user.getUserid())



                }
            });


        }
    }
}
