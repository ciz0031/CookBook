package com.example.erika.cookbook;

/**
 * Created by Erika on 1. 9. 2016.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBreceptyHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "/data/data/com.example.erika.cookbook/databases/";
    private static final String DATABASE_NAME = "recepty.db";
    private static final int DATABASE_VERSION = 1;
    static final String COLUMN_NAZEV_PODKATEGORIE = "nazev_podkategorie";
    static final String COLUMN_NAZEV_KATEGORIE = "nazev_kategorie";
    static final String TABLE_KATEGORIE = "kategorie";
    static final String TABLE_PODKATEGORIE = "podkategorie";
    public Context context;
    static SQLiteDatabase sqliteDataBase;

    private static DBreceptyHelper sInstance = null;
    private int mOpenCounter = 0;
    private static SQLiteOpenHelper mDatabaseHelper;


    public static synchronized DBreceptyHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBreceptyHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     * Parameters of super() are    1. Context
     *                              2. Data Base Name.
     *                              3. Cursor Factory.
     *                              4. Data Base Version.
     */
    private DBreceptyHelper(Context context) {
        super(context, DATABASE_NAME, null ,DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * By calling this method and empty database will be created into the default system path
     * of your application so we are gonna be able to overwrite that database with our database.
     * */
    public void createDataBase() throws IOException{
        //check if the database exists
        boolean databaseExist = checkDataBase();

        if(databaseExist){
            //copyDataBase();
            this.getWritableDatabase();
        }else{
            this.getWritableDatabase();
            this.close();
            copyDataBase();
        }// end if else dbExist
    } // end createDataBase().

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase(){
        File databaseFile = new File(DB_PATH + DATABASE_NAME);
        return databaseFile.exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring byte stream.
     * */
    private void copyDataBase() throws IOException{
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

    /**
     * This method opens the data base connection.
     * First it create the path up till data base of the device.
     * Then create connection with data base.
     */
    public void openDataBase() throws SQLException{
        mOpenCounter++;
        if (mOpenCounter == 1){
            //Open the database
            String myPath = DB_PATH + DATABASE_NAME;
            sqliteDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            //sqliteDataBase = mDatabaseHelper.getWritableDatabase();
        }
    }

    @Override
    public synchronized void close() {
        mOpenCounter--;
        if(mOpenCounter == 0)
            sqliteDataBase.close();
        //super.close();
    }

    public Cursor getPodkategorie(int id_kategorie){
        SQLiteDatabase db = this.getInstance(context).getWritableDatabase();
        String query = "select * from " + TABLE_PODKATEGORIE + " where ID_kategorie = " + id_kategorie;
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public Cursor getPodkategorieName(int id_podkategorie){
        SQLiteDatabase db = this.getInstance(context).getWritableDatabase();
        String query = "select * from " + TABLE_PODKATEGORIE + " where _id = " + id_podkategorie;
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public Cursor getKategorieName(int id_kategorie){
        SQLiteDatabase db = this.getInstance(context).getWritableDatabase();
        String query = "select * from " + TABLE_KATEGORIE + " where _id = " + id_kategorie;
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // No need to write the create table query.
        // As we are using Pre built data base.
        // Which is ReadOnly.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No need to write the update table query.
        // As we are using Pre built data base.
        // Which is ReadOnly.
        // We should not update it as requirements of application.
    }


}
