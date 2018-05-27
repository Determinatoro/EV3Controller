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

import com.japr.ev3controller.helpers.BluetoothHelper;
import com.japr.ev3controller.helpers.Helper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BluetoothHelper.OnBluetoothListener, View.OnClickListener{

    private Button btnSendCommand;
    private Button btnStop;

    private BluetoothHelper bluetoothHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSendCommand = findViewById(R.id.btnSendCommand);
        btnStop = findViewById(R.id.btnStop);
        btnSendCommand.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        // PERMISSIONS

        Helper.requestPermissions(this, 0);
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

        bluetoothHelper = new BluetoothHelper(this, this);
        bluetoothHelper.searchForDevices("EV3");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothHelper != null)
            bluetoothHelper.disconnect();
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
            char[] chars = new char[]{0x0D, 0x00, 0x2A, 0x00, 0x80, 0x00, 0x00, 0xA4, 0x00, 0x01, 0x81, 0x64, 0xA6, 0x00, 0x01};
            bluetoothHelper.sendMessage(new String(chars));
        } else if (view == btnStop) {
            char[] chars = new char[]{0x09, 0x00, 0x2A, 0x00, 0x00, 0x00, 0x00, 0xA3, 0x00, 0x0F, 0x00};
            bluetoothHelper.sendMessage(new String(chars));
        }
    }
}
