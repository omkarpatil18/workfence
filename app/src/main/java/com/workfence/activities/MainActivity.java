package com.workfence.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.workfence.R;
import com.workfence.database.DataContract;
import com.workfence.fragments.AttHistoryFragment;
import com.workfence.fragments.AttendanceFragment;
import com.workfence.fragments.ContactsFragment;
import com.workfence.fragments.EmployeeMetricsFragment;
import com.workfence.fragments.InteracHistoryFragment;
import com.workfence.fragments.InteractionFragment;
import com.workfence.others.BluetoothBroadcastReceiver;
import com.workfence.others.CustomDialog;
import com.workfence.others.MyService;
import com.workfence.others.ScreenTimeBroadcastReceiver;
import com.workfence.others.Utils;
import com.workfence.others.VolleyMultiPartRequest;
import com.workfence.others.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, SensorEventListener {

    private static final int OFFICE_MASK_CHECK = 22;
    static CoordinatorLayout mCoordinatorLayout;
    static ConstraintLayout constraintLayout;
    final int REQUEST_ENABLE_BT = 42;
    final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 666;
    BluetoothAdapter bluetoothAdapter;
    NotificationManager notificationManager;
    Boolean hasPermission = false;
    LocationProvider locationProvider;
    LocationManager locationManager;
    Location location;
    float altitude;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationClient;
    ExtendedFloatingActionButton fab_check;
    ExtendedFloatingActionButton fab_in_office;
    ExtendedFloatingActionButton fab_wfh;
    ExtendedFloatingActionButton fab_timer;
    FloatingActionButton fab_close;
    String masked;
    SensorManager sensorManager;
    Sensor sensor;
    Boolean isSensorPresent = false;
    boolean perm_loc, perm_cam;
    long elapsedHours;
    long elapsedMinutes;
    int calib_state = 0;


    Timer timer;
    Long checkInTime;
    Toolbar toolbar;

    Context context;

    boolean flag = false;

    public static void showSnack(String s) {
        Log.i("TAG", "showSnack: ");
        Snackbar.make(constraintLayout, s, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        masked="0";
        String phone = Utils.getString(context, "phone", "");
        if (phone.length() == 0 || Utils.getBoolean(context, "isOtp", false)) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }
        boolean token = Utils.getBoolean(context, "token", false);
        if (!token) {
            Intent i = new Intent(this, TokenActivity.class);
            startActivity(i);
            finish();
            return;
        }

        constraintLayout = findViewById(R.id.cons_layout);

        timer = new Timer();
        perm_cam = perm_loc = false;
        // todo :: Replace all sharedPref operations with Utils
        mCoordinatorLayout = findViewById(R.id.main_content);

        if (Utils.getBoolean(context, "settingsScreen", true)) {
            permissions();
        }

        if (Utils.getBoolean(context, "settingsLocation", true)) {
            init();
        }
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            isSensorPresent = true;
        } else {
            isSensorPresent = false;
        }

        fab_check = findViewById(R.id.fab_check);
        fab_in_office = findViewById(R.id.fab_in_office);
        fab_wfh = findViewById(R.id.fab_wfh);
        fab_close = findViewById(R.id.fab_close);
        fab_timer = findViewById(R.id.fab_timer);

        fab_check.setOnClickListener(this);
        fab_in_office.setOnClickListener(this);
        fab_wfh.setOnClickListener(this);
        fab_close.setOnClickListener(this);
        fab_timer.setOnClickListener(this);

        Date date = Calendar.getInstance().getTime(), lastDate = null;
        String storedDate;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        storedDate = Utils.getString(context, "todo", "");
        try {
            lastDate = sdf.parse(storedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String modelName = getDeviceName();
        Utils.setString(context, "model", modelName);
        getMyTxPower(Utils.getString(context, "clientID", "00000"));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        permissions();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("workfence.data", MODE_PRIVATE);
        if (!preferences.getBoolean("serviceStarted", false)) {

        } else {
            Log.i("TAG8", "onCreate: false");
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final DrawerLayout drawer = findViewById(R.id.activity_main);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);

        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigation = findViewById(R.id.nv);
        View header = navigation.getHeaderView(0);
        navigation.setNavigationItemSelectedListener(this);

        TextView comp = header.findViewById(R.id.comp_name);
        TextView username = header.findViewById(R.id.user_name);

        comp.setText(Utils.getString(context, "compname", "WorkFence"));
        username.setText(Utils.getString(context, "name", "Admin"));
        Menu nav_Menu = navigation.getMenu();
        if (!Utils.getIsAdmin(this)) {
            nav_Menu.findItem(R.id.interactions).setVisible(false);
            nav_Menu.findItem(R.id.attendance).setVisible(false);
            nav_Menu.findItem(R.id.emp_metrics).setVisible(false);
        } else {
            nav_Menu.findItem(R.id.attd_history).setVisible(false);
            nav_Menu.findItem(R.id.interaction_history).setVisible(false);
        }

        if (getIntent().getBooleanExtra("backPress", false)) {
            displaySelectedScreen(R.id.emp_metrics);
            navigation.getMenu().getItem(4).setChecked(true);
        } else {
            if (Utils.getIsAdmin(this)) {
                displaySelectedScreen(R.id.interactions);
            } else {
                displaySelectedScreen(R.id.ble_contacts);
            }
            navigation.getMenu().getItem(0).setChecked(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean isTimerOn = Utils.getBoolean(context, "isTimerOn", false);
        if (isTimerOn) {
            fab_check.setVisibility(View.VISIBLE);
            fab_close.setVisibility(View.GONE);
            fab_in_office.setVisibility(View.GONE);
            fab_wfh.setVisibility(View.GONE);
            fab_timer.setVisibility(View.VISIBLE);
            fab_check.setText(R.string.end_office);
            fab_check.setIcon(getDrawable(R.drawable.ic_baseline_house_24));
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    long inTime = Utils.getLong(context, "checkInTime", (long) 0);
                    printDifference(inTime, Calendar.getInstance().getTimeInMillis());
                }
            }, 0, 60000);
        } else {
            fab_in_office.setVisibility(View.GONE);
            fab_wfh.setVisibility(View.GONE);
            fab_close.setVisibility(View.GONE);
            fab_timer.setVisibility(View.GONE);
            fab_check.setText(R.string.start_office);
            fab_check.setVisibility(View.VISIBLE);
        }
    }

    void permissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                perm_loc = true;

            perm_cam = true;
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect peripherals.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    }
                });
                builder.show();
            }
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void buildlocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location i : locationResult.getLocations()) {
                    MainActivity.this.location = i;
                }
            }
        };

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        //locationRequest.setSmallestDisplacement(0);

    }

    private void init() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        //locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        //locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);

        //locationProvider.supportsAltitude();
        //location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        buildLocationRequest();
        buildlocationCallBack();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return model.substring(manufacturer.length() + 1).replaceAll(" ", "_");
        } else {
            return model.replaceAll(" ", "_");
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.help) {
            Intent i = new Intent(this, HelpActivity.class);
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.links) {
            Intent i = new Intent(this, LinksActivity.class);
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.privacy) {
            Intent i = new Intent(this, PrivacyPolicy.class);
            startActivity(i);
            return true;
        }
        displaySelectedScreen(item.getItemId());
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.activity_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void displaySelectedScreen(int itemId) {

        Fragment fragment = null;
        String s = "";

        switch (itemId) {
            case R.id.ble_contacts:
                Utils.fragment_id = R.id.ble_contacts;
                s = "Personal Distancing";
                fragment = new ContactsFragment(this);
                break;
            case R.id.attendance:
                Utils.fragment_id = R.id.attendance;
                s = "Daily Attendance";
                fragment = new AttendanceFragment(this);
                break;
            case R.id.interactions:
                Utils.fragment_id = R.id.interactions;
                s = "Employee Distancing";
                fragment = new InteractionFragment(this);
                break;
            case R.id.emp_metrics:
                Utils.fragment_id = R.id.emp_metrics;
                s = "Employee Metrics";
                fragment = new EmployeeMetricsFragment(this);
                break;
            case R.id.attd_history:
                Utils.fragment_id = R.id.attd_history;
                s = "Attendance History";
                fragment = new AttHistoryFragment(this);
                break;
            case R.id.interaction_history:
                Utils.fragment_id = R.id.interaction_history;
                s = "Interaction History";
                fragment = new InteracHistoryFragment(this);
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            toolbar.setTitle(s);
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.activity_main);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void getMyTxPower(final String uuid) {
        final StringRequest request = new StringRequest(
                Request.Method.GET,
                getString(R.string.url_root) + "/devicedata/by_client/?client_id=" + uuid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("calib_testing", "TxPower response:" + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Utils.setFloat(context, "myTxPower", (float) (jsonObject.getDouble("txPower") + Math.log10(jsonObject.getDouble("distance")) * 30));
                            Utils.setBoolean(context, "isTxPower", true);
                            calib_state = jsonObject.getInt("calibConf");
                            Utils.setInteger(context, "isCalibrated", calib_state);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getString(context, "authToken", ""));
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final boolean isTimerOn = Utils.getBoolean(context, "isTimerOn", false);
        int id = item.getItemId();
        if (id == R.id.logout_menu) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Logout");
            if (isTimerOn) builder.setMessage("Working Hours will end if you Logout");
            else builder.setMessage("Are you sure want to Logout");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (isTimerOn) {
                        flag = true;
                        checkOutTasks();
                    } else {

                        Utils.clearPrefs(MainActivity.this);
                        Log.i("userparams", Utils.getUserParams(MainActivity.this));

                        Intent stopIntent = new Intent(MainActivity.this, MyService.class);
                        stopIntent.setAction("stopService");

                        startService(stopIntent);

                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        finish();
                        MainActivity.this.startActivity(i);

                        stopService(new Intent(MainActivity.this, MyService.class));


                    }

                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } else if (id == R.id.info) {
            CustomDialog dialog;
            switch (Utils.fragment_id) {
                case R.id.interactions:
                    dialog = new CustomDialog(MainActivity.this, R.drawable.help_interac_img, R.string.help_company_interaction);
                    dialog.show();
                    break;
                case R.id.ble_contacts:
                    dialog = new CustomDialog(MainActivity.this, R.drawable.help_personal_interac_img, R.string.help_personal_interaction);
                    dialog.show();
                    break;
                case R.id.attendance:
                    dialog = new CustomDialog(MainActivity.this, R.drawable.help_attendance, R.string.help_attendance);
                    dialog.show();
                    break;
                case R.id.emp_metrics:
                    dialog = new CustomDialog(MainActivity.this, R.drawable.help_emp_metrics_img, R.string.help_emp_metrics);
                    dialog.show();
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean showTodo(Date startDate, Date endDate) {
        if (startDate == null)
            return true;
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;

        return elapsedDays >= 1.0;

    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.fab_check:
                if (fab_check.getVisibility() == View.VISIBLE) {
                    boolean isTimerOn = Utils.getBoolean(context, "isTimerOn", false);
                    if (isTimerOn) {
                        Utils.swiped = 1;

                        if (!isSensorPresent && !Utils.getBoolean(context, "wfh", false)) {
                            init();
                            if (location != null)
                                altitude = (float) location.getAltitude();
                            else
                                altitude = -1;
                            //altitude = 66;
                            Log.i("TAG9", "onStateChange: " + altitude);
                        }
                        if (!Utils.getBoolean(context, "wfh", false) && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                        }
                        if (!Utils.getBoolean(context, "wfh", false)) {
                            init();
                            checkOutTasks();
                        } else checkOutTasks();
                    } else {
                        fab_check.setVisibility(View.GONE);
                        fab_close.setVisibility(View.VISIBLE);
                        fab_in_office.setVisibility(View.VISIBLE);
                        fab_wfh.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.fab_in_office:
                startFaceScan();

                break;
            case R.id.fab_wfh:
                Utils.wfh = true;
                Utils.swiped = 1;
                checkInTasks();

                break;
            case R.id.fab_close:
                fab_check.setVisibility(View.VISIBLE);
                fab_close.setVisibility(View.GONE);
                fab_in_office.setVisibility(View.GONE);
                fab_wfh.setVisibility(View.GONE);
                break;
        }
    }

    private void startFaceScan() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setMessage("The app will scan your face to check for mask in order to allow you to record your attendance.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, ClassifierActivity.class);
                startActivityForResult(intent, OFFICE_MASK_CHECK);
            }
        }).setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create();

        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OFFICE_MASK_CHECK:
                if (resultCode == 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setMessage("You are not wearing a mask. Do you still want to check in?").setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.wfh = false;
                            Utils.swiped = 1;
                            masked = "0";
                            checkInTasks();
                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();

                    alertDialog.show();
                } else {
                    Utils.wfh = false;
                    Utils.swiped = 1;
                    masked = "1";
                    checkInTasks();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent)
            sensorManager.unregisterListener(this);
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void checkInTasks() {

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkOutTasks();
            }
        }, 60000);*/


        if (!(perm_cam && perm_loc) && Utils.getBoolean(context, "settingsScreen", true)) {
            permissions();
            return;
        }

        String currTime = String.valueOf(System.currentTimeMillis());
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        String curr_time = dateFormat.format(time);

        float storedLat = 0, storedLng = 0, storedAlt;
        float hori_dist, vert_dist;

        if (!isNetworkAvailable()) {
            Snackbar.make(mCoordinatorLayout, "Check your internet connection", Snackbar.LENGTH_LONG).show();
            return;
        }

        double lat = -1, lon = -1;
        if (false && Utils.getFloat(context, "lat", (float) 0.0) == 0.0 && Utils.getFloat(context, "lng", (float) 0.0) == 0.0 && !Utils.getBoolean(context, "settingsScreen", true)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Attendance Message");
            //builder.setMessage(R.string.att_msg);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (location != null) {
                        Utils.setFloat(context, "lat", (float) location.getLatitude());
                        Utils.setFloat(context, "lng", (float) location.getLongitude());
                        Utils.setFloat(context, "altitude", (float) location.getAltitude());

                    } else {

                        Utils.setFloat(context, "lat", (float) -1);
                        Utils.setFloat(context, "lng", (float) -1);
                        Utils.setFloat(context, "altitude", (float) -1);

                    }

                    checkInTasks();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } else {

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

            ContentValues values = new ContentValues();
            values.put(DataContract.AttendanceEntry.COLUMN_ID, "0adc");
            values.put(DataContract.AttendanceEntry.COLUMN_MODE, "right");
            values.put(DataContract.AttendanceEntry.COLUMN_TIME, curr_time);
            getContentResolver().insert(DataContract.AttendanceEntry.CONTENT_URI, values);
            Log.d("XXX", (location != null) + " " + storedLat + " " + storedLng + " " + hori_dist);
            checkIn(currTime, lat, lon, hori_dist, vert_dist);
        }
    }

    private void updateCheckInStatus(final int status) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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
                        jsonBody.put("status_in", 2);
                    else
                        jsonBody.put("status_in", status);
                    final String mRequestBody = jsonBody.toString();
                    Log.d("XXX", mRequestBody);
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getString(context, "authToken", ""));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(volleyMultipartRequest);
    }

    private void checkIn(final String time, final double lat, final double lon, final float hori_dist, final float vert_dist) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.url_root) + "/attendance/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response).getJSONObject("company").getJSONObject("permissions");
                            Log.d("XXX", jsonObject.toString());
                            Utils.setBoolean(context, "settingsCamera", jsonObject.getBoolean("camera"));
                            Utils.setBoolean(context, "settingsLocation", jsonObject.getBoolean("location"));
                            Utils.setBoolean(context, "settingsScreen", jsonObject.getBoolean("screen"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Utils.setString(context, "inTime", time);
                        Utils.setBoolean(context, "inside", true);
                        Utils.setBoolean(context, "checkInMarked", true);
                        Utils.setBoolean(context, "exit", false);
                        Utils.setBoolean(context, "isTimerOn", true);
                        Utils.setLong(context, "screenTime", (long) 0);
                        if (Utils.wfh) {
                            Utils.setBoolean(context, "wfh", true);
                        } else {
                            Utils.setBoolean(context, "wfh", false);
                        }

                        if (hori_dist < 500 && hori_dist >= 0) {
                            Utils.setBoolean(context, "checkInOk", true);
                            updateCheckInStatus(0);
                        } else {
                            updateCheckInStatus(1);
                            Utils.setBoolean(context, "checkInOk", false);
                        }

                        Snackbar.make(mCoordinatorLayout, "Have a great day ahead!", Snackbar.LENGTH_LONG).show();
                        fab_check.setVisibility(View.VISIBLE);
                        fab_close.setVisibility(View.GONE);
                        fab_in_office.setVisibility(View.GONE);
                        fab_wfh.setVisibility(View.GONE);
                        fab_timer.setVisibility(View.VISIBLE);
                        fab_check.setText(R.string.end_office);
                        fab_check.setIcon(getDrawable(R.drawable.ic_baseline_house_24));
                        checkInTime = Calendar.getInstance().getTimeInMillis();
                        Utils.setLong(context, "checkInTime", checkInTime);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                long inTime = Utils.getLong(context, "checkInTime", (long) 0);
                                printDifference(inTime, Calendar.getInstance().getTimeInMillis());
                            }
                        }, 0, 60000);

                        Intent i = new Intent(MainActivity.this, MyService.class);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            startForegroundService(i);
                        else
                            startService(i);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NetworkError) {
                            Snackbar.make(mCoordinatorLayout, "Check your network connection", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(mCoordinatorLayout, "Some error occurred. Try again Later", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }) {

            /*@Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("clientID", Utils.clientID);
                params.put("uuid", getSharedPreferences("workfence.data",MODE_PRIVATE).getString("uuid", "-1111"));
                params.put("inTime", time);
                params.put("inLocLat", String.valueOf(lat));
                params.put("inLocLon", String.valueOf(lon));
                return params;
            }*/

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("date", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                    jsonBody.put("in_time", Long.valueOf(time));
                    jsonBody.put("in_loc_lat", String.valueOf(lat));
                    jsonBody.put("cam_count", 0);
                    jsonBody.put("out_duration", 0);
                    jsonBody.put("screen_time", 0);
                    jsonBody.put("status", 1);
                    jsonBody.put("mask", Integer.parseInt(masked));
                    final String mRequestBody = jsonBody.toString();
                    Log.d("ReturnData",mRequestBody);
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException uee) {
                    Log.e("Volley Error", uee.toString());
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token " + Utils.getAuthToken(MainActivity.this));
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void printDifference(Long startDate, Long endDate) {
        long different = endDate - startDate;

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;


        elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        elapsedMinutes = different / minutesInMilli;

        if(elapsedHours==10)checkOutTasks();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab_timer.setText(elapsedHours + " hr " + elapsedMinutes + " min");
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float pressure = event.values[0];
        altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void checkOut(final String time, final double lat, final double lon, final long bletime) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Utils.setInteger(context, "secCount", 0);
                        Utils.setInteger(context, "minCount", 0);
                        Intent stopIntent = new Intent(MainActivity.this, MyService.class);
                        stopIntent.setAction("stopService");
                        startService(stopIntent);

                        Utils.setBoolean(context, "inside", false);
                        Utils.zone = 3;
                        stopService(new Intent(MainActivity.this, MyService.class));

                        if (fab_timer.getVisibility() == View.VISIBLE) {
                            fab_timer.setVisibility(View.GONE);
                        }

                        fab_check.setText(R.string.start_office);
                        fab_check.setIcon(getDrawable(R.drawable.ic_baseline_business_center_24));
                        Snackbar.make(mCoordinatorLayout, "Your office hours have ended. This app is no longer operational.", Snackbar.LENGTH_LONG).show();
                        Utils.setBoolean(context, "isTimerOn", false);
                        if (flag) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    flag = false;
                                    Utils.clearPrefs(MainActivity.this);
                                    Log.i("userparams", Utils.getUserParams(MainActivity.this));

                                    Intent stopIntent = new Intent(MainActivity.this, MyService.class);
                                    stopIntent.setAction("stopService");
                                    startService(stopIntent);
                                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    finish();
                                    MainActivity.this.startActivity(i);
                                    stopService(new Intent(MainActivity.this, MyService.class));
                                }
                            }, 0);
                        }
                        //finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NetworkError) {
                            Snackbar.make(mCoordinatorLayout, "Check your network connection", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(mCoordinatorLayout, "Some error occurred. Try again Later", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }) {

            /*@Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uuid", Utils.getString(context,"uuid", "-1"));
                params.put("outTime", time);
                params.put("outLocLat", String.valueOf(lat));
                params.put("outLocLon", String.valueOf(lon));
                params.put("inTime", Utils.getString(context,"inTime", "-1"));
                return params;
            }*/

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("out_time", Long.valueOf(time));
                    jsonBody.put("screen_time", ScreenTimeBroadcastReceiver.screenOnTimeSingle);
                    jsonBody.put("bleoff", bletime);
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
                params.put("Authorization", "Token " + Utils.getString(context, "authToken", ""));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(volleyMultipartRequest);
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

                    }
                }) {

            /*@Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uuid", Utils.getString(context,"uuid", "-1"));
                params.put("outDuration", String.valueOf(outDuration));
                params.put("inTime", Utils.getString(context,"inTime", "-1"));
                return params;
            }*/

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
                params.put("Authorization", "Token " + Utils.getString(context, "authToken", ""));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(volleyMultipartRequest);
    }

    private void checkOutTasks() {

        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        String curr_time = dateFormat.format(time);

        if (!isNetworkAvailable()) {
            Snackbar.make(mCoordinatorLayout, "Check your internet connection", Snackbar.LENGTH_LONG).show();
            return;
        }

        long bletime = 0;

        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            BluetoothBroadcastReceiver.endTimer = System.currentTimeMillis();
            if (BluetoothBroadcastReceiver.startTimer != -1) {
                bletime = BluetoothBroadcastReceiver.endTimer - BluetoothBroadcastReceiver.startTimer;
                BluetoothBroadcastReceiver.startTimer = -1;
            }
        }

        if (Utils.getBoolean(context, "settingsScreen", true)) {
            ScreenTimeBroadcastReceiver.endTimer = System.currentTimeMillis();
            ScreenTimeBroadcastReceiver.screenOnTimeSingle = ScreenTimeBroadcastReceiver.endTimer - ScreenTimeBroadcastReceiver.startTimer;
        } else
            ScreenTimeBroadcastReceiver.screenOnTimeSingle = 0;

        float storedLat, storedLng, storedAlt;
        float hori_dist, vert_dist;

        ContentValues values = new ContentValues();
        values.put(DataContract.AttendanceEntry.COLUMN_ID, "0adc");
        values.put(DataContract.AttendanceEntry.COLUMN_MODE, "right");
        values.put(DataContract.AttendanceEntry.COLUMN_TIME, curr_time);

        getContentResolver().insert(DataContract.AttendanceEntry.CONTENT_URI, values);

        double lat = -1, lon = -1;

        if (location != null) {
            storedLat = Utils.getFloat(context, "lat", (float) 0.0);
            storedLng = Utils.getFloat(context, "lng", (float) 0.0);
            storedAlt = Utils.getFloat(context, "altitude", (float) 0.0);
            vert_dist = (float) Math.abs(storedAlt - location.getAltitude());
            Location storedLoc = new Location(LOCATION_SERVICE);
            storedLoc.setLatitude(storedLat);
            storedLoc.setLongitude(storedLng);
            hori_dist = storedLoc.distanceTo(location);
            lat = location.getLatitude();
            lon = location.getLongitude();
            if (hori_dist < 500 && hori_dist >= 0) {
                updateCheckOutStatus(0, lat, lon, bletime);
            } else {
                updateCheckOutStatus(1, lat, lon, bletime);
            }
        } else {
            updateCheckOutStatus(1, lat, lon, bletime);
        }
    }

    private void updateCheckOutStatus(final int status, final double lat, final double lon, final long bletime) {
        VolleyMultiPartRequest volleyMultipartRequest = new VolleyMultiPartRequest(Request.Method.PUT, getString(R.string.url_root) + "/attendance/upd/",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        checkOut(String.valueOf(System.currentTimeMillis()), lat, lon, bletime);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
                params.put("Authorization", "Token " + Utils.getString(context, "authToken", ""));
                return params;
            }

        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(volleyMultipartRequest);
    }

}
