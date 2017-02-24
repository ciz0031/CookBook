package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Erika on 10. 11. 2016.
 */
public class recipeIngredientsPart extends Fragment {
    private ImageButton nakupniSeznamB,minusArrowB, plusArrowB;
    private LinearLayout ingredienceLL, surovinaLL;
    private SurovinaReceptTable DBsurovina_recept;
    private ReceptyTable DBrecepty;
    private String nazev_receptu, stringPocetPorci;
    private TextView TVingredience, TVpocetPorci;
    private int pocetSurovin = 0;
    private static final float INGREDIENCE_DP_WIDTH = 30.0f;
    private static final float INGREDIENCE_DP_HEIGHT = 30.0f;
    private int INGREDIENCE_PX_WIDTH;
    private int INGREDIENCE_PX_HEIGHT;
    private ArrayList<Float> mnozstviIngredience = new ArrayList<>();
    private float scale;
    private Cursor recept;

    final Handler handler = new Handler();
    public ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recipe_ingredients_layout, container, false);

        ingredienceLL = (LinearLayout) v.findViewById(R.id.ingredienceLL);
        minusArrowB = (ImageButton) v.findViewById(R.id.minusArrowB);
        plusArrowB = (ImageButton) v.findViewById(R.id.plusArrowB);
        TVpocetPorci = (TextView) v.findViewById(R.id.pocet_porci);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBsurovina_recept = SurovinaReceptTable.getInstance(getActivity());
        DBrecepty = ReceptyTable.getInstance(getActivity());

        scale = getResources().getDisplayMetrics().density;
        INGREDIENCE_PX_WIDTH = (int) (INGREDIENCE_DP_WIDTH * scale + 0.5f);
        INGREDIENCE_PX_HEIGHT = (int) (INGREDIENCE_DP_HEIGHT * scale + 0.5f);

        Bundle extras = getActivity().getIntent().getExtras();
        nazev_receptu = extras.getString("nazev_receptu");

        LongOperationsThread longOperationsIngredience = new LongOperationsThread();
        longOperationsIngredience.execute(nazev_receptu);

    }

    @Override
    public void onStart() {
        super.onStart();
        recept = DBrecepty.getReceptAccordingToName(nazev_receptu);
        recept.moveToFirst();
        stringPocetPorci = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POCET_PORCI));
        recept.close();
        //DBrecepty.close();

        TVpocetPorci.setText(stringPocetPorci);

        plusArrowB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TVpocetPorci.setText(String.valueOf(Integer.parseInt(TVpocetPorci.getText().toString()) + 1));
                prepocitatIngredience(Integer.parseInt(TVpocetPorci.getText().toString()));
            }
        });

        //odebrani poctu porci a prepocet surovin
        minusArrowB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TVpocetPorci.getText().toString() == "1") {
                    //doNothing
                    //pokud se pocet porci dostane na hodnotu 1, nelze uz pocet porci odebrat
                } else {
                    TVpocetPorci.setText(String.valueOf(Integer.parseInt(TVpocetPorci.getText().toString()) - 1));
                    prepocitatIngredience(Integer.parseInt(TVpocetPorci.getText().toString()));
                }
            }


        });
    }

    private void prepocitatIngredience(int pocetPorci){
        LinearLayout surovinaLL;
        TextView surovinaTV;
        String surovinaS, ingredience;
        double noveMnozstvi;
        for (int i = 0; i < mnozstviIngredience.size(); i++){
            surovinaLL = (LinearLayout) ingredienceLL.findViewWithTag("surovinaLL" + (i+1));
            surovinaTV = (TextView) surovinaLL.findViewWithTag("TVingredience" + (i+1));
            surovinaS = surovinaTV.getText().toString();

            noveMnozstvi = mnozstviIngredience.get(i) / Float.parseFloat(stringPocetPorci);
            noveMnozstvi = noveMnozstvi * pocetPorci;

            BigDecimal bd = new BigDecimal(Double.toString(noveMnozstvi));//nastaveni poctu desetinnych mist (a zaokrouhleni)
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

            ingredience = surovinaS.replaceAll("(\\d+)","").replaceAll("(\\.)",""); //vymaze puvodni hodnoty mnozstvi suroviny (i tecky)
            surovinaTV.setText(bd+ingredience);
        }
    }

    View.OnClickListener pridatDoNakupnihoSeznamu(final ImageButton button){
        return new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout ingredienceLinearLayout;
                TextView surovina;
                int idSuroviny = button.getId();
                //Log.d("polozka id", String.valueOf(idPolozky));
                ingredienceLinearLayout = (LinearLayout) ingredienceLL.findViewWithTag("surovinaLL" + idSuroviny);
                surovina = (TextView) ingredienceLinearLayout.findViewWithTag("TVingredience" + idSuroviny);
                String surovinaString = surovina.getText().toString();
                String surovinaSubString = surovinaString.substring(surovinaString.indexOf(' ') + 1);
                String surovinaSubSubString = surovinaSubString.substring(surovinaSubString.indexOf(" ") + 1);

                Intent intent = new Intent(getActivity(), NakupniSeznam.class);
                Bundle bundle = new Bundle();
                bundle.putString("surovina", surovinaSubSubString);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };
    }


    private class LongOperationsThread extends AsyncTask<String, Void, ArrayList<SurovinaReceptO>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<SurovinaReceptO> ALsurovinaRecept) {
            super.onPostExecute(ALsurovinaRecept);
            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            for (SurovinaReceptO surovinaReceptObj : ALsurovinaRecept){
                pocetSurovin++;
                surovinaLL = new LinearLayout(getActivity());
                surovinaLL.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                surovinaLL.setOrientation(LinearLayout.HORIZONTAL);
                surovinaLL.setPadding(20, 0, 0, 0);
                surovinaLL.setTag("surovinaLL" + pocetSurovin);

                TVingredience = new TextView(getActivity());
                TVingredience.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                TVingredience.setText(surovinaReceptObj.mnozstvi + " " + surovinaReceptObj.typ_mnozstvi + " " + surovinaReceptObj.surovina);
                TVingredience.setTextColor(Color.BLACK);
                TVingredience.setTag("TVingredience" + pocetSurovin);
                mnozstviIngredience.add(surovinaReceptObj.mnozstvi);

                nakupniSeznamB = new ImageButton(getActivity());
                nakupniSeznamB.setLayoutParams(new ViewGroup.LayoutParams(INGREDIENCE_PX_WIDTH, INGREDIENCE_PX_HEIGHT));//30x30dp
                nakupniSeznamB.setImageResource(R.drawable.ikona_shopping_list);
                nakupniSeznamB.setTag("nakupniSeznamB" + pocetSurovin);
                nakupniSeznamB.setId(pocetSurovin);
                nakupniSeznamB.setBackgroundColor(Color.TRANSPARENT);

                ingredienceLL.addView(surovinaLL);
                surovinaLL.addView(TVingredience);
                surovinaLL.addView(nakupniSeznamB);
                nakupniSeznamB.setOnClickListener(pridatDoNakupnihoSeznamu(nakupniSeznamB));
            }
        }

        @Override
        protected ArrayList<SurovinaReceptO> doInBackground(String... strings) {
            handler.postDelayed(pdRunnable, 200);
            final ArrayList<SurovinaReceptO> ALsurovinaRecept = DBsurovina_recept.getSurovinaRecept(strings[0]);
            return ALsurovinaRecept;
        }
    }

    public final Runnable pdRunnable = new Runnable() {
        @Override
        public void run() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    };
}
