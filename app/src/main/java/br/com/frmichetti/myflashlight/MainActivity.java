package br.com.frmichetti.myflashlight;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Context context;

    private Toolbar toolbar;

    private ActionBar actionBar;

    private ImageButton btnSwitch;

    private Camera camera;

    private boolean permission = false;

    private boolean isFlashOn = false;

    private boolean hasFlash = false;

    private boolean hasCamera = false;

    private Parameters params;

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        doCastComponents();

        doSetListeners();

        doConfigure();
    }


    private void doSetListeners() {

        // Switch button click event to toggle flash on/off
        btnSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isFlashOn) {
                    // turn off flash
                    turnOffFlash();
                } else {
                    // turn on flash
                    turnOnFlash();
                }
            }
        });
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        doRequestPermission();

        hasCamera = doCheckForCamera();

        if(!hasCamera){

            finish();

            Toast.makeText(context,getString(R.string.trouble_on_camera),Toast.LENGTH_LONG).show();

        }else{

            hasFlash = doCheckCameraFlash();
        }

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application

            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();

            alert.setTitle(getString(R.string.error));

            alert.setMessage(getString(R.string.doesnt_support));

            alert.setButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });

            alert.show();

            return;
        }

        // get the camera
        doGetCamera();

        // displaying button image
        toggleButtonImage();


    }

    private void doRequestPermission() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.CAMERA)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    Toast.makeText(context,getString(R.string.explanation),Toast.LENGTH_LONG).show();

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},0 );

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},0 );

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }


    }

    private void doConfigure() {

        context = this;

        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setTitle(getString(R.string.app_name));

        actionBar.setSubtitle(getString(R.string.subtitle));

    }

    private boolean doCheckForCamera(){

        if (Build.VERSION.SDK_INT >= 23){

            boolean permission = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

            if(permission){

                // First check if device is supporting flashlight or not
                hasCamera = getApplicationContext().getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_CAMERA);

            }else{

                Toast.makeText(context,getString(R.string.not_authorized_yet),Toast.LENGTH_LONG).show();
            }
        }else{
            hasCamera = getApplicationContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA);

        }

        return hasCamera;
    }

    private boolean doCheckCameraFlash() {

        boolean hasFlash = false;

        if (Build.VERSION.SDK_INT >= 23) {

            boolean permission = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

            if(permission){

                // First check if device is supporting flashlight or not
                hasFlash = getApplicationContext().getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

            }else{

                Toast.makeText(context,getString(R.string.not_authorized_yet),Toast.LENGTH_LONG).show();
            }


        }else{
            // First check if device is supporting flashlight or not
            hasFlash = getApplicationContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        }

        return hasFlash;

    }

    private void doCastComponents() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // flash switch button
        btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);

    }


    // Get the camera
    private void doGetCamera() {

        if (camera == null) {

            try {

                camera = Camera.open();

                params = camera.getParameters();

            } catch (NullPointerException e) {

                Toast.makeText(context,getString(R.string.trouble_on_camera),Toast.LENGTH_LONG).show();

                Log.e("Camera Error", "Failed to Open. Error: " + e.toString());
            }
        }
    }


    // Turning On flash
    private void turnOnFlash() {

        if(hasFlash){

            if (!isFlashOn) {

                if (camera == null || params == null) {
                    return;
                }

                // play sound
                doPlaySound();

                params = camera.getParameters();

                params.setFlashMode(Parameters.FLASH_MODE_TORCH);

                camera.setParameters(params);

                camera.startPreview();

                isFlashOn = true;

                // changing button/switch image
                toggleButtonImage();
            }
        }



    }


    // Turning Off flash
    private void turnOffFlash() {

        if(hasFlash){

            if (isFlashOn) {

                if (camera == null || params == null) {
                    return;
                }

                // play sound
                doPlaySound();

                params = camera.getParameters();

                params.setFlashMode(Parameters.FLASH_MODE_OFF);

                camera.setParameters(params);

                camera.stopPreview();

                isFlashOn = false;

                // changing button/switch image
                toggleButtonImage();
            }
        }


    }


    // Playing sound
    // will play button toggle sound on flash on / off
    private void doPlaySound(){

        if(isFlashOn){

            mp = MediaPlayer.create(context, R.raw.light_switch_off);

        }else{

            mp = MediaPlayer.create(context, R.raw.light_switch_on);

        }
        mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

                mp.release();
            }
        });

        mp.start();
    }

    /*
     * Toggle switch button images
     * changing image states to on / off
     * */
    private void toggleButtonImage(){

        if(isFlashOn){

            btnSwitch.setImageResource(R.drawable.btn_switch_on);

        }else{

            btnSwitch.setImageResource(R.drawable.btn_switch_off);
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        // on pause turn off the flash
        turnOffFlash();
    }

    @Override
    protected void onResume() {

        super.onResume();

        // on resume turn on the flash
        if(hasCamera && hasFlash){
            turnOnFlash();
        }

    }

    @Override
    protected void onStart() {

        super.onStart();

        if(hasCamera && hasFlash){
            // on starting the app get the camera params
            doGetCamera();
        }


    }

    @Override
    protected void onStop() {

        super.onStop();

        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case 0: {

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    Toast.makeText(context,getString(R.string.permission_accepted),Toast.LENGTH_LONG).show();

                    permission = true;

                }else{

                    Toast.makeText(context,getString(R.string.permission_not_accepted),Toast.LENGTH_LONG).show();

                    permission = false;

                }
                return;
            }
        }
    }

}