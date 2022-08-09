package com.example.android.pascuccimenu;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pascuccimenu.data.PascucciMenuContract;

import java.util.Locale;

import static com.example.android.pascuccimenu.MainActivity.adminpermission;
import static com.example.android.pascuccimenu.data.PascucciMenuLocalHelper.getLanguage;

/**
 * Created by Trouble_Maker on 10/18/2018.
 */

public class OffersActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the pascuccimenu data loader
     */
    private static final int PASCUCCIMENU_LOADER = 0;
    /**
     * Adapter for the ListView
     */
    MenuItemCursorAdapter mCursorAdapter;
    private String name;
    private String description;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Locale.getDefault().getLanguage().equals("ar")) {
            name = PascucciMenuContract.MenuEntry.COLUMN_NAME_AR;
            description = PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION_AR;

        } else {
            name = PascucciMenuContract.MenuEntry.COLUMN_NAME;
            description = PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION;

        }

        setContentView(R.layout.activity_offers);
        Configuration configuration = getResources().getConfiguration();
        configuration.setLayoutDirection(new Locale(getLanguage(OffersActivity.this)));
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        if (adminpermission) {
            // Setup FAB to open EditorActivity
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(OffersActivity.this, EditorActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.hide();
        }
        // Find the ListView which will be populated with the pascuccimenu data
        final ListView PascucciMenuListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        PascucciMenuListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pascuccimenu data in the Cursor.
        // There is no menuitem data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new MenuItemCursorAdapter(this, null);
        PascucciMenuListView.setAdapter(mCursorAdapter);


        // Setup the item click listener
        PascucciMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri uri = PascucciMenuContract.MenuEntry.CONTENT_URI;
                String[] Projectiont = new String[]{PascucciMenuContract.MenuEntry._ID, name, PascucciMenuContract.MenuEntry.COLUMN_PHOTO, PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID, description, PascucciMenuContract.MenuEntry.COLUMN_TYPE, PascucciMenuContract.MenuEntry.COLUMN_PRICE};
                String Selectiont = PascucciMenuContract.MenuEntry.COLUMN_TYPE + "=? And " + PascucciMenuContract.MenuEntry._ID + "=?";
                String[] SelectiontArgs = new String[]{String.valueOf(PascucciMenuContract.MenuEntry.TYPE_ITEM), String.valueOf(id)};
                Cursor cursort = getContentResolver().query(uri, Projectiont, Selectiont, SelectiontArgs, null, null);
                /*
                 * if menu item is an item
                 *  start item activity
                 */
                if (cursort != null && cursort.moveToFirst()) {
                    int typeColumnIndex = cursort.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_TYPE);

                    if (cursort.getInt(typeColumnIndex) == 1) {
                        cursort.close();
                        // if menu item is an item
                        // setContentView(R.Layout.item_activity);
                        //Create new intent to go to {@link ItemActivity}
                        Intent intent = new Intent(OffersActivity.this, ItemActivity.class);
                        // Form the content URI that represents the specific menu item that was clicked on,
                        // by appending the "id" (passed as input to this method) onto the
                        // {@link MenuEntry#CONTENT_URI}.
                        // For example, the URI would be "content://com.example.android.pascuccimenu/pascuccimenu/2"
                        // if the menu item with ID 2 was clicked on.
                        Uri currentMenuItemURI = ContentUris.withAppendedId(PascucciMenuContract.MenuEntry.CONTENT_URI, id);
                        // Set the URI on the data field of the intent
                        intent.setData(currentMenuItemURI);
                        // Launch the {@link ItemActivity} to display the data for the current menuitem.
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
        if (adminpermission) {
            PascucciMenuListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    // Create new intent to go to {@link EditorActivity}
                    Intent intent = new Intent(OffersActivity.this, EditorActivity.class);
                    // Form the content URI that represents the specific menu item that was clicked on,
                    // by appending the "id" (passed as input to this method) onto the
                    // {@link MenuEntry#CONTENT_URI}.
                    // For example, the URI would be "content://com.example.android.pascuccimenu/pascuccimenu/2"
                    // if the menu item with ID 2 was clicked on.
                    Uri currentMenuItemURI = ContentUris.withAppendedId(PascucciMenuContract.MenuEntry.CONTENT_URI, id);
                    // Set the URI on the data field of the intent
                    intent.setData(currentMenuItemURI);
                    // Launch the {@link EditorActivity} to display the data for the current menuitem.
                    startActivity(intent);
                    finish();
                    return true;
                }
            });
        }
        // Kick off the loader
        getLoaderManager().initLoader(PASCUCCIMENU_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PascucciMenuContract.MenuEntry._ID,
                PascucciMenuContract.MenuEntry.COLUMN_NAME,
                PascucciMenuContract.MenuEntry.COLUMN_NAME_AR,
                PascucciMenuContract.MenuEntry.COLUMN_PRICE,
                PascucciMenuContract.MenuEntry.COLUMN_PHOTO,
                PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION,
                PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION_AR,
                PascucciMenuContract.MenuEntry.COLUMN_TYPE,
                PascucciMenuContract.MenuEntry.COLUMN_OFFER,
                PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID,
        };
        String Selection = PascucciMenuContract.MenuEntry.COLUMN_OFFER + "=?";
        String[] SelectionArgs = new String[]{String.valueOf(1)};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PascucciMenuContract.MenuEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                Selection,                   // No selection clause
                SelectionArgs,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link MenuItemCursorAdapter} with this new cursor containing updated menu item data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
