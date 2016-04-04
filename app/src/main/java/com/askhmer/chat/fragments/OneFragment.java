package com.askhmer.chat.fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.askhmer.chat.DividerItemDecoration;
import com.askhmer.chat.R;
import com.askhmer.chat.activity.MainActivityTab;
import com.askhmer.chat.adapter.AddfriendAdapter;
import com.askhmer.chat.adapter.FriendAdapter;
import com.askhmer.chat.model.Friends;
import com.askhmer.chat.model.Contact;

import java.util.ArrayList;
import java.util.List;


public class OneFragment extends Fragment {


    private List<Friends> friendtList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FriendAdapter fAdapter;
    private LinearLayout firstShow;


    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareAddfriendData();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View oneFragmentView = inflater.inflate(R.layout.fragment_one, container, false);

        setHasOptionsMenu(true);


        recyclerView = (RecyclerView) oneFragmentView.findViewById(R.id.recycler_view);
        firstShow = (LinearLayout) oneFragmentView.findViewById(R.id.layout_first_friend);

        fAdapter = new FriendAdapter(friendtList);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fAdapter);


        recyclerView.addOnItemTouchListener(new OneFragment.RecyclerTouchListener(getActivity(), recyclerView, new OneFragment.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Friends friends = friendtList.get(position);
                Toast.makeText(getActivity(), friends.getFriName() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));



        if( friendtList.size()==0){
            firstShow.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            firstShow.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        return oneFragmentView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      // inflater.inflate(R.menu.menu_friend, menu);
       // super.onCreateOptionsMenu(menu, inflater);

        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.menu_friend, menu);


        MenuItem searchItem = menu.findItem(R.id.search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }





    private void prepareAddfriendData() {

        for(int i=0;i<=20;i++) {
            Friends friend = new Friends();
            friend.setFriName("Torn Longdy");
            friend.setChatId("xyz123hangtom");
            friendtList.add(friend);

        }
        /// fAdapter.notifyDataSetChanged();

    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private OneFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final OneFragment.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}
