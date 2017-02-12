package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Erika on 10. 11. 2016.
 */
public class recipeSummaryPart extends Fragment{
    private TextView TVpocetPorci, TVdobaPripravy, TVdobaPeceni, TVstupne, TVkategorie, TVnazev_receptu;
    private String stringPocetPorci, stringDobaPripravy, stringDobaPeceni, stringStupne, stringPodkategorie,
            stringKategorie, nazev_receptu;
    private ReceptyTable DBrecepty;
    private DBreceptyHelper DBhelper;
    private int ID_receptu,ID_podkategorie, ID_kategorie;
    private Cursor recept, podkategorie, kategorie;
    private ImageButton timerImageButton;
    private RatingBar hodnoceni;
    final Handler handler = new Handler();
    public ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recipe_summary_layout, container, false);
        TVdobaPeceni = (TextView) v.findViewById(R.id.doba_peceni);
        TVpocetPorci = (TextView) v.findViewById(R.id.pocet_porci);
        TVdobaPripravy = (TextView) v.findViewById(R.id.doba_pripravy);
        TVstupne = (TextView) v.findViewById(R.id.stupne);
        TVkategorie = (TextView) v.findViewById(R.id.kategorie);
        timerImageButton = (ImageButton) v.findViewById(R.id.timerImageButton);
        hodnoceni = (RatingBar) v.findViewById(R.id.ratingBar);
        //TVnazev_receptu = (TextView) v.findViewById(R.id.nazev_receptuTV);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBrecepty = new ReceptyTable(getActivity());
        DBhelper = new DBreceptyHelper(getActivity());



        Bundle extras = getActivity().getIntent().getExtras();
        nazev_receptu = extras.getString("nazev_receptu");

        LongOperationsThreadRecept MyThreadRecept = new LongOperationsThreadRecept();
        MyThreadRecept.execute("acc_to_name");
    }

    public void setReceptProperties(Cursor recept){
        ID_receptu = Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID)));
//        ID_podkategorie = Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_PODKATEGORIE)));
        String IDpodkategorie = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_PODKATEGORIE));
        ID_kategorie = Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_KATEGORIE)));
        if (IDpodkategorie != null){
            ID_podkategorie = Integer.parseInt(IDpodkategorie);
        }else ID_podkategorie = 0;

        kategorie = DBhelper.getKategorieName(ID_kategorie);
        kategorie.moveToFirst();
        stringKategorie = kategorie.getString(kategorie.getColumnIndex(DBreceptyHelper.COLUMN_NAZEV_KATEGORIE));
        kategorie.close();

        if(ID_podkategorie != 0){
            podkategorie = DBhelper.getPodkategorieName(ID_podkategorie);
            podkategorie.moveToFirst();
            stringPodkategorie = podkategorie.getString(podkategorie.getColumnIndex(DBreceptyHelper.COLUMN_NAZEV_PODKATEGORIE));
            podkategorie.close();
            DBhelper.close();
            TVkategorie.setText(stringKategorie + ", " + stringPodkategorie);
        }else
            TVkategorie.setText(stringKategorie);

        stringPocetPorci = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POCET_PORCI));
        stringDobaPripravy = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PRIPRAVY));
        stringDobaPeceni = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PECENI));
        stringStupne = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_STUPNE));
        hodnoceni.setRating(Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_HODNOCENI))));
        recept.close();

        TVpocetPorci.setText(stringPocetPorci);
        TVdobaPripravy.setText(stringDobaPripravy);
        TVdobaPeceni.setText(stringDobaPeceni);
        TVstupne.setText("(na " + stringStupne + " °C)");


        timerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //otevreni aktivity s countdownTimerem (s moznosti spustit odpocet) + poslat hodnotu DobaPeceni
                Bundle bundle = new Bundle();
                bundle.putInt("doba_peceni", Integer.valueOf(stringDobaPeceni));
                Intent countdownTimer = new Intent(getActivity(), CountdownTimer.class);
                countdownTimer.putExtras(bundle);
                startActivity(countdownTimer);
            }
        });

    }
    @Override
    public void onResume() {
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
        DBreceptyHelper DBreceptyHelper = new DBreceptyHelper(getActivity());
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
            setReceptProperties(recept);


            //Ingredience - nacteni z DB a vypsani do nove vytvorenych views - pomoci asynctask
            //LongOperationsThread MyLongOperations = new LongOperationsThread();
            //MyLongOperations.execute(stringNazevReceptu);

        }

        @Override
        protected Cursor doInBackground(String... strings) {

            handler.postDelayed(pdRunnable, 500);
            try {
                DBreceptyHelper.createDataBase();
                Log.d("DB", "database created");
            } catch (IOException e) {
                e.printStackTrace();
            }
            DBreceptyHelper.openDataBase();

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
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        };
    }
}
