package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class RozsireneVyhledavani extends Activity {
    private SurovinaReceptTable DBrecepty;
    private DBeanHelper dbEanHelper;
    private ArrayList al, suroviny;
    private Button hledatButton;
    private LinearLayout vyhledaneReceptyLL;
    private String surovina;
    private ListView vyhledaneReceptyListView;
    private TextView chyboveHlaseni;
    private AutoCompleteTextView hledatACTV;
    private ImageButton hledatPodleEANu;
    public ProgressDialog progressDialog;
    final Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rozsirene_vyhledavani);

        DBrecepty = new SurovinaReceptTable(this);
        dbEanHelper = new DBeanHelper(this);

        al = new ArrayList();
        suroviny = new ArrayList();

        hledatButton = (Button) findViewById(R.id.hledatButton);
        vyhledaneReceptyLL = (LinearLayout) findViewById(R.id.vyhledaneReceptyLL);
        hledatACTV = (AutoCompleteTextView) findViewById(R.id.surovinaACTV);
        hledatPodleEANu = (ImageButton) findViewById(R.id.EANbutton);

        //našeptávač na surovinu
        LongOperationsThread MyLongOperations = new LongOperationsThread();
        MyLongOperations.execute();


    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        String scanContent = scanningResult.getContents();
        if (scanContent != null) {
            if(scanContent.length() > 0){
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

    private class LongOperationsThread extends AsyncTask<String, Void, ArrayList<SurovinaReceptO>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final ArrayList<SurovinaReceptO> arrayListVsechSurovin) {
            super.onPostExecute(arrayListVsechSurovin);
            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            if (arrayListVsechSurovin.size() > 0){
                for (SurovinaReceptO surovina : arrayListVsechSurovin){
                    suroviny.add(surovina.surovina);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(RozsireneVyhledavani.this, android.R.layout.simple_dropdown_item_1line, suroviny);
            hledatACTV.setAdapter(adapter);

            hledatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    al.clear();

                    if (vyhledaneReceptyLL.getChildCount() > 0)
                        vyhledaneReceptyLL.removeAllViews();

                    surovina = hledatACTV.getText().toString();
                    //TODO: getSurovinaReceptSurovina pro vic surovin
                    final ArrayList<SurovinaReceptO> arrayList = DBrecepty.getSurovinaReceptSurovina(surovina);

                    if (arrayList.size() == 0) { //nenalezeni zadneho receptu odpovidajiciho surovinam
                        vyhledaneReceptyLL.removeAllViews();
                        chyboveHlaseni = new TextView(RozsireneVyhledavani.this);
                        chyboveHlaseni.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        chyboveHlaseni.setPadding(20, 20, 0, 0);
                        chyboveHlaseni.setText(R.string.no_recipe_found);
                        vyhledaneReceptyLL.addView(chyboveHlaseni);
                    } else {//nalezeny recepty - vypsani do arraylistu
                        for (SurovinaReceptO recept : arrayList) {
                            al.add(recept.nazev_receptu);
                        }

                        Collator collator = Collator.getInstance(new Locale("pt"));
                        Collections.sort(al, collator);

                        final ArrayAdapter arrayAdapter = new ArrayAdapter(RozsireneVyhledavani.this, android.R.layout.simple_list_item_1, al);
                        vyhledaneReceptyListView = new ListView(RozsireneVyhledavani.this);
                        vyhledaneReceptyListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        vyhledaneReceptyListView.setPadding(0, 0, 0, 0);
                        vyhledaneReceptyListView.setAdapter(arrayAdapter);
                        vyhledaneReceptyLL.addView(vyhledaneReceptyListView);
                        vyhledaneReceptyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                String clickedObj = String.valueOf(vyhledaneReceptyListView.getItemAtPosition(i));
                                Intent intent = new Intent(getApplicationContext(), Recept.class);
                                Bundle dataBundle = new Bundle();
                                dataBundle.putString("nazev_receptu", clickedObj);
                                intent.putExtras(dataBundle);
                                startActivity(intent);
                            }
                        });
                        DBrecepty.close();
                    }
                }
            });

            hledatPodleEANu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //instantiate ZXing integration class
                    IntentIntegrator scanIntegrator = new IntentIntegrator(RozsireneVyhledavani.this);
                    //start scanning
                    scanIntegrator.initiateScan();
                }
            });
        }

        @Override
        protected ArrayList<SurovinaReceptO> doInBackground(String... strings) {
            final ArrayList<SurovinaReceptO> arrayListVsechSurovin;
            handler.postDelayed(pdRunnable, 500);
            try {
                dbEanHelper.createDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dbEanHelper.openDataBase();

            arrayListVsechSurovin = DBrecepty.getAllIngredients();


            return arrayListVsechSurovin;
        }

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
                Toast toast = Toast.makeText(getApplicationContext(), "EAN nenalezen v DB", Toast.LENGTH_SHORT);
                toast.show();
            }

            for (EANsurovinaO ean : eanSurovinaOs){
                String surovina = ean.surovina;

                hledatACTV.setText(surovina);
            }
        }

        @Override
        protected ArrayList<EANsurovinaO> doInBackground(String... strings) {
            handler.postDelayed(pdRunnable, 500);
            final ArrayList<EANsurovinaO> arrayListSurovin = dbEanHelper.getSurovina(strings[0]);
            return arrayListSurovin;
        }
    }

}
