package com.askhmer.chat.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.askhmer.chat.R;
import com.askhmer.chat.network.API;
import com.askhmer.chat.network.GsonObjectRequest;
import com.askhmer.chat.network.MySingleton;
import com.askhmer.chat.util.CustomDialogSweetAlert;
import com.askhmer.chat.util.SharedPreferencesFile;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;


public class PhoneLogIn extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spinner1;
    private Button btnnext, btnLogin, btnClear;
    ImageButton btn_login_medayi;

    @NotEmpty(messageId = R.string.validation_empty)
    private EditText etPhnoeno;
    private TextView temp;
    private String phoneno;
    private String countryCode;
    private String val;

    private LoginButton btnfb;
    private AccessToken accessToken;
    private CallbackManager callbackManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private SharedPreferencesFile mSharedPref;
    private  String user_id = null;

    private String name;
    private String id;
    private String gender;
    private String email;
    private String accessTokenFB;
    private String town;
    private String location;;

    public static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.READ_PHONE_STATE",
//            "android.permission.BLUETOOTH",
//            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.VIBRATE",
            "android.permission.READ_CONTACTS",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_SMS",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*initialize facebook*/
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_phone_log_in);

        mSharedPref = SharedPreferencesFile.newInstance(this, SharedPreferencesFile.PREFER_FILE_NAME);

        // Application permission 23
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            checkPermission(MANDATORY_PERMISSIONS);
        }
        spinner1 = (Spinner) findViewById(R.id.spinner);
        btnnext = (Button) findViewById(R.id.btnnext);
        etPhnoeno = (EditText) findViewById(R.id.et_phone_no);
        btnLogin = (Button) findViewById(R.id.btn_log_in_with_email);
        btn_login_medayi = (ImageButton) findViewById(R.id.btn_login_medayi);
//        btnClear = (Button) findViewById(R.id.btn_clear_num);
//        btnClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                etPhnoeno.setText("");
//            }
//        });

        etPhnoeno.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //--todo begin validate
                if(FormValidator.validate(PhoneLogIn.this, new SimpleErrorPopupCallback(PhoneLogIn.this))){
                String formatedPhNumber = etPhnoeno.getText().toString();
                if(isValidMobile(formatedPhNumber)) {
                    phoneno = formatedPhNumber.replaceAll("[^\\.0123456789]", "");
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PhoneLogIn.this);
                    alertDialogBuilder.setTitle(R.string.confirmation);
                    alertDialogBuilder.setMessage(getApplicationContext().getString(R.string.use_this_number) + "\n\n" + phoneno);

                    alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            int randomPIN = (int) (Math.random() * 9000) + 1000;
                            val = "" + randomPIN;

                            String ind = String.valueOf(phoneno.charAt(0));

                            if (ind.equals("0")) {
                                String fulPhoneNum = countryCode + phoneno.substring(1);
                                //   Toast.makeText(PhoneLogIn.this, fulPhoneNum + "  " + val, Toast.LENGTH_LONG).show();
                                sendSMS(fulPhoneNum, val);
                                mSharedPref.putStringSharedPreference(SharedPreferencesFile.VERIFYCODE, val);
                                mSharedPref.putStringSharedPreference(SharedPreferencesFile.PHONENO, fulPhoneNum);
                                Intent intent = new Intent(PhoneLogIn.this, VerifyCode.class);
                                intent.putExtra("verifyno", val);
                                startActivity(intent);
                            } else {
                                String fulPhoneNum = countryCode + phoneno;
                                //    Toast.makeText(PhoneLogIn.this, fulPhoneNum + "  " + val, Toast.LENGTH_LONG).show();
                                sendSMS(fulPhoneNum, val);
                                mSharedPref.putStringSharedPreference(SharedPreferencesFile.VERIFYCODE, val);
                                mSharedPref.putStringSharedPreference(SharedPreferencesFile.PHONENO, fulPhoneNum);
                                Intent intent = new Intent(PhoneLogIn.this, VerifyCode.class);
                                intent.putExtra("verifyno", val);
                                startActivity(intent);
                            }
                        }
                    });

                    alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else{
                    Toast.makeText(PhoneLogIn.this, "Phone number format incorrect!!", Toast.LENGTH_SHORT).show();
                }
                //--todo end validate
                FormValidator.validate(PhoneLogIn.this, new SimpleErrorPopupCallback(PhoneLogIn.this));
                FormValidator.stopLiveValidation(this);
                }
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(PhoneLogIn.this, Login.class);
                startActivity(in);
            }
        });

        btn_login_medayi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder msg = new AlertDialog.Builder(PhoneLogIn.this);
                msg.setTitle("LOGIN WITH MEDAYI ACCOUNT");
                msg.setMessage("Coming soon...");
                msg.show();
            }
        });

        spinner1.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        categories.add("Cambodia  +855");
        categories.add("South Korea  +82");
        categories.add("United States  +1");
        categories.add("Thailand  +66");
        categories.add("Vietnam  +84");
        categories.add("Laos  +856");
        categories.add("Japan  +81");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner1.setAdapter(dataAdapter);


        sharedPreferences = this.getSharedPreferences("accessTokenFB", 0);
        editor = sharedPreferences.edit();

        btnfb = (LoginButton) findViewById(R.id.btnfb);
        //btnfb.setReadPermissions("user_friends");
        btnfb.setReadPermissions(Arrays.asList("user_friends", "user_hometown", "user_location", "public_profile", "email", "user_birthday"));
        btnfb.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                Gson gson = new Gson();
                String json = gson.toJson(accessToken);
                editor.putString("dataAccessToken", json);
                editor.commit();

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    if (!object.isNull("hometown")) {
                                        JSONObject hometown = object.getJSONObject("hometown");
                                        town = hometown.getString("name");
                                    }else {
                                        town = "";
                                    }

                                    if (!object.isNull("location")) {
                                        JSONObject locations = object.getJSONObject("location");
                                        location = locations.getString("name");
                                    } else {
                                        location = "";
                                    }

