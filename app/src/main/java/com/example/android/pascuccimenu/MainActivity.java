package com.example.android.pascuccimenu;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static com.example.android.pascuccimenu.data.PascucciMenuLocalHelper.getLanguage;

//import android.content.DialogInterface;

public class MainActivity extends AppCompatActivity {
    //variables
    public static boolean adminpermission;
    final String Default = "N/A";
    private final String Admin = "Admin";
    Button show, show2, Continue, asAdmin, asGuest;
    Switch sarabic;
    EditText edit_password, edit_password2;
    TextView toast, name_display, forget;

    ImageView icon_user;
    //Used to add_item some time so that user cannot directly press and exity out of the activity
    boolean doubleBackToExitPressedOnce = false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPreferences = getSharedPreferences("Content_main", Context.MODE_PRIVATE);//reference to shared preference file
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getLargeMemoryClass();
        long heapSize = Runtime.getRuntime().maxMemory();
        //Creating a shared preference file  to save the name ,password and also for setting the correct xml file
        String guest_file = sharedPreferences.getString("guest", Default);
        String code_file = sharedPreferences.getString("code", "code");
        final String name_file = sharedPreferences.getString("name", Admin);
        String pass_file = sharedPreferences.getString("password", Admin);


        setContentView(R.layout.activity_main);
        Configuration configuration = getResources().getConfiguration();
        configuration.setLayoutDirection(new Locale(getLanguage(MainActivity.this)));
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        toast = (TextView) findViewById(R.id.toast_help);//toast_help object
        asAdmin = (Button) findViewById(R.id.asadmin);
        //asGuest = (Button) findViewById(R.id.asGuest);

      /*  asGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adminpermission = false;
                        Intent intent = new Intent(MainActivity.this, RecyclerCatalogActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }, 1000);
            }
        });*/
        asAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main_second);
                icon_user = (ImageView) findViewById(R.id.image_icon);
                icon_user.setImageResource(R.drawable.man);
                name_display = (TextView) findViewById(R.id.name_display);
                name_display.setText(name_file);
                edit_password2 = (EditText) findViewById(R.id.password2);
                show2 = (Button) findViewById(R.id.show2);
                show2.setOnClickListener(new showOrHidePassword2());
                forget = (TextView) findViewById(R.id.forget);
                Continue = (Button) findViewById(R.id.Continue);
                try {
                    Continue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String local_pass2 = edit_password2.getText().toString();
                            if (sharedPreferences.getString("pass_file", Admin).equals(local_pass2)) {
                                adminpermission = true;
                                //This handler will add_item a delay of 3 seconds
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Intent start to open the navigation drawer activity
                                        Intent intent = new Intent(MainActivity.this, RecyclerCatalogActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 500);

                            } else {
                                Toast.makeText(MainActivity.this, "Please Enter correct password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Warning", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Used to show the help by triggering a toast
    public void showHelp(View view) {
        Toast toast_help = new Toast(getApplicationContext());
        toast_help.setGravity(Gravity.CENTER, 0, 0);
        toast_help.setDuration(Toast.LENGTH_LONG);
        LayoutInflater inflater = getLayoutInflater();
        View appear = inflater.inflate(R.layout.toast_help, (ViewGroup) findViewById(R.id.linear));
        toast_help.setView(appear);
        toast_help.show();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
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

    public void showDialog(View view) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed}, // pressed
                new int[]{android.R.attr.state_enabled}
        };
        int[] colors = new int[]{
                Color.parseColor("#9B1D20"), // red
                Color.parseColor("#AAFAC8") //light green
        };
        ColorStateList list = new ColorStateList(states, colors);
        forget.setTextColor(list);
        final AlertDialog.Builder alertDialog;//Create a dialog object
        alertDialog = new AlertDialog.Builder(MainActivity.this);
        //EditText to show up in the AlertDialog so that the user can enter the email address
        final EditText editTextDialog = new EditText(MainActivity.this);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextDialog.setLayoutParams(layoutParams);
        editTextDialog.setHint("Code");
        //Adding EditText to Dialog Box
        alertDialog.setView(editTextDialog);
        alertDialog.setTitle("Enter Reset Code");
        final SharedPreferences sharedPreferences = getSharedPreferences("Content_main", Context.MODE_PRIVATE);

        alertDialog.setPositiveButton("AGREE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String code_dialog = editTextDialog.getText().toString();
                if (sharedPreferences.getString("code", "code").equals(code_dialog)) {
                    // codetrue=true;
                    showdialogPass();
                    //This intent will call the package manager and restart the current activity
                   /* Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);*/
                } else {
                    Toast.makeText(MainActivity.this, "Enter correct code", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setNegativeButton("DISAGREE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //When the Disagree button is pressed
            }
        });
        //Showing up the alert dialog box
        alertDialog.show();
    }

    public void showdialogPass() {
        final AlertDialog.Builder alerttDialog;//Create a dialog object
        alerttDialog = new AlertDialog.Builder(MainActivity.this);
        //EditText to show up in the AlertDialog so that the user can enter the email address
        final EditText edittTextDialog = new EditText(MainActivity.this);
        final LinearLayout.LayoutParams layouttParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        edittTextDialog.setLayoutParams(layouttParams);
        edittTextDialog.setHint("New Password");
        //Adding EditText to Dialog Box
        alerttDialog.setView(edittTextDialog);
        alerttDialog.setTitle("Enter New Password");
        final SharedPreferences shareddPreferences = getSharedPreferences("Content_main", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edittor = shareddPreferences.edit();
        alerttDialog.setPositiveButton("AGREE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pass_dialog = edittTextDialog.getText().toString();
                edittor.putString("pass_file", pass_dialog);
                edittor.apply();
                Toast.makeText(MainActivity.this, "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                //This intent will call the package manager and restart the current activity
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
        alerttDialog.setNegativeButton("DISAGREE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //When the Disagree button is pressed
            }
        });
        //Showing up the alertt dialog box
        alerttDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //class to show or hide password on button click in main activity
    class showOrHidePassword implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (show.getText().toString() == "SHOW") {
                edit_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                show.setText("HIDE");

            } else {

                edit_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                show.setText("SHOW");
            }
        }
    }

    //class to show or hide password on button click in main activity second
    class showOrHidePassword2 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (show2.getText().toString().equals("SHOW") ) {
                edit_password2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                show2.setText("HIDE");

            } else {

                edit_password2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                show2.setText("SHOW");
            }
        }
    }


}
