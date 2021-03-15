package com.conestoga.househunt.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.conestoga.househunt.Adapters.MyAdapter;
import com.conestoga.househunt.Model.Property;
import com.conestoga.househunt.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentTabsFavorite extends Fragment {

    RecyclerView recyclerView;
    private List<Property> properties;
    private MyAdapter myAdapter;
    private DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;


    public FragmentTabsFavorite() {
    }

    public static FragmentTabsFavorite newInstance() {
        FragmentTabsFavorite fragment = new FragmentTabsFavorite();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View main_view = inflater.inflate(R.layout.fragment_tabs_favorite, container, false);

        recyclerView = main_view.findViewById(R.id.recyclerItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        properties = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user!=null) {
            if (user.getEmail() != null) {
                databaseReference = FirebaseDatabase.getInstance().getReference(user.getUid()).child("FavListing");
            }
        }


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Property upload = postSnapshot.getValue(Property.class);
                    properties.add(upload);
                }
                myAdapter = new MyAdapter(getActivity(),properties);
                recyclerView.setAdapter(myAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return main_view;
    }
}