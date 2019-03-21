package edu.sutd.organice;
import android.provider.BaseColumns;

public class EventContract {

    private EventContract(){
    }

    // EventEntry stores column names
    public static final class EventEntry implements BaseColumns {

        public static final String TABLE_NAME = "EventsByGroup";
        // 6 columns names
        public static final String COL_CHATID = "ChatID";
        public static final String COL_TITLE = "Title";
        public static final String COL_DATESTART = "DateStart";
        public static final String COL_DATEEND = "DateEnd";
        public static final String COL_VENUE = "Venue";
        public static final String COL_NOTE = "Note";
    }

    //EventSql stores SQL commands
    public static final class EventSql {

        public static String SPACE = " ";
        public static String COMMA = ",";

        public static String SQL_CREATE_TABLE = "CREATE TABLE" + SPACE
                + EventEntry.TABLE_NAME + SPACE + "("
                + EventEntry._ID + SPACE + "INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA
                + EventEntry.COL_CHATID + SPACE + "TEXT NOT NULL" + COMMA
                + EventEntry.COL_TITLE + SPACE + "TEXT NOT NULL" + COMMA
                + EventEntry.COL_DATESTART + SPACE + "TEXT NOT NULL" + COMMA
                + EventEntry.COL_DATEEND + SPACE + "TEXT NOT NULL" + COMMA
                + EventEntry.COL_VENUE + SPACE + "TEXT NOT NULL" + COMMA
                + EventEntry.COL_NOTE + SPACE + "TEXT NOT NULL" + COMMA + ");" ;

        public static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME;

        public static String SQL_QUERY_ALL_ROWS = "SELECT * FROM " + EventEntry.TABLE_NAME;

    }

}
