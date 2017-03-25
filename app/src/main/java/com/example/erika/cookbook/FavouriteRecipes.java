package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class FavouriteRecipes extends Activity {
    private String nazev_receptu = "";
    public ProgressDialog progressDialog;
    private LinearLayout oblibeneReceptyLL;
    private ListView oblibeneReceptyLV;
    private TextView chybovaHlaska;
    private ArrayList al;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        al = new ArrayList();

        oblibeneReceptyLL = (LinearLayout) findViewById(R.id.oblibeneReceptyLL);

        LongOperationsThread longOperationsThread = new LongOperationsThread();
        longOperationsThread.execute();
    }
    private class LongOperationsThread extends AsyncTask<String, Void, ArrayList<RecipeObject>> {
        RecipeTable DBrecepty = RecipeTable.getInstance(FavouriteRecipes.this);
        DBreceptyHelper DBreceptyHelper = com.example.erika.cookbook.DBreceptyHelper.getInstance(FavouriteRecipes.this);
        final Handler handler = new Handler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        final Runnable pdRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(FavouriteRecipes.this);
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
            DBreceptyHelper.openDataBase();

            final ArrayList<RecipeObject> arrayList = DBrecepty.getFavouriteRecipe();

            return arrayList;

        }

        @Override
        protected void onPostExecute(final ArrayList<RecipeObject> arrayList) {
            super.onPostExecute(arrayList);
            // Dismiss the progress dialog
            handler.removeCallbacks(pdRunnable);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            if (arrayList.size() == 0) {
                //nenalezeni zadneho oblibeneho receptu
                chybovaHlaska = new TextView(FavouriteRecipes.this);
                chybovaHlaska.setText("Nenalezen žádný oblíbený recept.");
                oblibeneReceptyLL.addView(chybovaHlaska);
            } else {//nalezeny recepty - vypsani do arraylistu
                for (RecipeObject recept : arrayList) {
                    al.add(recept.nazev_receptu);
                }

                //seradit recepty podle nazvu, nehlede na diakritiku
                Collator collator = Collator.getInstance(new Locale("pt"));
                Collections.sort(al, collator);

                final ArrayAdapter arrayAdapter = new ArrayAdapter(FavouriteRecipes.this, android.R.layout.simple_list_item_1, al);
                oblibeneReceptyLV = new ListView(FavouriteRecipes.this);
                oblibeneReceptyLV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                oblibeneReceptyLV.setPadding(0, 0, 0, 100);
                oblibeneReceptyLV.setAdapter(arrayAdapter);
                oblibeneReceptyLL.addView(oblibeneReceptyLV);
                oblibeneReceptyLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String clickedItem = String.valueOf(oblibeneReceptyLV.getItemAtPosition(i));
                        Intent intent = new Intent(getApplicationContext(), Recipe.class);
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("nazev_receptu", clickedItem);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    }
                });
                //DBreceptyHelper.close();
            }
        }
    }
}
