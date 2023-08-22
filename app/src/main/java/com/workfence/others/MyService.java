package com.workfence.others;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.workfence.R;
import com.workfence.activities.MainActivity;
import com.workfence.activities.MaskChangeActivity;
import com.workfence.database.DataContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MyService extends Service implements SensorEventListener {

    public static LinkedHashMap<String, ContentValues> UIReadings = new LinkedHashMap<>();
    public static ScreenTimeBroadcastReceiver screenTimeBroadcastReceiver = new ScreenTimeBroadcastReceiver();
    public static BluetoothBroadcastReceiver bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
    public static BatteryLevelBroadcastReceiver batteryLevelBroadcastReceiver = new BatteryLevelBroadcastReceiver();
    static NotificationManager notificationManager;
    static int PHONE_STATE = 3;
    static Timer timer;
    final int MAX_COUNT = 3;
    byte[] prevPassData = null;
    boolean sendFaceScan = true;
    Context context = this;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScannerCompat bluetoothLeScanner;
    NotificationChannel notificationChannel;
    NotificationChannel facenotificationChannel;
    Queue<ContentValues> recentReadings = new LinkedList<>();
    String notificationChannelId = "SocDisServiceChannel";
    String facenotificationChannelId = "FaceMaskServiceChannel";
    int notifCount = 0, type = 0;
    float avgRssi = 0, distance = 0, avgDistance = 0, minDistance = 0;
    ContentResolver contentResolver;
    boolean isAdvertising = false;
    boolean shouldBeAdvertising = false;
    boolean shouldBeScanning = false;
    int charLength = 4;
    Notification notif;
    SensorManager sensorManager;
    boolean isDeviceTopFacing = false, isDeviceMoving = false, isDeviceFlat = false;
    Sensor sensorGyro, sensorAccelo;
    BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
    boolean a = true, prev = false;
    //MediaPlayer mediaPlayer;
    boolean isPlaying = false;
    CameraManager cameraManager;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationClient;
    Location location = null;
    HashMap<String, Float> scanRssi = new HashMap<>();
    HashMap<String, Float> advData = new HashMap<>();
    CameraManager.AvailabilityCallback availabilityCallback = new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraAvailable(@NonNull String cameraId) {
            super.onCameraAvailable(cameraId);
        }

        @Override
        public void onCameraUnavailable(@NonNull String cameraId) {
            super.onCameraUnavailable(cameraId);
            cameraCount(1);
        }
    };
    private boolean mScanning = false;
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i("TAG1", "onScanResult: " + callbackType);

            if (result.getScanRecord() != null && result.getScanRecord().getManufacturerSpecificData() != null) {
                if (result.getScanRecord().getManufacturerSpecificData().get(2242) != null)
                    Log.i("heyabruhh", Arrays.toString(result.getScanRecord().getManufacturerSpecificData().get(2242)));
                else {
                    Log.i("heyabruhh", "onScanResult: " + "null");
                    return;
                }
            } else {
                return;
            }

            byte[] data = result.getScanRecord().getManufacturerSpecificData().get(2242);
            /*
            // construct audio stream in 16bit format with sample rate of 44100Hz
            int minSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,  AudioFormat.ENCODING_PCM_16BIT);
            AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
            ...
            //use formula to get the wave data in specific frequency (15500Hz)

            genTone();
            */

            if (data.length < 10)
                return;

            String uuid = new String(Arrays.copyOfRange(data, 0, 5), StandardCharsets.UTF_8);
            getTxPower(uuid, result);
        }
    };
    private AdvertiseCallback callback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            isAdvertising = true;
            Log.i("TAG2", settingsInEffect.toString());
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            String reason;
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED: {
                    reason = "ADVERTISE_FAILED_ALREADY_STARTED";
                    isAdvertising = true;
                }
                break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED: {
                    reason = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
                    isAdvertising = false;
                }
                break;
                case ADVERTISE_FAILED_INTERNAL_ERROR: {
                    reason = "ADVERTISE_FAILED_INTERNAL_ERROR";
                    isAdvertising = false;
                }
                break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS: {
                    reason = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
                    isAdvertising = false;
                }
                break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE: {
                    reason = "ADVERTISE_FAILED_DATA_TOO_LARGE";
                    isAdvertising = false;
                    charLength--;
                }
                break;
                default:
                    reason = "UNDOCUMENTED";
            }
            Log.i("TAG3", reason);
        }
    };

    public static void stopNotification() {
        if (notificationManager != null)
            notificationManager.cancel(1);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*void genTone() {
        int angle = 0;
        int increment = 2 * Math.PI * freqOfTone/sampleRate;
        // angular increment
        for (int i = 0; i < sample.length; i++) {
            sample[i] = Math.sin(angle) * Short.MAX_VALUE;
            angle += increment;}track.write(sample, 0, sample.length);
        // write data to audio hardware
        track.play();// play an AudioTrack
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        //mediaPlayer = MediaPlayer.create(this, R.raw.buzz_sound);
        contentResolver = getContentResolver();
        registerNetworkCallback();
        startForeground(1, createNotif());
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorAccelo = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorAccelo, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorGyro, SensorManager.SENSOR_DELAY_NORMAL);

        try {
            unregisterReceiver(screenTimeBroadcastReceiver);
            unregisterReceiver(bluetoothBroadcastReceiver);
            unregisterReceiver(batteryLevelBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (Utils.getBoolean(context, "settingsScreen", true)) {
            IntentFilter lockFilter = new IntentFilter();
            lockFilter.addAction(Intent.ACTION_SCREEN_ON);
            lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenTimeBroadcastReceiver, lockFilter);
            screenTimeBroadcastReceiver.onReceive(this, new Intent(Intent.ACTION_SCREEN_ON));
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothBroadcastReceiver, filter);
        filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelBroadcastReceiver, filter);

        notif = new Notification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notif = new NotificationCompat.Builder(this, notificationChannelId)
                    .setContentText("No nearby devices with WorkFence app within 1.5 meters")
                    .setOnlyAlertOnce(true)
                    .setColorized(true)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(getColor(R.color.notifSafe))
                    .setContentTitle("Social Distancing")
                    .setChannelId("exampleServiceChannel")
                    .build();
        } else {
            notif = new NotificationCompat.Builder(this, notificationChannelId)
                    .setContentText("No nearby devices with WorkFence app within 1.5 meters")
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setColor(getColor(R.color.notifSafe))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentTitle("Social Distancing")
                    .build();
        }
        a = true;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (a && !prev && bluetoothAdapter != null && BluetoothAdapter.STATE_ON == bluetoothAdapter.getState()) {
                    notificationManager.notify(1, notif);
                    prev = true;
                } else {
                    prev = false;
                }
                a = true;
            }
        }, 1000, 5000);
        if (Utils.getBoolean(context, "settingsCamera", true)) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            cameraManager.registerAvailabilityCallback(availabilityCallback, null);
        }
    }

    private void scanLeDevice() {

        ArrayList<ScanFilter> filters = new ArrayList<>();
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setReportDelay(0)
                .setUseHardwareBatchingIfSupported(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setManufacturerData(2242, new byte[]{});
        ScanFilter filter = builder.build();
        filters.add(filter);

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (bluetoothLeScanner != null && !mScanning) {
                mScanning = true;
                bluetoothLeScanner.startScan(filters, scanSettings, mScanCallback);
            } else {
                if (bluetoothLeScanner != null)
                    bluetoothLeScanner.stopScan(mScanCallback);
                mScanning = false;
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("stopService")) {
            Log.i("STOP", "Received Stop Foreground Intent");
            //your end servce code
            stopAdvertising();
            mScanning = false;
            stopForeground(true);
            shouldBeScanning = false;

            stopSelf();

            return START_NOT_STICKY;
        }


        shouldBeScanning = true;

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        bluetoothLeScanner = BluetoothLeScannerCompat.getScanner();
        notificationManager.cancel(1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(notificationChannelId, "SocDis", NotificationManager.IMPORTANCE_HIGH);
            facenotificationChannel = new NotificationChannel(
                    facenotificationChannelId,
                    "FaceScan",
                    NotificationManager.IMPORTANCE_HIGH
            );
        }
        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            Intent bIntent = new Intent(BluetoothAdapter.ACTION_STATE_CHANGED);
            bIntent.putExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            bluetoothBroadcastReceiver.onReceive(context, bIntent);
        }
        createNotification();
        scanLeDevice();


        byte[] passData = ArrayUtils.concatByteArrays(Utils.getClientID(context).getBytes(), toByteArray(Utils.getString(context, "model", "nil").hashCode()), new byte[]{(byte) (Utils.getInteger(context, "isCalibrated", 0))});
        startAdvertising(passData);

        if (Utils.getBoolean(context, "checkInOk", false) && !Utils.getBoolean(context, "wfh", false)) {
            float storedLat, storedLng, storedAlt;
            float hori_dist, vert_dist;
            final LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(500);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                //getCurrentLocation();
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //location = locationManager != null ? locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) : null;
                //Toast.makeText(context, "Please provide location permission from app settings to experience complete functionality.", Toast.LENGTH_LONG).show();
            }

            LocationServices.getFusedLocationProviderClient(MyService.this)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {

                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(MyService.this)
                                    .removeLocationUpdates(this);
                            if (locationRequest != null && locationResult.getLocations().size() > 0) {
                                int lastestLocationIndex = locationResult.getLocations().size() - 1;
                                location = locationResult.getLocations().get(lastestLocationIndex);
                            }
                        }
                    }, Looper.myLooper());

            if (location != null && Utils.getBoolean(context, "settingsLocation", true)) {
                storedLat = Utils.getFloat(context, "lat", (float) 0.0);
                storedLng = Utils.getFloat(context, "lng", (float) 0.0);
                storedAlt = Utils.getFloat(context, "altitude", (float) 0.0);
                vert_dist = (float) Math.abs(storedAlt - location.getAltitude());
                Location storedLoc = new Location(LOCATION_SERVICE);
                storedLoc.setLatitude(storedLat);
                storedLoc.setLongitude(storedLng);
                hori_dist = storedLoc.distanceTo(location);
            } else {
                vert_dist = -1;
                hori_dist = -1;
            }

            if (hori_dist < 500 && hori_dist >= 0) {
                Utils.setBoolean(context, "checkInOk", true);

                updateStatus(0);
            }
        }

        // Removed - if(Utils.getBoolean(context, "checkInOk", false))
        // Not sure why it was kept as a condition for the below timer- it did not allow auto-calibration for WFH.
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                byte[] passData = ArrayUtils.concatByteArrays(Utils.getClientID(context).getBytes(), toByteArray(Utils.getString(context, "model", "nil").hashCode()), new byte[]{(byte) (Utils.getInteger(context, "isCalibrated", 0))});
                Log.d("calib_testing", "passData: " + passData.toString());

                if (bluetoothLeScanner != null && bluetoothAdapter != null) {
                    if (!bluetoothAdapter.isEnabled()) {
                        bluetoothLeScanner.stopScan(mScanCallback);
                        mScanning = false;
                    }

                    if (!mScanning && bluetoothAdapter.isEnabled() && shouldBeScanning) {
                        scanLeDevice();

                        byte[] data;
                        if (!advData.isEmpty()) {
                            Map.Entry<String, Float> entry = advData.entrySet().iterator().next();
                            data = ArrayUtils.concatByteArrays(passData, entry.getKey().getBytes(), String.format("%.2f", entry.getValue()).getBytes());
                            advData.remove(entry.getKey());

                            Log.d("calib_testing", "Advertising data: " + data.toString());
                            startAdvertising(data);
                            prevPassData = Arrays.copyOf(data, data.length);
                            isAdvertising = true;
                        } else {
                            startAdvertising(passData);
                            prevPassData = Arrays.copyOf(passData, passData.length);
                            isAdvertising = true;
                        }
                        return;
                    }
                }

                if (isAdvertising && advertiser != null) {
                    byte[] data = passData;

                    if (!advData.isEmpty()) {
                        Map.Entry<String, Float> entry = advData.entrySet().iterator().next();
                        data = ArrayUtils.concatByteArrays(passData, entry.getKey().getBytes(), String.format("%.2f", entry.getValue()).getBytes());
                        advData.remove(entry.getKey());
                    }

                    if (!Arrays.equals(data, prevPassData) || data.length > 10) {
                        advertiser.stopAdvertising(callback);
                        Log.d("calib_testing", "Advertising data: " + data.toString());
                        startAdvertising(data);
                        prevPassData = Arrays.copyOf(passData, passData.length);
                    }
                } else {
                    byte[] data;
                    if (!advData.isEmpty()) {
                        Map.Entry<String, Float> entry = advData.entrySet().iterator().next();
                        data = ArrayUtils.concatByteArrays(passData, entry.getKey().getBytes(), String.format("%.2f", entry.getValue()).getBytes());
                        advData.remove(entry.getKey());

                        startAdvertising(data);
                        prevPassData = Arrays.copyOf(data, data.length);
                        isAdvertising = true;
                    } else {
                        startAdvertising(passData);
                        prevPassData = Arrays.copyOf(passData, passData.length);
                        isAdvertising = true;
                    }
                }
            }
        }, 1000, 1000);

        return START_STICKY;
    }

    // Todo :: Can TxPower be sent through the advertising data itself? Fetching it from the server is not optimal.
    private void getTxPower(final String uuid, final ScanResult result) {
        final StringRequest request = new StringRequest(
                Request.Method.GET,
                getString(R.string.url_root) + "/devicedata/by_client/?client_id=" + uuid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Utils.otherName = jsonObject.getString("name");
                            Utils.model = jsonObject.getString("model");
                            Utils.otherTxPower = (float) (jsonObject.getDouble("txPower") + Math.log10(jsonObject.getInt("distance")) * 30);
                            update(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", uuid + "Error in Volley" + error.toString());
                        Utils.otherName = "No Name";
                        Utils.model = "Not Available";
                        Utils.otherTxPower = -80;
                        update(result);
                    }
                }) {
            /*@Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uuid", uuid);
                return params;
            }*/

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    private void setTxPower(final String modelName, final float txPower) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.POST, getString(R.string.url_root) + "/devicedata/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d("calib_testing", "Tx power set for " + modelName);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!error.toString().equals("com.android.volley.TimeoutError")) {
                            Log.e("Volley error", error.toString());
                        }

                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_MODEL, modelName);
                            contentValues.put(DataContract.VolleyCache.COLUMN_TX_POWER, txPower);
                            getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);
                        }

                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("modelHash", String.valueOf(modelName.hashCode()));
                    jsonBody.put("model", modelName);
                    jsonBody.put("txPower", String.valueOf(txPower));
                    jsonBody.put("distance", 1);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);

    }

    private void setTxPowerUser(final String uuid, final float txPower) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PATCH, getString(R.string.url_root) + "/user/" + uuid + "/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!error.toString().equals("com.android.volley.TimeoutError")) {
                            Log.e("Volley error", error.toString());
                        }

                        /*if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_MODEL, modelName);
                            contentValues.put(DataContract.VolleyCache.COLUMN_TX_POWER, txPower);
                            getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);
                        }*/

                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("txPower", String.valueOf(txPower));
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);
    }

    public void update(ScanResult result) {
        byte[] data = result.getScanRecord().getManufacturerSpecificData().get(2242);
        String uuid = new String(Arrays.copyOfRange(data, 0, 5));
        String dev_address = result.getDevice().getAddress();
        //Utils.address = dev_address;
        long start_time = System.currentTimeMillis();
        getDistances(result.getRssi(), start_time, uuid); // avgRssi, distance, avgDistance, minDistance will be updated in this function
        Utils.rssi = (int) avgRssi;
        configuration(distance);
        Utils.distance = distance;

        if (notifCount == MAX_COUNT) {
            updateNotification(type);
        }

        ContentValues values = new ContentValues();
        values.put(DataContract.InteractionEntry.COLUMN_ADDRESS, dev_address);
        values.put(DataContract.InteractionEntry.COLUMN_AVG_RSSI, Utils.rssi);
        values.put(DataContract.InteractionEntry.COLUMN_DISTANCE, distance);
        values.put(DataContract.InteractionEntry.COLUMN_ZONE, type);
        values.put(DataContract.InteractionEntry.COLUMN_PHONE_STATE, PHONE_STATE);
        values.put(DataContract.InteractionEntry.COLUMN_MODEL, Utils.model);
        values.put(DataContract.InteractionEntry.COLUMN_NAME, Utils.otherName);
        values.put(DataContract.InteractionEntry.COLUMN_CLIENTID, uuid);
        values.put(DataContract.InteractionEntry.COLUMN_AVG_DISTANCE, avgDistance);
        values.put(DataContract.InteractionEntry.COLUMN_MIN_DISTANCE, minDistance);

        // changing the selection criteria to column_name, because column_address changes for a device with time
        Cursor exist = getContentResolver().query(DataContract.InteractionEntry.CONTENT_URI, new String[]{DataContract.InteractionEntry._ID, DataContract.InteractionEntry.COLUMN_TSTART, DataContract.InteractionEntry.COLUMN_TSTOP}, DataContract.InteractionEntry.COLUMN_CLIENTID + "=?", new String[]{uuid}, DataContract.InteractionEntry.COLUMN_TSTOP + " DESC");
        if (exist != null) {
            exist.moveToFirst();
            int id = 0;
            long last_time = 0;
            long first_time = 0;
            if (exist.getCount() > 0) {
                id = Integer.parseInt(exist.getString(exist.getColumnIndex(DataContract.InteractionEntry._ID)));
                first_time = Long.parseLong(exist.getString(exist.getColumnIndex(DataContract.InteractionEntry.COLUMN_TSTART)));
                last_time = Long.parseLong(exist.getString(exist.getColumnIndex(DataContract.InteractionEntry.COLUMN_TSTOP)));
            }
            if (exist.getCount() > 0 && start_time < last_time + 60000) {
                values.put(DataContract.InteractionEntry.COLUMN_TSTART, first_time);
                values.put(DataContract.InteractionEntry.COLUMN_TSTOP, start_time + 1000);
                if (distance <= 1.5) {
                    getContentResolver().update(DataContract.InteractionEntry.CONTENT_URI, values, DataContract.InteractionEntry._ID + "=?", new String[]{String.valueOf(id)});
                    upload(uuid, getString(R.string.url_root) + "/interaction/customupdate/", first_time, start_time + 1000, minDistance, avgDistance, avgRssi);
                    if(start_time-first_time>60000 && sendFaceScan) {
                        Intent intent = new Intent(this, MaskChangeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                        Notification noti = new NotificationCompat.Builder(this, facenotificationChannelId)
                                .setContentText("You have breached social distancing for more than a minute. Please scan your face.")
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.ic_notif)
                                .setPriority(Notification.PRIORITY_DEFAULT)
                                .setContentTitle("Face Scan")
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .build();

                        notificationManager.notify(5, noti);
                        sendFaceScan=false;
                    }
                }
            } else {
                values.put(DataContract.InteractionEntry.COLUMN_TSTART, start_time);
                values.put(DataContract.InteractionEntry.COLUMN_TSTOP, start_time + 1000);
                if (distance <= 1.5) {
                    getContentResolver().insert(DataContract.InteractionEntry.CONTENT_URI, values);
                    upload(uuid, getString(R.string.url_root) + "/interaction/", start_time, start_time + 1000, minDistance, avgDistance, avgRssi);
                    sendFaceScan=true;
                }

            }
            exist.close();
        }

        recentReadings.add(values);

        long last_time = 0;
        long first_time = 0;
        if (UIReadings.containsKey(uuid)) {
            last_time = UIReadings.get(uuid).getAsLong(DataContract.InteractionEntry.COLUMN_TSTOP);
            first_time = UIReadings.get(uuid).getAsLong(DataContract.InteractionEntry.COLUMN_TSTART);
        }

        if (UIReadings.containsKey(uuid) && start_time < last_time + 10000) {
            if (distance < 1.5) {
                values.put(DataContract.InteractionEntry.COLUMN_TSTART, first_time);
                values.put(DataContract.InteractionEntry.COLUMN_TSTOP, start_time + 1000);
            } else {
                values.put(DataContract.InteractionEntry.COLUMN_TSTART, first_time);
                values.put(DataContract.InteractionEntry.COLUMN_TSTOP, last_time);
            }
        } else {
            values.put(DataContract.InteractionEntry.COLUMN_TSTART, start_time);
            values.put(DataContract.InteractionEntry.COLUMN_TSTOP, start_time + 1000);
        }
        UIReadings.put(uuid, values);

        Log.i("Avg", "onScanResult: " + result.getRssi() + "   " + distance);

        boolean destinationMe = false;
        if (data.length >= 15)
            destinationMe = (new String(Arrays.copyOfRange(data, 10, 15))).equals(Utils.getClientID(context));

        if (data[9] > Utils.getInteger(context, "isCalibrated", 0)) {
            Log.d("calib_testing", "Found calib possibility for : " + Utils.getName(context));
            if (data.length >= 19 && destinationMe) {
                float value = Float.parseFloat(new String(Arrays.copyOfRange(data, 15, 19)));
                Log.d("calib_testing", "Calibration constant obtained :" + value);
                if (data[9] == 50) {
                    Log.d("calib_testing", "Calibrating : " + Utils.getString(context, "model", "No Name"));
                    setTxPower(Utils.getString(context, "model", "No Name"), value);
                } else
                    setTxPowerUser(Utils.getClientID(context), value);
                Utils.setInteger(context, "isCalibrated", (int) data[9]);
                Utils.setFloat(context, "myTxPower", value);
                Utils.setBoolean(context, "isTxPowerSet", true);

                advData.clear();
                scanRssi.clear();
            } else {
                if (!advData.containsKey(uuid) && distance > 0.5 && distance < 1.5) {
                    advData.put(uuid, distance);
                    Log.d("calib_testing", Utils.getName(context) + " : Adding distance to the advertising data");
                    Log.d("calib_testing", advData.toString());
                }
            }

        } else if (Utils.getInteger(context, "isCalibrated", 0) > data[9]) {
            if (data.length >= 19 && destinationMe && scanRssi.containsKey(uuid)) {
                float prevRssi = scanRssi.get(uuid);
                float prevDistance = Float.parseFloat(new String(Arrays.copyOfRange(data, 15, 19)));
                float value = (float) (prevRssi + Math.log10(prevDistance) * 30);
                advData.put(uuid, value);
                Log.d("calib_testing", "Sending calibrated constant to the other phone :" + value);
                Log.d("calib_testing", "Adv data : " + advData);

            } else {
                if (distance <= 2)
                    scanRssi.put(uuid, avgRssi);
            }
        }
    }

    void startAdvertising(byte[] serviceDataByteArray) {

        shouldBeAdvertising = true;
        Log.d("passData", Arrays.toString(serviceDataByteArray) + " " + Utils.getInteger(context, "isCalibrated", -1));

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(2242, serviceDataByteArray)
                .build();

        if (advertiser == null)
            advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            advertiser.startAdvertising(settings, data, callback);
        }
    }

    void stopAdvertising() {
        Log.d("TAG4", "stop advertising");

        if (advertiser != null) {
            advertiser.stopAdvertising(callback);
            shouldBeAdvertising = false;
        }
    }

    private Notification createNotif() {
        String notificationChannelId = "exampleServiceChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelId,
                    "WorkFence",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Stay Safe!");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
            notificationManager.createNotificationChannel(channel);
        }

        PendingIntent notificationIntent;
        Intent i = new Intent(this, MainActivity.class);
        notificationIntent = PendingIntent.getActivity(this, 0, i, 0);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, notificationChannelId);
        else
            builder = new Notification.Builder(this);

        return builder
                .setContentTitle("Work Fence")
                .setContentText("Stay Safe!")
                .setSmallIcon(R.drawable.ic_notif)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
                .build();
    }

    private void configuration(float distance) {
        int type;
        if (distance < 1.5)
            type = 1;
        else
            type = 3;
        if (type == this.type) {
            notifCount++;
        } else {
            notifCount = 0;
        }
        if (notifCount > MAX_COUNT)
            notifCount = 0;
        this.type = type;
        Log.i("Config", type + " * " + notifCount);
    }

    private void upload(final String uuid, final String url, final long startTime, final long stopTime, final float minDistance, final float avgDistance, final float avgRssi) {
        int method;
        if (url.contains("customupdate"))
            method = Request.Method.PATCH;
        else
            method = Request.Method.POST;
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(method, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!error.toString().equals("com.android.volley.TimeoutError")) {
                            Log.e("Volley error", error.toString());
                        }
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_CLIENTID, uuid);
                            contentValues.put(DataContract.VolleyCache.COLUMN_URL, url);
                            contentValues.put(DataContract.VolleyCache.COLUMN_START, startTime);
                            contentValues.put(DataContract.VolleyCache.COLUMN_STOP, stopTime);
                            contentValues.put(DataContract.VolleyCache.COLUMN_MIN, minDistance);
                            contentValues.put(DataContract.VolleyCache.COLUMN_AVG, avgDistance);
                            contentValues.put(DataContract.VolleyCache.COLUMN_AVG_RSSI, avgRssi);
                            getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);
                        }
                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("client_id", Utils.getClientID(context));
                    jsonBody.put("date", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                    jsonBody.put("company", Utils.getCompanyID(context));
                    jsonBody.put("title", Utils.getString(context, "name", ""));
                    jsonBody.put("status", "");
                    jsonBody.put("loc_lat", "");
                    jsonBody.put("loc_long", "");
                    jsonBody.put("minDist", minDistance);
                    jsonBody.put("maxDist", avgDistance);
                    jsonBody.put("avgRssi", avgRssi);
                    jsonBody.put("start_time", startTime);
                    jsonBody.put("stop_time", stopTime);
                    jsonBody.put("creator", Utils.getClientID(context));
                    String temp = ",\"employees\": [\"" + uuid + "\"]}";
                    Log.i("taggy", temp);
                    String mRequestBody = jsonBody.toString();
                    mRequestBody = mRequestBody.substring(0, mRequestBody.length() - 1);
                    mRequestBody += temp;
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);
    }

    private void updateStatus(final int status) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        /*if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_STATUS, status);
                            context.getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);
                        }*/
                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    if (Utils.getBoolean(context, "wfh", false))
                        jsonBody.put("status_out", 2);
                    else
                        jsonBody.put("status_out", status);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);
    }

    public void createNotification() {
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Bluetooth is off!!")
                .setContentText("Turn on bluetooth for workfence to work")
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(context.getColor(R.color.notifAlert))
                .setOnlyAlertOnce(true)
                .build();

        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, noti);
    }

    public void updateNotification(int type) {
        Notification noti = new Notification();
        a = false;

        Log.i("TAG5", "updateNotification: " + type + Utils.zone);
        if (Utils.zone != type) {
            switch (type) {
                case 1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        noti = new NotificationCompat.Builder(this, notificationChannelId)
                                .setContentText("Social distancing breach detected!")
                                .setColorized(true)
                                .setSmallIcon(R.drawable.ic_notif)
                                .setContentTitle("Social Distancing")
                                .setColor(getColor(R.color.notifAlert))
                                .setChannelId("exampleServiceChannel")
                                .build();
                    } else {
                        noti = new NotificationCompat.Builder(this, notificationChannelId)
                                .setContentText("Social distancing breach detected!")
                                .setContentTitle("Social Distancing")
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setSmallIcon(R.drawable.ic_notif)
                                .setColor(getColor(R.color.notifAlert))
                                .build();
                    }
                    /*if (!isPlaying) {
                        mediaPlayer.start();
                        isPlaying = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isPlaying) {
                                    mediaPlayer.pause();
                                    isPlaying = false;
                                }
                            }
                        }, 5000);
                    }*/
                    break;
                case 2:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        noti = new NotificationCompat.Builder(this, notificationChannelId)
                                .setContentText("You are close to breaching social distancing norms!")
                                .setColorized(true)
                                .setSmallIcon(R.drawable.ic_notif)
                                .setOnlyAlertOnce(true)
                                .setColor(getColor(R.color.notifWarning))
                                .setContentTitle("Social Distancing")
                                .setChannelId("exampleServiceChannel")
                                .build();
                    else
                        noti = new NotificationCompat.Builder(this, notificationChannelId)
                                .setContentText("You are close to breaching social distancing norms!")
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.ic_notif)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setColor(getColor(R.color.notifWarning))
                                .setContentTitle("Social Distancing")
                                .build();
                    break;
                case 3:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        noti = new NotificationCompat.Builder(this, notificationChannelId)
                                .setContentText("No nearby devices with WorkFence app within 1.5 meters")
                                .setOnlyAlertOnce(true)
                                .setColorized(true)
                                .setSmallIcon(R.drawable.ic_notif)
                                .setColor(getColor(R.color.notifSafe))
                                .setContentTitle("Social Distancing")
                                .setChannelId("exampleServiceChannel")
                                .build();
                    } else {
                        noti = new NotificationCompat.Builder(this, notificationChannelId)
                                .setContentText("No nearby devices with WorkFence app within 1.5 meters")
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.ic_notif)
                                .setColor(getColor(R.color.notifSafe))
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setContentTitle("Social Distancing")
                                .build();
                    }
                    /*if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isPlaying = false;
                    }*/
            }
            Utils.zone = type;
            notificationManager.notify(1, noti);
        }
        /*else if (type == 1) {
            Log.i("TAG", "updateNotification: " + mediaPlayer.isPlaying());
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                isPlaying = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isPlaying) {
                            mediaPlayer.pause();
                            isPlaying = false;
                        }
                    }
                }, 5000);
            }
        }*/
    }

    public void getDistances(float rssi, long currTime, String uuid) {

        /*int max_weightage = 5000;

        distance = (float) Math.pow(10,(Utils.otherTxPower - rssi)/30.0);
        avgDistance = distance * max_weightage;
        minDistance = distance;
        float sum = rssi * max_weightage;
        int count = max_weightage;

        if (UIReadings.containsKey(uuid))
        {
            ContentValues reading = UIReadings.get(uuid);

            long time = reading.getAsLong(DataContract.InteractionEntry.COLUMN_TSTOP);
            float currRssi = reading.getAsFloat(DataContract.InteractionEntry.COLUMN_AVG_RSSI);
            float currDistance = reading.getAsFloat(DataContract.InteractionEntry.COLUMN_DISTANCE);
            float currMin = reading.getAsFloat(DataContract.InteractionEntry.COLUMN_MIN_DISTANCE);
            String int_uuid = reading.getAsString(DataContract.InteractionEntry.COLUMN_UUID);

            long diff = currTime - time;
            long weightage = 5000 - diff;

            if (diff < 5000) {
                if (int_uuid.equals(uuid)) {
                    if(minDistance > currMin)
                        minDistance = currMin;
                    avgDistance += weightage * currDistance;
                    sum += weightage * currRssi;
                    count += weightage;
                }
            }
        }

        avgDistance = avgDistance / count;
        avgRssi = sum / count;
        distance = (float) Math.pow(10,(Utils.otherTxPower - avgRssi)/30.0);*/

        distance = (float) Math.pow(10, (Utils.otherTxPower - rssi) / 30.0);
        avgDistance = distance * (recentReadings.size() + 1);
        minDistance = distance;
        float sum = rssi * (recentReadings.size() + 1);
        int count = (recentReadings.size() + 1);
        int clearCount = 0;
        int i = 0;
        for (ContentValues recentReading : recentReadings) {
            long time = recentReading.getAsLong(DataContract.InteractionEntry.COLUMN_TSTOP);
            float currRssi = recentReading.getAsFloat(DataContract.InteractionEntry.COLUMN_AVG_RSSI);
            float currDistance = recentReading.getAsFloat(DataContract.InteractionEntry.COLUMN_DISTANCE);
            float currMin = recentReading.getAsFloat(DataContract.InteractionEntry.COLUMN_MIN_DISTANCE);
            String int_uuid = recentReading.getAsString(DataContract.InteractionEntry.COLUMN_CLIENTID);
            if (currTime - time <= 5000) {
                if (int_uuid.equals(uuid)) {
                    if (minDistance > currMin)
                        minDistance = currMin;
                    avgDistance += (i + 1) * currDistance;
                    sum += (i + 1) * currRssi;
                    count += (i + 1);
                }
            } else if (currTime - time > 10000) {
                clearCount++;
            }

            i++;
        }

        for (int j = 0; j < clearCount; j++)
            recentReadings.remove();

        avgDistance = avgDistance / count;
        avgRssi = sum / count;
        distance = (float) Math.pow(10, (Utils.otherTxPower - avgRssi) / 30.0);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float rotX, rotY, rotZ;
        float accX, accY, accZ;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accX = event.values[0];
            accY = event.values[1];
            accZ = event.values[2];
            //Log.i("TAG", "onSensorChanged: " + accX + "-" + accY + "-" + accZ);
            isDeviceMoving = Math.abs(accX) > 3 || Math.abs(accY) > 3 || Math.abs(accZ - 9) > 3;
        } else {
            rotX = event.values[0];
            rotY = event.values[1];
            rotZ = event.values[2];
            isDeviceTopFacing = Math.abs(rotX) < 3 && Math.abs(rotY) < 3 && Math.abs(rotZ) < 3;
        }
        isDeviceFlat = isDeviceTopFacing && isDeviceMoving;
        if (isDeviceFlat)
            PHONE_STATE = 0;
        else if (isDeviceMoving)
            PHONE_STATE = 1;
        else
            PHONE_STATE = 2;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAdvertising();
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(mScanCallback);
            mScanning = false;
        }
        if (Utils.getBoolean(context, "settingsScreen", true)) {
            try {
                unregisterReceiver(screenTimeBroadcastReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        unregisterReceiver(bluetoothBroadcastReceiver);
        unregisterReceiver(batteryLevelBroadcastReceiver);

        if (Utils.getBoolean(context, "settingsCamera", true)) {
            cameraManager.unregisterAvailabilityCallback(availabilityCallback);
        }

        Utils.setBoolean(context, "serviceStarted", false);

        timer.cancel();
        sensorManager.unregisterListener(this);

        super.onDestroy();
    }

    private void updateAttendance(final int status) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_STATUS, status);
                            context.getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);
                        }
                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("status", status);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);
    }

    private void cameraCount(final long cameraCount) {

        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_CAMERA_COUNT, cameraCount);
                            context.getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);
                        }
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("cam_count", cameraCount);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);
    }

    private void outDuration(final long outDuration) {

        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!error.toString().equals("com.android.volley.TimeoutError")) {
                            Log.e("Volley error", error.toString());
                        }

                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_OUT_DURATION, outDuration);
                            getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);
                        }
                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("out_duration", outDuration);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);
    }

    private void screenTime(final Long time) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DataContract.VolleyCache.COLUMN_SCREEN_TIME, time);
                            getContentResolver().insert(DataContract.VolleyCache.CONTENT_URI, contentValues);
                        }
                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("screen_time", time);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(context));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(volleyMultipartRequest);
    }

    @SuppressLint("MissingPermission")
    public void registerNetworkCallback() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                                                                       @Override
                                                                       public void onAvailable(Network network) {

                                                                           ArrayList<String> toDelete = new ArrayList<>();
                                                                           Cursor cursor = contentResolver.query(DataContract.VolleyCache.CONTENT_URI, new String[]{DataContract.VolleyCache._ID, DataContract.VolleyCache.COLUMN_CLIENTID, DataContract.VolleyCache.COLUMN_URL, DataContract.VolleyCache.COLUMN_START, DataContract.VolleyCache.COLUMN_STOP, DataContract.VolleyCache.COLUMN_MIN, DataContract.VolleyCache.COLUMN_AVG, DataContract.VolleyCache.COLUMN_AVG_RSSI}, DataContract.VolleyCache.COLUMN_MIN + " IS NOT NULL", null, null);

                                                                           if (cursor != null) {

                                                                               cursor.moveToFirst();
                                                                               while (!cursor.isAfterLast()) {
                                                                                   String id = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache._ID));
                                                                                   String uuid = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_CLIENTID));
                                                                                   String url = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_URL));
                                                                                   long start_time = cursor.getLong(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_START));
                                                                                   long stop_time = cursor.getLong(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_STOP));
                                                                                   float min = cursor.getFloat(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_MIN));
                                                                                   float avg = cursor.getFloat(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_AVG));
                                                                                   float avgRssi = cursor.getFloat(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_AVG_RSSI));

                                                                                   upload(uuid, url, start_time, stop_time, min, avg, avgRssi);
                                                                                   toDelete.add(id);
                                                                                   cursor.moveToNext();
                                                                               }

                                                                               if (toDelete.size() > 0) {
                                                                                   String[] ids = new String[toDelete.size()];
                                                                                   String whereClause = String.format(DataContract.VolleyCache._ID + " in (%s)", TextUtils.join(",", Collections.nCopies(ids.length, "?")));
                                                                                   getContentResolver().delete(DataContract.VolleyCache.CONTENT_URI, whereClause, toDelete.toArray(ids));
                                                                                   toDelete.clear();
                                                                               }

                                                                               cursor.close();
                                                                           }

                                                                           cursor = contentResolver.query(DataContract.VolleyCache.CONTENT_URI, new String[]{DataContract.VolleyCache._ID, DataContract.VolleyCache.COLUMN_MODEL, DataContract.VolleyCache.COLUMN_TX_POWER}, DataContract.VolleyCache.COLUMN_MODEL + " IS NOT NULL", null, null);

                                                                           if (cursor != null) {

                                                                               cursor.moveToFirst();
                                                                               while (!cursor.isAfterLast()) {

                                                                                   String id = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache._ID));
                                                                                   String model = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_MODEL));
                                                                                   float txPower = cursor.getFloat(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_TX_POWER));

                                                                                   setTxPower(model, txPower);
                                                                                   toDelete.add(id);
                                                                                   cursor.moveToNext();
                                                                               }

                                                                               if (toDelete.size() > 0) {
                                                                                   String[] ids = new String[toDelete.size()];
                                                                                   String whereClause = String.format(DataContract.VolleyCache._ID + " in (%s)", TextUtils.join(",", Collections.nCopies(ids.length, "?")));
                                                                                   getContentResolver().delete(DataContract.VolleyCache.CONTENT_URI, whereClause, toDelete.toArray(ids));
                                                                                   toDelete.clear();
                                                                               }

                                                                               cursor.close();
                                                                           }

                                                                           cursor = contentResolver.query(DataContract.VolleyCache.CONTENT_URI, new String[]{DataContract.VolleyCache._ID, DataContract.VolleyCache.COLUMN_STATUS}, DataContract.VolleyCache.COLUMN_STATUS + " IS NOT NULL", null, null);

                                                                           if (cursor != null) {

                                                                               cursor.moveToFirst();
                                                                               while (!cursor.isAfterLast()) {

                                                                                   String id = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache._ID));
                                                                                   int status = cursor.getInt(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_STATUS));

                                                                                   updateAttendance(status);
                                                                                   toDelete.add(id);
                                                                                   cursor.moveToNext();
                                                                               }

                                                                               if (toDelete.size() > 0) {
                                                                                   String[] ids = new String[toDelete.size()];
                                                                                   String whereClause = String.format(DataContract.VolleyCache._ID + " in (%s)", TextUtils.join(",", Collections.nCopies(ids.length, "?")));
                                                                                   getContentResolver().delete(DataContract.VolleyCache.CONTENT_URI, whereClause, toDelete.toArray(ids));
                                                                                   toDelete.clear();
                                                                               }

                                                                               cursor.close();
                                                                           }

                                                                           cursor = contentResolver.query(DataContract.VolleyCache.CONTENT_URI, new String[]{DataContract.VolleyCache._ID, DataContract.VolleyCache.COLUMN_OUT_DURATION}, DataContract.VolleyCache.COLUMN_OUT_DURATION + " IS NOT NULL", null, null);

                                                                           if (cursor != null) {

                                                                               cursor.moveToFirst();
                                                                               while (!cursor.isAfterLast()) {

                                                                                   String id = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache._ID));
                                                                                   long outDuration = cursor.getLong(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_OUT_DURATION));

                                                                                   outDuration(outDuration);
                                                                                   toDelete.add(id);
                                                                                   cursor.moveToNext();
                                                                               }

                                                                               if (toDelete.size() > 0) {
                                                                                   String[] ids = new String[toDelete.size()];
                                                                                   String whereClause = String.format(DataContract.VolleyCache._ID + " in (%s)", TextUtils.join(",", Collections.nCopies(ids.length, "?")));
                                                                                   getContentResolver().delete(DataContract.VolleyCache.CONTENT_URI, whereClause, toDelete.toArray(ids));
                                                                                   toDelete.clear();
                                                                               }

                                                                               cursor.close();
                                                                           }

                                                                           cursor = contentResolver.query(DataContract.VolleyCache.CONTENT_URI, new String[]{DataContract.VolleyCache._ID, DataContract.VolleyCache.COLUMN_SCREEN_TIME}, DataContract.VolleyCache.COLUMN_SCREEN_TIME + " IS NOT NULL", null, null);

                                                                           if (cursor != null) {

                                                                               cursor.moveToFirst();
                                                                               while (!cursor.isAfterLast()) {

                                                                                   String id = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache._ID));
                                                                                   long screenTime = cursor.getLong(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_SCREEN_TIME));

                                                                                   screenTime(screenTime);
                                                                                   toDelete.add(id);
                                                                                   cursor.moveToNext();
                                                                               }

                                                                               if (toDelete.size() > 0) {
                                                                                   String[] ids = new String[toDelete.size()];
                                                                                   String whereClause = String.format(DataContract.VolleyCache._ID + " in (%s)", TextUtils.join(",", Collections.nCopies(ids.length, "?")));
                                                                                   getContentResolver().delete(DataContract.VolleyCache.CONTENT_URI, whereClause, toDelete.toArray(ids));
                                                                                   toDelete.clear();
                                                                               }

                                                                               cursor.close();
                                                                           }

                                                                           cursor = contentResolver.query(DataContract.VolleyCache.CONTENT_URI, new String[]{DataContract.VolleyCache._ID, DataContract.VolleyCache.COLUMN_CAMERA_COUNT}, DataContract.VolleyCache.COLUMN_CAMERA_COUNT + " IS NOT NULL", null, null);

                                                                           if (cursor != null) {

                                                                               cursor.moveToFirst();
                                                                               while (!cursor.isAfterLast()) {

                                                                                   String id = cursor.getString(cursor.getColumnIndex(DataContract.VolleyCache._ID));
                                                                                   int cameraCount = cursor.getInt(cursor.getColumnIndex(DataContract.VolleyCache.COLUMN_CAMERA_COUNT));

                                                                                   cameraCount(cameraCount);
                                                                                   toDelete.add(id);
                                                                                   cursor.moveToNext();
                                                                               }

                                                                               if (toDelete.size() > 0) {
                                                                                   String[] ids = new String[toDelete.size()];
                                                                                   String whereClause = String.format(DataContract.VolleyCache._ID + " in (%s)", TextUtils.join(",", Collections.nCopies(ids.length, "?")));
                                                                                   getContentResolver().delete(DataContract.VolleyCache.CONTENT_URI, whereClause, toDelete.toArray(ids));
                                                                                   toDelete.clear();
                                                                               }

                                                                               cursor.close();
                                                                           }

                                                                       }

                                                                       @Override
                                                                       public void onLost(Network network) {

                                                                       }
                                                                   }

                );
            }
        } catch (Exception e) {
            Log.e("TAG", e.toString());
        }
    }

    byte[] toByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value};
    }

    int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                ((bytes[3] & 0xFF));
    }
}
