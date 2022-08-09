package com.example.android.pascuccimenu;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.example.android.pascuccimenu.data.PascucciMenuContract;

import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

import static com.example.android.pascuccimenu.MainActivity.adminpermission;
import static com.example.android.pascuccimenu.data.PascucciMenuLocalHelper.getLanguage;
import static java.sql.Types.NULL;

/**
 * Created by Trouble_Maker on 11/1/2018.
 */

public class RecyclerCatalogActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>{
    CursorRecyclerViewAdapter mCursorAdapter;
    private static final int PASCUCCIMENU_LOADER = 0;
    public static String current="NULL";
    private int po=0;
    private int stid=0;
    private String name;
    private String description;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    Stack<Integer> stack = new Stack<Integer>();
    MapCache cache = new MapCache();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        if (Locale.getDefault().getLanguage().equals("ar")) {
            name = PascucciMenuContract.MenuEntry.COLUMN_NAME_AR;
            description = PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION_AR;
        } else {
            name = PascucciMenuContract.MenuEntry.COLUMN_NAME;
            description = PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION;
        }
        setContentView(R.layout.activity_recyclecatalog);
        verifyStoragePermissions(this);
        Configuration configuration = getResources().getConfiguration();
        configuration.setLayoutDirection(new Locale(getLanguage(RecyclerCatalogActivity.this)));
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        if (adminpermission) {
            // Setup FAB to open EditorActivity
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(RecyclerCatalogActivity.this, EditorActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
            fab.hide();
        }



        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use a GridLayoutManager
        RecyclerView.LayoutManager mRecycle=new GridLayoutManager(this,3,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mRecycle);

        mRecyclerView.dispatchWindowFocusChanged(false);
        mRecyclerView.setFocusable(false);



        // specify an adapter (see also next example)
        mCursorAdapter =new CursorRecyclerViewAdapter(this, null) ;
        mRecyclerView.setAdapter(mCursorAdapter);


        // Setup the item click listener
        mCursorAdapter.setOnItemClickListener(new CursorRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                po=position;
                mRecyclerView.dispatchWindowFocusChanged(false);
                mRecyclerView.setFocusable(false);
                String itemname = ((TextView) Objects.requireNonNull(mRecyclerView.findViewHolderForAdapterPosition(position)).itemView.findViewById(R.id.itemname)).getText().toString();
                Uri uri = PascucciMenuContract.MenuEntry.CONTENT_URI;
                String[] Projection = new String[]{PascucciMenuContract.MenuEntry._ID, name, PascucciMenuContract.MenuEntry.COLUMN_PHOTO, PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID, description, PascucciMenuContract.MenuEntry.COLUMN_TYPE, PascucciMenuContract.MenuEntry.COLUMN_PRICE , PascucciMenuContract.MenuEntry.COLUMN_OFFER};
                String Selection = PascucciMenuContract.MenuEntry.COLUMN_NAME + "=? OR " + PascucciMenuContract.MenuEntry.COLUMN_NAME_AR + "=?";
                String[] SelectionArgs = new String[]{itemname,itemname};
                Cursor cursor = getContentResolver().query(uri, Projection, Selection, SelectionArgs, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int id =cursor.getInt(cache.getColumnIndex(cursor,PascucciMenuContract.MenuEntry._ID));
                    int idd =cursor.getInt(cache.getColumnIndex(cursor,PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID));
                    if(stack.empty())
                    stack.push(0);
                    if(stack.peek()!=idd)
                    stack.push(idd);

                    current = cursor.getString(cache.getColumnIndex(cursor,name)) ;
                    cursor.close();

                    Uri urii = PascucciMenuContract.MenuEntry.CONTENT_URI;
                    String[] Projectionn = new String[]{PascucciMenuContract.MenuEntry._ID, name, PascucciMenuContract.MenuEntry.COLUMN_PHOTO, PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID, description, PascucciMenuContract.MenuEntry.COLUMN_PRICE, PascucciMenuContract.MenuEntry.COLUMN_TYPE, PascucciMenuContract.MenuEntry.COLUMN_OFFER};
                    String Selectionn = PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID + "=?";
                    String[] SelectionArgss = new String[]{String.valueOf(id)};
                    Cursor cursorr = getContentResolver().query(urii, Projectionn, Selectionn, SelectionArgss, null, null);

                    String[] Projectiont = new String[]{PascucciMenuContract.MenuEntry._ID, name, PascucciMenuContract.MenuEntry.COLUMN_PHOTO, PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID, description, PascucciMenuContract.MenuEntry.COLUMN_TYPE, PascucciMenuContract.MenuEntry.COLUMN_PRICE , PascucciMenuContract.MenuEntry.COLUMN_OFFER};
                    String Selectiont = PascucciMenuContract.MenuEntry.COLUMN_TYPE + "=? And " + PascucciMenuContract.MenuEntry._ID + "=?";
                    String[] SelectiontArgs = new String[]{String.valueOf(PascucciMenuContract.MenuEntry.TYPE_ITEM), String.valueOf(id)};
                    Cursor cursort = getContentResolver().query(uri, Projectiont, Selectiont, SelectiontArgs, null, null);
                    /*
                     * if menu item is an item
                     *  start item activity
                     */
                    if (cursort != null && cursort.moveToFirst()) {
                        int typeColumnIndex = cache.getColumnIndex(cursort,PascucciMenuContract.MenuEntry.COLUMN_TYPE);

                        if (cursort.getInt(typeColumnIndex) == 1) {
                            stack.pop();
                            // if menu item is an item
                            // setContentView(R.Layout.item_activity);
                            //Create new intent to go to {@link ItemActivity}
                            Intent intent = new Intent(RecyclerCatalogActivity.this, ItemActivity.class);
                            // if the menu item with ID 2 was clicked on.
                            Uri currentMenuItemURI = ContentUris.withAppendedId(PascucciMenuContract.MenuEntry.CONTENT_URI, id);
                            // Set the URI on the data field of the intent
                            intent.setData(currentMenuItemURI);
                            // Launch the {@link ItemActivity} to display the data for the current menuitem.
                            startActivity(intent);


                        }
                    }
                    else if(cursort == null || !cursort.moveToFirst() || cursorr!=null && cursorr.moveToFirst())
                    {
                        mCursorAdapter.swapCursor(cursorr);

                        if(cursort != null){
                        cursort.close();}
                    }
                }
            }
                @Override
                public void onItemLongClick ( int position, View v) {
                    if (adminpermission) {
                        po=position;
                        mRecyclerView.dispatchWindowFocusChanged(false);
                        mRecyclerView.setFocusable(false);
                        String itemname = ((TextView) Objects.requireNonNull(mRecyclerView.findViewHolderForAdapterPosition(position)).itemView.findViewById(R.id.itemname)).getText().toString();
                        Uri uri = PascucciMenuContract.MenuEntry.CONTENT_URI;
                        String[] Projection = new String[]{PascucciMenuContract.MenuEntry._ID, name, PascucciMenuContract.MenuEntry.COLUMN_PHOTO, PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID, description, PascucciMenuContract.MenuEntry.COLUMN_TYPE, PascucciMenuContract.MenuEntry.COLUMN_PRICE, PascucciMenuContract.MenuEntry.COLUMN_OFFER};
                        String Selection = PascucciMenuContract.MenuEntry.COLUMN_NAME + "=? OR " + PascucciMenuContract.MenuEntry.COLUMN_NAME_AR + "=?";
                        String[] SelectionArgs = new String[]{itemname, itemname};
                        Cursor cursor = getContentResolver().query(uri, Projection, Selection, SelectionArgs, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int id =cursor.getInt(cache.getColumnIndex(cursor,PascucciMenuContract.MenuEntry._ID));
                            current = cursor.getString(cache.getColumnIndex(cursor,name)) ;
                            cursor.close();
                            // Create new intent to go to {@link EditorActivity}
                            Intent intent = new Intent(RecyclerCatalogActivity.this, EditorActivity.class);
                            // if the menu item with ID was clicked on.
                            Uri currentMenuItemURI = ContentUris.withAppendedId(PascucciMenuContract.MenuEntry.CONTENT_URI, id);
                            // Set the URI on the data field of the intent
                            intent.setData(currentMenuItemURI);
                            // Launch the {@link EditorActivity} to display the data for the current menuitem.
                            startActivity(intent);

                        }
                    }
                }
        });
                // Kick off the loader
                getLoaderManager().initLoader(PASCUCCIMENU_LOADER, null, this);

    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
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
        String Selection = PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID + "=?";
        String[] SelectionArgs = new String[]{String.valueOf(NULL)};
        stack.push(0 );
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PascucciMenuContract.MenuEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                Selection,                   // No selection clause
                SelectionArgs,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        // Update {@link MenuItemCursorAdapter} with this new cursor containing updated menu item data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        current="NULL";
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }


    @Override
    public void onBackPressed() {
    RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
           if (stack.empty())
               {
                   super.onBackPressed();
                   finish();

               } else if(!stack.empty()) {
                stid=stack.pop();
                Uri uri = PascucciMenuContract.MenuEntry.CONTENT_URI;
                String[] Projection = new String[]{PascucciMenuContract.MenuEntry._ID, name, PascucciMenuContract.MenuEntry.COLUMN_PHOTO, PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID, description, PascucciMenuContract.MenuEntry.COLUMN_PRICE, PascucciMenuContract.MenuEntry.COLUMN_TYPE, PascucciMenuContract.MenuEntry.COLUMN_OFFER};
                String Selection = PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID + "=?";
                String[] SelectionArgs = new String[]{String.valueOf(stid)};
                Cursor cursor = getContentResolver().query(uri, Projection, Selection, SelectionArgs, null, null);
                assert cursor != null;
                cursor.moveToFirst();
                mCursorAdapter.swapCursor(cursor);
            }
        mRecyclerView.scrollToPosition(po);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if(event.getAction()==KeyEvent.ACTION_UP){
            onBackPressed();
         return true;
        }
        if(event.getAction()==KeyEvent.ACTION_DOWN){

            return true;
        }
        return true;
    }
}
