package com.example.deepak.habittracker;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.deepak.habittracker.data.HabitContract.HabitEntry;

public class Editor extends AppCompatActivity implements
        android.app.LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the habit data loader
     */
    private static final int EXISTING_HABIT_LOADER = 0;

    /**
     * Content URI for the existing habit (null if it's a new habit)
     */
    private Uri mCurrentHabitUri;

    /**
     * EditText field to enter the habit
     */
    private EditText mHabitEditText;

    /**
     * Spinner to select habit frequency
     */
    private Spinner mFrequencySpinner;

    private int mFrequency = HabitEntry.FREEQUENCY_SELECT;

    /**
     * Boolean flag that keeps track of whether the habit has been edited (true) or not (false)
     */
    private boolean mHabitHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mHabitHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mHabitHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mHabitEditText = (EditText) findViewById(R.id.habit_edit);
        mFrequencySpinner = findViewById(R.id.freequency_spinner);
        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new habit or editing an existing one.
        Intent intent = getIntent();
        mCurrentHabitUri = intent.getData();

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new pet.
        if (mCurrentHabitUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_activity_title_new_habit));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_habit));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_HABIT_LOADER, null, this);


            // Setup OnTouchListeners on all the input fields, so we can determine if the user
            // has touched or modified them. This will let us know if there are unsaved changes
            // or not, if the user tries to leave the editor without saving.
            mHabitEditText.setOnTouchListener(mTouchListener);
            mFrequencySpinner.setOnTouchListener(mTouchListener);


        }


        /**
         * Setup the dropdown spinner that allows the user to select the gender of the pet.
         */
        setupSpinner();
    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter habitSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.habit_freequency, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        habitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mFrequencySpinner.setAdapter(habitSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.frequency_daily))) {
                        mFrequency = HabitEntry.FREEQUENCY_DAILY;
                    } else if (selection.equals(getString(R.string.frequency_once_a_week))) {
                        mFrequency = HabitEntry.FREEQUENCY_ONCE_A_WEEK;
                    } else if (selection.equals(getString(R.string.frequency_twice_a_week))) {
                        mFrequency = HabitEntry.FREEQUENCY_TWICE_A_WEEK;
                    } else if (selection.equals(getString(R.string.frequency_thrice_a_week))) {
                        mFrequency = HabitEntry.FREEQUENCY_THRICE_A_WEEK;
                    } else if (selection.equals(getString(R.string.frequency_daily))) {
                        mFrequency = HabitEntry.FREEQUENCY_DAILY;
                    } else {
                        mFrequency = HabitEntry.FREEQUENCY_SELECT;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mFrequency = HabitEntry.FREEQUENCY_SELECT;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/editor_menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    /**
     * Get user input from editor and save habit into database.
     */
    private void saveHabit() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String habitString = mHabitEditText.getText().toString().trim();

        // Check if this is supposed to be a new habit
        // and check if all the fields in the editor are blank
        ContentValues values = new ContentValues();

        if (!TextUtils.isEmpty(habitString) && mFrequency != 0) {//modified just now
            // Create a ContentValues object where column names are the keys,
            // and habit attributes from the editor are the values.

            values.put(HabitEntry.COLUMN_HABIT, habitString);
            values.put(HabitEntry.COLUMN_FREQUENCY, mFrequency);
        } else {
            Toast.makeText(this, "Enter all the fields", Toast.LENGTH_SHORT).show();
            return;
        }


        // Determine if this is a new or existing Habit by checking if mCurrentPetUri is null or not
        if (mCurrentHabitUri == null) {
            // This is a NEW Habit, so insert a new pet into the provider,
            // returning the content URI for the new Habit.
            Uri newUri = getContentResolver().insert(HabitEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_habit_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_habit_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING Habit, so update the habit with content URI: mCurrentHabitUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentHabitUri will already identify the correct row in the database that
            // we want to modify.

            int rowsAffected = getContentResolver().update(mCurrentHabitUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_habit_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_habit_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save_habit:
                // Save habit to database
                saveHabit();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mHabitHasChanged) {
                    NavUtils.navigateUpFromSameTask(Editor.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(Editor.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the habit.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentHabitUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentHabitUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_habit_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_habit_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all habit attributes, define a projection that contains
        // all columns from the habit table
        String[] projection = {
                HabitEntry._ID,
                HabitEntry.COLUMN_HABIT,
                HabitEntry.COLUMN_FREQUENCY,};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentHabitUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int habitColumnIndex = cursor.getColumnIndex(HabitEntry.COLUMN_HABIT);
            int frequencyColumnIndex = cursor.getColumnIndex(HabitEntry.COLUMN_FREQUENCY);

            // Extract out the value from the Cursor for the given column index
            String habit = cursor.getString(habitColumnIndex);
            int frequency = cursor.getInt(frequencyColumnIndex);

            // Update the views on the screen with the values from the database
            mHabitEditText.setText(habit);

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (frequency) {
                case HabitEntry.FREEQUENCY_DAILY:
                    mFrequencySpinner.setSelection(1);
                    break;
                case HabitEntry.FREEQUENCY_ONCE_A_WEEK:
                    mFrequencySpinner.setSelection(2);
                    break;
                case HabitEntry.FREEQUENCY_TWICE_A_WEEK:
                    mFrequencySpinner.setSelection(3);
                    break;
                case HabitEntry.FREEQUENCY_THRICE_A_WEEK:
                    mFrequencySpinner.setSelection(4);
                    break;
                case HabitEntry.FREEQUENCY_WEEKLY:
                    mFrequencySpinner.setSelection(5);
                    break;
                default:
                    mFrequencySpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mHabitEditText.setText("");
        mFrequencySpinner.setSelection(0); // Select "select frequency"
    }

}
