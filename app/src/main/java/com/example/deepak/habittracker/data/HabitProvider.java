package com.example.deepak.habittracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.deepak.habittracker.data.HabitContract.HabitEntry;

public class HabitProvider extends ContentProvider {

    public static final String LOG_TAG = HabitProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the habits table
     */
    private static final int HABITS = 100;

    /**
     * URI matcher code for the content URI for a single habit in the habits table
     */
    private static final int HABIT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_HABITS, HABITS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_HABITS + "/#", HABIT_ID);
    }

    /**
     * Database helper object
     */
    private HabitDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new HabitDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                // For the HABITS code, query the habits table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the habits table.
                cursor = database.query(HabitEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case HABIT_ID:
                // For the HABIT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = HabitEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the habits table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(HabitEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                return insertHabit(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a habit into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertHabit(Uri uri, ContentValues values) {
        // Check that the habit is not null
        String habit = values.getAsString(HabitEntry.COLUMN_HABIT);
        if (habit == null) {
            throw new IllegalArgumentException("Enter a habit");
        }

        // Check that the frequency is valid
        Integer freequency = values.getAsInteger(HabitEntry.COLUMN_FREQUENCY);
        if (freequency == null || !HabitEntry.isValidFreequency(freequency)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(HabitEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the habits content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                return updateHabit(uri, values, selection, selectionArgs);
            case HABIT_ID:
                // For the HABIT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = HabitEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateHabit(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    /**
     * Update habits in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more habits).
     * Return the number of rows that were successfully updated.
     */
    private int updateHabit(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link HabitEntry#COLUMN_HABIT} key is present,
        // check that the habit value is not null.
        if (values.containsKey(HabitEntry.COLUMN_HABIT)) {
            String habit = values.getAsString(HabitEntry.COLUMN_HABIT);
            if (habit == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link HabitEntry#COLUMN_FREQUENCY} key is present,
        // check that the frequency value is valid.
        if (values.containsKey(HabitEntry.COLUMN_FREQUENCY)) {
            Integer frequency = values.getAsInteger(HabitEntry.COLUMN_FREQUENCY);
            if (frequency == null || !HabitEntry.isValidFreequency(frequency)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }


        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(HabitEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(HabitEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case HABIT_ID:
                // Delete a single row given by the ID in the URI
                selection = HabitEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(HabitEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                return HabitEntry.CONTENT_LIST_TYPE;
            case HABIT_ID:
                return HabitEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}
