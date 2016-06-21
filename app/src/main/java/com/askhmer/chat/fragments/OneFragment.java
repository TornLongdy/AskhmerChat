package com.askhmer.chat.fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.askhmer.chat.R;
import com.askhmer.chat.adapter.FriendAdapter;
import com.askhmer.chat.adapter.SecretChatRecyclerAdapter;
import com.askhmer.chat.model.Friends;
import com.askhmer.chat.network.API;
import com.askhmer.chat.network.GsonObjectRequest;
import com.askhmer.chat.network.MySingleton;
import com.askhmer.chat.util.SharedPreferencesFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class OneFragment extends Fragment {

    final String TAG = "TAG";
    String myid;
    private SharedPreferencesFile mSharedPrefer;

    private List<Friends> friendtList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private String textSearch;
    private LinearLayout firstShow;


    public OneFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefer = SharedPreferencesFile.newInstance(getContext(), SharedPreferencesFile.PREFER_FILE_NAME);
        myid = mSharedPrefer.getStringSharedPreference(SharedPreferencesFile.USERIDKEY);

        prepareAddfriendData();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View oneFragmentView = inflater.inflate(R.layout.fragment_one, container, false);

        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) oneFragmentView.findViewById(R.id.recycler_view);
        firstShow = (LinearLayout) oneFragmentView.findViewById(R.id.layout_first_friend);


        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(mLayoutManager);



      /********************************************************************************************/
       // recyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);

        /********************************************************************************************/



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
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered

                textSearch = newText;
                adapter.clearData();
                listSearchFriend();

                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                //Here u can get the value "query" which is entered in the search box.
                //Toast.makeText(ActivitySearchSub.this, query , Toast.LENGTH_SHORT).show();

                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void prepareAddfriendData() {

        //***************===<< begin new style >>====******************************

        //String url = "http://10.0.3.2:8080/ChatAskhmer/api/friend/listfriendById/"+ myid;
        String url = API.LISTFRIEND + myid;
        GsonObjectRequest jsonRequest = new GsonObjectRequest(Request.Method.POST, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("DATA")) {
                        JSONArray jsonArray = response.getJSONArray("DATA");
                        //list item
                        for (int i = 0; i < jsonArray.length(); i++) {
                                    Friends item = new Friends();
                                    item.setFriId(jsonArray.getJSONObject(i).getInt("userId"));
                                    item.setFriName(jsonArray.getJSONObject(i).getString("userName"));
                                    item.setChatId(jsonArray.getJSONObject(i).getString("userNo"));
                                    item.setImg(jsonArray.getJSONObject(i).getString("userPhoto"));
                                    friendtList.add(item);
                                }
                    }else{
                        Toast.makeText(getContext(), "No Friend Found !", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    // CustomDialog.hideProgressDialog();

                               adapter = new FriendAdapter(friendtList);
                               adapter.notifyDataSetChanged();
                               recyclerView.setAdapter(adapter);

                            if (friendtList.size() == 0) {
                                firstShow.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                firstShow.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               // CustomDialog.hideProgressDialog();
                    Toast.makeText(getContext(),"Error", Toast.LENGTH_LONG).show();
            }
        });
        // Add request queue
       // VolleySingleton.getsInstance().addToRequestQueue(jsonRequest);     ***** it not work
       MySingleton.getInstance(getContext()).addToRequestQueue(jsonRequest);


        //***************===<< end new style >>====******************************
    }


    /**
     * search
     */

    private void listSearchFriend() {

        String url = "http://10.0.3.2:8080/ChatAskhmer/api/friend/searchfriend/" + textSearch + "/"+ myid;
        url = url.replaceAll(" ", "%20");
        GsonObjectRequest jsonRequest = new GsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("RES_DATA")) {
                        JSONArray jsonArray = response.getJSONArray("RES_DATA");
                        //list item
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Friends item = new Friends();
                            item.setFriId(jsonArray.getJSONObject(i).getInt("userId"));
                            item.setFriName(jsonArray.getJSONObject(i).getString("userName"));
                            item.setChatId(jsonArray.getJSONObject(i).getString("userNo"));
                            item.setImg(jsonArray.getJSONObject(i).getString("userPhoto"));
                            friendtList.add(item);
                        }
                    } else {
                        //Toast.makeText(getApplicationContext(), "No Friend Found !", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    // e.printStackTrace();
                } finally {

                    adapter = new FriendAdapter(friendtList);
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // CustomDialog.hideProgressDialog();
                adapter.clearData();
                prepareAddfriendData();
                //   Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        });
        MySingleton.getInstance(getContext()).addToRequestQueue(jsonRequest);
    }


    /********************************************************************************************/
//
//    private RecyclerView.OnScrollListener
//            mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
//        @Override
//        public void onScrollStateChanged(RecyclerView recyclerView,
//                                         int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//        }
//
//        @Override
//        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//            LinearLayoutManager mLayoutManager =  new LinearLayoutManager(getActivity());
//            int visibleItemCount = mLayoutManager.getChildCount();
//            int totalItemCount = mLayoutManager.getItemCount();
//            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
////
////            if (!mIsLoading && !mIsLastPage) {
////                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
////                        && firstVisibleItemPosition >= 0
////                        && totalItemCount >= PAGE_SIZE) {
////                    loadMoreItems();
////                }
////            }
//        }
//    };


    /********************************************************************************************/

}
