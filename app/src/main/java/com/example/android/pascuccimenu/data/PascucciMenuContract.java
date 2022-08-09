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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the PascucciMenu app.
 */
public final class PascucciMenuContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
     static final String CONTENT_AUTHORITY = "com.example.android.pascuccimenu";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
     static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.PascucciMenu/PascucciMenu/ is a valid path for
     * looking at menu item data. content://com.example.android.PascucciMenu/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
     static final String PATH_PASCUCCIMENU = "pascuccimenu";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PascucciMenuContract() {
    }

    /**
     * Inner class that defines constant values for the PascucciMenu database table.
     * Each entry in the table represents a single menuentry.
     */
    public static final class MenuEntry implements BaseColumns {

        /**
         * The content URI to access the PascucciMenu data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PASCUCCIMENU);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of menuitems.
         */
         static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PASCUCCIMENU;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single menuitems.
         */
         static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PASCUCCIMENU;

        /**
         * Name of database table for menuitems
         */
         final static String TABLE_NAME = "pascuccimenu";

        /**
         * Unique ID number for the menu item (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the menuitem.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_NAME = "name";

        /**
         * Description of the menuitem.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_DESCRIPTION = "description";

        /**
         * Type of the menuitem.
         * <p>
         * The only possible values are {@link #TYPE_MAIN},
         * or {@link #TYPE_ITEM}.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_TYPE = "type";

        /**
         * Price of the menuitem.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_PRICE = "price";

        /**
         * Photo of the menuitem.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_PHOTO = "photo";

        /**
         * Parent_id of the menuiem.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_PARENT_ID = "parent_id";

        /**
         * arabic name of the menuiem.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_NAME_AR = "name_ar";

        /**
         * arabic description of the menuiem.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_DESCRIPTION_AR = "description_ar";

        /**
         * boolean offer of the menuiem.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_OFFER = "offer";

        /**
         * Possible values for the types of the menuitems.
         */
        public static final int TYPE_MAIN = 0;
        public static final int TYPE_ITEM = 1;

        /**
         * Returns whether or not the given type is {@link #TYPE_MAIN},
         * or {@link #TYPE_ITEM}.
         */
         static boolean isValidType(int type) {
             return(type == TYPE_MAIN || type == TYPE_ITEM);
        }
    }

}

