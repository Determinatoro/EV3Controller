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
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(activity,
                arr, permissionRequestCode);
    }

    //endregion

}
