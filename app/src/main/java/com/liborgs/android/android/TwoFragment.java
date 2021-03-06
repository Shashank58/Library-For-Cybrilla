package com.liborgs.android.android;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.liborgs.android.R;
import com.liborgs.android.adapters.ShelfAdapter;
import com.liborgs.android.datamodle.HomeView;
import com.liborgs.android.util.Constants;
import com.liborgs.android.util.SharedPreferencesHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * My Shelf fragment which fetches issued books.
 **/

public class TwoFragment extends android.support.v4.app.Fragment {
    private ShelfAdapter bookList;
    private List<HomeView> myBooks;
    private RecyclerView recList;

    public TwoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_two, container, false);
        recList = (RecyclerView) view.findViewById(R.id.cardListTwo);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getBaseContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        return view;
    }

    public void getData(){
        myBooks = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jObject = new JsonObjectRequest(Method.GET, Constants.GET_USER_ISSUED_BOOKS,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        TextView noBooks = (TextView) getActivity().findViewById(R.id.noBooks);
                        try {
                            if(response.has("data")) {
                                noBooks.setVisibility(View.GONE);
                                JSONArray data = response.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject book = (JSONObject) data.get(i);
                                    String title = book.getString("title");
                                    JSONArray authors = book.getJSONArray("author");
                                    String author = authors.getString(0);
                                    JSONObject issueData = book.getJSONObject("issue_data");
                                    long issueDate = issueData.getLong("issue_date");
                                    String isbn = issueData.getString("isbn");
                                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                    cal.setTimeInMillis(issueDate * 1000L);
                                    String date = DateFormat.format("dd-MM-yyyy", cal).toString();
                                    cal.add(Calendar.DATE, 15);
                                    String dueDate = DateFormat.format("dd-MM-yyyy", cal).toString();
                                    String returnDate = null;
                                    int returnStatus = -1;
                                    JSONObject returnData;
                                    if (!book.isNull("return_data")) {
                                        returnData = book.getJSONObject("return_data");
                                        long returnDateUnix = returnData.getLong("return_date");
                                        Calendar calander = Calendar.getInstance(Locale.ENGLISH);
                                        calander.setTimeInMillis(returnDateUnix * 1000L);
                                        returnDate = DateFormat.format("dd-MM-yyyy", calander).toString();
                                        returnStatus = returnData.getInt("return_status");
                                    }
                                    String thumbnail = book.getString("thumbnail");
                                    myBooks.add(new HomeView(title, author, date, thumbnail, dueDate, issueDate
                                            , isbn, returnDate, returnStatus, true));
                                }
                                Collections.sort(myBooks, new Comparator<HomeView>() {
                                    @Override
                                    public int compare(HomeView lhs, HomeView rhs) {
                                        return lhs.getReturnStatus() - rhs.getReturnStatus();
                                    }
                                });
                                bookList = new ShelfAdapter(myBooks, getActivity(), getActivity());
                                recList.setAdapter(bookList);
                            } else {
                                noBooks.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError{
                HashMap<String, String> params = new HashMap<>();
                SharedPreferencesHandler s = new SharedPreferencesHandler();
                params.put("auth-token", s.getKeyAuth(getActivity()));
                return params;
            }
        };

        queue.add(jObject);
    }
}
