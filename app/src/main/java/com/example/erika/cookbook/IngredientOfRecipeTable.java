package com.example.erika.cookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Erika on 10. 9. 2016.
 */
public class IngredientOfRecipeTable {
    static DBreceptyHelper DBrecepty;
    static Context context;
    public static String TABLE_NAME = "surovina_recept";
    public static String COLUMN_SUROVINA = "surovina";
    public static String COLUMN_RECEPT = "recept";
    public static String COLUMN_MNOZSTVI = "mnozstvi";
    public static String COLUMN_TYP = "typ_mnozstvi";
    ArrayList arrayListSurovinaRecept;

    private static IngredientOfRecipeTable sInstance = null;

    private IngredientOfRecipeTable(Context context){
        this.context = context;
        //DBrecepty = new DBreceptyHelper(context);
        arrayListSurovinaRecept = new ArrayList();
    }

    public static synchronized IngredientOfRecipeTable getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new IngredientOfRecipeTable(context.getApplicationContext());
        }
        return sInstance;
    }

    public void insertSurovinaRecept(IngredientOfRecipeObject IngredientOfRecipeObject){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("surovina", IngredientOfRecipeObject.surovina);
        cv.put("recept", IngredientOfRecipeObject.nazev_receptu);
        cv.put("mnozstvi", IngredientOfRecipeObject.mnozstvi);
        cv.put("typ_mnozstvi", IngredientOfRecipeObject.typ_mnozstvi);
        db.insert(TABLE_NAME, null, cv);
    }

    public void deleteSurovinaRecept(String recept){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        db.delete(TABLE_NAME, "recept = '" + recept + "'", null);
    }

    public ArrayList<IngredientOfRecipeObject> getSurovinaRecept(String nazev_receptu){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "select * from " + TABLE_NAME + " where " + COLUMN_RECEPT + " ='" + nazev_receptu + "'";
        Cursor surovinaRecept = db.rawQuery(query, null);
        surovinaRecept.moveToFirst();
        ArrayList<IngredientOfRecipeObject> surovinaReceptObj = ReadSurovinaRecept(surovinaRecept);
        surovinaRecept.close();
        return surovinaReceptObj;
    }


    public ArrayList<IngredientOfRecipeObject> getSurovinaRecepty(String suroviny){ //pro vyhledavani receptů podle surovin
        String[] items = suroviny.split(", ");
        String surovina = "";
        String query = "";
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String partOfQuery= " INTERSECT SELECT " + COLUMN_RECEPT + " FROM " + TABLE_NAME + " WHERE " + COLUMN_SUROVINA + " = ";

        if (suroviny.length()>1) {
            for (String item : items) {
                Log.d("SUROVINA", item);
                surovina = surovina + "'" + item + "'" + partOfQuery;
                //select recept from surovina_recept where foodstuff = "brambory" intersect select recept from surovina_recept where foodstuff = "polohrubá mouka"
            }
            surovina = surovina.substring(0, surovina.length() - partOfQuery.length());
            query = "select distinct " + COLUMN_RECEPT + " from " + TABLE_NAME + " where " + COLUMN_SUROVINA + " = " + surovina;
        }else {
            surovina = suroviny;
            query = "select distinct " + COLUMN_RECEPT + " from " + TABLE_NAME + " where " + COLUMN_SUROVINA + " LIKE '" + surovina + "%'";
        }

        Cursor surovinaRecept = db.rawQuery(query, null);
        surovinaRecept.moveToFirst();
        ArrayList<IngredientOfRecipeObject> surovinaReceptObj = new ArrayList<IngredientOfRecipeObject>();
        while (surovinaRecept.isAfterLast() == false){
            IngredientOfRecipeObject SurovinaRecept = new IngredientOfRecipeObject();
            SurovinaRecept.nazev_receptu = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_RECEPT));
            surovinaReceptObj.add(SurovinaRecept);
            surovinaRecept.moveToNext();
        }
        surovinaRecept.close();
        return surovinaReceptObj;
    }

    public ArrayList<IngredientOfRecipeObject> getAllIngredients(){ //vyhledani vsech surovin
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "select distinct " + COLUMN_SUROVINA + " from " + TABLE_NAME;//distinct = bez duplicitnich hodnot
        Cursor surovinaRecept = db.rawQuery(query, null);
        surovinaRecept.moveToFirst();

        ArrayList<IngredientOfRecipeObject> surovinaReceptObj = new ArrayList<IngredientOfRecipeObject>();
        while (surovinaRecept.isAfterLast() == false){
            IngredientOfRecipeObject SurovinaRecept = new IngredientOfRecipeObject();
            SurovinaRecept.surovina = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_SUROVINA));
            surovinaReceptObj.add(SurovinaRecept);
            surovinaRecept.moveToNext();
        }
        return surovinaReceptObj;
    }

    private ArrayList<IngredientOfRecipeObject> ReadSurovinaRecept(Cursor surovinaRecept){
        ArrayList<IngredientOfRecipeObject> surovinaReceptOBJ = new ArrayList<IngredientOfRecipeObject>();
        while(surovinaRecept.isAfterLast() == false){
            IngredientOfRecipeObject SurovinaRecept = new IngredientOfRecipeObject();
            SurovinaRecept.surovina = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_SUROVINA));
            SurovinaRecept.nazev_receptu = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_RECEPT));
            SurovinaRecept.mnozstvi = surovinaRecept.getFloat(surovinaRecept.getColumnIndex(COLUMN_MNOZSTVI));
            SurovinaRecept.typ_mnozstvi = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_TYP));
            surovinaReceptOBJ.add(SurovinaRecept);
            surovinaRecept.moveToNext();
        }
        return surovinaReceptOBJ;
    }

    public void close(){
        DBrecepty.close();
    }
}
