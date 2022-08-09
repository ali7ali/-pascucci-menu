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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pascuccimenu.data.PascucciMenuContract.MenuEntry;

/**
 * Database helper for pascuccimenu app. Manages database creation and version management.
 */
public class PascucciMenuDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PascucciMenuDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "menu.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link PascucciMenuDbHelper}.
     *
     * @param context of the app
     */
     PascucciMenuDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pascuccimenu table
        String SQL_CREATE_PASCUCCIMENU_TABLE = "CREATE TABLE " + MenuEntry.TABLE_NAME + " ("
                + MenuEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MenuEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL, "
                + MenuEntry.COLUMN_NAME_AR + " TEXT UNIQUE NOT NULL, "
                + MenuEntry.COLUMN_PHOTO + " TEXT NOT NULL, "
                + MenuEntry.COLUMN_PARENT_ID + " INTEGER, "
                + MenuEntry.COLUMN_DESCRIPTION + " TEXT, "
                + MenuEntry.COLUMN_DESCRIPTION_AR + " TEXT, "
                + MenuEntry.COLUMN_TYPE + " INTEGER NOT NULL, "
                + MenuEntry.COLUMN_OFFER + " INTEGER DEFAULT 0, "
                + MenuEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PASCUCCIMENU_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}