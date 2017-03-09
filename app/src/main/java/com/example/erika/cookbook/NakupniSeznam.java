package com.example.erika.cookbook;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NakupniSeznam extends Activity {
    private Button pridatPolozkuB;
    public Button smazatPolozkuB;
    public EditText polozkaSeznamuET;
    public LinearLayout polozkaSeznamuLL, mainLL, pridatPolozkuLL;
    public int pocetPolozek = 0;
    private int POLOZKA_SEZNAMU_ET_WIDTH_PX;
    private int SMAZAT_POLOZKU_B_PX;
    private static final float SMAZAT_POLOZKU_B_DP = 40.0f;
    private static final float POLOZKA_SEZNAMU_ET_WIDTH_DP = 290.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nakupni_seznam);


        final float scale = getResources().getDisplayMetrics().density;
        POLOZKA_SEZNAMU_ET_WIDTH_PX = (int) (POLOZKA_SEZNAMU_ET_WIDTH_DP * scale + 0.5f);
        SMAZAT_POLOZKU_B_PX = (int) (SMAZAT_POLOZKU_B_DP * scale + 0.5f);

        mainLL = (LinearLayout) findViewById(R.id.mainLL);
        pridatPolozkuLL = (LinearLayout) findViewById(R.id.pridatPolozkuLL);
        pridatPolozkuB = (Button) findViewById(R.id.pridatPolozkuSeznamuB);

        pridatPolozkuLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pridatPolozkuDoSeznamu(null);
            }
        });
        pridatPolozkuB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pridatPolozkuDoSeznamu(null);
            }
        });
        //nacteni ulozenych shared pref.
        loadSavedPreferences();

        //posilani suroviny z receptu do nakupniho seznamu
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            String surovina = extras.getString("surovina");
            if (surovina.length() > 1){
                pridatPolozkuDoSeznamu(surovina);
            }

            onBackPressed();
        }

        if (mainLL.getChildCount() < 1){
            pridatPolozkuDoSeznamu(null);
        }
    }
    private void loadSavedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int sizeOfSharedPref = sharedPreferences.getAll().size();
        Log.d("sizeOfSharedPref", "velikost " + sizeOfSharedPref);
        String value;

        for (int i = 0; i <= sizeOfSharedPref+1; i++){
            value = sharedPreferences.getString(String.valueOf(i), "");
            if (value == "" || value == " "){
                //doNothing
            }else {
                pridatPolozkuDoSeznamu(value);
            }
        }
    }

    private void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void clearPref(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public void pridatPolozkuDoSeznamu(String polozkaSeznamu){
        pocetPolozek++;
        polozkaSeznamuLL = new LinearLayout(NakupniSeznam.this);
        polozkaSeznamuLL.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        polozkaSeznamuLL.setOrientation(LinearLayout.HORIZONTAL);
        polozkaSeznamuLL.setTag("polozkaSeznamuLL" + pocetPolozek);

        polozkaSeznamuET = new EditText(NakupniSeznam.this);
        polozkaSeznamuET.setLayoutParams(new ViewGroup.LayoutParams(POLOZKA_SEZNAMU_ET_WIDTH_PX, ViewGroup.LayoutParams.WRAP_CONTENT));//width 300dp
        polozkaSeznamuET.setTag("polozkaSeznamuET" + pocetPolozek);
        polozkaSeznamuET.setText(polozkaSeznamu);
        polozkaSeznamuET.requestFocus();
        //donuti ukazani klavesnice na nove vytvoreny text box ..
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        smazatPolozkuB = new Button(NakupniSeznam.this);
        smazatPolozkuB.setLayoutParams(new ViewGroup.LayoutParams(SMAZAT_POLOZKU_B_PX, SMAZAT_POLOZKU_B_PX));//40x40dp
        smazatPolozkuB.setTag("smazatPolozkuB" + pocetPolozek);
        smazatPolozkuB.setId(pocetPolozek);
        smazatPolozkuB.setText("x");
        smazatPolozkuB.setTextColor(Color.RED);
        smazatPolozkuB.setBackgroundColor(Color.TRANSPARENT);
        smazatPolozkuB.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

        mainLL.addView(polozkaSeznamuLL);
        polozkaSeznamuLL.addView(polozkaSeznamuET);
        polozkaSeznamuLL.addView(smazatPolozkuB);

        smazatPolozkuB.setOnClickListener(handleClick(smazatPolozkuB));
    }

    View.OnClickListener handleClick(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout polozkaSeznamuLinearLayout;
                int idPolozky = button.getId();
                //Log.d("polozka id", String.valueOf(idPolozky));
                polozkaSeznamuLinearLayout = (LinearLayout) mainLL.findViewWithTag("polozkaSeznamuLL" + idPolozky);
                mainLL.removeView(polozkaSeznamuLinearLayout);
                if(mainLL.getChildCount() < 1){
                    pocetPolozek = 0;
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nakupni_seznam, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_deleteShoppingList) {
            mainLL.removeAllViews();
            clearPref();
            pridatPolozkuDoSeznamu(null);
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        //Log.d("back", "back button pressed!");
        //ulozi do shared pref. udaje z edittextu a vrati na main activiny + ukaze toast s Ulozeno
        String value;
        EditText polozka;
        clearPref();
        //Log.d("pocet polozek", String.valueOf(pocetPolozek));
        for (int i = 0; i <= pocetPolozek; i++){
            polozka = (EditText) mainLL.findViewWithTag("polozkaSeznamuET" + i);
            if (polozka != null){
                value = polozka.getText().toString();
                savePreferences(String.valueOf(i), value);
            }

        }
        Toast toast = Toast.makeText(this, "Nákupní seznam aktualizován.", Toast.LENGTH_SHORT);
        toast.show();
        super.onBackPressed();
    }
}
