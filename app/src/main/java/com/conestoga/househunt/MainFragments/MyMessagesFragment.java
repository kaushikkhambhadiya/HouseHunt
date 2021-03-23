package com.conestoga.househunt.MainFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.conestoga.househunt.Adapters.MessagesListAdapter;
import com.conestoga.househunt.Model.ChatMessage;
import com.conestoga.househunt.Model.SenderDetails;
import com.conestoga.househunt.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class MyMessagesFragment extends Fragment {

    RecyclerView chat_recycleview;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private DatabaseReference mDatabase;
    private ArrayList<SenderDetails> chat_history_list;
    private ArrayList<ChatMessage> chat_messages;
    MessagesListAdapter mAdapter;

    public MyMessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View main_view =  inflater.inflate(R.layout.fragment_my_messages, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("Chat_Room").child(user.getUid());

        chat_recycleview = main_view.findViewById(R.id.chat_recycleview);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    chat_history_list = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final SenderDetails senderDetails = postSnapshot.getValue(SenderDetails.class);

                        assert senderDetails != null;
                        mDatabase.child(senderDetails.getSendid()).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                chat_messages = new ArrayList<>();
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    ChatMessage chatMessage = postSnapshot.getValue(ChatMessage.class);
                                    chat_messages.add(chatMessage);
                                    assert chatMessage != null;
                                    senderDetails.setLastmessage(chatMessage.getMessage());
                                }

                                chat_history_list.add(senderDetails);
                                mAdapter = new MessagesListAdapter(getActivity(), chat_history_list);
                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                                chat_recycleview.setLayoutManager(mLayoutManager);
                                chat_recycleview.setItemAnimator(new DefaultItemAnimator());
                                chat_recycleview.setAdapter(mAdapter);
                                chat_recycleview.scrollToPosition(chat_history_list.size() - 1);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.getMessage();
            }
        });

        return main_view;
    }
}