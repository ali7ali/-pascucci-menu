package com.example.android.pascuccimenu;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.pascuccimenu.data.PascucciMenuContract;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Trouble_Maker on 10/17/2018.
 */

public class SettingsActivity extends AppCompatActivity {

    Button export,mport,exportM,mportM;

    String fPath,mPath;
    private static final int PICKFILE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        export=findViewById(R.id.exportbutton);
        mport=findViewById(R.id.importbutton);
        exportM=findViewById(R.id.exportbuttonM);
        mportM=findViewById(R.id.importbuttonM);
         getIMGpathsDB();
         getIMGpathsFS();
         DeleteIMG(getIMGpathsDB(),getIMGpathsFS());
        export.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                //creating a new folder for the database to be backuped to
                File direct = new File(Environment.getExternalStorageDirectory() + "/PascucciBackup");

                if(!direct.exists()) {
                    direct.mkdir();
                }
                else {
                    try {
                        File sd = Environment.getExternalStorageDirectory();
                        File data = Environment.getDataDirectory();

                        if (sd.canWrite()) {
                            String currentDBPath = "//data//" + "com.android.pascuccimenu"
                                    + "//databases//" + "menu.db";
                            String backupDBPath = "/PascucciBackup/menu.db";
                            File currentDB = new File(data, currentDBPath);
                            File backupDB = new File(sd, backupDBPath);

                            FileChannel src = new FileInputStream(currentDB).getChannel();
                            FileChannel dst = new FileOutputStream(backupDB).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                            Toast.makeText(getBaseContext(), backupDB.toString() +" Exporting Data Completed Successfully",
                                    Toast.LENGTH_LONG).show();

                        }
                    } catch (Exception e) {

                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                                .show();

                    }
                }
            }
        });
        mport.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                trySelector();
            }
        });

        exportM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creating a new folder for the media to be backuped to
                File direct = new File(Environment.getExternalStorageDirectory() + "/PascucciBackup");
                if(!direct.exists()) {
                    direct.mkdir();
                }
                else {
                    try {
                        File sd = Environment.getExternalStorageDirectory();
                        if (sd.canWrite()) {
                          File  mDirPath= new File(getExternalFilesDir(null)+"/PascucciMedia") ;
                          String destP=Environment.getExternalStorageDirectory()+"/PascucciBackup/Media.zip";
                            File  old = new File(Environment.getExternalStorageDirectory()+"/PascucciBackup/Media.zip") ;
                            boolean delete = old.delete();
                            ZipFile media=new ZipFile(destP);
                            ZipParameters parameters=new ZipParameters();
                            parameters.setCompressionMethod(CompressionMethod.DEFLATE);

                            parameters.setCompressionLevel(CompressionLevel.NORMAL);
                            media.addFolder(mDirPath,parameters);

                            Toast.makeText(getBaseContext(), destP.toString() +" Exporting Media Completed Successfully",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });
mportM.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        trySelectorr();
    }
});
    }
    public void trySelectorr() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICKFILE_REQUEST_CODE);
            return;
        }
        openSelectorr();
    }

    private void openSelectorr() {
        Intent intent;
        intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(getString(R.string.intent_type_folder));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_folder)), 1);
    }
    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICKFILE_REQUEST_CODE);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(getString(R.string.intent_type_folder));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_folder)), 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelectorr();
                }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                fPath = data.getData().getPath();
                importDB(fPath);
            }
        }
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mPath = data.getData().getPath();
                importMedia(mPath);
            }
        }
    }
    //importing database
    private void importDB(String ppath) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data  = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String  currentDBPath= "/data/com.android.pascuccimenu/databases/menu.db";
                File  backupDB= new File(data, currentDBPath);
                String[] sArr = ppath.split("\\:");
                String output = sArr[sArr.length-1];
                File currentDB  = new File(sd, output);
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), backupDB.toString()+" Importing Data Completed Successfully",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }
    //importing Media
    private void importMedia(String ppath) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String  dest = getExternalFilesDir(null)+"/";
                String[] sArr = ppath.split("\\:");
                String output = sArr[sArr.length-1];
                String source=sd+"/"+output;
                ZipFile zipFile = new ZipFile(source);
                zipFile.extractAll(dest);
                Toast.makeText(getBaseContext(), source+" Importing Media Completed Successfully",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }
    public List<String> getIMGpathsDB() {
        List<String> imgpaths = new ArrayList<String>();
        Uri uri = PascucciMenuContract.MenuEntry.CONTENT_URI;
        String[] Projection = new String[]{PascucciMenuContract.MenuEntry.COLUMN_PHOTO};
        Cursor cursor = getContentResolver().query(uri, Projection, null, null, null, null);
        // looping through all rows and adding to list
        // imgpaths.add("NULL");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                imgpaths.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        // closing connection
        if(cursor != null && cursor.moveToFirst())
        {cursor.close();}
        // returning lables
        return imgpaths;
    }
    public List<String> getIMGpathsFS(){
        List<String> imgpaths = new ArrayList<String>();
        String path = getExternalFilesDir(null) +"/PascucciMedia";
        Log.d("Files", "Path: " + path);

        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (File f:files) {

            imgpaths.add(f.getPath());
        }
        return imgpaths;
    }
    public void DeleteIMG(List<String> DB, List<String> FS){

        List<String> imgpaths = new ArrayList<String>();
        for (int i=0; i<DB.size();i++) {
            File imgDB = new File(DB.get(i));
            imgpaths.add(imgDB.getName());
        }
        for (int i=0; i<FS.size();i++)
        {
            File imgFS = new File(FS.get(i));

            if(!imgpaths.contains(imgFS.getName()) && !imgFS.getName().equals(".nomedia"))
            {
                boolean deleted = imgFS.delete();

            }
        }
    }
}