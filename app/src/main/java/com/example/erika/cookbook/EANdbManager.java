package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class EANdbManager extends Activity {
    private ImageButton scanEANbutton;
    private Button saveButton;
    private TextView numberTV, alreadyInDB;
    private EditText foodstuffET;
    private DBeanHelper dbHelper;
    public ProgressDialog progressDialog;
    private boolean isClickable = true;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eandb_manager);

        scanEANbutton = (ImageButton) findViewById(R.id.scanEANbutton);
        saveButton = (Button) findViewById(R.id.ulozitDoDBeanuButton);
        numberTV = (TextView) findViewById(R.id.numberTV);
        alreadyInDB = (TextView) findViewById(R.id.existingInDB);
        foodstuffET = (EditText) findViewById(R.id.surovinaET);

        alreadyInDB.setVisibility(View.INVISIBLE);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberTV.getText() == "-vyfoť EAN pro získání čísla-"){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.invalid_string, Toast.LENGTH_SHORT);
                    toast.show();
                }else if (foodstuffET.getText().length() <= 1){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.string_too_short, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    if(dbHelper.insertSurovinaEAN(Long.parseLong(numberTV.getText().toString()), foodstuffET.getText().toString())){
                        Toast.makeText(getApplicationContext(), R.string.new_item_saved, Toast.LENGTH_SHORT).show();
                        numberTV.setText("-vyfoť EAN pro získání čísla-");
                        foodstuffET.setText("");
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.item_not_saved, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        scanEANbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //instantiate ZXing integration class
                IntentIntegrator scanIntegrator = new IntentIntegrator(EANdbManager.this);
                //start scanning
                scanIntegrator.initiateScan();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("EAN", numberTV.getText().toString());
        outState.putString("NAME", foodstuffET.getText().toString());
        outState.putBoolean("isClickable", isClickable);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        numberTV.setText(savedInstanceState.getString("EAN"));
        foodstuffET.setText(savedInstanceState.getString("NAME"));
        isClickable = savedInstanceState.getBoolean("isClickable");

        if (!isClickable){
            saveButton.setClickable(false);
            saveButton.setBackgroundColor(getResources().getColor(R.color.grey));
            alreadyInDB.setVisibility(View.VISIBLE);
        }
        else {
            saveButton.setClickable(true);
            alreadyInDB.setVisibility(View.INVISIBLE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        dbHelper = DBeanHelper.getInstance(this);
        dbHelper.openDataBase();
        String scanContent = scanningResult.getContents();
        if (scanContent != null) {
            if(scanContent.length() > 0){
                numberTV.setText(scanContent);

                LongOperationsThreadEANs MyLongOperations = new LongOperationsThreadEANs();
                MyLongOperations.execute(scanContent);
            }
        }
        else{
            //invalid scan data or scan canceled
            Toast toast = Toast.makeText(getApplicationContext(), "Scanování zrušeno.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    private class LongOperationsThreadEANs extends AsyncTask<String, Void, ArrayList<EANsurovinaO>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<EANsurovinaO> eanSurovinaOs) {
            super.onPostExecute(eanSurovinaOs);
            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            if (eanSurovinaOs.size() == 0){
                isClickable = true;
                saveButton.setClickable(true);
                alreadyInDB.setVisibility(View.INVISIBLE);
            }

            for (EANsurovinaO ean : eanSurovinaOs){
                isClickable = false;
                String surovina = ean.foodstuff;
                foodstuffET.setText(surovina);
                saveButton.setClickable(false);
                saveButton.setBackgroundColor(getResources().getColor(R.color.grey));
                alreadyInDB.setVisibility(View.VISIBLE);
            }
            dbHelper.getInstance(getApplication()).close();
        }

        @Override
        protected ArrayList<EANsurovinaO> doInBackground(String... strings) {
            handler.postDelayed(pdRunnable, 500);
            final ArrayList<EANsurovinaO> arrayListSurovin = dbHelper.getSurovina(strings[0]);
            return arrayListSurovin;
        }

        public final Runnable pdRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(getApplicationContext());
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        };
    }


}
