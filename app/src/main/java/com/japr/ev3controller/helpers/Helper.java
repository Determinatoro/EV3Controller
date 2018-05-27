package com.japr.ev3controller.helpers;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;

/**
 * Created by sporv on 27-05-2018.
 */

public class Helper {

    //************************************************************/
    // PERMISSIONS
    //************************************************************/
    //region Permissions

    //********************************************/
    // Request permissions
    //********************************************/
    public static void requestPermissions(Activity activity, int permissionRequestCode) {
        String[] arr = new String[]{
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.INTERNET,
                Manifest.permission.VIBRATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.CAMERA
        };
        ActivityCompat.requestPermissions(activity,
                arr, permissionRequestCode);
    }

    //endregion

}
