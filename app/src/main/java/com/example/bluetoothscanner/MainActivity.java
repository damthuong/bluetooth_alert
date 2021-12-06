package com.example.bluetoothscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    FirebaseDatabase database;
    DatabaseReference ref;
    User user;
 //   private boolean mScanning;

    private static final int RQS_ENABLE_BLUETOOTH = 1;

    Button btnScan;
    ListView listViewLE;

    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;

    private static final int REQUEST_ENABLE_BT = 0;
    ArrayList<String> listBluetooth;
    ArrayList<String> listBluetoothTemp;
    Button btnOnOff;
    Button btnLs;
    Button btnSupport;
    ImageView mBlueIv;
    TextView tvafterScan;
    Button Logout;

    Integer i = 1;
    LocationManager locationManager;
    String provider;
    String DeviceID;

    private static final int REQUEST_CHECK_CODE = 8989;
    private LocationSettingsRequest.Builder builder;
    boolean GpsStatus ;

    private static final int PERMISSION_REQUEST_LOCATION = 0;

    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);

        DeviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        tvafterScan = (TextView)findViewById(R.id.tvAfterScan);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        user = new User();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("User: " + DeviceID) ;
        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("BLUETOOTH_LE not supported in this device!");
            finish();
        }

        getBluetoothAdapterAndLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
           // showToast("bluetoothManager.getAdapter()==null");
            //finish();
            //return;
        }
        mBlueIv = (ImageView) findViewById(R.id.mBlueIv);
        mBlueIv.setImageResource(R.drawable.ic_action_on);

        btnOnOff = (Button) findViewById(R.id.btnOnOff);
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isEnabled()) {
                    showToast("Turning On Bluetooth....");
                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                } else if (mBluetoothAdapter.isEnabled()) {
                    // showToast("Bluetooth is already on");
                    mBluetoothAdapter.disable();
                    showToast("Turing Bluetooth off");
                    mBlueIv.setImageResource(R.drawable.ic_action_off);
                }
            }
        });

        btnScan = (Button) findViewById(R.id.scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isEnabled()) {
                    showToast("Turning On Bluetooth....");
                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                }
                if (mBluetoothAdapter.isEnabled()) {

                    showLocation();
                }
            }
        });

        btnLs = (Button) findViewById(R.id.btnHt);
        btnLs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Lich_su_quet.class);
                startActivity(intent);
            }
        });


        btnSupport = (Button) findViewById(R.id.btnSp);
        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Support_Activity.class);
                startActivity(intent);
            }
        });

        listViewLE = (ListView) findViewById(R.id.lelist);
        listBluetooth = new ArrayList<>();
        listBluetoothTemp = new ArrayList<>();
        ArrayAdapter adapterLeScanResult = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, listBluetooth);
        listViewLE.setAdapter(adapterLeScanResult);
        mHandler = new Handler();
        listViewLE.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //i trả về vị trí click trong listview
                showToast((" " + listBluetooth.get(i)));

            }
        });

        Logout = findViewById(R.id.logout);
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember","false");
                editor.apply();

                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, RQS_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RQS_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        getBluetoothAdapterAndLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            showToast("bluetoothManager.getAdapter()==null");
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getBluetoothAdapterAndLeScanner() {
        // Get BluetoothAdapter and BluetoothLeScanner.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

     //   mScanning = false;
    }

    /*
    to call startScan (ScanCallback callback),
    Requires BLUETOOTH_ADMIN permission.
    Must hold ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get results.
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            //listBluetoothDevice.clear();
            listBluetooth.clear();
            //listBluetooth.add("Các thiết bị Bluetooth xung quanh: ");
            listViewLE.invalidateViews();

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(scanCallback);
                    listViewLE.invalidateViews();

                    showToast("Scan timeout");
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
           //         mScanning = false;
                    btnScan.setEnabled(true);
                }
            }, SCAN_PERIOD);

            mBluetoothLeScanner.startScan(scanCallback);
            mBlueIv.setImageResource(R.drawable.ic_action_scan);
         //   mScanning = true;
            btnScan.setEnabled(false);
        } else {
            mBluetoothLeScanner.stopScan(scanCallback);
       //     mScanning = false;
            btnScan.setEnabled(true);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addBluetoothDevice(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                addBluetoothDevice(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            showToast("onScanFailed: " + String.valueOf(errorCode));
        }

        private void addBluetoothDevice(BluetoothDevice device) {
            String nameDevice = device.getName();
            if (!listBluetoothTemp.contains(device.getAddress())) {
                getValue(device);
                if(nameDevice == null){
                    nameDevice = "Thiết bị không tên";
                }
                listBluetooth.add(nameDevice);
                listBluetoothTemp.add(device.getAddress());
                listViewLE.invalidateViews();
            }
        }
    };

    private void getValue(BluetoothDevice device) {
        if (device.getName() == null) {
            user.setName("Thiết bị không tên thứ " + i);
        }
        String adrresss = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //DeviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        //user.setPhone(device.getAddress());
        user.setAddress(adrresss);
        user.setBlAddress(device.getAddress());
        ref.child(user.getBlAddress()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                showToast("Add user success");
            }
        });
        i++;
    }

    private void showLocation() {
        // BEGIN_INCLUDE(startCamera)
        // Check if the Camera permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            //startCamera();
            GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(GpsStatus == false) {
                showToast("Cần đồng ý truy cập vị trí dể sử dụng tính năng này.");
                turnonGPS();
            }
            else {
                Snackbar.make(mLayout,
                        R.string.location_permission_available,
                        Snackbar.LENGTH_SHORT).show();
                tvafterScan.setText("Các thiết bị bluetooth xung quanh ");
                scanLeDevice(true);

            }
        } else {
            // Permission is missing and must be requested.
            setPermissionRequestLocation();
        }
        // END_INCLUDE(startCamera)
    }

    private void setPermissionRequestLocation() {
        // Bắt đầu hỏi quyền truy cập vị trí
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Hiển thị thông báo yêu cầu cấp quyền.
            Snackbar.make(mLayout, R.string.location_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                }
            }).show();
        } else {
            // Quyền truy cập chưa được cấp, hỏi trực tiêp người dùng.
            Snackbar.make(mLayout, R.string.location_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
    }

    private void turnonGPS(){
        LocationRequest request = new LocationRequest().setFastestInterval(1500).setInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode())
                    {
                        case LocationSettingsStatusCodes
                                .RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, REQUEST_CHECK_CODE);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            } catch (ClassCastException exception){

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        {
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        //broadcastIntent.setClass(this, .class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        //    unregisterReceiver(myreceiver);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

//    public void setDeviceBl(BluetoothDevice deviceBl) {
//        this.deviceBl = deviceBl;
//    }
}