package com.liborgs.android;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private String mFname, mLname, mEmail, mPassword, mConfirm;
    private EditText fname, lname, email, password, confirm;
    private Button createAccount;
    public static final String KEY_FNAME = "firstname";
    public static final String KEY_LNAME = "lastname";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    private static final String SIGN_UP = "Sign Up";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViews();

    }

    private void findViews(){
        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirm = (EditText) findViewById(R.id.confirmPassword);
        createAccount = (Button) findViewById(R.id.createAccount);
    }

    public void loginScreen(View v){
        mPassword = password.getText().toString();
        mConfirm = confirm.getText().toString();
        mFname = fname.getText().toString().trim();
        mLname = lname.getText().toString().trim();
        if(mConfirm.equals(mPassword)) {
            if (isNetworkAvailable()) {
                createAccount.setEnabled(false);
                registerUser();
            }
            else {
                showDialog("Liborg", "Please connect to internet");
            }
        } else {
            showDialog(SIGN_UP, "Password not matching");
        }
    }

    private void showDialog(String title, String message){
        new AlertDialog.Builder(SignUp.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void registerUser(){
        mEmail = email.getText().toString().trim();
        RequestQueue queue = Volley.newRequestQueue(this);

        String REGISTER_URL = "https://liborgs-1139.appspot.com/users/register";

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj = new JSONObject(response);
                            String message = jObj.getString("message");
                            Boolean status = jObj.getBoolean("status");
                            if(status){
                                new AlertDialog.Builder(SignUp.this)
                                        .setTitle(SIGN_UP)
                                        .setMessage(message)
                                        .setPositiveButton(android.R.string.yes,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                }).show();
                            } else {
                                showDialog(SIGN_UP, message);
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put(KEY_FNAME, mFname);
                params.put(KEY_LNAME, mLname);
                params.put(KEY_EMAIL, mEmail);
                params.put(KEY_PASSWORD, mPassword);
                return params;
            }
        };

        queue.add(jsonObjectRequest);
    }

    public void backToSignIn(){
        finish();
    }
}