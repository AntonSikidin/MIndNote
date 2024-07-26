package com.AgaKakje.mindnote;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends  AppCompatActivity  {
    private RelativeLayout mainLayout;
    private DatabaseHelper dbHandler;

    public static final int IDM_OPEN = 101;
    public static final int IDM_SAVE = 102;



    private  ScreenControl m_screen_control;

    @Override
    public void onBackPressed() {

        int current = dbHandler.getCurent();

        Log.d("onBackPressed", "onBackPressed: ");

        if (current == 0){
            super.onBackPressed();
            finish();
            return;
        }
        Log.d("onBackPressed", "onBackPressed: redraw");

        int parrent =  dbHandler.getParrent();

        dbHandler.setCurrent( parrent );
        List<RotateZoomImageView> myList =    m_screen_control.draw_current();

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (RelativeLayout) findViewById(R.id.main);

        mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {

                if ( m_screen_control.m_entity == null ){
                    return false;
                }
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, mainLayout);
                popupMenu.inflate(R.menu.popup_menu2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    popupMenu.setForceShowIcon(true);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        int id = item.getItemId();

                        if (id == R.id.Paste) {
                            m_screen_control.m_entity.i_parent = dbHandler.getCurent();
                            dbHandler.InsertEmployee_existing( m_screen_control.m_entity);
                            m_screen_control.draw_current();

                            return true;
                        }


                        return true;

                    }
                });


                popupMenu.show();


                return false;
            }
        });

        dbHandler = new DatabaseHelper(MainActivity.this);


        dbHandler.initdb();

        if ( checkStoragePermissions( ) ){
            Log.d("Permission", "True");
        }else{
            Log.d("Permission", "False");
            requestForStoragePermissions( );
        }

        makeDir() ;

        m_screen_control = new ScreenControl(this,mainLayout,dbHandler,getResources());

        List<RotateZoomImageView> myList =    m_screen_control.draw_current();

        Intent intent = getIntent();

        handleIntent( intent);



    }

    public boolean checkStoragePermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        }else {
            //Below android 11


            return false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Log.d("Send",action );
            Log.d("Send",type );
            if (type.startsWith("image/")) {
                Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

                if (imageUri != null) {
                    Log.d("Send",imageUri.toString());
                    ContentResolver contentResolver = getContentResolver();
                    try {
                        InputStream inputStream =  contentResolver.openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        long id = m_screen_control.create_image( bitmap );


                        RotateZoomImageView im1 = m_screen_control.draw_image( (int) id );

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Log.d("Send", "Null Uri");
                }

            }
        }
    }
    private void makeDir(){
        File yourAppDir = new File(Environment.getExternalStorageDirectory()+ File.separator+"MindNotes");

        if(!yourAppDir.exists() && !yourAppDir.isDirectory())

        {
            // create empty directory
            if (yourAppDir.mkdirs())
            {
                Log.i("CreateDir","App dir created");
            }
            else
            {
                Log.w("CreateDir","Unable to create app dir!");
            }
        }
        else
        {
            Log.i("CreateDir","App dir already exists");
        }
    }

    private static final int STORAGE_PERMISSION_CODE = 23;

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }

    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){

                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()){
                                    //Manage External Storage Permissions Granted
                                    Log.d("Permission", "onActivityResult: Manage External Storage Permissions Granted");
                                }else{
                                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //Below android 11

                            }
                        }
                    });



}

