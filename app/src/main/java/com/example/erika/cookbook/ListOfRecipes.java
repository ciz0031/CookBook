package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class ListOfRecipes extends Activity {
    private Context context;
    private ListView listw;
    private DBreceptyHelper DBrecepty;
    public ArrayList al;
    public RecipeTable recipeTable;
    final Handler handler = new Handler();
    public ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seznam_receptu);
        Bundle extras = getIntent().getExtras();
        DBrecepty = DBreceptyHelper.getInstance(this);
        recipeTable = RecipeTable.getInstance(this);
        al = new ArrayList();
        if(extras != null)
        {
            int kategorie = extras.getInt("id");

            LongOperationsThread MyThread = new LongOperationsThread();
            MyThread.execute(kategorie);

        }
    }

    private class LongOperationsThread extends AsyncTask<Integer, Void, ArrayList<RecipeObject>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final ArrayList<RecipeObject> arrayList) {
            super.onPostExecute(arrayList);

            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            for(RecipeObject rec : arrayList){//foreach
                al.add(rec.nazev_receptu);
            }

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String razeniReceptu = SP.getString("razeniReceptu","1");
            if (razeniReceptu.equals("1")) {
                Collator collator = Collator.getInstance(new Locale("pt"));
                Collections.sort(al, collator);
            }


            final ArrayAdapter arrayAdapter = new ArrayAdapter(ListOfRecipes.this,android.R.layout.simple_list_item_1, al);

            listw = (ListView)findViewById(R.id.listView2);
            listw.setAdapter(arrayAdapter);
            listw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    String clickedItem = String.valueOf(listw.getItemAtPosition(arg2));
                    Intent intent = new Intent(getApplicationContext(), Recipe.class);
                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("nazev_receptu", clickedItem);
                    intent.putExtras(dataBundle);
                    startActivity(intent);
                }
            });
            //receptyTable.close();
        }

        @Override
        protected ArrayList<RecipeObject> doInBackground(Integer... ints) {
            handler.postDelayed(pdRunnable, 500);
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String razeniReceptu = SP.getString("razeniReceptu","1");
            ArrayList<RecipeObject> arrayList = recipeTable.getRecipeFromDB(ints[0], razeniReceptu);
            return arrayList;

        }
        final Runnable pdRunnable = new Runnable() {
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
