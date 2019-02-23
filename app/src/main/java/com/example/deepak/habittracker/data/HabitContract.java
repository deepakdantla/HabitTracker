package com.example.deepak.habittracker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class HabitContract {

    public HabitContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.deepak.habittracker";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_HABITS = "habits";


    public static final class HabitEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HABITS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HABITS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HABITS;

        public static final String TABLE_NAME = "habits";

        public final static String _ID = BaseColumns._ID;

        public static final String COLUMN_HABIT = "habit";

        public final static String COLUMN_FREQUENCY = "frequency";

        /**
         * Possible values for the gender of the pet.
         */
        public static final int FREEQUENCY_SELECT = 0;
        public static final int FREEQUENCY_DAILY = 1;
        public static final int FREEQUENCY_ONCE = 2;
        public static final int FREEQUENCY_TWICE = 3;
        public static final int FREEQUENCY_THRICE = 4;
        public static final int FREEQUENCY_WEEKLY = 5;


        public static boolean isValidFreequency(int frequency) {
            if (frequency == FREEQUENCY_SELECT || frequency == FREEQUENCY_DAILY ||
                    frequency == FREEQUENCY_ONCE || frequency == FREEQUENCY_TWICE
                    || frequency == FREEQUENCY_THRICE || frequency == FREEQUENCY_WEEKLY ) {
                return true;
            }
            return false;
        }
    }


}
