package com.example.erika.cookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Erika on 10. 9. 2016.
 */
public class SurovinaReceptTable {
    static DBreceptyHelper DBrecepty;
    static Context context;
    public static String TABLE_NAME = "surovina_recept";
    public static String COLUMN_SUROVINA = "surovina";
    public static String COLUMN_RECEPT = "recept";
    public static String COLUMN_MNOZSTVI = "mnozstvi";
    public static String COLUMN_TYP = "typ_mnozstvi";
    ArrayList arrayListSurovinaRecept;

    public SurovinaReceptTable(Context context){
        this.context = context;
        DBrecepty = new DBreceptyHelper(context);
        arrayListSurovinaRecept = new ArrayList();
    }

    public void insertSurovinaRecept(SurovinaReceptO SurovinaReceptO){
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("surovina", SurovinaReceptO.surovina);
        cv.put("recept", SurovinaReceptO.nazev_receptu);
        cv.put("mnozstvi", SurovinaReceptO.mnozstvi);
        cv.put("typ_mnozstvi", SurovinaReceptO.typ_mnozstvi);
        db.insert(TABLE_NAME, null, cv);

        db.close();
    }

    public void updateSurovinaRecept(String surovinaOld, String surovinaNew, String recept, String mnozstvi, String typ_mnozstvi){
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("surovina", surovinaNew);
        cv.put("recept", recept);
        cv.put("mnozstvi",mnozstvi);
        cv.put("typ_mnozstvi", typ_mnozstvi);
        db.update(TABLE_NAME, cv, " surovina = '" + surovinaOld + "' AND recept = '" + recept + "'", null);
        db.close();
    }

    public void updateSurovinaReceptNazevReceptu(String novyNazevReceptu, String staryNazevReceptu){
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("recept", novyNazevReceptu);
        db.update(TABLE_NAME, cv, " recept = '" + staryNazevReceptu + "'", null);
        db.close();
    }

    public void deleteSurovinaRecept(String recept){
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        db.delete(TABLE_NAME, "recept = '" + recept + "'", null);
        db.close();
    }

    public ArrayList<SurovinaReceptO> getSurovinaRecept(String nazev_receptu){
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String query = "select * from " + TABLE_NAME + " where " + COLUMN_RECEPT + " ='" + nazev_receptu + "'";
        Cursor surovinaRecept = db.rawQuery(query, null);
        surovinaRecept.moveToFirst();
        ArrayList<SurovinaReceptO> SurovinaReceptObj = ReadSurovinaRecept(surovinaRecept);
        surovinaRecept.close();
        db.close();
        return SurovinaReceptObj;
    }

    public ArrayList<SurovinaReceptO> getSurovinaReceptSurovina(String surovina){ //pro vyhledavani receptu podle suroviny
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String query = "select distinct " + COLUMN_RECEPT + " from " + TABLE_NAME + " where " + COLUMN_SUROVINA + " LIKE '" + surovina + "%'";
        Cursor surovinaRecept = db.rawQuery(query, null);
        surovinaRecept.moveToFirst();
        ArrayList<SurovinaReceptO> SurovinaReceptObj = new ArrayList<SurovinaReceptO>();
        while (surovinaRecept.isAfterLast() == false){
            SurovinaReceptO SurovinaRecept = new SurovinaReceptO();
            SurovinaRecept.nazev_receptu = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_RECEPT));
            SurovinaReceptObj.add(SurovinaRecept);
            surovinaRecept.moveToNext();
        }
        surovinaRecept.close();
        db.close();
        return SurovinaReceptObj;
    }

    public ArrayList<SurovinaReceptO> getAllIngredients(){ //vyhledani vsech surovin
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String query = "select distinct " + COLUMN_SUROVINA + " from " + TABLE_NAME;//distinct = bez duplicitnich hodnot
        Cursor surovinaRecept = db.rawQuery(query, null);
        surovinaRecept.moveToFirst();

        ArrayList<SurovinaReceptO> SurovinaReceptObj = new ArrayList<SurovinaReceptO>();
        while (surovinaRecept.isAfterLast() == false){
            SurovinaReceptO SurovinaRecept = new SurovinaReceptO();
            SurovinaRecept.surovina = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_SUROVINA));
            SurovinaReceptObj.add(SurovinaRecept);
            surovinaRecept.moveToNext();
        }

        surovinaRecept.close();
        db.close();
        return SurovinaReceptObj;
    }

    private ArrayList<SurovinaReceptO> ReadSurovinaRecept(Cursor surovinaRecept){
        ArrayList<SurovinaReceptO> SurovinaReceptOBJ = new ArrayList<SurovinaReceptO>();
        while(surovinaRecept.isAfterLast() == false){
            SurovinaReceptO SurovinaRecept = new SurovinaReceptO();
            SurovinaRecept.surovina = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_SUROVINA));
            SurovinaRecept.nazev_receptu = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_RECEPT));
            SurovinaRecept.mnozstvi = surovinaRecept.getFloat(surovinaRecept.getColumnIndex(COLUMN_MNOZSTVI));
            SurovinaRecept.typ_mnozstvi = surovinaRecept.getString(surovinaRecept.getColumnIndex(COLUMN_TYP));
            SurovinaReceptOBJ.add(SurovinaRecept);
            surovinaRecept.moveToNext();
        }
        return SurovinaReceptOBJ;
    }

    public void close(){
        DBrecepty.close();
    }
}
