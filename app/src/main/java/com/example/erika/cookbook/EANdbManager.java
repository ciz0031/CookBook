package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private Button ulozitButton;
    private TextView numberTV;
    private EditText surovinaET;
    private DBeanHelper db;
    public ProgressDialog progressDialog;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eandb_manager);

        scanEANbutton = (ImageButton) findViewById(R.id.scanEANbutton);
        ulozitButton = (Button) findViewById(R.id.ulozitDoDBeanuButton);
        numberTV = (TextView) findViewById(R.id.numberTV);
        surovinaET = (EditText) findViewById(R.id.surovinaET);


        ulozitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberTV.getText() == "-vyfoť pro získání čísla-"){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.invalid_string, Toast.LENGTH_SHORT);
                    toast.show();
                }else if (surovinaET.getText().length() <= 1){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.string_too_short, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    if(db.insertSurovinaEAN(Integer.parseInt(numberTV.getText().toString()), surovinaET.getText().toString())){
                        Toast.makeText(getApplicationContext(), R.string.new_item_saved, Toast.LENGTH_SHORT).show();
                        numberTV.setText("-vyfoť pro získání čísla-");
                        surovinaET.setText("");
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        db = new DBeanHelper(this);
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
            Toast toast = Toast.makeText(getApplicationContext(), "Scan canceled.", Toast.LENGTH_SHORT);
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
                ulozitButton.setClickable(true);
            }

            for (EANsurovinaO ean : eanSurovinaOs){
                String surovina = ean.surovina;
                surovinaET.setText(surovina);
                ulozitButton.setClickable(false);
                ulozitButton.setBackgroundColor(getResources().getColor(R.color.grey));
            }
        }

        @Override
        protected ArrayList<EANsurovinaO> doInBackground(String... strings) {
            handler.postDelayed(pdRunnable, 500);
            final ArrayList<EANsurovinaO> arrayListSurovin = db.getSurovina(strings[0]);
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
