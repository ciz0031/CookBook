package com.example.erika.cookbook;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

public class Recept extends FragmentActivity {
    private TextView TWpocetPorci, TWpostup, TWdobaPripravy, TWdobaPeceni, TWstupne, TVingredience, TVprilohy, TVnazev_receptu, TVkategorie;
    private ImageButton nakupniSeznamB, minusArrowB, plusArrowB;
    private LinearLayout ingredienceLL, surovinaLL;
    private String stringPocetPorci, stringPostup, stringDobaPripravy, stringDobaPeceni, stringStupne, stringPodkategorie, stringPrilohy, stringNazevReceptu = "", nazev_receptu, stringKategorie;
    private ReceptyTable DBrecepty;
    private SurovinaReceptTable DBsurovina_recept;
    private DBreceptyHelper DBreceptyHelper;
    private int ID_receptu,ID_podkategorie, ID_kategorie;
    private Cursor recept, podkategorie, kategorie;

    private int pocetSurovin = 0;
    private static final float INGREDIENCE_DP_WIDTH = 30.0f;
    private static final float INGREDIENCE_DP_HEIGHT = 30.0f;
    private int INGREDIENCE_PX_WIDTH;
    private int INGREDIENCE_PX_HEIGHT;
    private ArrayList<Float> mnozstviIngredience;

    final Handler handler = new Handler();
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recept);

        Bundle extras = getIntent().getExtras();
        TVnazev_receptu = (TextView) findViewById(R.id.nazev_receptuTV);
        DBrecepty = new ReceptyTable(this);
        DBsurovina_recept = new SurovinaReceptTable(this);

        LongOperationsThreadRecept MyThreadRecept = new LongOperationsThreadRecept();
        if(extras != null) {
            ID_receptu = extras.getInt("id_receptu");
            nazev_receptu = extras.getString("nazev_receptu");
            if (nazev_receptu != null){ //pri otevreni receptu z vyhledanych surovin podle nazvu receptu
                MyThreadRecept.execute("acc_to_name");
            }
            if(ID_receptu > 0){ //pri otevreni receptu ze seznamu receptu podle ID receptu
                MyThreadRecept.execute("get_recept");
            }


        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    private void setReceptName(Cursor recept){
        stringNazevReceptu = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_NAZEV_RECEPTU));
        TVnazev_receptu.setText(stringNazevReceptu);
        recept.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recept, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_rate) {
            //TODO: hodnoceni receptu
            return true;
        }
        else if (id == R.id.action_update){
            Intent intent = new Intent(getApplicationContext(), NovyRecept.class);
            Bundle dataBundle = new Bundle();
            dataBundle.putString("nazev_receptu", nazev_receptu);
            intent.putExtras(dataBundle);
            startActivity(intent);
        }
        else if(id == R.id.action_deleteRecept){
            //smazani receptu - alert dialog na potvrzeni + pokud ano tak smazat polozku z DB
            AlertDialog.Builder builder = new AlertDialog.Builder(Recept.this);
            builder.setMessage(R.string.dialog_message)
                    .setTitle(R.string.dialog_title);
            builder.setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog - vratit zpet na recept
                    //doNothing
                }
            });
            builder.setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button - smazat polozku z DB
                    //Log.d("id receptu pro smazani", String.valueOf(ID_receptu));
                    Bundle kategorieID = new Bundle();
                    Cursor receptCursor = DBrecepty.getReceptAccordingToName(nazev_receptu);
                    receptCursor.moveToFirst();
                    int IDkategorie = Integer.parseInt(receptCursor.getString(receptCursor.getColumnIndex(DBrecepty.COLUMN_ID_KATEGORIE)));
                    kategorieID.putInt("id", IDkategorie);
                    //Log.d("kategorie receptu", String.valueOf(IDkategorie));
                    String ID_receptu = receptCursor.getString(receptCursor.getColumnIndex(DBrecepty.COLUMN_ID));
                    DBrecepty.deleteRecept(ID_receptu);
                    //Log.d("nazev receptu pro smazani", nazev_receptu);
                    DBsurovina_recept.deleteSurovinaRecept(nazev_receptu);
                    DBrecepty.close();
                    DBsurovina_recept.close();

                    Toast.makeText(getApplicationContext(), R.string.item_deleted + " (" + nazev_receptu + ").", Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(Recept.this, MainActivity.class);
                    startActivity(mainActivity);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if (id == R.id.action_shoppingList){
            Intent nakupniSeznam = new Intent(getApplicationContext(), NakupniSeznam.class);
            startActivity(nakupniSeznam);
        }
        else if (id == R.id.action_share){
            //TODO:sdílení receptu
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // check if any view exists on current view
            TVnazev_receptu = (TextView) findViewById(R.id.nazev_receptuTV);
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




    private class LongOperationsThreadRecept extends AsyncTask<String, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Cursor recept) {
            super.onPostExecute(recept);
            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            recept.moveToFirst();
            setReceptName(recept);

            //Ingredience - nacteni z DB a vypsani do nove vytvorenych views - pomoci asynctask
            //LongOperationsThread MyLongOperations = new LongOperationsThread();
            //MyLongOperations.execute(stringNazevReceptu);


        }

        @Override
        protected Cursor doInBackground(String... strings) {

            handler.postDelayed(pdRunnable, 500);

            String method = strings[0];
            if (method.equals("acc_to_name")){
                recept = DBrecepty.getReceptAccordingToName(nazev_receptu);
            }
            else if (method.equals("get_recept")){
                recept = DBrecepty.getRecept(ID_receptu);
            }

            return recept;
        }
        final Runnable pdRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(Recept.this);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        };
    }
}
