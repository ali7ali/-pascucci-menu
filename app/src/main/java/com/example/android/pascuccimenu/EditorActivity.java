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
package com.example.android.pascuccimenu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pascuccimenu.data.PascucciMenuContract;
import com.example.android.pascuccimenu.data.PascucciMenuContract.MenuEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.example.android.pascuccimenu.data.PascucciMenuLocalHelper.getLanguage;
import static java.sql.Types.NULL;

/**
 * Allows user to create a new menu item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the menu item  data loader
     */
    private static final int EXISTING_MENUITEM_LOADER = 0;

    /**
     * Content URI for the existing menu item (null if it's a new menu item)
     */
    private Uri mCurrentMenuItemURI;

    /**
     * EditText field to enter the menu item's name
     */
    private EditText mNameEditText;

    private String name;
    /**
     * EditText field to enter the menu item's type
     */
    private EditText mDescriptionEditText;
    /**
     * EditText field to enter the menu item's name
     */
    private EditText marNameEditText;

    /**
     * EditText field to enter the menu item's type
     */
    private EditText marDescriptionEditText;

    /**
     * ImageView field to enter the menu item's photo
     */
    private ImageView mPhoto;

    /**
     * URI of menu item image
     */
    private Uri mImageUri;

    String s=RecyclerCatalogActivity.current;
    /**
     * EditText field to enter the menu item's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the menu item's price
     */
    private CheckBox mOffer;

    /**
     * EditText field to enter the menu item's type
     */
    private Spinner mTypeSpinner;

    private Spinner mNameParentSpinner;
    private int listid;
    /**
     * Type of the menu item. The possible valid values are in the PascucciMenuContract.java file:
     * {@link PascucciMenuContract.MenuEntry#TYPE_MAIN},  or
     * {@link PascucciMenuContract.MenuEntry#TYPE_ITEM}.
     */
    private int mType = MenuEntry.TYPE_MAIN;
    private int mParentNameID = 0;
    /**
     * Boolean flag that keeps track of whether the menu item has been edited (true) or not (false)
     */
    private boolean mMenuItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mMenuItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMenuItemHasChanged = true;
            return false;
        }
    };
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



    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        verifyStoragePermissions(this);

        Configuration configuration = getResources().getConfiguration();
        configuration.setLayoutDirection(new Locale(getLanguage(EditorActivity.this)));
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        if (Locale.getDefault().getLanguage().equals("ar")) {
            name = PascucciMenuContract.MenuEntry.COLUMN_NAME_AR;
        } else {
            name = PascucciMenuContract.MenuEntry.COLUMN_NAME;
        }

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new menu item or editing an existing one.
        Intent intent = getIntent();
        mCurrentMenuItemURI = intent.getData();

        // If the intent DOES NOT contain a menu item content URI, then we know that we are
        // creating a new menu item.
        if (mCurrentMenuItemURI == null) {
            // This is a new menu item, so change the app bar to say "Add a menu item"
            setTitle(getString(R.string.editor_activity_title_new_menuitem));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a menu item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing menu item, so change app bar to say "Edit menu item"
            setTitle(getString(R.string.editor_activity_title_edit_menuitem));
            // Initialize a loader to read the menu items data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_MENUITEM_LOADER, null, this);
        }
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_menuitem_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_menuitem_description);
        marNameEditText = (EditText) findViewById(R.id.edit_menuitem_namear);
        marDescriptionEditText = (EditText) findViewById(R.id.edit_menuitem_descriptionar);
        mPriceEditText = (EditText) findViewById(R.id.edit_menuitm_price);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);
        mNameParentSpinner = (Spinner) findViewById(R.id.spinner_name);
        mPhoto = (ImageView) findViewById(R.id.edit_menuitem_photo);
        mOffer = (CheckBox) findViewById(R.id.checkBox);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        marNameEditText.setOnTouchListener(mTouchListener);
        marDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);
        mOffer.setOnTouchListener(mTouchListener);
        // mPhoto.setOnTouchListener(mTouchListener);

        mNameParentSpinner.setOnTouchListener(mTouchListener);
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mMenuItemHasChanged = true;
            }
        });
        setupSpinner();
        NameParentSpinnerSetup();
        ArrayList<String> listt = (ArrayList<String>) getAllNames();
        mNameParentSpinner.setSelection(listt.indexOf(s));
    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT > 19) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mPhoto.setImageURI(mImageUri);
                mPhoto.invalidate();
                new Thread(new Runnable() {
                    public void run() {
                        // a potentially time consuming task
                        try {
                         //  String ret = getImageRealPath(getContentResolver(), mImageUri);
                            String ret = getRealPathFromURI(mImageUri);
                        //    String ret = getUriRealPath(getBaseContext(),mImageUri);
                            String imgP = pascucciGetMedia(ret);

                            mImageUri = Uri.parse("file://"+imgP);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
                mPhoto.setImageURI(mImageUri);
                mPhoto.invalidate();
            }
        }
    }
    /* Get uri related content real local file path. */
    private String getUriRealPath(Context ctx, Uri uri)
    {
        String ret = "";
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT  && DocumentsContract.isDocumentUri(ctx, uri)) {


            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];


                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            }
        }
        else if( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT )
        {
            // Android OS above sdk version 19.
            ret = getUriRealPathAboveKitkat(ctx, uri);
        }else
        {
            // Android OS below sdk version 19
            ret = getImageRealPath(getContentResolver(), uri, null);
        }

        return ret;
    }

    private String getUriRealPathAboveKitkat(Context ctx, Uri uri)
    {
        String ret = "";

        if(ctx != null && uri != null) {

            if(isContentUri(uri))
            {
                if(isGooglePhotoDoc(uri.getAuthority()))
                {
                    ret = uri.getLastPathSegment();
                }else {
                    ret = getImageRealPath(getContentResolver(), uri, null);
                }
            }else if(isFileUri(uri)) {
                ret = uri.getPath();
            }else if(isDocumentUri(ctx, uri)){

                // Get uri related document id.
                String documentId = DocumentsContract.getDocumentId(uri);

                // Get uri authority.
                String uriAuthority = uri.getAuthority();

                if(isMediaDoc(uriAuthority))
                {
                    String idArr[] = documentId.split(":");
                    if(idArr.length == 2)
                    {
                        // First item is document type.
                        String docType = idArr[0];

                        // Second item is document real id.
                        String realDocId = idArr[1];

                        // Get content uri by document type.
                        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                         if("video".equals(docType))
                        {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        }else if("audio".equals(docType))
                        {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        // Get where clause with real document id.
                        String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;

                        ret = getImageRealPath(getContentResolver(), mediaContentUri, whereClause);
                    }

                }else if(isDownloadDoc(uriAuthority))
                {
                    // Build download uri.
                    Uri downloadUri = Uri.parse("content://downloads/public_downloads");

                    // Append download document id at uri end.
                    Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));

                    ret = getImageRealPath(getContentResolver(), downloadUriAppendId, null);

                }else if(isExternalStoreDoc(uriAuthority))
                {
                    String idArr[] = documentId.split(":");
                    if(idArr.length == 2)
                    {
                        String type = idArr[0];
                        String realDocId = idArr[1];

                        if("primary".equalsIgnoreCase(type))
                        {
                            ret = Environment.getExternalStorageDirectory() + "/" + realDocId;
                        }
                    }
                }
            }
        }

        return ret;
    }

    /* Check whether current android os version is bigger than kitkat or not. */
    private boolean isAboveKitKat()
    {
        boolean ret = false;
        ret = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        return ret;
    }

    /* Check whether this uri represent a document or not. */
    private boolean isDocumentUri(Context ctx, Uri uri)
    {
        boolean ret = false;
        if(ctx != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(ctx, uri);
        }
        return ret;
    }

    /* Check whether this uri is a content uri or not.
     *  content uri like content://media/external/images/media/1302716
     *  */
    private boolean isContentUri(Uri uri)
    {
        boolean ret = false;
        if(uri != null) {
            String uriSchema = uri.getScheme();
            if("content".equalsIgnoreCase(uriSchema))
            {
                ret = true;
            }
        }
        return ret;
    }

    /* Check whether this uri is a file uri or not.
     *  file uri like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg
     * */
    private boolean isFileUri(Uri uri)
    {
        boolean ret = false;
        if(uri != null) {
            String uriSchema = uri.getScheme();
            if("file".equalsIgnoreCase(uriSchema))
            {
                ret = true;
            }
        }
        return ret;
    }


    /* Check whether this document is provided by ExternalStorageProvider. */
    private boolean isExternalStoreDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.externalstorage.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by DownloadsProvider. */
    private boolean isDownloadDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.providers.downloads.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by MediaProvider. */
    private boolean isMediaDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.providers.media.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by google photos. */
    private boolean isGooglePhotoDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.google.android.apps.photos.content".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Return uri represented document file real local path.*/
    private String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause)
    {
        String ret = "";

        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if(cursor!=null)
        {
            boolean moveToFirst = cursor.moveToFirst();
            if(moveToFirst)
            {

                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;

                if( uri==MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Images.Media.DATA;
                }else if( uri==MediaStore.Audio.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Audio.Media.DATA;
                }else if( uri==MediaStore.Video.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Video.Media.DATA;
                }

                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }
        if (cursor!=null)
        { cursor.close();}
        return ret;
    }
    private String getImageRealPath(ContentResolver contentResolver, Uri uri)
    {
        String ret = "";
        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if(cursor!=null && cursor.moveToFirst())
        {
                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;
                if( uri==MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Images.Media.DATA;
                }else if( uri==MediaStore.Audio.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Audio.Media.DATA;
                }else if( uri==MediaStore.Video.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Video.Media.DATA;
                }
                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);
                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
        }
        if(cursor!=null)
        {cursor.close();}
        return ret;
    }
    public String getRealPathFromURI(Uri uri) {

        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
        String result = "";
        Context ctx = getBaseContext();
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(ctx, uri)) {


            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];


                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            }
        }
        // for API 19 and above
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {


            if (cursor != null && cursor.moveToFirst()) {
                String image_id = cursor.getString(0);
                image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
                cursor.close();

                cursor = this.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
            }
        }

        if (cursor != null && cursor.moveToFirst()){
            result = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
     }
        return result;
    }
    private String pascucciGetMedia(String path)
    {
        File mDir=new File(getExternalFilesDir(null) +"/PascucciMedia");
        if(!mDir.exists()) {
            mDir.mkdir();
        }
        String  mDirPath="";
        try {
                File sd = Environment.getExternalStorageDirectory();
                File data = getExternalFilesDir(null);
                if (sd.canWrite()) {
                   // UUID uid = UUID.fromString(path.substring(path.lastIndexOf("/")+1));
                    String uuid = UUID.randomUUID().toString();
                   // mDirPath= "/PascucciMedia/"+path.substring(path.lastIndexOf("/")+1);
                    mDirPath="/PascucciMedia/"+uuid+path.substring(path.lastIndexOf("."));
                    File  backupM = new File(data, mDirPath);

                    InputStream in = getContentResolver().openInputStream(mImageUri);
                    try {
                        File f = new File(data,mDirPath);
                        f.setWritable(true, true);
                        OutputStream outputStream = new FileOutputStream(f);
                        byte buffer[] = new byte[8*1024*1024];
                        int length = 0;

                        while ((length = in.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }

                        outputStream.close();
                        in.close();
                    }
                    catch (Exception e)
                    {e.printStackTrace();}



                    String[] sArr = path.split("\\:");
                    String output = sArr[sArr.length-1];

                   // File currentM  = new File(sd, output);
                    File currentM=new File(output);

                    FileChannel src = new FileInputStream(currentM).getChannel();
                    FileChannel dst = new FileOutputStream(backupM).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    File file = new File(getExternalFilesDir(null) +"/PascucciMedia/.nomedia");
                    if(!file.exists()) { file.createNewFile();}
                }
            } catch (Exception e) {

            e.printStackTrace();
            }
 return getExternalFilesDir(null) +mDirPath;

    }

//    private byte[] getImage(ImageView img){
//           Bitmap bitmap=((BitmapDrawable)img.getDrawable()).getBitmap();
//           ByteArrayOutputStream stream=new ByteArrayOutputStream();
//           bitmap.compress(Bitmap.CompressFormat.WEBP,25,stream);
//        return stream.toByteArray();
//    }
    /**
     * Setup the dropdown name spinner that allows the user to select the name of the parent item
     */
    private void NameParentSpinnerSetup() {
        ArrayList<String> list = (ArrayList<String>) getAllNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        // Drop down layout style - list view with radio button
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNameParentSpinner.setAdapter(adapter);
        mNameParentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    Uri uri = MenuEntry.CONTENT_URI;
                    String[] Projection = new String[]{MenuEntry._ID, name};
                    String Selection = name + "=?";
                    String[] SelectionArgs = new String[]{selection};
                    Cursor cursor = getContentResolver().query(uri, Projection, Selection, SelectionArgs, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int iiiii = cursor.getColumnIndex(MenuEntry._ID);
                        mParentNameID = cursor.getInt(iiiii);
                    }
                    if (mParentNameID == 0) {
                        mParentNameID = NULL;

                    }
                    if (cursor!=null)
                    cursor.close();
                }

            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mParentNameID = NULL;
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the menu item.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);
        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        mTypeSpinner.setAdapter(typeSpinnerAdapter);
        // Set the integer mSelected to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                   if (selection.equals(getString(R.string.type_item))) {
                        mType = MenuEntry.TYPE_ITEM;
                    } else {
                        mType = MenuEntry.TYPE_MAIN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = MenuEntry.TYPE_MAIN;
            }
        });
    }

    /**
     * Get user input from editor and save menu item into database.
     */
    private void saveMenuItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String arnameString = marNameEditText.getText().toString().trim();
        String ardescriptionString = marDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        boolean offerbool = mOffer.isChecked();
        // Check if this is supposed to be a new menu item
        // and check if all the fields in the editor are blank
        if (mCurrentMenuItemURI == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString) &&
                TextUtils.isEmpty(priceString) && mType == MenuEntry.TYPE_MAIN) {
            // Since no fields were modified, we can return early without creating a new menu item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
        // Create a ContentValues object where column names are the keys,
        // and menu item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        // values.put(PascucciMenuContract.MenuEntry.COLUMN_NAME, nameString);
        values.put(MenuEntry.COLUMN_DESCRIPTION, descriptionString);
        values.put(MenuEntry.COLUMN_DESCRIPTION_AR, ardescriptionString);
        values.put(MenuEntry.COLUMN_TYPE, mType);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(MenuEntry.COLUMN_PRICE, price);
        // If the checkbox is checked set offer value to 1
        // else set to 0
        int offer = 0;
        if (offerbool) {
            offer = 1;
        }
        values.put(MenuEntry.COLUMN_OFFER, offer);
        // If the parent id is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.

        values.put(MenuEntry.COLUMN_PARENT_ID, mParentNameID);
        //chech name validation
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.validation_msg_menuitem_name), Toast.LENGTH_SHORT).show();
            return;
        } else {
            values.put(MenuEntry.COLUMN_NAME, nameString);
        }
        if (TextUtils.isEmpty(arnameString)) {
            Toast.makeText(this, getString(R.string.validation_msg_menuitem_name), Toast.LENGTH_SHORT).show();
            return;
        } else {
            values.put(MenuEntry.COLUMN_NAME_AR, arnameString);
        }
        //check image validation
        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.validation_msg_product_image), Toast.LENGTH_SHORT).show();
            return;
        } else {
            //values.put(MenuEntry.COLUMN_PHOTO, mImageUri.toString());
            values.put(MenuEntry.COLUMN_PHOTO,mImageUri.toString());
        }
        // Determine if this is a new or existing menu item by checking if mCurrentMenuItemURI is null or not
        if (mCurrentMenuItemURI == null) {
            // This is a NEW menu item, so insert a new menu item into the provider,
            // returning the content URI for the new menu item.
            Uri newUri = getContentResolver().insert(MenuEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_menuitem_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_menuitem_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING menu item, so update the menu item with content URI: mCurrentMenuItemURI
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentMenuItemURI will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentMenuItemURI, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_menuitem_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_menuitem_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new menu item, hide the "Delete" menu item.
        if (mCurrentMenuItemURI == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save menu item to database
                saveMenuItem();
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
                // If the menu item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mMenuItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the menu item hasn't changed, continue with handling back button press
        if (!mMenuItemHasChanged) {
            s="NULL";
            ArrayList<String> listt = (ArrayList<String>) getAllNames();
            mNameParentSpinner.setSelection(listt.indexOf(s));
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all menu item attributes, define a projection that contains
        // all columns from the menu item table
        String[] projection = {
                MenuEntry._ID,
                MenuEntry.COLUMN_NAME,
                MenuEntry.COLUMN_DESCRIPTION,
                MenuEntry.COLUMN_NAME_AR,
                MenuEntry.COLUMN_DESCRIPTION_AR,
                MenuEntry.COLUMN_PHOTO,
                MenuEntry.COLUMN_PARENT_ID,
                MenuEntry.COLUMN_TYPE,
                MenuEntry.COLUMN_OFFER,
                MenuEntry.COLUMN_PRICE};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentMenuItemURI,         // Query the content URI for the current menu item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ArrayList<String> listt = (ArrayList<String>) getAllNames();
        mNameParentSpinner.setSelection(listt.indexOf(s));
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of menu item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_DESCRIPTION);
            int arnameColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_NAME_AR);
            int ardescriptionColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_DESCRIPTION_AR);
            int typeColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_TYPE);
            int priceColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_PRICE);
            int pictureColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_PHOTO);
            int parentidColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_PARENT_ID);
            int offerColumnIndex = cursor.getColumnIndex(MenuEntry.COLUMN_OFFER);
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String arname = cursor.getString(arnameColumnIndex);
            String ardescription = cursor.getString(ardescriptionColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
           // byte [] imgByte = cursor.getBlob(pictureColumnIndex);
            String imageUriString = cursor.getString(pictureColumnIndex);
            int parentid = cursor.getInt(parentidColumnIndex);
            int offer = cursor.getInt(offerColumnIndex);
            mImageUri = Uri.parse(imageUriString);
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            marNameEditText.setText(arname);
            marDescriptionEditText.setText(ardescription);
            mPriceEditText.setText(Integer.toString(price));
            mPhoto.setImageURI(mImageUri);
            //Bitmap bitmap= BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            //mPhoto.setImageBitmap(bitmap);
            if (offer == 1) {
                mOffer.setChecked(true);
            } else {
                mOffer.setChecked(false);
            }
            /*
             *
             */
            if (Locale.getDefault().getLanguage().equals("ar")) {
                name = PascucciMenuContract.MenuEntry.COLUMN_NAME_AR;
            } else {
                name = PascucciMenuContract.MenuEntry.COLUMN_NAME;
            }

            Uri uri = MenuEntry.CONTENT_URI;
            String[] Projection = new String[]{MenuEntry._ID, name, MenuEntry.COLUMN_PARENT_ID};
            String Selection = MenuEntry._ID + "=?";
            String[] SelectionArgs = new String[]{String.valueOf(parentid)};
            Cursor cursorr = getContentResolver().query(uri, Projection, Selection, SelectionArgs, null, null);
            if (cursorr != null && cursorr.moveToFirst()) {
                int iiiii = cursorr.getColumnIndex(name);
                String sss="";
                sss= cursorr.getString(iiiii);
                ArrayList<String> list = (ArrayList<String>) getAllNames();
                mNameParentSpinner.setSelection(list.indexOf(sss));
            }
            if(cursorr!=null)
            cursorr.close();
            // Type is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Main, 1 is Sub, 2 is Item).
            // Then call setSelection() so that option is displayed on screen as the current selection.
           if(type == MenuEntry.TYPE_ITEM)
           {
               mTypeSpinner.setSelection(1);
           }
           else {
               mTypeSpinner.setSelection(0);
           }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        marNameEditText.setText("");
        marDescriptionEditText.setText("");
        mPriceEditText.setText("");
        mTypeSpinner.setSelection(0); // Select "main" type
        mPhoto.setImageResource(R.drawable.add_item);
        s="NULL";

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
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the menu item.
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
     * Prompt the user to confirm that they want to delete this menu item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the menu item.
                deleteMenuItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the menu item.
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
     * Perform the deletion of the menu item in the database.
     */
    private void deleteMenuItem() {
        // Only perform the delete if this is an existing menu item.
        if (mCurrentMenuItemURI != null) {
            // Call the ContentResolver to delete the menu item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentMenuItemURI
            // content URI already identifies the menu item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentMenuItemURI, null, null);
            String s= mCurrentMenuItemURI.toString().replaceAll("[^0-9]+", "");

                Toast.makeText(this, (s+ " is the id index of item uri"),
                        Toast.LENGTH_SHORT).show();
            Uri uri = PascucciMenuContract.MenuEntry.CONTENT_URI;
            String[] Projection = new String[]{PascucciMenuContract.MenuEntry._ID, PascucciMenuContract.MenuEntry.COLUMN_PARENT_ID};
            String Selection = MenuEntry.COLUMN_PARENT_ID + "=?";
            String[] SelectionArgs = new String[]{s};
            Cursor cursor = getContentResolver().query(uri, Projection, Selection, SelectionArgs, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int r=cursor.getColumnIndex(MenuEntry._ID);
                int rr=cursor.getInt(r);


                Toast.makeText(this, (rr + " deleted successfully"),
                        Toast.LENGTH_LONG).show();
                String clause="_ID IN ("+rr+")";
                do {
                    int rows=getContentResolver().delete(uri,clause,null);
                    if (rows == 0) {
                        // If no rows were deleted, then there was an error with the delete.
                        Toast.makeText(this, getString(R.string.editor_delete_menuitem_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the delete was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.editor_delete_menuitem_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                while (cursor.moveToNext());

            }
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_menuitem_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_menuitem_successful),
                        Toast.LENGTH_SHORT).show();
            }
            if (cursor!=null)
            cursor.close();
        }
        // Close the activity
        finish();
    }

    public List<String> getAllNames() {
        List<String> itemnames = new ArrayList<String>();
        Uri uri = MenuEntry.CONTENT_URI;
        String[] Projection = new String[]{MenuEntry._ID, name};
        Cursor cursor = getContentResolver().query(uri, Projection, null, null, null, null);
        // looping through all rows and adding to list
        itemnames.add("NULL");
        if (cursor!=null && cursor.moveToFirst()) {
            do {
                itemnames.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        // closing connection
        if(cursor!=null)
        cursor.close();
        // returning lables
        return itemnames;
    }
}