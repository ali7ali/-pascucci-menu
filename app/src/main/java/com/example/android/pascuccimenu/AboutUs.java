package com.example.android.pascuccimenu;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

import static com.example.android.pascuccimenu.data.PascucciMenuLocalHelper.getLanguage;

public class AboutUs extends AppCompatActivity {
    Button b;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Configuration configuration = getResources().getConfiguration();
        configuration.setLayoutDirection(new Locale(getLanguage(AboutUs.this)));
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        b = (Button) findViewById(R.id.visit_website);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Use when user trigger on  visit website
                String url = "https://www.pascuccicafe.com";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                Intent chooser = Intent.createChooser(intent, "Open with");
                startActivity(chooser);
            }
        });

    }
}
