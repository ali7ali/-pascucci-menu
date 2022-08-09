/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pascuccimenu.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pascuccimenu.data.PascucciMenuContract.MenuEntry;

/**
 * {@link ContentProvider} for pascucci menu app.
 */
public class PascucciMenuProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PascucciMenuProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the menu table
     */
    private static final int MENUITEMS = 100;

    /**
     * URI matcher code for the content URI for a single item in the menu table
     */
    private static final int MENUITEM_ID = 101;

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

        // The content URI of the form "content://com.example.android.pascuccimenu/pascuccimenu" will map to the
        // integer code {@link #MENUITEMS}. This URI is used to provide access to MULTIPLE rows
        // of the menu table.
        sUriMatcher.addURI(PascucciMenuContract.CONTENT_AUTHORITY, PascucciMenuContract.PATH_PASCUCCIMENU, MENUITEMS);

        // The content URI of the form "content://com.example.android.pascuccimenu/pascuccimenu/#" will map to the
        // integer code {@link #MENUITEM_ID}. This URI is used to provide access to ONE single row
        // of the pascucci menu table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pascuccimenu/pascuccimenu/3" matches, but
        // "content://com.example.android.pascuccimenu/pascuccimenu" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PascucciMenuContract.CONTENT_AUTHORITY, PascucciMenuContract.PATH_PASCUCCIMENU + "/#", MENUITEM_ID);
    }

    /**
     * Database helper object
     */
    private PascucciMenuDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PascucciMenuDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MENUITEMS:
                // For the MENUITEMS code, query the pascucci menu table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the menu table.
                cursor = database.query(MenuEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MENUITEM_ID:
                // For the MENUITEM_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pascuccimenu/pascuccimenu/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = MenuEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the pascuccimenu table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(MenuEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
       try {
           cursor.setNotificationUri(getContext().getContentResolver(), uri);
       } catch (Exception e) {
           e.printStackTrace();
       }
        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        if(match==MENUITEMS){
            return insertMenuItem(uri, contentValues);
        }
        else {
            throw new IllegalArgumentException("Insertion is not supported for " + uri);

        }
    }

    /**
     * Insert a menu item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertMenuItem(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(MenuEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Menuitem requires a name");
        }
        // Check that the name is not null
        String name_ar = values.getAsString(MenuEntry.COLUMN_NAME_AR);
        if (name_ar == null) {
            throw new IllegalArgumentException("العنصر بحاجة لاسم");
        }

        // Check that the type is valid
        Integer type = values.getAsInteger(MenuEntry.COLUMN_TYPE);
        if (type == null || !MenuEntry.isValidType(type)) {
            throw new IllegalArgumentException("Menuitem requires valid type");
        }

        // If the price is provided, check that it's greater than or equal to 0 $
        Integer price = values.getAsInteger(MenuEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Menuitem requires valid price");
        }

        // Check that the photo is not null
        //String photo = values.getAsString(MenuEntry.COLUMN_PHOTO);
        String photo = values.getAsString(MenuEntry.COLUMN_PHOTO);
        if (photo == null) {
            throw new IllegalArgumentException("Menuitem requires valid photo");
        }
        // No need to check the parent-id, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new menuitem with the given values
        long id = database.insert(PascucciMenuContract.MenuEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pascuccimenu content URI
        try {
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MENUITEMS:
                return updateMenuItem(uri, contentValues, selection, selectionArgs);
            case MENUITEM_ID:
                // For the MENUITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = MenuEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateMenuItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update menuitems in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more menuitems).
     * Return the number of rows that were successfully updated.
     */
    private int updateMenuItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link MenuEntry#COLUMN_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(MenuEntry.COLUMN_NAME)) {
            String name = values.getAsString(MenuEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Menuitem requires a name");
            }
        }
        if (values.containsKey(MenuEntry.COLUMN_NAME_AR)) {
            String name = values.getAsString(MenuEntry.COLUMN_NAME_AR);
            if (name == null) {
                throw new IllegalArgumentException("العنصر بحاجة لاسم");
            }
        }
        // If the {@link MenuEntry#COLUMN_TYPE} key is present,
        // check that the type value is valid.
        if (values.containsKey(MenuEntry.COLUMN_TYPE)) {
            Integer type = values.getAsInteger(MenuEntry.COLUMN_TYPE);
            if (type == null || !PascucciMenuContract.MenuEntry.isValidType(type)) {
                throw new IllegalArgumentException("Menuitem requires valid type");
            }
        }

        // If the {@link MenuEntry#COLUMN_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(MenuEntry.COLUMN_PRICE)) {
            // Check that the price is greater than or equal to 0 kg
            Integer price = values.getAsInteger(MenuEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Menuitm requires valid price");
            }
        }

        // If the {@link MenuEntry#COLUMN_PHOTO} key is present,
        // check that the photo value is not null.
        if (values.containsKey(MenuEntry.COLUMN_PHOTO)) {
            //String photo = values.getAsString(MenuEntry.COLUMN_PHOTO);
            String photo = values.getAsString(MenuEntry.COLUMN_PHOTO);
            if (photo == null) {
                throw new IllegalArgumentException("Menuitem requires a photo");
            }
        }
        // No need to check the parent-id, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(MenuEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
           try {
               getContext().getContentResolver().notifyChange(uri, null);
           } catch (Exception e) {
               e.printStackTrace();
           }
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MENUITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(MenuEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MENUITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = MenuEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(MenuEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MENUITEMS:
                return MenuEntry.CONTENT_LIST_TYPE;
            case MENUITEM_ID:
                return MenuEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
