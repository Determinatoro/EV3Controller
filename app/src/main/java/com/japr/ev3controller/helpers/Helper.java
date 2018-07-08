package com.japr.ev3controller.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

import com.japr.ev3controller.R;

public class Helper {

    //************************************************************/
    // PERMISSIONS
    //************************************************************/
    //region Permissions

    /**
     * Request all permissions
     *
     * @param activity The activity using the function
     * @return A flag telling you if you got all permissions
     */
    public static boolean requestPermissions(Activity activity) {
        String[] arr = new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean gotAllPermissions = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : arr) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    gotAllPermissions = false;
            }
        }

        if (!gotAllPermissions)
            ActivityCompat.requestPermissions(activity, arr, 0);

        return gotAllPermissions;
    }

    //endregion

    //************************************************************/
    // TOOLBAR
    //************************************************************/
    //region Toolbar

    /**
     * A function to setup the toolbar
     *
     * @param activity The activity using this function
     * @param includeResource The toolbar include resource id
     * @param title The string to as the title
     * @param subTitle The string to set as the subtitle
     * @param showSubTitle Flag for showing the subtitle
     * @param firstColor First color in the gradient
     * @param secondColor Second color in the gradient
     * @return The toolbar from the include resource
     */
    public static Toolbar setupToolbar(AppCompatActivity activity, int includeResource, String title, String subTitle, boolean showSubTitle, int firstColor, int secondColor) {
        // Make gradient for actionbar background
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{firstColor, secondColor});

        // Get toolbar
        Toolbar toolbar = activity.findViewById(includeResource);
        // Set background
        toolbar.setBackground(gd);
        // Set actionbar
        activity.setSupportActionBar(toolbar);

        // Title
        AppCompatTextView toolbar_title = toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setText(title);
        // Subtitle
        AppCompatTextView toolbar_title_sub = toolbar.findViewById(R.id.toolbar_title_sub);
        toolbar_title_sub.setText(subTitle);

        if (!showSubTitle) {
            toolbar_title.setGravity(Gravity.CENTER);
            toolbar_title.setPadding(0, 0, 0, 0);

            toolbar_title_sub.setVisibility(View.GONE);
        } else {
            toolbar_title_sub.setVisibility(View.VISIBLE);
        }

        // Hide title
        if(activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

        return toolbar;
    }

    //endregion

}
