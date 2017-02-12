package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class HodnoceniReceptu extends Activity {
    private RatingBar hodnoceni;
    private TextView nazev_receptu;
    private EditText komentar;
    private Button uloz;
    Bundle data;
    String nazev_receptuS ="";
    private DBreceptyHelper DBreceptyHelper;
    private ReceptyTable DBrecepty;
    final Handler handler = new Handler();
    public ProgressDialog progressDialog;
    Cursor recept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hodnoceni_receptu);

        DBrecepty = new ReceptyTable(this);
        DBreceptyHelper = new DBreceptyHelper(this);
        final LongOperationsThread MyThreadRecept = new LongOperationsThread();

        hodnoceni = (RatingBar) findViewById(R.id.ratingBar2);
        nazev_receptu = (TextView) findViewById(R.id.receptTV);
        komentar = (EditText) findViewById(R.id.komentarET);
        uloz = (Button) findViewById(R.id.ulozit);

        data = getIntent().getExtras();
        if (data != null){
            nazev_receptuS = data.getString("nazev_receptu");
            if (nazev_receptuS != null){
                nazev_receptu.setText(nazev_receptuS);
            }
        }else {
            Toast.makeText(HodnoceniReceptu.this, "Něco se nepovedlo..", Toast.LENGTH_SHORT);
        }

        MyThreadRecept.execute();

        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hodnoceni.getRating() != 0){
                    DBrecepty.updateRecipe_setRating(nazev_receptuS, (int)hodnoceni.getRating(), komentar.getText().toString());
                    finish();
                }
            }
        });


    }
    private class LongOperationsThread extends AsyncTask<String, Void, Cursor> {
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
            hodnoceni.setRating(recept.getInt(recept.getColumnIndex(DBrecepty.COLUMN_HODNOCENI)));
            if (recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_KOMENTAR))!= null){
                komentar.setText(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_KOMENTAR)));
            }

        }

        @Override
        protected Cursor doInBackground(String... strings) {

            handler.postDelayed(pdRunnable, 500);

            recept = DBrecepty.getReceptAccordingToName(nazev_receptuS);
            return recept;
        }
        final Runnable pdRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(HodnoceniReceptu.this);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        };
    }
}
