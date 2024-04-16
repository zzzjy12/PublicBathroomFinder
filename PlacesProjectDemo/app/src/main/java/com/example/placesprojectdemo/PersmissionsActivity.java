package com.example.placesprojectdemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


public class PersmissionsActivity extends AppCompatActivity implements View.OnClickListener {
 Button btnGrant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persmissions);
       //the user just need to provide the permission once
        //if he already privde the permission then the user doesnot need to see the screen
        //and we can directly go the maps activity
        if(ContextCompat.checkSelfPermission(PersmissionsActivity.this
                ,Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            startActivity(new Intent(PersmissionsActivity.this,MapsActivity.class));
            finish();
            return;
        }

        btnGrant= findViewById(R.id.btnGrant);
        /*if the permission is not available
        * then the user needs to click on the grant
        * button and then w ehave to show the dialoge that you get the permission*/
        btnGrant.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {
        Dexter.withActivity(PersmissionsActivity.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //redirect to maps Activity
                        startActivity(new Intent(PersmissionsActivity.this,MapsActivity.class));
                        finish();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if(permissionDeniedResponse.isPermanentlyDenied()){
                           //user needs to go to the setting to allow the permission
                            AlertDialog.Builder builder= new AlertDialog.Builder(PersmissionsActivity.this);
                            builder.setTitle("Permission Denied")
                                    .setMessage("Permission to access device location is permanently denied" +
                                            "you need to go to the settings to allow the permission")
                                    .setNegativeButton("Cancel",null)
                                    .setPositiveButton("Ok ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent= new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.fromParts("package",getPackageName(),null));

                                }
                            })
                            .show();

                        }else{
                            Toast.makeText(PersmissionsActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();

    }
}