package com.japr.ev3controller;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.japr.ev3controller.helpers.BluetoothHelper;
import com.japr.ev3controller.helpers.EV3Helper;
import com.japr.ev3controller.helpers.Helper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BluetoothHelper.OnBluetoothListener, View.OnClickListener{

    private Button btnSendCommand, btnStop;
    private TextView tvText;

    private BluetoothHelper bluetoothHelper;
    private Runnable actionAfterPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helper.setupToolbar(this, R.id.toolbar, getString(R.string.app_name), null, false, 0xFF202020, 0xFF202020);

        // PERMISSIONS

        actionAfterPermissions = () -> {
            bluetoothHelper = new BluetoothHelper(MainActivity.this, MainActivity.this);
            bluetoothHelper.searchForDevices("EV3");
        };

        if (Helper.requestPermissions(this))
            runOnUiThread(actionAfterPermissions);
    }

    // onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.error))
                            .setMessage(getString(R.string.not_all_permissions_granted_error_message))
                            .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            });
                    builder.create().show();
                    return;
                }
            }
        }

        runOnUiThread(actionAfterPermissions);
    }

    @Override
    protected void onDestroy() {
        if (bluetoothHelper != null)
            bluetoothHelper.disconnect();

        super.onDestroy();
    }

    String message = "";

    @Override
    public void bluetoothMessageReceived(String message) {
        this.message += message;
    }

    @Override
    public void foundBluetoothDevice(List<BluetoothDevice> bluetoothDevices, BluetoothDevice bluetoothDevice) {
        bluetoothHelper.connect(bluetoothDevice);
    }

    @Override
    public void onClick(View view) {
        if (view == btnSendCommand) {
            bluetoothHelper.sendMessage(EV3Helper.startMotorCommand(new EV3Helper.Motor[]{EV3Helper.Motor.B, EV3Helper.Motor.C}, new char[]{100, (char)-100}));
        } else if (view == btnStop) {
            bluetoothHelper.sendMessage(EV3Helper.stopMotors());
        }
    }
}
