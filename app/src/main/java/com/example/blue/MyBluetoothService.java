package com.example.blue;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MyBluetoothService {


    private Activity activity;
    private static final String TAG = "MY_APP_DEBUG_TAG";

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 3;

    private Handler readHandler = new Handler(Looper.getMainLooper());
    private Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            readData();
            readHandler.postDelayed(this, 3000);  // Schedule to run again in 1 second
        }
    };

    // Method to start reading data
    public void startReading() {
        Log.e("BLE","Start ble read hander");
        readHandler.post(readRunnable);
    }


    public MyBluetoothService(Activity activity, BluetoothGatt gatt, ConnectionLostListener listener) {
        this.activity = activity;  // And this line
        this.bluetoothGatt = gatt;
        this.listener = listener;
    }


    private BluetoothGatt bluetoothGatt;
    private ConnectionLostListener listener;
    private final String CUSTOM_READ_SERVICE_UUID = "78563412-f0de-bc9a-0000-000112345678";

    // Define a UUID for your custom characteristic
    private final String CUSTOM_READ_CHAR_UUID = "78563412-f0de-bc9a-0000-000112345678";

    private final String CUSTOM_WRITE_SERVICE = "00000000-0000-0000-0000-00BCF0BC7834";
    private final String CUSTOM_WRITE_CHAR_UUID = "00000000-0000-0000-0000-00BDF0BC7834";


    public void sendData(String text) {
        // Convert the text string to bytes
        byte[] data = text.getBytes(Charset.forName("UTF-8"));

        // Get the custom service
        UUID serviceUUID = UUID.fromString(CUSTOM_WRITE_SERVICE);
        BluetoothGattService service = bluetoothGatt.getService(serviceUUID);

        if (service != null) {
            // Get the custom characteristic
            UUID charUUID = UUID.fromString(CUSTOM_WRITE_CHAR_UUID);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUUID);

            if (characteristic != null) {
                // Set the characteristic value
                characteristic.setValue(data);
                Log.e("BLE", "Sending  data" + text);

                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    // Write the characteristic value
                    bluetoothGatt.writeCharacteristic(characteristic);
                } else {
                    // The app does not have the necessary permissions
                    Log.e("BLE", "App does not have the necessary permissions to write characteristic.");
                }
            }
        }
    }


    public void readData() {
        // Check if the necessary permissions are granted

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            // Get the service and characteristic to read from
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(CUSTOM_READ_SERVICE_UUID));
            // check if service is found and correct
            if (service != null) {
                if(service.getUuid().equals(UUID.fromString(CUSTOM_READ_SERVICE_UUID))) {

                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CUSTOM_READ_CHAR_UUID));

                    // Initiate a read operation
                    bluetoothGatt.readCharacteristic(characteristic);
                } else {
                    Log.e("BLE", CUSTOM_READ_SERVICE_UUID+" is not the same as " + service.getUuid());
                }
            } else {
                Log.e("BLE", "Service with UUID " + CUSTOM_READ_SERVICE_UUID + " doesn't exist");
            }

        } else {
            Log.e("BLE","Ei lupia");
            // If permissions are not granted, request them
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }
}
