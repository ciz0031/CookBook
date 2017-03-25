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
public class RecipeTable {
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

    private static RecipeTable sInstance = null;

    private RecipeTable(Context context) {
        this.context = context;
        //DBrecepty = new DBreceptyHelper(context);
    }
    public static synchronized RecipeTable getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new RecipeTable(context.getApplicationContext());
        }
        return sInstance;
    }
    public static boolean insertRecept(RecipeObject RecipeObject){
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("nazev_receptu", RecipeObject.nazev_receptu);
        contentValues.put("postup", RecipeObject.postup);
        contentValues.put("doba_pripravy", RecipeObject.doba_pripravy);
        contentValues.put("doba_peceni", RecipeObject.doba_peceni);
        contentValues.put("stupne", RecipeObject.stupne);
        contentValues.put("prilohy", RecipeObject.prilohy);
        contentValues.put("ID_kategorie", RecipeObject.ID_kategorie);
        contentValues.put("ID_podkategorie", RecipeObject.ID_podkategorie);
        contentValues.put("foto", RecipeObject.foto);
        contentValues.put("pocet_porci", RecipeObject.pocet_porci);
        contentValues.put("oblibeny", RecipeObject.oblibeny);
        contentValues.put("hodnoceni", RecipeObject.hodnoceni);
        contentValues.put("komentar", RecipeObject.komentar);
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

    public static ArrayList<RecipeObject> getRecipeFromDB(int ID_kategorie, String razeni){ //podle kategorie
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
        ArrayList<RecipeObject> RecipeObject = ReadRecepty(cursor);
        cursor.close();
        return RecipeObject;
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

    public static ArrayList<RecipeObject> getOrderedRecipe(String nazev_receptu, String razeni){ //podle nazvu receptu
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
        ArrayList<RecipeObject> RecipeObject = ReadRecepty(cursor);
        cursor.close();
        return RecipeObject;
    }

    public static ArrayList<RecipeObject> getFavouriteRecipe(){ //podle oblibenosti receptu
        SQLiteDatabase db = DBrecepty.getInstance(context).getWritableDatabase();
        String query = "select * from recept where " + COLUMN_OBLIBENY + " = 1";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<RecipeObject> RecipeObject = ReadRecepty(cursor);
        cursor.close();
        return RecipeObject;
    }

    private static ArrayList<RecipeObject> ReadRecepty(Cursor cursor)
    {
        ArrayList<RecipeObject> ReceptyO = new ArrayList<RecipeObject>();
        while (cursor.isAfterLast() == false)
        {
            RecipeObject RecipeObject = new RecipeObject();
            RecipeObject.ID_receptu = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            RecipeObject.nazev_receptu = cursor.getString(cursor.getColumnIndex(COLUMN_NAZEV_RECEPTU));
            RecipeObject.postup = cursor.getString(cursor.getColumnIndex(COLUMN_POSTUP));
            RecipeObject.doba_pripravy = cursor.getInt(cursor.getColumnIndex(COLUMN_DOBA_PRIPRAVY));
            RecipeObject.doba_peceni = cursor.getInt(cursor.getColumnIndex(COLUMN_DOBA_PECENI));
            RecipeObject.stupne = cursor.getInt(cursor.getColumnIndex(COLUMN_STUPNE));
            RecipeObject.prilohy = cursor.getString(cursor.getColumnIndex(COLUMN_PRILOHY));
            RecipeObject.ID_kategorie = cursor.getInt(cursor.getColumnIndex(COLUMN_ID_KATEGORIE));
            //ReceptO.ID_podkategorie = cursor.getInt(cursor.getColumnIndex(COLUMN_ID_PODKATEGORIE));
            RecipeObject.foto = cursor.getString(cursor.getColumnIndex(COLUMN_FOTO));
            RecipeObject.pocet_porci = cursor.getInt(cursor.getColumnIndex(COLUMN_POCET_PORCI));
            RecipeObject.oblibeny = cursor.getInt(cursor.getColumnIndex(COLUMN_OBLIBENY));
            RecipeObject.hodnoceni = cursor.getInt(cursor.getColumnIndex(COLUMN_HODNOCENI));
            RecipeObject.komentar = cursor.getString(cursor.getColumnIndex(COLUMN_KOMENTAR));
            ReceptyO.add(RecipeObject);
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
