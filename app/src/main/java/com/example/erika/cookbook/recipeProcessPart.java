package com.example.erika.cookbook;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Erika on 10. 11. 2016.
 */
public class recipeProcessPart extends Fragment{
    private RecipeTable DBrecepty;
    private TextView TVpostup, TVprilohy;
    private int ID_receptu;
    private String stringPostup, stringPrilohy, nazev_receptu;
    final Handler handler = new Handler();
    public ProgressDialog progressDialog;
    private Cursor recept;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recipe_process_layout, container, false);

        TVpostup = (TextView) v.findViewById(R.id.postup);
        TVprilohy = (TextView) v.findViewById(R.id.prilohy);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBrecepty = RecipeTable.getInstance(getActivity());



    }

    @Override
    public void onStart() {
        Bundle extras = getActivity().getIntent().getExtras();
        nazev_receptu = extras.getString("nazev_receptu");

        LongOperationsThreadPostup longOperationsThreadPostup = new LongOperationsThreadPostup();
        longOperationsThreadPostup.execute("acc_to_name");
        super.onStart();
    }

    public void setReceptProperties(Cursor recept){
        ID_receptu = Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID)));

        stringPostup = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POSTUP));
        stringPrilohy = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_PRILOHY));
        recept.close();

        TVpostup.setText(stringPostup);

        if (stringPrilohy == null){
            TVprilohy.setText("-žádné uložené-");
        }else{
            TVprilohy.setText(stringPrilohy);
        }
    }

    private class LongOperationsThreadPostup extends AsyncTask<String, Void, Cursor> {
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
