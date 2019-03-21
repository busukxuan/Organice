package edu.sutd.organice;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObservable;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class DbHelper extends SQLiteOpenHelper {
    //information of database
    private final Context context;
    private static String PACKAGE_NAME;
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteDatabase readableDb;
    private SQLiteDatabase writeableDb;
    private static DbHelper DbHelper;

    //constructor
    private DbHelper(Context context){
        super(context, EventContract.EventEntry.TABLE_NAME, null, DATABASE_VERSION );
        this.context = context;
    }

    //singleton pattern
    static DbHelper createCharaDbHelper(Context context) {
        if( DbHelper == null){
            DbHelper = new DbHelper(context.getApplicationContext());
        }
        return DbHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(EventContract.EventSql.SQL_CREATE_TABLE);
        fillTable(sqLiteDatabase);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(EventContract.EventSql.SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }


    private void fillTable(SQLiteDatabase sqLiteDatabase){

    }



}
