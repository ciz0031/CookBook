package com.example.erika.cookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    public static String COLUMN_OBLIBENY = "oblibeny";
    public static String COLUMN_HODNOCENI = "hodnoceni";
    public static String COLUMN_KOMENTAR = "komentar";

    private static ReceptyTable sInstance = null;

    private ReceptyTable(Context context) {
        this.context = context;
        //DBrecepty = new DBreceptyHelper(context);
    }
    public static synchronized ReceptyTable getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new ReceptyTable(context.getApplicationContext());
        }
        return sInstance;
    }
    public static boolean insertRecept(ReceptO ReceptO){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();

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
        contentValues.put("oblibeny", ReceptO.oblibeny);
        contentValues.put("hodnoceni", ReceptO.hodnoceni);
        contentValues.put("komentar", ReceptO.komentar);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public void updateRecipe_setFavourite(String nazev_receptu, int oblibeny){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "update " + TABLE_NAME + " set " + COLUMN_OBLIBENY + " = " + oblibeny + " where " +
                COLUMN_NAZEV_RECEPTU + " = '" + nazev_receptu + "'";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        c.close();
    }

    public void updateRecipe_setRating(String nazev_receptu, int rating, String komentar){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "update " + TABLE_NAME + " set " + COLUMN_HODNOCENI + " = " + rating + ", " +
                COLUMN_KOMENTAR + " = '" + komentar + "' where " + COLUMN_NAZEV_RECEPTU + " = '" + nazev_receptu + "'";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        c.close();
    }

    public static ArrayList<ReceptO> getRecipeFromDB(int ID_kategorie, String razeni){ //podle kategorie
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "";
        if (razeni.equals("1")){
            query = "select * from recept where ID_kategorie = " + ID_kategorie;
        }else if (razeni.equals("2")){
            query = "select * from recept where ID_kategorie = " + ID_kategorie + " order by " + COLUMN_HODNOCENI + " desc";
        }else if (razeni.equals("3")){
            query = "select * from recept where ID_kategorie = " + ID_kategorie + " order by "
                    + COLUMN_DOBA_PECENI + " + " + COLUMN_DOBA_PRIPRAVY + " asc";
        }
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<ReceptO> ReceptO = ReadRecepty(cursor);
        cursor.close();
        return ReceptO;
    }

    public void insertImagePath(String recept, String image) {
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "update " + TABLE_NAME + " set " + COLUMN_FOTO + " = '" + image + "' where " + COLUMN_NAZEV_RECEPTU +
                " = '" + recept + "'";
        Cursor cursor = db.rawQuery(query, null);
        Log.d("INSERT IMAGE", query);
        cursor.moveToFirst();
        cursor.close();
    }

    public Cursor getImagePath(int id_receptu) {
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "select " + COLUMN_FOTO + " from " + TABLE_NAME + " where " + COLUMN_ID + " = " + id_receptu;
        Cursor cur = db.rawQuery(query, null);
        return cur;
    }

    public static ArrayList<ReceptO> getOrderedRecipe(String nazev_receptu, String razeni){ //podle nazvu receptu
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "";
        if (razeni.equals("1")){
            query = "select * from recept where nazev_receptu LIKE '" + nazev_receptu + "%'";
        }else if (razeni.equals("2")){
            query = "select * from recept where nazev_receptu LIKE '" + nazev_receptu + "%'" + " order by "
                    + COLUMN_HODNOCENI + " desc";
        }else if (razeni.equals("3")){
            query = "select * from recept where nazev_receptu LIKE '" + nazev_receptu + "%'" + " order by "
                    + COLUMN_DOBA_PECENI + " + " + COLUMN_DOBA_PRIPRAVY + " asc";
        }

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<ReceptO> ReceptO = ReadRecepty(cursor);
        cursor.close();
        return ReceptO;
    }

    public static ArrayList<ReceptO> getFavouriteRecipe(){ //podle oblibenosti receptu
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "select * from recept where " + COLUMN_OBLIBENY + " = 1";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<ReceptO> ReceptO = ReadRecepty(cursor);
        cursor.close();
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
            ReceptO.foto = cursor.getString(cursor.getColumnIndex(COLUMN_FOTO));
            ReceptO.pocet_porci = cursor.getInt(cursor.getColumnIndex(COLUMN_POCET_PORCI));
            ReceptO.oblibeny = cursor.getInt(cursor.getColumnIndex(COLUMN_OBLIBENY));
            ReceptO.hodnoceni = cursor.getInt(cursor.getColumnIndex(COLUMN_HODNOCENI));
            ReceptO.komentar = cursor.getString(cursor.getColumnIndex(COLUMN_KOMENTAR));
            ReceptyO.add(ReceptO);
            cursor.moveToNext();
        }
        return ReceptyO;
    }

    public Cursor getRecept(int IDreceptu){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "select * from recept where _id = " + IDreceptu;
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public Cursor getReceptAccordingToName(String nazev_receptu){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "select * from " + TABLE_NAME + " where " + COLUMN_NAZEV_RECEPTU + " ='" + nazev_receptu + "'";
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public void deleteRecept(String IDreceptu){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        db.delete(TABLE_NAME, "_id = " + IDreceptu, null);
    }

    public void close() { DBrecepty.close(); }

}