//                                    String birthday = object.getString("birthday"); // 01/31/1980 format

                                    name = object.getString("name").toString();
                                    id = object.getString("id").toString();
                                    gender = object.getString("gender").toString();
                                    email = object.getString("email").toString();
                                    accessTokenFB = loginResult.getAccessToken().toString();

                                    /*addUser(name, gender, email, town, location, id, loginResult.getAccessToken().toString());*/

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {
                                    mSharedPref.putStringSharedPreference(SharedPreferencesFile.ACCESSTOKEN, accessTokenFB);
                                    addUser(name, gender, email, town, location, id, accessTokenFB);
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                /*parameters.putString("fields", "id,name,gender,email,birthday,hometown,location");*/
                parameters.putString("fields", "id,name,gender,email,birthday,location");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.i("status", "Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("status", "Error");
            }
        });
    }


    /*facebook override function*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        countryCode = item.replaceAll("[^\\.0123456789]", "");
        // Showing selected spinner item
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void printHashkey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.askhmer.chat",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    public void addUser(final String name, String gender, String email, String town, String location, String id, String accessToken) {
        JSONObject params = new JSONObject();
        try {
            params.put("userName", name);
           try {
                if (gender.equals("male")) {
                    gender = "M";
                } else {
                    gender = "F";
                }
            }catch (NullPointerException e){
               e.printStackTrace();
               gender = "";
           }

            params.put("gender", gender);
            params.put("userPhoto", "https://graph.facebook.com/" + id + "/picture?width=500&height=500");
            params.put("userEmail", email);
            params.put("userHometown", town);
            params.put("userCurrentCity", location);
            params.put("facebookId", id);
            params.put("userAccessToken", accessToken);

            GsonObjectRequest jsonRequest = new GsonObjectRequest(Request.Method.POST, API.FBSIGNUP, params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getString("STATUS").equals("200")) {
                            String uId = response.getString("MESSAGE_USERID");

                            mSharedPref.putStringSharedPreference(SharedPreferencesFile.USERIDKEY, uId);
                            mSharedPref.putStringSharedPreference(SharedPreferencesFile.USERNAME, name);
                            mSharedPref.putBooleanSharedPreference(SharedPreferencesFile.PERFER_VERIFY_KEY, true);

                            user_id = mSharedPref.getStringSharedPreference(SharedPreferencesFile.USERIDKEY);
                            Log.d("Phone login: ", "User id " + user_id);

                            if (!user_id.equals(null)) {
                                CustomDialogSweetAlert.hideLoadingProcessDialog();
                                Intent intent = new Intent(PhoneLogIn.this, MainActivityTab.class);
                                startActivity(intent);
                                finish();
                            } else {
                                CustomDialogSweetAlert.showLoadingProcessDialog(PhoneLogIn.this);
                            }

                        } else {
                            Toast.makeText(PhoneLogIn.this, "Login not success!!!", Toast.LENGTH_SHORT).show();
                            LoginManager.getInstance().logOut();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        LoginManager.getInstance().logOut();
                    } finally {
                        /*user_id = mSharedPref.getStringSharedPreference(SharedPreferencesFile.USERIDKEY);
                        Log.d("Phone login: ","User id "+ user_id);

                        if (!user_id.equals("") || !user_id.equals(null)) {
                            CustomDialogSweetAlert.hideLoadingProcessDialog();
                            Intent intent = new Intent(PhoneLogIn.this, MainActivityTab.class);
                            startActivity(intent);
                        } else {
                            CustomDialogSweetAlert.showLoadingProcessDialog(PhoneLogIn.this);
                        }*/
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(PhoneLogIn.this, "Something error!!!", Toast.LENGTH_SHORT).show();
                    LoginManager.getInstance().logOut();
                }
            });
            MySingleton.getInstance(this).addToRequestQueue(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            LoginManager.getInstance().logOut();
        }
    }

    //send SMS to client
    public void sendSMS(final String receiver, final String verifycode){
        String url = API.VERIFYPHONENUMBER+"/" + receiver+"/"+verifycode;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            Intent intent = new Intent(PhoneLogIn.this, VerifyCode.class);
                            intent.putExtra("verifyno", val);
                            startActivity(intent);
                            mSharedPref.putStringSharedPreference(SharedPreferencesFile.PHONENO, receiver);
                            mSharedPref.putStringSharedPreference(SharedPreferencesFile.VERIFYCODE, val);

                            Log.d("respone", response.toString());

                        }else{
                            Toast.makeText(PhoneLogIn.this, "request failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error_testing", error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                String creds = String.format("%s:%s","admin","123");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;

            }
        };
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    // Application permission 23
    private final int MY_PERMISSION_REQUEST_STORAGE = 100;
    @SuppressLint("NewApi")
    private void checkPermission(String[] permissions) {

        requestPermissions(permissions, MY_PERMISSION_REQUEST_STORAGE);
    }
    // Application permission 23
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_STORAGE:
                int cnt = permissions.length;
                for(int i = 0; i < cnt; i++ ) {

                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED ) {

                        Log.i("Permission", "Permission[" + permissions[i] + "] = PERMISSION_GRANTED");

                    } else {

                        Log.i("Permission", "permission[" + permissions[i] + "] always deny");
                    }
                }
                break;
        }
    }


    private boolean isValidMobile(String phone)
    {
        String formatedPhNumber = etPhnoeno.getText().toString();
        String newPhNumber = phone.replaceAll("[^\\.0123456789]", "");
        boolean check=false;
        if(!Pattern.matches("[a-zA-Z]+", formatedPhNumber))
        {
            if(newPhNumber.length() < 8 || newPhNumber.length() > 10)
            {
                check = false;
            }
            else
            {
                check = true;
            }
        }
        else
        {
            check=false;
        }
        return check;
//        return android.util.Patterns.PHONE.matcher(phone).matches();
    }
}
