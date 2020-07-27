package com.example.payapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.payapp.Adapters.AllHistoryAdapter;
import com.example.payapp.Adapters.AllUsersAdapter;
import com.example.payapp.Models.Paid;
import com.example.payapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    RecyclerView rc;
    EditText srch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history,container,false);
        final FragmentActivity c = getActivity();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        srch = view.findViewById(R.id.search);

        rc = view.findViewById(R.id.recyclerView);
            rc.setLayoutManager(new LinearLayoutManager(c));

        fetchAllUsers();

            srch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(srch.getText().toString().length() > 0){
                    fetchSearchedUsers(srch.getText().toString());
                }else{
                    fetchAllUsers();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
            rc.setHasFixedSize(true);
            return view;
}

    private void fetchAllUsers() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("History").child(firebaseUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Paid> userArrayList = new ArrayList<>();
                userArrayList.clear();

                if(!dataSnapshot.exists()){
                    Toast.makeText(getActivity(), "No history registered", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e("all contacts efore","userarraylist:"+userArrayList.toString());

                auth = FirebaseAuth.getInstance();
                firebaseUser = auth.getCurrentUser();

                if(firebaseUser.getUid()!=null)
                    for(DataSnapshot dss : dataSnapshot.getChildren()){

                        Paid paid = dss.getValue(Paid.class);
//                      Log.e("user and fireid:",user.getUserid()+" --" +firebaseUser.getUid());

                        userArrayList.add(paid);
                        Log.e("Added:",paid.getHis_id());

                        AllHistoryAdapter adapter = new AllHistoryAdapter(getActivity(), userArrayList);
                        rc.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void fetchSearchedUsers(final String s) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<User> userArrayList = new ArrayList<>();
                userArrayList.clear();

                if(!dataSnapshot.exists()){
                    Toast.makeText(getActivity(), "No users registered", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(DataSnapshot dss : dataSnapshot.getChildren()){

                    User user = dss.getValue(User.class);
                    Log.e("user and fireid:",user.getUserid()+" --" +firebaseUser.getUid());

                    if(!user.getUserid().equals(firebaseUser.getUid()) ){
                        if(user.getName().toLowerCase().contains(s.toLowerCase()))
                            userArrayList.add(user);
                        Log.e("Added:",user.getEmail());
                    }

                    AllUsersAdapter adapter = new AllUsersAdapter(getActivity() ,userArrayList);
                    rc.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                //Log.e("all contacts","userarraylist:"+userArrayList.toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "error h :"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
