package com.example.blue;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;

import android.bluetooth.BluetoothGattCharacteristic;
import android.widget.ArrayAdapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.blue.databinding.ActivityMainBinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;




public class MainActivity extends AppCompatActivity {


    // Used to load the 'blue' library on application startup.
    static {
        System.loadLibrary("blue");
    }

    private ActivityMainBinding binding;

    private BluetoothGatt bluetoothGatt;
    public static MyBluetoothService myBluetoothService;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 3;

    Button connectButton;

    private void handleBluetoothMessage(float value, int i){
        switch(i){

            case 1:
                GlobalClass.battery1.setVoltage(value);
                break;
            case 2:
                GlobalClass.battery1.setCurrent(value);
                break;
            case 3:
                GlobalClass.battery1.setAmpereHours(value);
                break;

            case 4:
                GlobalClass.battery2.setVoltage(value);
                break;

            case 5:
                GlobalClass.battery2.setCurrent(value);

            case 6:
                GlobalClass.battery2.setAmpereHours(value);
                break;

            default:
                break;
        }
    }

    private void handleReadData(String message)
    {
        String[] parts = message.split("\\|");

        // Check if parts array has the expected size
        if (parts.length != 6) { // We expect 7 parts because the indices go from 0 to 6
            Log.e("TAG", "Unexpected number of parts: " + parts.length);
            return;
        }

        try {


            // Compare the received and calculated checksums


            // Try to convert parts 1 to 6 to float
            for (int i = 0; i <= 5; i++) {
                float value = Float.parseFloat(parts[i]);
                handleBluetoothMessage(value, i);
            }


        } catch (NumberFormatException e) {
            Log.e("TAG", "Error parsing float value", e);
        }
    }
    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("BLE", "Luetaan BLE dataa callbackissa");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // The data will be in the characteristic's value
                byte[] data = characteristic.getValue();

                // Convert the data to a string, assuming UTF-8 encoding
                String dataStr = new String(data, StandardCharsets.UTF_8);

                // Log the data
                Log.e("TAG", "Received data: " + dataStr);

                handleReadData(dataStr);

            } else {
                Log.e("TAG", "Read failed, status: " + status);
            }
        }


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("BLE", "onConncectioStateChange state connected");

                // Connection established, start service discovery
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }


                 myBluetoothService = new MyBluetoothService(MainActivity.this, gatt, new ConnectionLostListener() {
                    @Override
                    public void onConnectionLost() {
                        // Handle connection lost
                    }
                });
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Connection lost, handle disconnection
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Bluetooth connection lost ", Toast.LENGTH_SHORT).show();
                        connectButton.setVisibility(View.VISIBLE);
                    }
                });
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    List<BluetoothGattService> services = gatt.getServices();
                    for (BluetoothGattService service : services) {
                        Log.d("BLE", "Service discovered: " + service.getUuid());
                    }


                    Log.d("BLE", "Number of services: " + services.size());


                    myBluetoothService.startReading();

                    // Start the new activity
                    Intent showBatteryValues = new Intent(MainActivity.this, ShowBatteryValues.class);
                    Log.e("NEW TASK", "Created show battery values");
                    startActivity(showBatteryValues);

                } else {
                    Log.e("BLE", "Service discovery failed, status: " + status);
                }


            }


    };


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
                        == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                                == PackageManager.PERMISSION_GRANTED) {
                    BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                    ScanCallback scanCallback = new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            BluetoothDevice device = result.getDevice();
                            // Handle found BLE device
                        }
                    };
                    scanner.startScan(scanCallback);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_BLUETOOTH_PERMISSIONS);
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


        ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
        // Adapter for the AlertDialog
        ArrayAdapter<String> deviceNamesAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
        connectButton.setOnClickListener(new View.OnClickListener() {
            private boolean scanning;
            private Handler handler = new Handler();

            // Stops scanning after 10 seconds.
            private static final long SCAN_PERIOD = 10000;

            // List to hold found Bluetooth devices


            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    // Permissions are granted, you can perform the operation
                    BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                    // Set to hold the addresses of found Bluetooth devices
                    Set<String> deviceAddresses = new HashSet<>();

                    ScanCallback scanCallback = new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            BluetoothDevice device = result.getDevice();
                            // Check if the device name is not null and the device address is not already in the set
                            if (device.getName() != null && !deviceAddresses.contains(device.getAddress())) {
                                // Add the device address to the set
                                deviceAddresses.add(device.getAddress());
                                // Add the device to the list and update the adapter
                                bluetoothDevices.add(device);
                                deviceNamesAdapter.add(device.getName() + " - " + device.getAddress());
                            }
                        }
                    };
                    // Start scanning
                    scanner.startScan(scanCallback);

                    // Show a dialog for the user to select a device
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Select a device")
                            .setSingleChoiceItems(deviceNamesAdapter, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                                        // Stop scanning
                                        scanner.stopScan(scanCallback);
                                        // Start the connection
                                        BluetoothDevice device = bluetoothDevices.get(which);


                                        Log.e("BLE","Kutsutaan connectGatt");
                                        BluetoothGatt gatt = device.connectGatt(MainActivity.this, false, gattCallback);

                                        if (gatt != null) {
                                            Log.e("BLE", "Gatt connectGatt is not null");

                                        } else {
                                            Log.e("BLE", "Unable to connect to GATT server");
                                        }



                                    } else {
                                        Log.i("BluetoothDevice", " There are no permissions for bluetooth");
                                        // Permissions are not granted, you should explain why you need the permissions and ask the user to grant them
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{android.Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN},
                                                REQUEST_BLUETOOTH_PERMISSIONS);
                                    }




                                    dialog.dismiss();
                                }
                            })
                            .show();

                } else {
                    Log.i("BluetoothDevice", " There are no permissions for bluetooth");
                    // Permissions are not granted, you should explain why you need the permissions and ask the user to grant them
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_BLUETOOTH_PERMISSIONS);
                }
            }
        });
    }
}


