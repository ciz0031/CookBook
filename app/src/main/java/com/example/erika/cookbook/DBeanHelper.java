package com.example.erika.cookbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Erika on 23. 10. 2016.
 */
public class DBeanHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "/data/data/com.example.erika.cookbook/databases/";

    public static final String DATABASE_NAME = "eans.db";
    public static final String EANS_TABLE_NAME = "eans";
    public static final String COLUMN_EAN = "eanNumber"; // ean
    public static final String COLUMN_NAME = "surovina"; //nazev produktu

    public Context context;
    static SQLiteDatabase sqliteDataBase;

    public static ArrayList<String> arrayList = new ArrayList<String>();

    public DBeanHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }


    public void createDataBase() throws IOException{
        boolean databaseExist = checkDataBase();

        if(databaseExist){

            this.getWritableDatabase();
            copyDataBase();
        }else{
            this.getWritableDatabase();
            this.close();
            copyDataBase();
        }// end if else dbExist
    }

    public boolean checkDataBase(){
        File databaseFile = new File(DB_PATH + DATABASE_NAME);
        return databaseFile.exists();
    }

    private void copyDataBase() throws IOException {
        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DATABASE_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DATABASE_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        //transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DATABASE_NAME;
        sqliteDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if(sqliteDataBase != null)
            sqliteDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insertSurovinaEAN(int ean, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EAN, ean);
        contentValues.put(COLUMN_NAME, name);
        db.insert(EANS_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public ArrayList<EANsurovinaO> getSurovina(String ean){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + EANS_TABLE_NAME + " where " + COLUMN_EAN + " = " + ean;
        Cursor suroviny =  db.rawQuery(query, null);
        suroviny.moveToFirst();
        ArrayList<EANsurovinaO> eanSurovina = ReadEANsurovina(suroviny);
        suroviny.close();
        db.close();
        return eanSurovina;
    }

    public boolean updateSurovinaEAN (int ean, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put(COLUMN_NAME, name);
        db.update(EANS_TABLE_NAME, data, COLUMN_EAN + "=" + ean, null);
        db.close();
        return true;
    }


    public ArrayList<EANsurovinaO> getAllSurovinaEANs()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + EANS_TABLE_NAME;
        Cursor res =  db.rawQuery(query , null );
        res.moveToFirst();

        ArrayList<EANsurovinaO> EANreceptObj = ReadEANsurovina(res);

        res.close();
        db.close();
        return EANreceptObj;
    }

    private ArrayList<EANsurovinaO> ReadEANsurovina (Cursor res){
        ArrayList<EANsurovinaO> EANreceptObj = new ArrayList<>();
        while(res.isAfterLast() == false){
            EANsurovinaO eanSurovinaO = new EANsurovinaO();
            eanSurovinaO.eanNumber = Integer.parseInt(res.getString(res.getColumnIndex(COLUMN_EAN)));
            eanSurovinaO.surovina = res.getString(res.getColumnIndex(COLUMN_NAME));
            EANreceptObj.add(eanSurovinaO);
            res.moveToNext();
        }
        return EANreceptObj;
    }


}
