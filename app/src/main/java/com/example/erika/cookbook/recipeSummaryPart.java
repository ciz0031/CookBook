package com.example.erika.cookbook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;


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
    private CheckBox oblibenyReceptChB;

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
        oblibenyReceptChB = (CheckBox) v.findViewById(R.id.oblibenyReceptChB);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBrecepty = ReceptyTable.getInstance(getActivity());
        DBhelper = com.example.erika.cookbook.DBreceptyHelper.getInstance(getActivity());
    }

    @Override
    public void onStart() {
        Bundle extras = getActivity().getIntent().getExtras();
        nazev_receptu = extras.getString("nazev_receptu");

        LongOperationsThreadRecept MyThreadRecept = new LongOperationsThreadRecept();
        MyThreadRecept.execute("acc_to_name");
        super.onStart();
    }

    public void setReceptProperties(Cursor recept){
        ID_receptu = Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID)));
        String IDpodkategorie = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_PODKATEGORIE));
        ID_kategorie = Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_KATEGORIE)));
        if (IDpodkategorie != null){
            ID_podkategorie = Integer.parseInt(IDpodkategorie);
        }else ID_podkategorie = 0;

        kategorie = DBhelper.getKategorieName(ID_kategorie);
        kategorie.moveToFirst();
        stringKategorie = kategorie.getString(kategorie.getColumnIndex(DBhelper.COLUMN_NAZEV_KATEGORIE));
        kategorie.close();

        if(ID_podkategorie != 0){
            podkategorie = DBhelper.getPodkategorieName(ID_podkategorie);
            podkategorie.moveToFirst();
            stringPodkategorie = podkategorie.getString(podkategorie.getColumnIndex(DBhelper.COLUMN_NAZEV_PODKATEGORIE));
            podkategorie.close();
            TVkategorie.setText(stringKategorie + ", " + stringPodkategorie);
        }else
            TVkategorie.setText(stringKategorie);

        stringPocetPorci = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POCET_PORCI));
        stringDobaPripravy = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PRIPRAVY));
        stringDobaPeceni = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PECENI));
        stringStupne = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_STUPNE));
        hodnoceni.setRating(Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_HODNOCENI))));

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
                Intent countdownTimer = new Intent(getActivity(), NewCountdownTimer.class);
                countdownTimer.putExtras(bundle);
                startActivity(countdownTimer);
            }
        });


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

        String isFavourite = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_OBLIBENY));
        if (isFavourite.equals("1")){
            oblibenyReceptChB.setChecked(true);
        }else {
            oblibenyReceptChB.setChecked(false);
        }
        recept.close();
    }



    @Override
    public void onResume() {
        super.onResume();
        this.onCreate(null);
    }
    private class LongOperationsThreadRecept extends AsyncTask<String, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            handler.postDelayed(pdRunnable, 500);
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

        }

        @Override
        protected Cursor doInBackground(String... strings) {
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
