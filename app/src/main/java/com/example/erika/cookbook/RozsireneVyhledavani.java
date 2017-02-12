package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    private Button hledatButton, pridatSurovinuButton, smazatSurovinuButton;
    private LinearLayout vyhledaneReceptyLL, hledejLL, surovinaLL, polozkaLL;
    private String surovina;
    private ListView vyhledaneReceptyListView;
    private TextView chyboveHlaseni;
    private AutoCompleteTextView hledatACTV;
    private ImageButton hledatPodleEANu;
    private int pocetSurovin;
    private int SMAZAT_POLOZKU_B_PX;
    private int EAN_IB_PX;
    private int MARGIN_PX;
    private int NEGATIVE_MARGIN_PX;
    private static final float SMAZAT_POLOZKU_B_DP = 40.0f;
    private static final float EAN_IB_DP = 45.0f;
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

        final float scale = getResources().getDisplayMetrics().density;
        SMAZAT_POLOZKU_B_PX = (int) (SMAZAT_POLOZKU_B_DP * scale + 0.5f);
        EAN_IB_PX = (int) (EAN_IB_DP * scale + 0.5f);
        MARGIN_PX = (int) (50 * scale + 0.5f);
        NEGATIVE_MARGIN_PX = (int) (-50 * scale + 0.5f);

        hledatButton = (Button) findViewById(R.id.hledatButton);
        vyhledaneReceptyLL = (LinearLayout) findViewById(R.id.vyhledaneReceptyLL);
        pridatSurovinuButton = (Button) findViewById(R.id.pridatSurovinuButton);
        hledejLL = (LinearLayout) findViewById(R.id.hledejLL);
        surovinaLL = (LinearLayout) findViewById(R.id.surovinaLL);

        pocetSurovin = 0;
        pridatSurovinu();
        //našeptávač na surovinu
        LongOperationsThread MyLongOperations = new LongOperationsThread();
        MyLongOperations.execute();

        pridatSurovinuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pridatSurovinu();
            }
        });

    }

    public void pridatSurovinu(){
        pocetSurovin++;
        polozkaLL = new LinearLayout(RozsireneVyhledavani.this);
        polozkaLL.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        polozkaLL.setOrientation(LinearLayout.HORIZONTAL);
        polozkaLL.setTag("polozkaSeznamuSurovinLL" + pocetSurovin);

        smazatSurovinuButton = new Button(RozsireneVyhledavani.this);
        smazatSurovinuButton.setLayoutParams(new ViewGroup.LayoutParams(SMAZAT_POLOZKU_B_PX, SMAZAT_POLOZKU_B_PX));
        smazatSurovinuButton.setTag("odebratSurovinuButton" + pocetSurovin);
        smazatSurovinuButton.setId(pocetSurovin);
        smazatSurovinuButton.setText("x");
        smazatSurovinuButton.setTextColor(Color.RED);
        smazatSurovinuButton.setBackgroundColor(Color.TRANSPARENT);
        smazatSurovinuButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

        hledatACTV = new AutoCompleteTextView(RozsireneVyhledavani.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, MARGIN_PX, 0);
        hledatACTV.setLayoutParams(params);
        hledatACTV.setTag("surovinaACTV" + pocetSurovin);

        hledatPodleEANu = new ImageButton(RozsireneVyhledavani.this);
        LinearLayout.LayoutParams paramsIB = new LinearLayout.LayoutParams(EAN_IB_PX, EAN_IB_PX);
        paramsIB.setMargins(NEGATIVE_MARGIN_PX, 0, 0, 0);
        hledatPodleEANu.setLayoutParams(paramsIB);
        hledatPodleEANu.setImageResource(R.drawable.scanean);
        hledatPodleEANu.setBackgroundColor(Color.TRANSPARENT);
        hledatPodleEANu.setTag("EANbutton" + pocetSurovin);

        surovinaLL.addView(polozkaLL);
        polozkaLL.addView(smazatSurovinuButton);
        polozkaLL.addView(hledatACTV);
        polozkaLL.addView(hledatPodleEANu);
        Log.d("addView", "polozkaLL addView called!" + pocetSurovin);

        smazatSurovinuButton.setOnClickListener(handleClick(smazatSurovinuButton));
        hledatPodleEANu.setOnClickListener(handleClickImageButton(hledatPodleEANu));

        LongOperationsThread MyLongOperations = new LongOperationsThread();
        MyLongOperations.execute();
    }
    View.OnClickListener handleClick(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout polozkaSeznamuLinearLayout;
                int idPolozky = button.getId();
                Log.d("polozka id", String.valueOf(idPolozky));
                polozkaSeznamuLinearLayout = (LinearLayout) surovinaLL.findViewWithTag("polozkaSeznamuSurovinLL" + idPolozky);
                surovinaLL.removeView(polozkaSeznamuLinearLayout);
                if(surovinaLL.getChildCount() < 1){
                    pocetSurovin = 0;
                }
            }
        };
    }

    View.OnClickListener handleClickImageButton(final ImageButton button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout polozkaSeznamuLinearLayout;
                int idPolozky = button.getId();
                //instantiate ZXing integration class
                IntentIntegrator scanIntegrator = new IntentIntegrator(RozsireneVyhledavani.this);
                //start scanning
                scanIntegrator.addExtra("IDtextView", idPolozky);
                scanIntegrator.initiateScan();
            }
        };
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        String scanContent = scanningResult.getContents();
        int positionID = scanningResult.getPosition();
        Log.d("positionID extras", positionID+"");
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

    private boolean IsInListByName(ArrayList arrayList, String name){
        for (int i = 0; i < arrayList.size(); i++){
            if (arrayList.get(i).equals(name)) {
                return true;
            }
        }
        return false;
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
                    if (!IsInListByName(suroviny, surovina.surovina)){
                        suroviny.add(surovina.surovina);
                    }
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(RozsireneVyhledavani.this, android.R.layout.simple_dropdown_item_1line, suroviny);
            hledatACTV.setAdapter(adapter);

            hledatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    al.clear();
                    surovina = "";
                    if (vyhledaneReceptyLL.getChildCount() > 0)
                        vyhledaneReceptyLL.removeAllViews();

                    for (int i = 0; i <= pocetSurovin; i++){
                        AutoCompleteTextView surovinaACTV =(AutoCompleteTextView) surovinaLL.findViewWithTag("surovinaACTV"+i);
                        if (surovinaACTV != null){
                            if (surovinaACTV.getText().length() >= 1) {
                                surovina = surovina + surovinaACTV.getText().toString() + ", ";
                            }
                        }

                    }
                    Log.d("SUROVINY", surovina);
                        final ArrayList<SurovinaReceptO> arrayList = DBrecepty.getSurovinaRecepty(surovina);

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
                            //DBrecepty.close();
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
                //TODO: aby byla spravna surovina ve spravnem policku - pri cteni pres EAN
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
