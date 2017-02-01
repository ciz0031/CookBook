package com.example.erika.cookbook;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NovyRecept extends Activity {
    private ReceptyTable DBrecepty;
    private DBreceptyHelper DBreceptyHelper;
    private SurovinaReceptTable DBsurovinaRecept;
    private EditText nazev_receptu, postup, doba_pripravy, doba_peceni, stupne, prilohy, pocet_porci, surovina, mnozstvi;
    private Spinner kategorie, podkategorie, typ_mnozstvi;
    private Button ulozit, pridat, odebrat, pridatFoto;
    private LinearLayout containerLayout, containerPodkategorie;
    private ImageView foto;
    int podkat = 0;
    static int totalEditTexts;
    int lastAdded = 0;
    private Bundle extras;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private ArrayList<SurovinaReceptO> surovinaReceptArrayList;
    private static final float ET_MNOZSTVI_DP_WIDTH = 60.0f;
    private static final float ET_TYP_MNOZSTVI_DP_WIDTH = 75.0f;
    private static final float ET_SUROVINA_DP_WIDTH = 210.0f;
    private int ET_MNOZSTVI_PX_WIDTH;
    private int ET_TYP_MNOZSTVI_PX_WIDTH;
    private int ET_SUROVINA_PX_WIDTH;
    private int ET_TYP_MNOZSTVI_PX_HEIGHT = 45;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novy_recept);
        this.setTitle("Vytvořit nový recept");
        DBrecepty = new ReceptyTable(this);
        DBsurovinaRecept = new SurovinaReceptTable(this);
        DBreceptyHelper = new DBreceptyHelper(this);
        surovinaReceptArrayList = new ArrayList();

        final float scale = getResources().getDisplayMetrics().density;
        ET_MNOZSTVI_PX_WIDTH = (int) (ET_MNOZSTVI_DP_WIDTH * scale + 0.5f);
        ET_TYP_MNOZSTVI_PX_WIDTH = (int) (ET_TYP_MNOZSTVI_DP_WIDTH * scale + 0.5f);
        ET_SUROVINA_PX_WIDTH = (int) (ET_SUROVINA_DP_WIDTH * scale + 0.5f);

        totalEditTexts = 0;
        nazev_receptu = (EditText) findViewById(R.id.nazev);
        postup = (EditText) findViewById(R.id.postup);
        kategorie = (Spinner) findViewById(R.id.spinnerKategorie);
        podkategorie = (Spinner) findViewById(R.id.spinnerPodkategorie);
        pridat = (Button) findViewById(R.id.pridatPolozkuSeznamuB);
        odebrat = (Button) findViewById(R.id.Bdelete);
        ulozit = (Button) findViewById(R.id.Bsave);
        pridatFoto = (Button) findViewById(R.id.BpridatFoto);
        doba_pripravy = (EditText)findViewById(R.id.doba_pripravy);
        doba_peceni = (EditText)findViewById(R.id.doba_peceni);
        stupne = (EditText)findViewById(R.id.stupne);
        prilohy = (EditText)findViewById(R.id.prilohy);
        pocet_porci = (EditText)findViewById(R.id.pocet_porci);
        containerLayout = (LinearLayout) findViewById(R.id.LL3);
        containerPodkategorie = (LinearLayout) findViewById(R.id.ll08);
        foto = (ImageView) findViewById(R.id.foto);

        odebrat.setVisibility(View.INVISIBLE);

        ArrayAdapter<CharSequence> kat = ArrayAdapter.createFromResource(this ,R.array.kategory_array, android.R.layout.simple_spinner_item);
        kat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kategorie.setAdapter(kat);

        spinnerArrayAdapter = new ArrayAdapter<>(NovyRecept.this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        podkategorie.setAdapter(spinnerArrayAdapter);

        //UPRAVENI RECEPTU - ACTIVITA SE MENI
        extras = getIntent().getExtras();
        if(extras != null){
            String nazev_receptuS = extras.getString("nazev_receptu");
            if (nazev_receptuS != null){
                Cursor recept = DBrecepty.getReceptAccordingToName(nazev_receptuS);
                recept.moveToFirst();
                this.setTitle("Upravit recept");

                nazev_receptu.setText(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_NAZEV_RECEPTU)));
                postup.setText(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POSTUP)));
                kategorie.setSelection(Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_KATEGORIE))) - 1);//protoze spinner zacina na 0, zatimco DB zacina na 1
                doba_pripravy.setText(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PRIPRAVY)));
                doba_peceni.setText(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PECENI)));
                stupne.setText(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_STUPNE)));
                prilohy.setText(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_PRILOHY)));
                pocet_porci.setText(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POCET_PORCI)));
                if(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_PODKATEGORIE)) != null)
                    podkategorie.setSelection(Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_PODKATEGORIE)))-1);

                Cursor podkategorieC = DBreceptyHelper.getPodkategorie(Integer.parseInt(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_ID_KATEGORIE))));
                podkategorieC.moveToFirst();
                while (podkategorieC.isAfterLast() == false){
                    spinnerArrayAdapter.add(podkategorieC.getString(podkategorieC.getColumnIndex(DBreceptyHelper.COLUMN_NAZEV_PODKATEGORIE)));
                    podkategorieC.moveToNext();
                }
                podkategorieC.close();

                //suroviny - nacteni surovin z DB, vytvoreni edittextu a naplneni
                surovinaReceptArrayList = DBsurovinaRecept.getSurovinaRecept(recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_NAZEV_RECEPTU)));
                for (SurovinaReceptO surovinaReceptObj : surovinaReceptArrayList){
                    loadAndFill(surovinaReceptObj);
                    odebrat.setVisibility(View.VISIBLE);
                }
                recept.close();
            }else if (extras.getString("nazev_receptu") != null){ //v pripade, ze pri hledani neexistujiciho receptu dame 'ANO' na vytvoreni noveho receptu
                nazev_receptu.setText(extras.getString("nazev_receptu"));
            }
        }else { //v pripade, ze se vola vytvoreni noveho receptu, vytvorim automaticky jedno policko pro ingredienci (dynamicky)
            loadAndFill(null);
        }

            pridat.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //pridani dalsich surovin, jejich typu a mnozstvi, max pocet surovin na recept je 15
                    if (totalEditTexts < 15){
                        loadAndFill(null);
                    }
                    odebrat.setVisibility(View.VISIBLE);
                }
            });
            odebrat.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //odebirani posledniho radku + kontrola pro viditelnost tlacitka na odebrani
                    if (lastAdded >= 1) {
                        containerLayout.removeView(findViewById(lastAdded));
                        lastAdded--;
                        totalEditTexts--;
                        Log.d("totalEdit po odebrani", "" + totalEditTexts);
                    }
                    if (totalEditTexts == 1 || containerLayout.getChildCount() == 0)
                        odebrat.setVisibility(View.INVISIBLE);
                }
            });
            ulozit.setOnClickListener(new OnClickListener() {
            //ulozeni noveho receptu do DB a kontrola uplnosti receptu, dale uprava existujiciho receptu
                @Override
                public void onClick(View view) {
                    //uprava receptu
                    if (extras != null) {
                        String nazev_receptuS = extras.getString("nazev_receptu");
                        //int ID_receptu = extras.getInt("id_receptu");
                        if (nazev_receptuS != null) {
                            Cursor receptID = DBrecepty.getReceptAccordingToName(nazev_receptuS);
                            receptID.moveToFirst();
                            try {
                                if (nazev_receptu.getText().length() <= 5 || postup.getText().length() <= 10 || doba_pripravy.getText().length() == 0 || doba_peceni.getText().length() == 0 ||
                                        stupne.getText().length() == 0 || pocet_porci.getText().length() == 0) {
                                    Toast.makeText(getApplicationContext(), "Recept není kompletní, je potřeba vyplnit všechna políčka :).", Toast.LENGTH_SHORT).show();
                                } else {
                                    int ID_receptu = Integer.parseInt(receptID.getString(receptID.getColumnIndex(DBrecepty.COLUMN_ID)));
                                    Cursor staryNazevReceptuCursor = DBrecepty.getRecept(ID_receptu);
                                    staryNazevReceptuCursor.moveToFirst();
                                    String staryNazevReceptu = staryNazevReceptuCursor.getString(staryNazevReceptuCursor.getColumnIndex(DBrecepty.COLUMN_NAZEV_RECEPTU));

                                    ReceptO recept = new ReceptO();
                                    recept.ID_receptu = ID_receptu;
                                    recept.nazev_receptu = nazev_receptu.getText().toString();
                                    recept.postup = postup.getText().toString();
                                    recept.doba_pripravy = Integer.parseInt(doba_pripravy.getText().toString());
                                    recept.doba_peceni = Integer.parseInt(doba_peceni.getText().toString());
                                    recept.stupne = Integer.parseInt(stupne.getText().toString());
                                    recept.prilohy = prilohy.getText().toString();
                                    recept.ID_kategorie = (kategorie.getSelectedItemPosition() + 1);
                                    recept.ID_podkategorie = podkat;
                                    recept.pocet_porci = Integer.parseInt(pocet_porci.getText().toString());
                                    DBrecepty.deleteRecept(String.valueOf(ID_receptu));
                                    DBrecepty.insertRecept(recept);
                                    //DBrecepty.updateRecept(recept);

                                    //zmena nazvu receptu musi ovlivnit taky nazev receptu v surovinaReceptArrayList !
                                    //Cursor staryNazevReceptuCursor = DBrecepty.getRecept(ID_receptu);
                                    //staryNazevReceptuCursor.moveToFirst();
                                    //String staryNazevReceptu = staryNazevReceptuCursor.getString(staryNazevReceptuCursor.getColumnIndex(DBrecepty.COLUMN_NAZEV_RECEPTU));
                                    DBsurovinaRecept.deleteSurovinaRecept(staryNazevReceptu);
                                    if (totalEditTexts < 1 || containerLayout.getChildCount() == 0){
                                        Toast.makeText(getApplicationContext(), "Recept není kompletní, je potřeba vyplnit všechna políčka :).", Toast.LENGTH_SHORT).show();
                                    }else {
                                        for (int i = 1; i <= totalEditTexts; i++) {
                                            EditText sur, mnozs;
                                            Spinner typ_mn;
                                            LinearLayout LL;
                                            LL = (LinearLayout) containerLayout.findViewWithTag("LL" + i);
                                            sur = (EditText) LL.findViewWithTag("surovina" + i);
                                            mnozs = (EditText) LL.findViewWithTag("mnozstvi" + i);
                                            typ_mn = (Spinner) LL.findViewWithTag("spinnerTyp_mnozstvi" + i);

                                            SurovinaReceptO novaSurovinaRecept = new SurovinaReceptO();
                                            if (sur.getText().toString().length() != 0 || mnozs.getText().toString().length() != 0){
                                                novaSurovinaRecept.surovina = sur.getText().toString();
                                                novaSurovinaRecept.nazev_receptu = nazev_receptu.getText().toString();
                                                novaSurovinaRecept.mnozstvi = Float.parseFloat(mnozs.getText().toString());
                                                novaSurovinaRecept.typ_mnozstvi = typ_mn.getSelectedItem().toString();
                                                DBsurovinaRecept.insertSurovinaRecept(novaSurovinaRecept);
                                                Toast.makeText(getApplicationContext(), "Záznam byl aktualizován :)", Toast.LENGTH_SHORT).show();
                                                Intent zpet = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(zpet);
                                            }else{
                                                Toast.makeText(getApplicationContext(), "Recept není kompletní, je potřeba vyplnit ingredience.", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    }
                                    DBrecepty.close();
                                }
                            } catch (SQLException ex) {
                                Toast.makeText(getApplicationContext(), R.string.item_not_saved, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else { //jedna se o vytvoreni noveho receptu
                        if (nazev_receptu.getText().length() <= 5 || postup.getText().length() <= 10 || doba_pripravy.getText().length() == 0 || doba_peceni.getText().length() == 0 ||
                                stupne.getText().length() == 0 || pocet_porci.getText().length() == 0 || surovina.getText().length() == 0 || mnozstvi.getText().length() == 0) {
                            Toast.makeText(getApplicationContext(), "Recept není kompletní, je potřeba vyplnit všechna políčka :).", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                if (podkategorie.getSelectedItem() == null) {
                                    podkat = 0;
                                } else {
                                    switch (String.valueOf(kategorie.getSelectedItemPosition() + 1)) { //silene drevacke -_-
                                        case "2": {
                                            podkat = (podkategorie.getSelectedItemPosition() + 1);
                                            break;
                                        }
                                        case "5": {
                                            podkat = (podkategorie.getSelectedItemPosition() + 6);
                                            break;
                                        }
                                        case "6": {
                                            podkat = (podkategorie.getSelectedItemPosition() + 8);
                                            break;
                                        }
                                        default:
                                            podkat = 0;
                                    }
                                }

                                ReceptO recept = new ReceptO();
                                recept.nazev_receptu = nazev_receptu.getText().toString();
                                recept.postup = postup.getText().toString();
                                recept.doba_pripravy = Integer.parseInt(doba_pripravy.getText().toString());
                                recept.doba_peceni = Integer.parseInt(doba_peceni.getText().toString());
                                recept.stupne = Integer.parseInt(stupne.getText().toString());
                                recept.prilohy = prilohy.getText().toString();
                                recept.ID_kategorie = (kategorie.getSelectedItemPosition() + 1);
                                recept.ID_podkategorie = podkat;
                                recept.pocet_porci = Integer.parseInt(pocet_porci.getText().toString());
                                DBrecepty.insertRecept(recept);

                                for (int i = 1; i <= totalEditTexts; i++) {
                                    EditText sur, mnozs;
                                    Spinner typ_mn;
                                    LinearLayout LL;
                                    LL = (LinearLayout) containerLayout.findViewWithTag("LL" + i);
                                    sur = (EditText) LL.findViewWithTag("surovina" + i);
                                    mnozs = (EditText) LL.findViewWithTag("mnozstvi" + i);
                                    typ_mn = (Spinner) LL.findViewWithTag("spinnerTyp_mnozstvi" + i);

                                    SurovinaReceptO surovinaRecept = new SurovinaReceptO();
                                    surovinaRecept.surovina = sur.getText().toString();
                                    surovinaRecept.nazev_receptu = nazev_receptu.getText().toString();
                                    surovinaRecept.mnozstvi = Integer.parseInt(mnozs.getText().toString());
                                    surovinaRecept.typ_mnozstvi = typ_mn.getSelectedItem().toString();
                                    DBsurovinaRecept.insertSurovinaRecept(surovinaRecept);

                                }
                                Toast.makeText(getApplicationContext(), R.string.new_item_saved, Toast.LENGTH_SHORT).show();
                                DBrecepty.close();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                            } catch (SQLException exception) {
                                Toast.makeText(getApplicationContext(), R.string.item_not_saved, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

            });
        kategorie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int spinnerKategoriePosition, long l) {
                //podle vybrane polozky kategorie se meni polozky podkategorii ve spinneru
                Cursor podkategorieC = DBreceptyHelper.getPodkategorie(spinnerKategoriePosition + 1);
                podkategorieC.moveToFirst();

                switch (spinnerKategoriePosition) {
                    case 0: { //snidane
                        containerPodkategorie.setVisibility(View.INVISIBLE);
                        containerPodkategorie.setPadding(0, -10, 0, -15);
                        spinnerArrayAdapter.clear();
                        break;
                    }
                    case 1: { //obed
                        containerPodkategorie.setVisibility(View.VISIBLE);
                        containerPodkategorie.setPadding(0, 35, 0, 20);
                        spinnerArrayAdapter.clear();
                        while (podkategorieC.isAfterLast() == false) {
                            spinnerArrayAdapter.add(podkategorieC.getString(podkategorieC.getColumnIndex(DBreceptyHelper.COLUMN_NAZEV_PODKATEGORIE)));
                            podkategorieC.moveToNext();
                        }
                        break;
                    }
                    case 2: { //vecere
                        containerPodkategorie.setVisibility(View.INVISIBLE);
                        containerPodkategorie.setPadding(0, -10, 0, -15);
                        spinnerArrayAdapter.clear();
                        break;
                    }
                    case 3: { //svacina
                        containerPodkategorie.setVisibility(View.INVISIBLE);
                        containerPodkategorie.setPadding(0, -10, 0, -15);
                        spinnerArrayAdapter.clear();
                        break;
                    }
                    case 4: { //zakusky
                        containerPodkategorie.setVisibility(View.VISIBLE);
                        containerPodkategorie.setPadding(0, 35, 0, 20);
                        spinnerArrayAdapter.clear();
                        while (podkategorieC.isAfterLast() == false) {
                            spinnerArrayAdapter.add(podkategorieC.getString(podkategorieC.getColumnIndex(DBreceptyHelper.COLUMN_NAZEV_PODKATEGORIE)));
                            podkategorieC.moveToNext();
                        }
                        break;
                    }
                    case 5: { //napoje
                        containerPodkategorie.setVisibility(View.VISIBLE);
                        containerPodkategorie.setPadding(0, 35, 0, 20);
                        spinnerArrayAdapter.clear();
                        while (podkategorieC.isAfterLast() == false) {
                            spinnerArrayAdapter.add(podkategorieC.getString(podkategorieC.getColumnIndex(DBreceptyHelper.COLUMN_NAZEV_PODKATEGORIE)));
                            podkategorieC.moveToNext();
                        }
                        break;
                    }
                }
                podkategorieC.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //doNothing
            }
        });
        pridatFoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: pridat foto - volani camery a ulozeni obrazku idealne do DB, local storage prozatim..
                Intent ziskatFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
                ziskatFoto.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
                startActivityForResult(ziskatFoto, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });
    }
    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Kuchařka");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Kuchařka", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }

    private void loadAndFill(SurovinaReceptO surovinaReceptObj){
        totalEditTexts++;
        LinearLayout LL = new LinearLayout(NovyRecept.this);
        mnozstvi = new EditText(NovyRecept.this);
        surovina = new EditText(NovyRecept.this);
        typ_mnozstvi = new Spinner(NovyRecept.this);

        LL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        LL.setOrientation(LinearLayout.HORIZONTAL);

        surovina.setLayoutParams(new LinearLayout.LayoutParams(ET_SUROVINA_PX_WIDTH, LinearLayout.LayoutParams.WRAP_CONTENT));
        mnozstvi.setLayoutParams(new LinearLayout.LayoutParams(ET_MNOZSTVI_PX_WIDTH, LinearLayout.LayoutParams.WRAP_CONTENT));
        mnozstvi.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        typ_mnozstvi.setLayoutParams(new LinearLayout.LayoutParams(ET_TYP_MNOZSTVI_PX_WIDTH, ET_TYP_MNOZSTVI_PX_HEIGHT));//width, height 75,45

        ArrayAdapter<CharSequence> typM = ArrayAdapter.createFromResource(NovyRecept.this, R.array.typ_mnozstvi_array, android.R.layout.simple_spinner_item);
        typM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typ_mnozstvi.setAdapter(typM);

        surovina.setTag("surovina" + totalEditTexts);
        mnozstvi.setTag("mnozstvi" + totalEditTexts);
        typ_mnozstvi.setTag("spinnerTyp_mnozstvi" + totalEditTexts);
        LL.setTag("LL" + totalEditTexts);

        lastAdded = totalEditTexts;
        surovina.setId(lastAdded);
        mnozstvi.setId(lastAdded);
        typ_mnozstvi.setId(lastAdded);

        if (surovinaReceptObj != null){
            surovina.setText(surovinaReceptObj.surovina);
            mnozstvi.setText(String.valueOf(surovinaReceptObj.mnozstvi));
            typ_mnozstvi.setSelection(typM.getPosition(surovinaReceptObj.typ_mnozstvi));
        }else{
            surovina.setText("");
            mnozstvi.setText("");
        }

        LL.setId(lastAdded);
        containerLayout.addView(LL);
        LL.addView(mnozstvi);
        LL.addView(typ_mnozstvi);
        LL.addView(surovina);
    }
}
