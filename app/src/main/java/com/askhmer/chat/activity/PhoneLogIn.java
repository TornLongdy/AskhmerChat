package com.askhmer.chat.activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.askhmer.chat.R;
import com.askhmer.chat.activity.VerifyCode;

import java.util.ArrayList;
import java.util.List;

public class PhoneLogIn extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spinner1;
    Button btnnext;
    EditText etPhnoeno;
    String phoneno;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_log_in);

        spinner1 = (Spinner) findViewById(R.id.spinner);
        btnnext = (Button) findViewById(R.id.btnnext);
        etPhnoeno = (EditText) findViewById(R.id.ephoneno);



        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phoneno = etPhnoeno.getText().toString();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PhoneLogIn.this);
                alertDialogBuilder.setTitle("Confirmation");
                alertDialogBuilder.setMessage("Are you sure to use this number?" + "\n\n" + phoneno);

                alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(PhoneLogIn.this, VerifyCode.class);
                        startActivity(intent);
                    }
                });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        spinner1.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        categories.add("Cambodia\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t+855");
        categories.add("North Korea\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t+850");
        categories.add("United States\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t+1");
        categories.add("Thailand\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t+66");
        categories.add("Vietnam\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t+84");
        categories.add("Laos\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t+856");
        categories.add("Japan\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t+81");



        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner1.setAdapter(dataAdapter);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
       String str ;
        str = item.replaceAll("[^\\.0123456789]","");
        // Showing selected spinner item
        Toast.makeText(parent.getContext(), str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
