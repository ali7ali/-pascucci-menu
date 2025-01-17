package com.example.android.pascuccimenu;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Locale;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

import static com.example.android.pascuccimenu.data.PascucciMenuLocalHelper.setLocale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainWActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    boolean doubleBackToExitPressedOnce = false;

    private View fabbutten;
    private ImageButton specialOffer;

    private String arLanguageCode = "ar";
    private String enLanguageCode = "en";

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        long heapSize = Runtime.getRuntime().maxMemory();
        setContentView(R.layout.activity_main_w);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        fabbutten = findViewById(R.id.fab_speed_dial);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.menuar).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.menuar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change Application level locale
                setLocale(MainWActivity.this, arLanguageCode);
                Configuration configuration = getResources().getConfiguration();
                configuration.setLayoutDirection(new Locale(arLanguageCode));

                getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
                //It is required to recreate the activity to reflect the change in UI.
                recreate();
                Toast.makeText(getBaseContext(), "أصبحت اللغة عربية", Toast.LENGTH_SHORT).show();
                Intent menu = new Intent(MainWActivity.this, RecyclerCatalogActivity.class);
                startActivity(menu);

            }
        });
        findViewById(R.id.menuen).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.menuen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change Application level locale
                setLocale(MainWActivity.this, enLanguageCode);
                Configuration configuration = getResources().getConfiguration();
                configuration.setLayoutDirection(new Locale(enLanguageCode));

                getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
                //It is required to recreate the activity to reflect the change in UI.
                recreate();
                Toast.makeText(getBaseContext(), "Language has been set to English", Toast.LENGTH_SHORT).show();
                Intent menu = new Intent(MainWActivity.this, RecyclerCatalogActivity.class);
                startActivity(menu);

            }
        });
        specialOffer = findViewById(R.id.offer_btn);
        specialOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offer = new Intent(MainWActivity.this, OffersActivity.class);
                startActivity(offer);

            }
        });
        FabSpeedDial fabSpeedDial = findViewById(R.id.fab_speed_dial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //TODO: Start some activity
                switch (menuItem.getItemId())
                {
                    case R.id.action_lang:
                        Intent settings = new Intent(MainWActivity.this, SettingsActivity.class);
                        startActivity(settings);

                        return true;
                        //
                    case R.id.action_about:
                        Intent about = new Intent(MainWActivity.this, AboutUs.class);
                        startActivity(about);

                        return true;
                        //
                    case R.id.action_login:
                        Intent login = new Intent(MainWActivity.this, MainActivity.class);
                        startActivity(login);

                        return true;
                        //
                }
                return false;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
        fabbutten.setVisibility(View.GONE);
        specialOffer.setVisibility(View.GONE);

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;
        fabbutten.setVisibility(View.VISIBLE);
        specialOffer.setVisibility(View.VISIBLE);
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {
               doubleBackToExitPressedOnce = false;
           }
       }, 4000);
    }

}
