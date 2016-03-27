package com.askhmer.chat.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.askhmer.chat.R;
import com.askhmer.chat.adapter.SecretChatRecyclerAdapter;
import com.askhmer.chat.model.Friends;

import java.util.ArrayList;

public class SecretChat extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private int position;
    private ArrayList<Friends> mFriends;

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_chat);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setTitle("select a friend");

        // Change from Navigation menu item image to arrow back image of toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Event Menu Item Back
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Setup layout manager for mBlogList and column count
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Bind adapter to recycler
        mFriends = new ArrayList<>();

        //list item
        for (int i = 0; i < 15; i++) {
            Friends item = new Friends();
            item.setFriName("Friend : " + i);
            item.setImg(R.drawable.ic_people);
            item.setChatId("chat Id : 000"+i);
            mFriends.add(item);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        SecretChatRecyclerAdapter adapter = new SecretChatRecyclerAdapter(mFriends);
        mRecyclerView.setAdapter(adapter);

    }
}
