package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class ExtendedSearch extends Activity {
    private IngredientOfRecipeTable DBrecepty;
    private DBeanHelper dbEanHelper;
    private ArrayList al, suroviny;
    private Button hledatButton, pridatSurovinuButton, smazatSurovinuButton;
    private LinearLayout vyhledaneReceptyLL, pridatSurovinuLL, surovinaLL, polozkaLL;
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

        DBrecepty = IngredientOfRecipeTable.getInstance(this);
        dbEanHelper = DBeanHelper.getInstance(this);

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
        pridatSurovinuLL = (LinearLayout) findViewById(R.id.pridatSurovinuLL);
        surovinaLL = (LinearLayout) findViewById(R.id.surovinaLL);

        pocetSurovin = 0;

        //našeptávač na surovinu
        LongOperationsThread MyLongOperations = new LongOperationsThread();
        MyLongOperations.execute();

        pridatSurovinuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pridatSurovinu("");
            }
        });
        pridatSurovinuLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pridatSurovinu("");
            }
        });

        loadSavedPreferences();
        if (surovinaLL.getChildCount() < 1) {
            pridatSurovinu("");
        }

    }

    private void loadSavedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("foodstuff_list", MODE_PRIVATE);
        String pocetUlozenychPolozek = sharedPreferences.getString("pocetSurovin", "0");
        int sizeOfSharedPref = Integer.valueOf(pocetUlozenychPolozek);

        for (int i = 0; i <= sizeOfSharedPref; i++){
            if (sharedPreferences.getString("surovinaACTV"+i, null) != null) {
                pridatSurovinu(sharedPreferences.getString("surovinaACTV"+i, ""));
            }
        }
    }

    private void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("foodstuff_list", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void clearPref(){
        SharedPreferences sharedPreferences = getSharedPreferences("foodstuff_list", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    protected void onPause() {
        clearPref();
        for (int i = 0; i <= pocetSurovin; i++){
            LinearLayout linearLayout = (LinearLayout) surovinaLL.findViewWithTag("polozkaSeznamuSurovinLL"+i);

            if (linearLayout != null){
                AutoCompleteTextView actv = (AutoCompleteTextView) linearLayout.findViewWithTag("surovinaACTV"+i);
                if (actv != null) {
                    String value = actv.getText().toString();
                    savePreferences("surovinaACTV"+i, value);
                }
            }
        }
        savePreferences("pocetSurovin", String.valueOf(pocetSurovin));
        super.onPause();
    }

    @Override
    protected void onStop() {
        clearPref();
        super.onStop();
    }

    public void pridatSurovinu(String surovina){
        pocetSurovin++;
        polozkaLL = new LinearLayout(ExtendedSearch.this);
        polozkaLL.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        polozkaLL.setOrientation(LinearLayout.HORIZONTAL);
        polozkaLL.setTag("polozkaSeznamuSurovinLL" + pocetSurovin);

        smazatSurovinuButton = new Button(ExtendedSearch.this);
        smazatSurovinuButton.setLayoutParams(new ViewGroup.LayoutParams(SMAZAT_POLOZKU_B_PX, SMAZAT_POLOZKU_B_PX));
        smazatSurovinuButton.setTag("odebratSurovinuButton" + pocetSurovin);
        smazatSurovinuButton.setId(pocetSurovin);
        smazatSurovinuButton.setText("x");
        smazatSurovinuButton.setTextColor(Color.RED);
        smazatSurovinuButton.setBackgroundColor(Color.TRANSPARENT);
        smazatSurovinuButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

        hledatACTV = new AutoCompleteTextView(ExtendedSearch.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, MARGIN_PX, 0);
        hledatACTV.setLayoutParams(params);
        hledatACTV.setText(surovina);
        hledatACTV.setTag("surovinaACTV" + pocetSurovin);

        hledatPodleEANu = new ImageButton(ExtendedSearch.this);
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
                IntentIntegrator scanIntegrator = new IntentIntegrator(ExtendedSearch.this);
                //start scanning
                scanIntegrator.initiateScan();
            }
        };
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

    private boolean IsInListByName(ArrayList arrayList, String name){
        for (int i = 0; i < arrayList.size(); i++){
            if (arrayList.get(i).equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rozsirene_vyhledavani, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_dbEANs){
            Intent EANdbManager = new Intent(getApplicationContext(), EANdbManager.class);
            startActivity(EANdbManager);
        }
        return super.onOptionsItemSelected(item);
    }

    private class LongOperationsThread extends AsyncTask<String, Void, ArrayList<IngredientOfRecipeObject>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final ArrayList<IngredientOfRecipeObject> arrayListVsechSurovin) {
            super.onPostExecute(arrayListVsechSurovin);
            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            if (arrayListVsechSurovin.size() > 0){
                for (IngredientOfRecipeObject surovina : arrayListVsechSurovin){
                    if (!IsInListByName(suroviny, surovina.surovina)){
                        suroviny.add(surovina.surovina);
                    }
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ExtendedSearch.this, android.R.layout.simple_dropdown_item_1line, suroviny);
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
                        final ArrayList<IngredientOfRecipeObject> arrayList = DBrecepty.getSurovinaRecepty(surovina);

                        if (arrayList.size() == 0) { //nenalezeni zadneho receptu odpovidajiciho surovinam
                            vyhledaneReceptyLL.removeAllViews();
                            chyboveHlaseni = new TextView(ExtendedSearch.this);
                            chyboveHlaseni.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            chyboveHlaseni.setPadding(20, 20, 0, 0);
                            chyboveHlaseni.setText(R.string.no_recipe_found);
                            vyhledaneReceptyLL.addView(chyboveHlaseni);
                        } else {//nalezeny recepty - vypsani do arraylistu
                            for (IngredientOfRecipeObject recept : arrayList) {
                                al.add(recept.nazev_receptu);
                            }

                            Collator collator = Collator.getInstance(new Locale("pt"));
                            Collections.sort(al, collator);

                            final ArrayAdapter arrayAdapter = new ArrayAdapter(ExtendedSearch.this, android.R.layout.simple_list_item_1, al);
                            vyhledaneReceptyListView = new ListView(ExtendedSearch.this);
                            vyhledaneReceptyListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            vyhledaneReceptyListView.setPadding(0, 0, 0, 0);
                            vyhledaneReceptyListView.setAdapter(arrayAdapter);
                            vyhledaneReceptyLL.addView(vyhledaneReceptyListView);
                            vyhledaneReceptyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    String clickedObj = String.valueOf(vyhledaneReceptyListView.getItemAtPosition(i));
                                    Intent intent = new Intent(getApplicationContext(), Recipe.class);
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
                    IntentIntegrator scanIntegrator = new IntentIntegrator(ExtendedSearch.this);
                    //start scanning
                    scanIntegrator.initiateScan();
                }
            });
        }

        @Override
        protected ArrayList<IngredientOfRecipeObject> doInBackground(String... strings) {
            final ArrayList<IngredientOfRecipeObject> arrayListVsechSurovin;
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
                String surovina = ean.foodstuff;
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
