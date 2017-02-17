package com.example.erika.cookbook;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

public class Recept extends FragmentActivity {
    private TextView TVnazev_receptu;
    private String stringNazevReceptu = "", nazev_receptu;
    private ReceptyTable DBrecepty;
    private SurovinaReceptTable DBsurovina_recept;
    private int ID_receptu;
    private Cursor recept;
    private CheckBox oblibenyReceptChB;

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
        oblibenyReceptChB = (CheckBox) findViewById(R.id.oblibenyReceptChB);

        final LongOperationsThreadRecept MyThreadRecept = new LongOperationsThreadRecept();
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

        oblibenyReceptChB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    //uprava receptu na oblibeny = 1
                    Log.d("oblibeny","1");
                    DBrecepty.updateRecipe_setFavourite(nazev_receptu, 1);
                }else {
                    //oblibeny = 0
                    Log.d("oblibeny","0");
                    DBrecepty.updateRecipe_setFavourite(nazev_receptu, 0);
                }
            }
        });

    }

    private void setReceptName(Cursor recept){
        stringNazevReceptu = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_NAZEV_RECEPTU));
        String isFavourite = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_OBLIBENY));
        TVnazev_receptu.setText(stringNazevReceptu);
        Log.d("isFavourite", isFavourite);
        if (isFavourite.equals("1")){
            oblibenyReceptChB.setChecked(true);
        }else {
            oblibenyReceptChB.setChecked(false);
        }
        //recept.close();
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
            Intent hodnoceni = new Intent(getApplicationContext(), HodnoceniReceptu.class);
            Bundle recept = new Bundle();
            recept.putString("nazev_receptu", nazev_receptu);
            hodnoceni.putExtras(recept);
            startActivity(hodnoceni);
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
                    //DBrecepty.close();
                    //DBsurovina_recept.close();

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
            shareIntentSpecificApps();
        }
        return super.onOptionsItemSelected(item);
    }

    public void shareIntentSpecificApps() {
        List<Intent> intentShareList = new ArrayList<Intent>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(shareIntent, 0);

        for (ResolveInfo resInfo : resolveInfoList) {
            String packageName = resInfo.activityInfo.packageName;
            String name = resInfo.activityInfo.name;

            if (packageName.contains("com.facebook") ||
                    packageName.contains("com.twitter.android") ||
                    packageName.contains("com.google.android.apps.plus") ||
                    packageName.contains("mms") ||
                    packageName.contains("com.google.android.talk") ||
                    packageName.contains("com.google.android.gm")) {

                if (name.contains("com.twitter.android.DMActivity")) {
                    continue;
                }

                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Recept - " + nazev_receptu);

                intent.putExtra(Intent.EXTRA_TEXT, nazev_receptu + "\n\n" + ingredientsOfRecipe(recept) +
                        "\n\n" + contentOfRecipe(recept) + "\n\n" + evaluationOfRecipe(recept));
                intentShareList.add(intent);
            }
        }

        if (intentShareList.isEmpty()) {
            Toast.makeText(Recept.this, "Nenalezeny žádné aplikace, pomocí kterých lze recept sdílet", Toast.LENGTH_SHORT).show();
        } else {
            Intent chooserIntent = Intent.createChooser(intentShareList.remove(0), "Sdílej recept pomocí ...");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentShareList.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        }
    }

    public String ingredientsOfRecipe(Cursor recept){
        recept.moveToFirst();
        String pocetPorci = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POCET_PORCI));
        String ingredientsOfRecipe = "Recept je na " + pocetPorci + " porce." + "\n\n" + "Ingredience: ";

        final ArrayList<SurovinaReceptO> ALsurovinaRecept = DBsurovina_recept.getSurovinaRecept(nazev_receptu);
        for (SurovinaReceptO surovinaReceptObj : ALsurovinaRecept){
            ingredientsOfRecipe = ingredientsOfRecipe + "\n" +
                    surovinaReceptObj.mnozstvi + surovinaReceptObj.typ_mnozstvi + " " + surovinaReceptObj.surovina;
        }


        return ingredientsOfRecipe;
    }

    public String contentOfRecipe(Cursor recept) {
        recept.moveToFirst();
        String postup = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POSTUP));
        String dobaPeceni = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PECENI));
        String dobaPripravy = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PRIPRAVY));
        String prilohy = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_PRILOHY));
        String stupne = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_STUPNE));

        String contentOfRecipe = "Příprava: " + dobaPripravy + "min \n" + "Tepelná úprava: " +
                dobaPeceni + "min (na " + stupne + "°C)\n\n" + "Postup: \n " + postup +"\n" + "Přílohy: " + prilohy;
        return contentOfRecipe;
    }

    public String evaluationOfRecipe(Cursor recept){
        recept.moveToFirst();
        String evaluationOfRecipe = "";
        int hodnoceni = recept.getInt(recept.getColumnIndex(DBrecepty.COLUMN_HODNOCENI));
        String komentar = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_KOMENTAR));
        evaluationOfRecipe = "Moje hodnocení receptu: \n Počet hvězdiček: " + hodnoceni + "\n Komentář: " + komentar;
        return evaluationOfRecipe;
    }
    @Override
    protected void onResume() {
        super.onResume();
        LongOperationsThreadRecept longOperationsThreadRecept = new LongOperationsThreadRecept();
        longOperationsThreadRecept.execute("acc_to_name");
        /*try {
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
        }*/
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
