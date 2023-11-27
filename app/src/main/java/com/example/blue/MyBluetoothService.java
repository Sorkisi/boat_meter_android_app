package com.example.blue;


import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private final Handler handler = new Handler(Looper.getMainLooper()) {
    private StringBuilder messageBuilder = new StringBuilder();



        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ:
                    // Convert the read bytes into a string
                    String readMessage = new String((byte[]) msg.obj, 0, msg.arg1);

                    // Append the message to the builder
                    messageBuilder.append(readMessage);

                    // Check if the message starts with "STR" and ends with "END"
                    if (messageBuilder.toString().startsWith("STR") && messageBuilder.toString().endsWith("END")) {
                        // Log the complete message
//                        Log.i(TAG, "Complete message: " + messageBuilder.toString());

                        String message = messageBuilder.toString();
                        // Check that the message is really valid. If yes, give the data to main activity
                        String[] parts = message.split("\\|");

                        // Check the parts
                        try {
                            // Try to convert parts 1 to 6 to float
                            for (int i = 1; i <= 6; i++) {
                                float value = Float.parseFloat(parts[i]);
                                handleBluetoothMessage(value, i);
                            }


                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing float value", e);
                        }

                        // Clear the message builder
                        messageBuilder.setLength(0);
                    } else if(messageBuilder.toString().endsWith("D")) {
                        // Clear the message builder
                        messageBuilder.setLength(0);
                    }
                    break;
                case MessageConstants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.i(TAG, "Message sent: " + writeMessage);
                    // You can also update your UI here
                    break;

                case MessageConstants.MESSAGE_TOAST:
                    // Handle toast message here
                    break;

                    case MessageConstants.MESSAGE_CONNECTION_LOST:
                        if (listener != null) {
                            listener.onConnectionLost();
                        }
            }
        }
    };


    private ConnectedThread connectedThread;
    private ConnectionLostListener listener;
    public MyBluetoothService(BluetoothSocket socket, ConnectionLostListener listener) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        this.listener = listener;

    }

    public void sentData(String text)
    {
        connectedThread.write(text.getBytes(StandardCharsets.UTF_8));
    }

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        public static final int MESSAGE_CONNECTION_LOST = 3;
        // ... (Add other message types here as needed.)
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);

                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    // Send a connection lost message to the UI activity.
                    Message connectionLostMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_CONNECTION_LOST);
                    connectionLostMsg.sendToTarget();

                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, bytes);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }



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

}
