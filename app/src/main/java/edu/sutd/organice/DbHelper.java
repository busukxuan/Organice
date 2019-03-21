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
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;


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

    //SQL_CREATE_TABLE generates the table, fillTable shows all data entries in DB
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
        ArrayList<NewEventRequest> arraylist = new ArrayList<>();
        PACKAGE_NAME = context.getPackageName();
        //TODO: add arraylist to table
    }


    //used in queryOneRow
    private NewEventRequest getDataFromCursor(int position, Cursor cursor){
        long chatID;
        String title;
        Date dateStart;
        Date dateEnd;
        String venue;
        String note;
        EventData eventData;

        cursor.moveToPosition(position);

        int chatIDIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_CHATID);
        chatID = cursor.getLong(chatIDIndex);

        int nameIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_TITLE);
        title = cursor.getString(nameIndex);

        int dateStartIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_DATESTART);
        dateStart = cursor.getDate(dateStartIndex); //TODO: read Date info from DB

        int dateEndIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_DATEEND);
        dateEnd = cursor.getDate(dateEndIndex); //TODO: read Date info from DB

        int venueIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_VENUE);
        venue = cursor.getString(venueIndex);

        int noteIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_NOTE);
        note = cursor.getString(noteIndex);

        eventData = new EventData(title, dateStart, dateEnd, venue, note);

        return new NewEventRequest(chatID, eventData);
    }



    //SQL operations: query, insert, delete


    public NewEventRequest queryOneRow(int position){

        if( readableDb == null){
            readableDb = getReadableDatabase();
        }

        Cursor cursor = readableDb.rawQuery(EventContract.EventSql.SQL_QUERY_ALL_ROWS, null);

        return getDataFromCursor(position, cursor);
    }
    //TODO: query many rows?


    //TODO: input may not be a completely filled NewEventRequest for insert & delete
    public void insertOneRow(NewEventRequest newEventRequest){
        if( writeableDb == null){
            writeableDb = getWritableDatabase();
        }

        ContentValues contentValues = new ContentValues();

        //TODO: Date datatype tbc
        contentValues.put(EventContract.EventEntry.COL_CHATID, newEventRequest.chatId);
        contentValues.put(EventContract.EventEntry.COL_TITLE, newEventRequest.eventData.title);
        contentValues.put(EventContract.EventEntry.COL_DATESTART, newEventRequest.eventData.dateStart);
        contentValues.put(EventContract.EventEntry.COL_DATEEND, newEventRequest.eventData.dateEnd);
        contentValues.put(EventContract.EventEntry.COL_VENUE, newEventRequest.eventData.venue);
        contentValues.put(EventContract.EventEntry.COL_NOTE, newEventRequest.eventData.note);

        writeableDb.insert(EventContract.EventEntry.TABLE_NAME, null, contentValues);

    }

    
    /*
    //TODO: delete tbc
    public void deleteOneRow(NewEventRequest newEventRequest){
        if( writeableDb == null){
            writeableDb = getWritableDatabase();
        }
        String WHERE_CLAUSE = EventContract.EventEntry.COL_CHATID + " = ?";
        String[] WHERE_ARGS = {title};
        int rowsDeleted = writeableDb.delete(CharaContract.CharaEntry.TABLE_NAME, WHERE_CLAUSE, WHERE_ARGS);
        Log.i("Logcat", "rows deleted: " + rowsDeleted);
        return rowsDeleted;

    }
    */



}
