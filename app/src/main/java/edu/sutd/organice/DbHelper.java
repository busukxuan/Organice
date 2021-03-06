package edu.sutd.organice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import static android.content.ContentValues.TAG;


public class DbHelper extends SQLiteOpenHelper {

    private static String LOG_TAG = "DbHelper";

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
        Log.i(LOG_TAG, "onCreate: table filled");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(EventContract.EventSql.SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }


    private void fillTable(SQLiteDatabase sqLiteDatabase){

        //query all data in DB and save in an arraylist
        ArrayList<NewEventRequest> arraylist = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery(EventContract.EventSql.SQL_QUERY_ALL_ROWS, null);
        int rowNum = cursor.getCount();
        Log.i(TAG, "fillTable: cursor object created. NumOfRows = "+ rowNum);

        for(int j=0; j<rowNum; j++) {
            NewEventRequest newEventRequest = getDataFromCursor(j, cursor);
            arraylist.add(newEventRequest);
        }

        Log.i(TAG, "fillTable: all NewEventRequest added to arraylist");

        //for each row, fill table by columns
        for (int i=0; i<arraylist.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(EventContract.EventEntry.COL_CHATID, arraylist.get(i).getChatID());
            cv.put(EventContract.EventEntry.COL_TITLE, arraylist.get(i).getTitle());

            // change Date to long
            Date tempDateStart = arraylist.get(i).getDateStart();
            Date tempDateEnd = arraylist.get(i).getDateEnd();
            long tempDateStartLong = tempDateStart.getTime();
            long tempDateEndLong = tempDateEnd.getTime();

            cv.put(EventContract.EventEntry.COL_DATESTART, tempDateStartLong);
            cv.put(EventContract.EventEntry.COL_DATEEND, tempDateEndLong);

            cv.put(EventContract.EventEntry.COL_VENUE, arraylist.get(i).getVenue());
            cv.put(EventContract.EventEntry.COL_NOTE, arraylist.get(i).getNote());

            sqLiteDatabase.insert(EventContract.EventEntry.TABLE_NAME, null, cv);
            Log.i(TAG, "fillTable: inserted one row");
        }
        Log.i(TAG, "fillTable: inserted all");
    }



    private NewEventRequest getDataFromCursor(int position, Cursor cursor){
        long chatID;
        String title;
        long dateStartLong;
        long dateEndLong;
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
        dateStartLong = cursor.getLong(dateStartIndex);
        dateStart = new Date(dateStartLong * 1000);

        int dateEndIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_DATEEND);
        dateEndLong = cursor.getLong(dateEndIndex);
        dateEnd = new Date(dateEndLong * 1000);

        int venueIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_VENUE);
        venue = cursor.getString(venueIndex);

        int noteIndex = cursor.getColumnIndex(EventContract.EventEntry.COL_NOTE);
        note = cursor.getString(noteIndex);

        eventData = new EventData(title, dateStart, dateEnd, venue, note);

        Log.i(TAG, "getDataFromCursor: Row"+position);

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



    //input may not be a completely filled NewEventRequest for insert & delete

    public void insertOneRow(NewEventRequest newEventRequest){
        if( writeableDb == null){
            writeableDb = getWritableDatabase();
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put(EventContract.EventEntry.COL_CHATID, newEventRequest.chatId);
        contentValues.put(EventContract.EventEntry.COL_TITLE, newEventRequest.eventData.title);

        // change Date to long
        Date tempDateStart = newEventRequest.eventData.dateStart;
        Date tempDateEnd = newEventRequest.eventData.dateEnd;
        long tempDateStartLong = tempDateStart.getTime();
        long tempDateEndLong = tempDateEnd.getTime();

        contentValues.put(EventContract.EventEntry.COL_DATESTART, tempDateStartLong);
        contentValues.put(EventContract.EventEntry.COL_DATEEND, tempDateEndLong);

        contentValues.put(EventContract.EventEntry.COL_VENUE, newEventRequest.eventData.venue);
        contentValues.put(EventContract.EventEntry.COL_NOTE, newEventRequest.eventData.note);

        writeableDb.insert(EventContract.EventEntry.TABLE_NAME, null, contentValues);
        Log.i(TAG, "insertOneRow: finished");

    }



    //where clause type may vary
    public void deleteOneRow(NewEventRequest newEventRequest){
        if( writeableDb == null){
            writeableDb = getWritableDatabase();
        }
        
        String WHERE_CLAUSE = EventContract.EventEntry.COL_TITLE + " = ?";
        String[] WHERE_ARGS = new String[]{"title"};

        int rowsDeleted = writeableDb.delete(EventContract.EventEntry.TABLE_NAME, WHERE_CLAUSE, WHERE_ARGS);
        Log.i("Logcat", "rows deleted: " + rowsDeleted);

    }





}
