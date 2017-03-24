package com.example.erika.cookbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class CookBook extends Activity implements View.OnClickListener {
    private ImageButton breakfast, lunch, dinner, desserts, drinks, snack;
    private Button searchButton, showCategoriesButton;
    private EditText searchEditText;
    private LinearLayout categoryLL, searchedRecipesLL, showCategoriesLL;
    private ListView searchedRecipesLV;
    private String recipeToSearch;
    private ArrayList arrayListOfRecipes;
    public ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arrayListOfRecipes = new ArrayList();

        breakfast = (ImageButton) findViewById(R.id.snidane);
        lunch = (ImageButton) findViewById(R.id.obed);
        dinner = (ImageButton) findViewById(R.id.vecere);
        desserts = (ImageButton) findViewById(R.id.zakusky);
        drinks = (ImageButton) findViewById(R.id.napoje);
        snack = (ImageButton) findViewById(R.id.svacina);
        searchButton = (Button) findViewById(R.id.hledejButton);
        searchEditText = (EditText) findViewById(R.id.hledejEditText);
        categoryLL = (LinearLayout) findViewById(R.id.kategorieLL);
        searchedRecipesLL = (LinearLayout) findViewById(R.id.vyhledaneReceptyLL);
        showCategoriesLL = (LinearLayout) findViewById(R.id.zobrazitKategorieLL);
        showCategoriesButton = (Button) findViewById(R.id.zobrazitKategorieButton);

        showCategoriesLL.setVisibility(View.INVISIBLE);
        breakfast.setOnClickListener(this);
        lunch.setOnClickListener(this);
        dinner.setOnClickListener(this);
        desserts.setOnClickListener(this);
        drinks.setOnClickListener(this);
        snack.setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hledani receptu
                arrayListOfRecipes.clear();
                categoryLL.setVisibility(View.INVISIBLE);
                showCategoriesLL.setVisibility(View.VISIBLE);
                if(searchedRecipesLL.getChildCount() > 0)
                    searchedRecipesLL.removeAllViews();

                recipeToSearch = searchEditText.getText().toString();

                LongOperationsThread MyLongOperations = new LongOperationsThread();
                MyLongOperations.execute(recipeToSearch);

            }
        });
        showCategoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchedRecipesLL.getChildCount() > 0)
                    searchedRecipesLL.removeAllViews();
                categoryLL.setVisibility(View.VISIBLE);
                showCategoriesLL.setVisibility(View.INVISIBLE);
            }
        });
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_new)//novy recept
        {
            Intent NovyRecept = new Intent(getApplicationContext(), NovyRecept.class);
            startActivity(NovyRecept);
        }
        else if (id == R.id.action_timer) {//casovac
            Intent Casovac = new Intent(getApplicationContext(), ListOfTimers.class);
            startActivity(Casovac);
        }
        else if (id == R.id.action_search) {//rozsirene vyhledavani - podle potravin
            Intent RozsireneVyhledavani = new Intent(getApplicationContext(), RozsireneVyhledavani.class);
            startActivity(RozsireneVyhledavani);
        }
        else if(id == R.id.action_shoppingList){ //nakupni seznam
            Intent NakupniSeznam = new Intent(getApplicationContext(), NakupniSeznam.class);
            startActivity(NakupniSeznam);
        }

        else if (id== R.id.action_favourites){
            Intent favourites = new Intent(getApplicationContext(), Favourites.class);
            startActivity(favourites);
        }
        else if (id== R.id.action_settings){
            Intent Settings = new Intent(this, Settings.class);
            startActivity(Settings);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.snidane: {
                Intent SeznamReceptu = new Intent(getApplicationContext(), SeznamReceptu.class);
                Bundle kategorie = new Bundle();
                kategorie.putInt("id", 1);
                SeznamReceptu.putExtras(kategorie);
                startActivity(SeznamReceptu);
                break;
            }
            case R.id.obed: {
                Intent SeznamReceptu = new Intent(getApplicationContext(), SeznamReceptu.class);
                Bundle kategorie = new Bundle();
                kategorie.putInt("id", 2);
                SeznamReceptu.putExtras(kategorie);
                startActivity(SeznamReceptu);
                break;
            }
            case R.id.vecere: {
                Intent SeznamReceptu = new Intent(getApplicationContext(), SeznamReceptu.class);
                Bundle kategorie = new Bundle();
                kategorie.putInt("id", 3);
                SeznamReceptu.putExtras(kategorie);
                startActivity(SeznamReceptu);
                break;
            }
            case R.id.svacina: {
                Intent SeznamReceptu = new Intent(getApplicationContext(), SeznamReceptu.class);
                Bundle kategorie = new Bundle();
                kategorie.putInt("id", 4);
                SeznamReceptu.putExtras(kategorie);
                startActivity(SeznamReceptu);
                break;
            }
            case R.id.zakusky: {
                Intent SeznamReceptu = new Intent(getApplicationContext(), SeznamReceptu.class);
                Bundle kategorie = new Bundle();
                kategorie.putInt("id", 5);
                SeznamReceptu.putExtras(kategorie);
                startActivity(SeznamReceptu);
                break;
            }
            case R.id.napoje: {
                Intent SeznamReceptu = new Intent(getApplicationContext(), SeznamReceptu.class);
                Bundle kategorie = new Bundle();
                kategorie.putInt("id", 6);
                SeznamReceptu.putExtras(kategorie);
                startActivity(SeznamReceptu);
                break;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            searchButton = (Button) findViewById(R.id.hledejButton);
        }catch (Exception e){
            Intent i = new Intent();
            i.setClass(getApplicationContext(), CookBook.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    private class LongOperationsThread extends AsyncTask<String, Void, ArrayList<ReceptO>> {
        ReceptyTable DBrecepty = ReceptyTable.getInstance(CookBook.this);
        DBreceptyHelper DBreceptyHelper = com.example.erika.cookbook.DBreceptyHelper.getInstance(CookBook.this);
        final Handler handler = new Handler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        final Runnable pdRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(CookBook.this);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        };

        @Override
        protected ArrayList doInBackground(String... params) {
            handler.postDelayed(pdRunnable, 500);
            try {
                DBreceptyHelper.createDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            DBreceptyHelper.getInstance(CookBook.this).openDataBase();

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String razeniReceptu = SP.getString("razeniReceptu","1");
            ArrayList<ReceptO> arrayList = DBrecepty.getOrderedRecipe(params[0], razeniReceptu);
            return arrayList;
        }

        @Override
        protected void onPostExecute(final ArrayList<ReceptO> arrayList) {
            super.onPostExecute(arrayList);
            // Dismiss the progress dialog
            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            if(arrayList.size() == 0){
                //nenalezeni zadneho receptu odpovidajiciho nazvu - vypis 'Recept neexistuje. Vytvořit?' - ANO/NE
                AlertDialog.Builder builder = new AlertDialog.Builder(CookBook.this);
                builder.setMessage(R.string.dialog_message_neexistujici_recept)
                        .setTitle(R.string.dialog_title_neexistujici_recept);
                builder.setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog - vratit zpet pred vyhledavani
                        searchEditText.setText("");
                        categoryLL.setVisibility(View.VISIBLE);
                    }
                });
                builder.setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button - vytvorit novy recept
                        Bundle nazev_receptu = new Bundle();
                        nazev_receptu.putString("nazev_receptu", recipeToSearch);
                        Intent novyRecept = new Intent(CookBook.this, NovyRecept.class);
                        novyRecept.putExtras(nazev_receptu);
                        startActivity(novyRecept);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{//nalezeny recepty - vypsani do arraylistu
                for(ReceptO recept : arrayList){
                    CookBook.this.arrayListOfRecipes.add(recept.nazev_receptu);
                }

                //seradit recepty podle nazvu, nehlede na diakritiku
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String razeniReceptu = SP.getString("razeniReceptu","1");
                if (razeniReceptu.equals("1")) {
                    Collator collator = Collator.getInstance(new Locale("pt"));
                    Collections.sort(CookBook.this.arrayListOfRecipes, collator);
                }

                final ArrayAdapter arrayAdapter = new ArrayAdapter(CookBook.this, android.R.layout.simple_list_item_1, CookBook.this.arrayListOfRecipes);
                searchedRecipesLV = new ListView(CookBook.this);
                searchedRecipesLV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                searchedRecipesLV.setPadding(0,0,0,100);
                searchedRecipesLV.setAdapter(arrayAdapter);
                searchedRecipesLL.addView(searchedRecipesLV);
                searchedRecipesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String clickedItem = String.valueOf(searchedRecipesLV.getItemAtPosition(i));
                        Intent intent = new Intent(getApplicationContext(), Recept.class);
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("nazev_receptu", clickedItem);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    }
                });
            }
        }

    }
}


