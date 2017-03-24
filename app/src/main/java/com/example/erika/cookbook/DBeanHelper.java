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
    private static DBeanHelper sInstance = null;
    private int mOpenCounter = 0;

    private DBeanHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    public static synchronized DBeanHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBeanHelper(context.getApplicationContext());
        }
        return sInstance;
    }



    public void createDataBase() throws IOException{
        boolean databaseExist = checkDataBase();

        if(databaseExist){

            this.getWritableDatabase();
            //copyDataBase();
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
        mOpenCounter++;
        if (mOpenCounter == 1) {
            //Open the database
            String myPath = DB_PATH + DATABASE_NAME;
            sqliteDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        }
    }

    @Override
    public synchronized void close() {
        mOpenCounter--;
        if(mOpenCounter == 0)
            sqliteDataBase.close();
        //super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insertSurovinaEAN(long ean, String name)
    {
        SQLiteDatabase db = this.getInstance(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EAN, ean);
        contentValues.put(COLUMN_NAME, name);
        db.insert(EANS_TABLE_NAME, null, contentValues);
        return true;
    }

    public ArrayList<EANsurovinaO> getSurovina(String ean){
        SQLiteDatabase db = this.getInstance(context).getReadableDatabase();
        String query = "select * from " + EANS_TABLE_NAME + " where " + COLUMN_EAN + " = '" + ean +"'";
        Cursor suroviny =  db.rawQuery(query, null);
        suroviny.moveToFirst();
        ArrayList<EANsurovinaO> eanSurovina = ReadEANsurovina(suroviny);
        suroviny.close();
        return eanSurovina;
    }

    private ArrayList<EANsurovinaO> ReadEANsurovina (Cursor res){
        ArrayList<EANsurovinaO> EANreceptObj = new ArrayList<>();
        while(res.isAfterLast() == false){
            EANsurovinaO eanSurovinaO = new EANsurovinaO();
            eanSurovinaO.eanNumber = Long.parseLong(res.getString(res.getColumnIndex(COLUMN_EAN)));
            eanSurovinaO.foodstuff = res.getString(res.getColumnIndex(COLUMN_NAME));
            EANreceptObj.add(eanSurovinaO);
            res.moveToNext();
        }
        return EANreceptObj;
    }


}
