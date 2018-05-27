package com.japr.ev3controller.helpers;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BluetoothHelper {

    //region Variables

    private String TAG = "BluetoothHelper";

    private Activity activity;
    private BluetoothAdapter bluetoothAdapter = null;

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    // Flags
    private boolean readData;

    // Listeners
    private OnBluetoothListener onBluetoothListener;

    // Threads
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    // Enums
    public enum BluetoothState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        SEARCHING,
        NOTHING
    }

    // Properties
    private String deviceSearchName = null;
    private List<BluetoothDevice> bluetoothDevices;
    private BluetoothState bluetoothState = BluetoothState.NOTHING;

    //endregion

    //region Properties

    public BluetoothState getBluetoothState() {
        return bluetoothState;
    }

    public void setBluetoothState(BluetoothState bluetoothState) {
        this.bluetoothState = bluetoothState;
    }

    public List<BluetoothDevice> getBluetoothDevices() {
        return bluetoothDevices;
    }

    //endregion

    //region Interface

    public interface OnBluetoothListener {
        void bluetoothMessageReceived(String message);
        void foundBluetoothDevice(List<BluetoothDevice> bluetoothDevices, BluetoothDevice bluetoothDevice);
    }

    //endregion

    //region Constructor

    public BluetoothHelper(Activity activity, OnBluetoothListener onBluetoothListener) {
        this.activity = activity;
        this.onBluetoothListener = onBluetoothListener;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            activity.registerReceiver(receiver, filter);

            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            activity.registerReceiver(receiver, filter);
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        if (device.getName().startsWith(deviceSearchName)) {
                            if (bluetoothDevices == null)
                                bluetoothDevices = new ArrayList<>();

                            if (!bluetoothDevices.contains(device)) {
                                bluetoothDevices.add(device);
                                if (onBluetoothListener != null)
                                    onBluetoothListener.foundBluetoothDevice(bluetoothDevices, device);
                            }
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (bluetoothState == BluetoothState.SEARCHING) {
                        if (bluetoothAdapter != null) {
                            if (bluetoothAdapter.isEnabled()) {
                                searchForDevices(deviceSearchName);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    };

    //endregion

    //region Functions

    public void searchForDevices(String deviceSearchName) {
        this.bluetoothDevices = new ArrayList<>();

        this.deviceSearchName = deviceSearchName;

        setBluetoothState(BluetoothState.SEARCHING);
        bluetoothDevices = new ArrayList<>();
        if (bluetoothAdapter != null)
            bluetoothAdapter.startDiscovery();
    }

    public void endSearchForDevices() {
        this.deviceSearchName = null;

        setBluetoothState(BluetoothState.NOTHING);
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    public void sendMessage(String message) {
        AsyncTask.execute(() -> {
            if (outputStream != null) {
                Log.d(TAG, "Sending: " + message);

                byte[] bytes = new byte[message.length()];

                for (int i = 0; i < message.length(); i++) {
                    byte b = (byte)message.charAt(i);
                    bytes[i] = b;
                }

                try {
                    outputStream.write(bytes);
                } catch (IOException ignored) {}
            }
        });
    }

    public void connect(BluetoothDevice bluetoothDevice) {
        if (connectThread != null && connectThread.isAlive())
            connectThread.interrupt();

        connectThread = new ConnectThread(bluetoothDevice);
        connectThread.start();
    }

    public void disconnect() {
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();

            } catch (IOException ignored) {}
        }

        outputStream = null;

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignored) {}
        }

        inputStream = null;

        try {
            bluetoothSocket.close();
            activity.unregisterReceiver(receiver);
        } catch (IOException ignored) {}
    }

    //endregion

    //region Threads

    //********************************************/
    // Connect thread
    //********************************************/
    private class ConnectThread extends Thread {
        private final BluetoothDevice device;
        BluetoothSocket tmp = null;

        ConnectThread(BluetoothDevice device) {
            this.device = device;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            } catch (Exception ignored) {
            }
            bluetoothSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "Connecting...");

            try {
                setBluetoothState(BluetoothState.CONNECTING);
                bluetoothSocket.connect();
            } catch (IOException e) {
                setBluetoothState(BluetoothState.DISCONNECTED);

                try {
                    Log.i(TAG, "Closing socket...");
                    bluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "Fail on closing socket...", e2);
                }

                return;
            }

            connectedThread = new ConnectedThread();
            connectedThread.start();
        }
    }

    //********************************************/
    // Connected thread
    //********************************************/
    private class ConnectedThread extends Thread {
        ConnectedThread() {
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Could not get input and output streams...", e);
                inputStream = null;
                outputStream = null;
            }
        }

        public void run() {
            setBluetoothState(BluetoothState.CONNECTED);
            Log.i(TAG, "Connected...");
            readData = true;

            // Listen to stream
            while (true) {
                if (!bluetoothSocket.isConnected())
                    break;

                if (readData) {
                    try {
                        while (inputStream.available() > 0) {
                            char character = (char) inputStream.read();
                            if (onBluetoothListener != null) {
                                onBluetoothListener.bluetoothMessageReceived(String.valueOf(character));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    //endregion

}
