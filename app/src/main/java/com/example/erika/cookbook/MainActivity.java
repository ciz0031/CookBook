package com.example.erika.cookbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener {
    private ImageButton snidane, obed, vecere, zakusky, napoje, svacina;
    private Button hledatButton, zobrazitKategorieButton;
    private EditText hledatEditText;
    private LinearLayout kategorieLL, vyhledaneReceptyLL, zobrazitKategorieLL;
    private ListView vyhledaneReceptyListView;
    private String receptProVyhledani;
    private ArrayList al;
    public ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        al = new ArrayList();

        snidane = (ImageButton) findViewById(R.id.snidane);
        obed = (ImageButton) findViewById(R.id.obed);
        vecere = (ImageButton) findViewById(R.id.vecere);
        zakusky = (ImageButton) findViewById(R.id.zakusky);
        napoje = (ImageButton) findViewById(R.id.napoje);
        svacina = (ImageButton) findViewById(R.id.svacina);
        hledatButton = (Button) findViewById(R.id.hledejButton);
        hledatEditText = (EditText) findViewById(R.id.hledejEditText);
        kategorieLL = (LinearLayout) findViewById(R.id.kategorieLL);
        vyhledaneReceptyLL = (LinearLayout) findViewById(R.id.vyhledaneReceptyLL);
        zobrazitKategorieLL = (LinearLayout) findViewById(R.id.zobrazitKategorieLL);
        zobrazitKategorieButton = (Button) findViewById(R.id.zobrazitKategorieButton);

        zobrazitKategorieLL.setVisibility(View.INVISIBLE);
        snidane.setOnClickListener(this);
        obed.setOnClickListener(this);
        vecere.setOnClickListener(this);
        zakusky.setOnClickListener(this);
        napoje.setOnClickListener(this);
        svacina.setOnClickListener(this);

        hledatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hledani receptu
                al.clear();
                kategorieLL.setVisibility(View.INVISIBLE);
                zobrazitKategorieLL.setVisibility(View.VISIBLE);
                if(vyhledaneReceptyLL.getChildCount() > 0)
                    vyhledaneReceptyLL.removeAllViews();

                receptProVyhledani = hledatEditText.getText().toString();

                LongOperationsThread MyLongOperations = new LongOperationsThread();
                MyLongOperations.execute(receptProVyhledani);

            }
        });
        zobrazitKategorieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vyhledaneReceptyLL.getChildCount() > 0)
                    vyhledaneReceptyLL.removeAllViews();
                kategorieLL.setVisibility(View.VISIBLE);
                zobrazitKategorieLL.setVisibility(View.INVISIBLE);
            }
        });
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
            Intent Casovac = new Intent(getApplicationContext(), CountdownTimer.class);
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
        else if (id== R.id.action_dbEANs){
            Intent EANdbManager = new Intent(getApplicationContext(), EANdbManager.class);
            startActivity(EANdbManager);
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
            // check if any view exists on current view
            hledatButton = (Button) findViewById(R.id.hledejButton);
        }catch (Exception e){
            // Button was not found
            // It means, your button doesn't exist on the "current" view
            // It was freed from the memory, therefore stop of activity was performed
            // In this case I restart my app
            Intent i = new Intent();
            i.setClass(getApplicationContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    private class LongOperationsThread extends AsyncTask<String, Void, ArrayList<ReceptO>> {
        ReceptyTable DBrecepty = new ReceptyTable(MainActivity.this);
        DBreceptyHelper DBreceptyHelper = new DBreceptyHelper(MainActivity.this);
        final Handler handler = new Handler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        final Runnable pdRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(MainActivity.this);
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

            //final ArrayList<ReceptO> arrayList = DBrecepty.getReceptPodleNazvu(params[0]);

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String razeniReceptu = SP.getString("razeniReceptu","1");
            if (razeniReceptu.equals("1")) {//razeni podle abecedy - default
                ArrayList<ReceptO> arrayList = DBrecepty.getReceptPodleNazvu(params[0], razeniReceptu);
                return arrayList;
            }else{ //razeni podle hodnoceni
                ArrayList<ReceptO> arrayList = DBrecepty.getReceptPodleNazvu(params[0], razeniReceptu);
                return arrayList;
            }

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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.dialog_message_neexistujici_recept)
                        .setTitle(R.string.dialog_title_neexistujici_recept);
                builder.setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog - vratit zpet pred vyhledavani
                        hledatEditText.setText("");
                        kategorieLL.setVisibility(View.VISIBLE);
                    }
                });
                builder.setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button - vytvorit novy recept
                        Bundle nazev_receptu = new Bundle();
                        nazev_receptu.putString("nazev_receptu", receptProVyhledani);
                        Intent novyRecept = new Intent(MainActivity.this, NovyRecept.class);
                        novyRecept.putExtras(nazev_receptu);
                        startActivity(novyRecept);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{//nalezeny recepty - vypsani do arraylistu
                for(ReceptO recept : arrayList){
                    al.add(recept.nazev_receptu);
                }

                //seradit recepty podle nazvu, nehlede na diakritiku
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String razeniReceptu = SP.getString("razeniReceptu","1");
                if (razeniReceptu.equals("1")) {
                    Collator collator = Collator.getInstance(new Locale("pt"));
                    Collections.sort(al, collator);
                }

                final ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, al);
                vyhledaneReceptyListView = new ListView(MainActivity.this);
                vyhledaneReceptyListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                vyhledaneReceptyListView.setPadding(0,0,0,100);
                vyhledaneReceptyListView.setAdapter(arrayAdapter);
                vyhledaneReceptyLL.addView(vyhledaneReceptyListView);
                vyhledaneReceptyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String clickedItem = String.valueOf(vyhledaneReceptyListView.getItemAtPosition(i));
                        Intent intent = new Intent(getApplicationContext(), Recept.class);
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("nazev_receptu", clickedItem);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    }
                });
                DBreceptyHelper.close();
            }
        }

    }
}


