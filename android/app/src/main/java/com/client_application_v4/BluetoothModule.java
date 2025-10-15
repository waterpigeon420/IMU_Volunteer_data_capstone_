package com.client_application_v4;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.*;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.facebook.react.bridge.*;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.module.Accelerometer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;

public class BluetoothModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private static final String TAG = "BluetoothModule";
    private final String MW_MAC_ADDRESS = "F2:FC:66:32:11:51"; // Changeable: Replace with MetaWear MAC address
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private Accelerometer accelModule;
    private FileWriter fileWriter;
    private File accelFile;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat fileNameDateFormat;

    BluetoothModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.i(TAG, "Initializing BluetoothModule...");
        reactContext.bindService(new Intent(reactContext, BtleService.class), this, Context.BIND_AUTO_CREATE);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        fileNameDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    }

    @Override
    public String getName() {
        return "BluetoothModule";
    }

    @ReactMethod
    public void startBluetooth(Promise promise) {
        try {
            Log.i(TAG, "Starting Bluetooth connection...");

            if (board == null) {
                retrieveBoard();
            }

            if (board != null) {
                board.connectAsync().continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) {
                        if (task.isFaulted()) {
                            Log.e(TAG, "❌ Connection Failed: " + task.getError().getMessage());
                            promise.reject("ERROR", "❌ Connection Failed: " + task.getError().getMessage());
                        } else {
                            Log.i(TAG, "Bluetooth Connected Successfully");
                            setupFileWriter();
                            setupAccelerometer();
                            startAccelerometer();
                            promise.resolve("Bluetooth Connected Successfully and Accelerometer Started");
                        }
                        return null;
                    }
                });

                board.onUnexpectedDisconnect(status -> {
                    Log.w(TAG, "Unexpected Disconnect! Status: " + status);
                    stopAccelerometer();
                });

            } else {
                Log.e(TAG, "Board not initialized");
                promise.reject("ERROR", "Board not initialized");
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to start Bluetooth: " + e.getMessage());
            promise.reject("ERROR", "Failed to start Bluetooth: " + e.getMessage());
        }
    }

    @ReactMethod
    public void stopBluetooth(Promise promise) {
        try {
            Log.i(TAG, "Stopping Bluetooth connection...");

            if (board != null) {
                stopAccelerometer();
                board.disconnectAsync().continueWith(task -> {
                    Log.i(TAG, "Bluetooth Disconnected Successfully");
                    closeFileWriter();
                    promise.resolve("Bluetooth Disconnected Successfully");
                    return null;
                });
            } else {
                Log.w(TAG, "Board not connected");
                promise.reject("ERROR", "Board not connected");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop Bluetooth: " + e.getMessage());
            promise.reject("ERROR", "Failed to stop Bluetooth: " + e.getMessage());
        }
    }

    private void setupAccelerometer() {
        if (board != null) {
            accelModule = board.getModule(Accelerometer.class);
            if (accelModule != null) {
                // Configure the accelerometer as needed
                accelModule.configure()
                    .odr(50f)       // Set output data rate to 50Hz
                    .range(8f)      // Set the range to ±8g
                    .commit();
                Log.i(TAG, "Accelerometer configured");
            } else {
                Log.e(TAG, "Failed to get accelerometer module");
            }
        }
    }

    private void startAccelerometer() {
        if (board == null || accelModule == null) {
            Log.e(TAG, "Accelerometer not initialized");
            return;
        }

        accelModule.acceleration().addRouteAsync(source -> source.stream((data, env) -> {
            Acceleration accel = data.value(Acceleration.class);
            Log.i(TAG, "Acceleration: x=" + accel.x() + ", y=" + accel.y() + ", z=" + accel.z());
            writeToFile(accel);
        })).continueWith(task -> {
            if (task.isFaulted()) {
                Log.e(TAG, "Failed to subscribe to accelerometer: " + task.getError().getMessage());
            } else {
                Log.i(TAG, "Accelerometer subscription successful");
                accelModule.acceleration().start();
                accelModule.start();
            }
            return null;
        });
    }

    private void setupFileWriter() {
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SensorData");
            // Use the directory if it exists (no need to create it)
            
            // Generate filename with timestamp
            String timestamp = fileNameDateFormat.format(new Date());
            String fileName = "accel_data_" + timestamp + ".csv";
            
            accelFile = new File(dir, fileName);
            fileWriter = new FileWriter(accelFile, true);
            fileWriter.write("timestamp,x,y,z\n");
            fileWriter.flush();
            Log.i(TAG, "File writer initialized: " + accelFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize file writer: " + e.getMessage());
        }
    }

    private void stopAccelerometer() {
        if (accelModule != null) {
            accelModule.stop();
            accelModule.acceleration().stop();
            Log.i(TAG, "Accelerometer stopped");
        }
    }

    private void writeToFile(Acceleration accel) {
        try {
            if (fileWriter != null) {
                String timestamp = dateFormat.format(new Date());
                String line = String.format(Locale.US, "%s,%.4f,%.4f,%.4f\n", 
                    timestamp, accel.x(), accel.y(), accel.z());
                
                fileWriter.write(line);
                fileWriter.flush();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to file: " + e.getMessage());
        }
    }

    private void closeFileWriter() {
        try {
            if (fileWriter != null) {
                fileWriter.close();
                Log.i(TAG, "File Writer Closed Successfully");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to close file writer: " + e.getMessage());
        }
    }

    private void retrieveBoard() {
        BluetoothManager btManager = (BluetoothManager) getReactApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothDevice btDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        if (btDevice != null) {
            board = serviceBinder.getMetaWearBoard(btDevice);
            Log.i(TAG, "Retrieved MetaWear board.");
        } else {
            Log.e(TAG, "Failed to retrieve MetaWear board.");
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        Log.i(TAG, "MetaWear Bluetooth service connected.");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.w(TAG, "MetaWear Bluetooth service disconnected.");
    }
}