package com.example.blue;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.blue.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'blue' library on application startup.
    static {
        System.loadLibrary("blue");
    }

    private ActivityMainBinding binding;

    private BluetoothSocket bluetoothSocket;
    public static  MyBluetoothService myBluetoothService;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1;
    private static final int REQUEST_ENABLE_BT= 2;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 3;

    Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address

                            Log.i("BluetoothDevice", "Device Name: " + deviceName);
                            Log.i("BluetoothDevice", "Device Hardware Address: " + deviceHardwareAddress);
                        }
                    }

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                }
            } else {
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
            }
        } else {
            // Device does not support Bluetooth...
        }

        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // List all bluetooth devices that are paired
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted, you can perform the operation
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        final BluetoothDevice[] devicesArray = new BluetoothDevice[pairedDevices.size()];
                        String[] deviceNames = new String[pairedDevices.size()];
                        int i = 0;
                        for (BluetoothDevice device : pairedDevices) {
                            devicesArray[i] = device;
                            deviceNames[i] = device.getName();
                            i++;
                        }

                        // Show a dialog for the user to select a device
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Select a device")
                                .setSingleChoiceItems(deviceNames, -1, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        connectButton.setVisibility(View.INVISIBLE);


                                        // Start the connection
                                        new ConnectThread(devicesArray[which]).start();
                                        // Check for permissions
                                        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                                            String name = devicesArray[which].getName();
                                            Log.i("Connect To Device",  name);
                                            
                                            // Show Toast that bluetooth is connecting
                                            Toast.makeText(MainActivity.this, "Connecting to " + name, Toast.LENGTH_SHORT).show();


                                        } else {
                                            // Permission is not granted. You can request for permission here.
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
                                        }

                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                } else {
                    Log.i("BluetoothDevice"," There is no permissions for bluetooth");
                    // Permission is not granted, you should explain why you need the permission and ask the user to grant it
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
                }
            }
        });

    }

    /**
     * A native method that is implemented by the 'blue' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        connectButton.setVisibility(View.VISIBLE);
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        private final String TAG = "ConnectThread";

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    // Get a BluetoothSocket to connect with the given BluetoothDevice.
                    // MY_UUID is the app's UUID string, also used by the server code.
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    Log.e(TAG, "Socket's create() method failed", e);
                }
            } else {
                // Request BLUETOOTH_CONNECT permission...
            }

            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            // bluetoothAdapter.cancelDiscovery();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    mmSocket.connect();
                } catch (IOException connectException) {
                    //Toast.makeText(MainActivity.this, "Bluetooth could not connect", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Could not connect to the bluetooth device");

                    // Show the toast here
                    runOnUiThread(new Runnable() {
                        public void run() {
                            showToast(MainActivity.this, "Bluetooth could not connect");
                        }
                    });
                    // Unable to connect; close the socket and return.
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        Log.e(TAG, "Could not close the client socket", closeException);
                    }
                    return;
                }

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(mmSocket);
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

        private void manageMyConnectedSocket(BluetoothSocket mmSocket){
            if (mmSocket.isConnected()) {
                Log.i("BluetoothConnection", "The device is connected.");
                bluetoothSocket = mmSocket;
                // Create a new instance of MyBluetoothService
                myBluetoothService = new MyBluetoothService(bluetoothSocket);

                // New window where commands are shown
                Intent intent = new Intent(MainActivity.this,  ShowBatteryValues.class);
                startActivity(intent);

            } else {
                Log.i("BluetoothConnection", "The device is not connected.");

            }
        }
    }

}