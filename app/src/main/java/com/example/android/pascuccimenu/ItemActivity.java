package com.example.android.pascuccimenu;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.pascuccimenu.data.PascucciMenuContract;

import java.util.Locale;

import static com.example.android.pascuccimenu.data.PascucciMenuLocalHelper.getLanguage;

/**
 * Created by Trouble_Maker on 10/11/2018.
 */

public class ItemActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the menu item  data loader
     */
    private static final int EXISTING_MENUITEM_LOADER = 0;

    /**
     * Content URI for the existing menu item (null if it's a new menu item)
     */
    private Uri mCurrentMenuItemURI;

    private TextView mNameText;
    private TextView mDesText;
    private TextView mPriceText;
    private ImageView mImage;

    /**
     * URI of menu item image
     */
     Uri mImageUri;

    /**
     * Type of the menu item. The possible valid values are in the PascucciMenuContract.java file:
     * {@link PascucciMenuContract.MenuEntry#TYPE_MAIN}, or
     * {@link PascucciMenuContract.MenuEntry#TYPE_ITEM}.
     */
    private int mType = PascucciMenuContract.MenuEntry.TYPE_MAIN;

    /**
     * Boolean flag that keeps track of whether the menu item has been edited (true) or not (false)
     */
    private boolean mMenuItemHasChanged = false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Configuration configuration = getResources().getConfiguration();
        configuration.setLayoutDirection(new Locale(getLanguage(ItemActivity.this)));
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new menu item or editing an existing one.
        Intent intent = getIntent();
        mCurrentMenuItemURI = intent.getData();
        setTitle(getString(R.string.item_activity));
        // Initialize a loader to read the menu items data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_MENUITEM_LOADER, null, this);

        mNameText = (TextView) findViewById(R.id.itemname);
        mDesText = (TextView) findViewById(R.id.itemdescription);
        mPriceText = (TextView) findViewById(R.id.itemprice);
        mImage = (ImageView) findViewById(R.id.itemimage);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all menu item attributes, define a projection that contains
        // all columns from the menu item table
        String[] projection = {
                PascucciMenuContract.MenuEntry._ID,
                PascucciMenuContract.MenuEntry.COLUMN_NAME,
                PascucciMenuContract.MenuEntry.COLUMN_NAME_AR,
                PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION,
                PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION_AR,
                PascucciMenuContract.MenuEntry.COLUMN_PHOTO,
                PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID,
                PascucciMenuContract.MenuEntry.COLUMN_TYPE,
                PascucciMenuContract.MenuEntry.COLUMN_PRICE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentMenuItemURI,         // Query the content URI for the current menu item
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
            // Find the columns of menu item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_NAME);
            int namearColumnIndex = cursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_NAME_AR);
            int descriptionColumnIndex = cursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION);
            int descriptionarColumnIndex = cursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_DESCRIPTION_AR);
            int typeColumnIndex = cursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_TYPE);
            int priceColumnIndex = cursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_PRICE);
            int pictureColumnIndex = cursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_PHOTO);
           // int parentidColumnIndex = cursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String arname =cursor.getString(namearColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String ardescription = cursor.getString(descriptionarColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String imageUriString = cursor.getString(pictureColumnIndex);
            //byte[] img=cursor.getBlob(pictureColumnIndex);
            //int parentid = cursor.getInt(parentidColumnIndex);
            mImageUri = Uri.parse(imageUriString);
            //Bitmap bitmap= BitmapFactory.decodeByteArray(img,0,img.length);
            if (Locale.getDefault().getLanguage().equals("ar")) {

                mNameText.setText(arname);
                mDesText.setText(ardescription);

            } else {

                mNameText.setText(name);
                mDesText.setText(description);

            }
           // mNameText.setText(name);
          //  mDesText.setText(description);
            mPriceText.setText(Integer.toString(price));
            mImage.setImageURI(mImageUri);
            //mImage.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}