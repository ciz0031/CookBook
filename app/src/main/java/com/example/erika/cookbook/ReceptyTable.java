package com.example.erika.cookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Erika on 10. 9. 2016.
 */
public class ReceptyTable {
    static DBreceptyHelper DBrecepty;
    static Context context;
    public static String TABLE_NAME = "recept";
    public static String COLUMN_ID = "_id";
    public static String COLUMN_NAZEV_RECEPTU = "nazev_receptu";
    public static String COLUMN_POSTUP = "postup";
    public static String COLUMN_DOBA_PRIPRAVY = "doba_pripravy";
    public static String COLUMN_DOBA_PECENI = "doba_peceni";
    public static String COLUMN_STUPNE = "stupne";
    public static String COLUMN_PRILOHY = "prilohy";
    public static String COLUMN_ID_KATEGORIE = "ID_kategorie";
    public static String COLUMN_ID_PODKATEGORIE = "ID_podkategorie";
    public static String COLUMN_FOTO = "foto";
    public static String COLUMN_POCET_PORCI = "pocet_porci";


    public ReceptyTable(Context context) {
        this.context = context;
        //DBrecepty = new DBreceptyHelper(context);
    }
    public static boolean insertRecept(ReceptO ReceptO){
        DBrecepty = new DBreceptyHelper(context);
        SQLiteDatabase db = DBrecepty.getWritableDatabase();

        String foto = "";
        ContentValues contentValues = new ContentValues();
        contentValues.put("nazev_receptu", ReceptO.nazev_receptu);
        contentValues.put("postup", ReceptO.postup);
        contentValues.put("doba_pripravy", ReceptO.doba_pripravy);
        contentValues.put("doba_peceni", ReceptO.doba_peceni);
        contentValues.put("stupne", ReceptO.stupne);
        contentValues.put("prilohy", ReceptO.prilohy);
        contentValues.put("ID_kategorie", ReceptO.ID_kategorie);
        contentValues.put("ID_podkategorie", ReceptO.ID_podkategorie);
        contentValues.put("foto", ReceptO.foto);
        contentValues.put("pocet_porci", ReceptO.pocet_porci);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public void updateRecept(ReceptO ReceptO){
        DBrecepty = new DBreceptyHelper(context);
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String foto = "";
        ContentValues data = new ContentValues();
        data.put("nazev_receptu", ReceptO.nazev_receptu);
        data.put("postup", ReceptO.postup);
        data.put("doba_pripravy", ReceptO.doba_pripravy);
        data.put("doba_peceni", ReceptO.doba_peceni);
        data.put("stupne", ReceptO.stupne);
        data.put("prilohy", ReceptO.prilohy);
        data.put("ID_kategorie", ReceptO.ID_kategorie);
        data.put("ID_podkategorie", ReceptO.ID_podkategorie);
        data.put("foto", ReceptO.foto);
        data.put("pocet_porci", ReceptO.pocet_porci);
        db.update(TABLE_NAME, data, "_id = " + ReceptO.ID_receptu, null);
        db.close();
    }

    public static ArrayList<ReceptO> getReceptyFromDB(int ID_kategorie){ //podle kategorie
        DBrecepty = new DBreceptyHelper(context);
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String query = "select * from recept where ID_kategorie = " + ID_kategorie;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<ReceptO> ReceptO = ReadRecepty(cursor);
        cursor.close();
        db.close();
        return ReceptO;
    }

    public static ArrayList<ReceptO> getAllData(){
        DBrecepty = new DBreceptyHelper(context);
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String query = "select * from recept";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<ReceptO> ReceptO = ReadRecepty(cursor);
        cursor.close();
        db.close();
        return ReceptO;
    }

    public static ArrayList<ReceptO> getReceptPodleNazvu(String nazev_receptu){ //podle nazvu receptu
        DBrecepty = new DBreceptyHelper(context);
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String query = "select * from recept where nazev_receptu LIKE '" + nazev_receptu + "%'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<ReceptO> ReceptO = ReadRecepty(cursor);
        cursor.close();
        db.close();
        return ReceptO;
    }

    private static ArrayList<ReceptO> ReadRecepty(Cursor cursor)
    {
        ArrayList<ReceptO> ReceptyO = new ArrayList<ReceptO>();

        while (cursor.isAfterLast() == false)
        {
            ReceptO ReceptO = new ReceptO();
            ReceptO.ID_receptu = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            ReceptO.nazev_receptu = cursor.getString(cursor.getColumnIndex(COLUMN_NAZEV_RECEPTU));
            ReceptO.postup = cursor.getString(cursor.getColumnIndex(COLUMN_POSTUP));
            ReceptO.doba_pripravy = cursor.getInt(cursor.getColumnIndex(COLUMN_DOBA_PRIPRAVY));
            ReceptO.doba_peceni = cursor.getInt(cursor.getColumnIndex(COLUMN_DOBA_PECENI));
            ReceptO.stupne = cursor.getInt(cursor.getColumnIndex(COLUMN_STUPNE));
            ReceptO.prilohy = cursor.getString(cursor.getColumnIndex(COLUMN_PRILOHY));
            ReceptO.ID_kategorie = cursor.getInt(cursor.getColumnIndex(COLUMN_ID_KATEGORIE));
            //ReceptO.ID_podkategorie = cursor.getInt(cursor.getColumnIndex(COLUMN_ID_PODKATEGORIE));
            //ReceptO.foto = cursor.getString(cursor.getColumnIndex(COLUMN_FOTO));
            ReceptO.pocet_porci = cursor.getInt(cursor.getColumnIndex(COLUMN_POCET_PORCI));
            ReceptyO.add(ReceptO);
            cursor.moveToNext();
        }
        return ReceptyO;
    }

    public Cursor getRecept(int IDreceptu){
        DBrecepty = new DBreceptyHelper(context);
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String query = "select * from recept where _id = " + IDreceptu;
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public Cursor getReceptAccordingToName(String nazev_receptu){
        DBrecepty = new DBreceptyHelper(context);
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        String query = "select * from " + TABLE_NAME + " where " + COLUMN_NAZEV_RECEPTU + " ='" + nazev_receptu + "'";
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public void deleteRecept(String IDreceptu){
        DBrecepty = new DBreceptyHelper(context);
        SQLiteDatabase db = DBrecepty.getWritableDatabase();
        db.delete(TABLE_NAME, "_id = " + IDreceptu, null);
        //db.close();
    }

    public void close() { DBrecepty.close(); }

}
