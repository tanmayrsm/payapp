package com.example.payapp.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.payapp.Grant;
import com.example.payapp.Models.Paid;
import com.example.payapp.Models.User;
import com.example.payapp.R;
import com.example.payapp.Request;
import com.example.payapp.Transfer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllHistoryAdapter extends RecyclerView.Adapter<AllHistoryAdapter.AllUsersViewholder> {

    Activity context;
    ArrayList<Paid> userArrayList;
    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    boolean busy  = false;
    String uska_id;

    DatabaseReference reference3 = FirebaseDatabase.getInstance().getReference().child("Calls");
    DatabaseReference ref_call_user = FirebaseDatabase.getInstance().getReference().child("Calls");


    public AllHistoryAdapter(Activity context ,ArrayList<Paid> userArrayList){
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @Override
    public AllUsersViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notts_history,parent,false);
        AllUsersViewholder allUsersAdapter = new AllUsersViewholder(view);

        return allUsersAdapter;
    }

    @Override
    public void onBindViewHolder(final AllUsersViewholder holder, int position) {
        final Paid paid = userArrayList.get(position);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        String userid = paid.getHis_id();
        Log.e("GOTTTTTT",userid + " ha");
        if(userid!=null) {



            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.user_ka_naam.setText(dataSnapshot.child("name").getValue().toString());
                    if(paid.getState().equals("received"))
                        holder.typo.setImageResource(R.drawable.ic_call_received_black_24dp);

                    holder.timo.setText(paid.getTime());
                    holder.Amt.setText(paid.getAmt());
                    holder.Desc.setText(paid.getDesc());

                    if (dataSnapshot.child("image").getValue().toString() != null)
                        Glide.with(context).load(dataSnapshot.child("image").getValue().toString()).into(holder.prof_image);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class AllUsersViewholder extends RecyclerView.ViewHolder{
        TextView user_ka_naam, timo, Amt, Desc;

        String my_name ,my_id;

        CircleImageView prof_image;
        ImageView typo;
        LinearLayout vu;

        public AllUsersViewholder(View itemView) {
            super(itemView);
            user_ka_naam = itemView.findViewById(R.id.itemName);
            prof_image = itemView.findViewById(R.id.itemImage);
            vu = itemView.findViewById(R.id.view_him);
            typo = itemView.findViewById(R.id.itemType);
            timo = itemView.findViewById(R.id.tym);
            Amt = itemView.findViewById(R.id.amt);
            Desc = itemView.findViewById(R.id.desc);

//            vu.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    User user = userArrayList.get(getAdapterPosition());
//                    auth = FirebaseAuth.getInstance();
//                    firebaseUser = auth.getCurrentUser();
//
//                    DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Transactions");
//                    // check if he has any pending history with user
//                    reff.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            User user = userArrayList.get(getAdapterPosition());
//                            auth = FirebaseAuth.getInstance();
//                            firebaseUser = auth.getCurrentUser();
//
//                            if(dataSnapshot.child("Request").child(firebaseUser.getUid()).child(user.getUserid()).exists()){
//                                Intent i = new Intent(context, Request.class);
//                                i.putExtra("USERID", user.getUserid());
//                                context.startActivity(i);
//                            }
//                            else if(dataSnapshot.child("Grant").child(firebaseUser.getUid()).child(user.getUserid()).exists()){
//                                Intent i = new Intent(context, Grant.class);
//                                i.putExtra("USERID", user.getUserid());
//                                context.startActivity(i);
//                            }else{
//                                Intent i = new Intent(context, Transfer.class);
//                                i.putExtra("USERID", user.getUserid());
//                                context.startActivity(i);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//                    //reff.child("Request").child(firebaseUser.getUid()).child(user.getUserid())
//
//
//
//                }
//            });


        }
    }
}

