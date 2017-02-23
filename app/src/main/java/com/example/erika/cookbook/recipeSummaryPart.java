package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private Cursor recept, podkategorie, kategorie, image;
    private ImageButton timerImageButton;
    private RatingBar hodnoceni;
    private ImageView foto;
    final Handler handler = new Handler();
    public ProgressDialog progressDialog;

    DBreceptyHelper DBreceptyHelper = new DBreceptyHelper(getActivity());

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
        foto = (ImageView) v.findViewById(R.id.imageViewFoto);
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

        LongOperationsThreadGetImage MyThreadGetImage = new LongOperationsThreadGetImage();
        MyThreadGetImage.execute();


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
        stringKategorie = kategorie.getString(kategorie.getColumnIndex(DBreceptyHelper.COLUMN_NAZEV_KATEGORIE));
        kategorie.close();

        if(ID_podkategorie != 0){
            podkategorie = DBhelper.getPodkategorieName(ID_podkategorie);
            podkategorie.moveToFirst();
            stringPodkategorie = podkategorie.getString(podkategorie.getColumnIndex(DBreceptyHelper.COLUMN_NAZEV_PODKATEGORIE));
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
                Intent countdownTimer = new Intent(getActivity(), CountdownTimer.class);
                countdownTimer.putExtras(bundle);
                startActivity(countdownTimer);
            }
        });
        recept.close();

        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: zobrazeni obrazku ve vetsim formatu
            }
        });
    }

    public void setImage(Cursor image){
        if (image != null) {
            String imagePathString = image.getString(image.getColumnIndex(DBrecepty.COLUMN_FOTO));
            if (imagePathString != null) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(imagePathString);
                foto.setImageBitmap(imageBitmap);
            } else {
                foto.setImageResource(R.drawable.noimagefound);
            }
        }
        image.close();
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
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        };
    }

    private class LongOperationsThreadGetImage extends AsyncTask<String, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Cursor image) {
            super.onPostExecute(image);
            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            image.moveToFirst();
            setImage(image);
        }

        @Override
        protected Cursor doInBackground(String... strings) {
            handler.postDelayed(pdRunnable, 100);
            recept = DBrecepty.getReceptAccordingToName(nazev_receptu);
            recept.moveToFirst();
            ID_receptu = recept.getInt(recept.getColumnIndex(DBrecepty.COLUMN_ID));
            image = DBrecepty.getImagePath(ID_receptu);
            return image;
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
