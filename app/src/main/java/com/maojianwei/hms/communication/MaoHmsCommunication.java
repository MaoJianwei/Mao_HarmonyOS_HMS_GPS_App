package com.maojianwei.hms.communication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.HWLocation;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStates;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MaoHmsCommunication extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = MaoHmsCommunication.class.getName();

    private final int REQUEST_PERMISSION_CODE = 7181;
    private final SimpleDateFormat localtimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    private final LocationRequest locationRequest = new LocationRequest().setInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private final MaoLocationCallback locationCallback = new MaoLocationCallback();

    private TextView gpsLocationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.StartGetLocation).setOnClickListener(this);
        findViewById(R.id.StopGetLocation).setOnClickListener(this);
        findViewById(R.id.OneshotGetLocation).setOnClickListener(this);

        gpsLocationView = findViewById(R.id.LocationDataTextView);

//        int a = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        int b = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
//        MaoLogHelper.i(TAG, String.format("Mao HMS on! %d %d %d", Build.VERSION.SDK_INT, a, b));

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);

        MaoLogHelper.i(TAG, String.format(Locale.CHINA,"Mao HMS permissions required! SDK: %d", Build.VERSION.SDK_INT));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        MaoLogHelper.i(TAG, String.format(Locale.CHINA,"Mao HMS permission granted! %d %s", requestCode, permissions[i]));
                    } else {
                        MaoLogHelper.i(TAG, String.format(Locale.CHINA,"Mao HMS permission denied! %d %s", requestCode, permissions[i]));
                    }
                }
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.StartGetLocation:
                requestLocationRefresh();
                break;
            case R.id.StopGetLocation:
                cancelLocationRefresh();
                break;
            case R.id.OneshotGetLocation:
                break;
        }
        MaoLogHelper.i(TAG, "radar click");
    }


    private void requestLocationRefresh() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        try {
            LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
            MaoHmsCommunication THIS = this;
            settingsClient.checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener(locationSettingsResponse -> {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                                .addOnSuccessListener(v -> MaoLogHelper.e(TAG, "requestLocationUpdates onSuccess"))
                                .addOnFailureListener(e -> MaoLogHelper.e(TAG, "requestLocationUpdates onFailure:" + e.getMessage()));

                        MaoLogHelper.e(TAG, "checkLocationSettings onSuccess, location update requested.");
                        //LocationSettingsStates l = locationSettingsResponse.getLocationSettingsStates();
                        //MaoLogHelper.e(TAG, String.format("checkLocationSettings onSuccess:\n" +
                        //        "%b,%b,%b,%b,\n%b,%b,%b,%b,\n%b,%b", l.isBlePresent(), l.isBleUsable(), l.isGpsPresent(), l.isGpsUsable(),
                        //        l.isHMSLocationPresent(), l.isHMSLocationUsable(), l.isLocationPresent(), l.isLocationUsable(),
                        //        l.isNetworkLocationPresent(), l.isNetworkLocationUsable()));
                        //    false,true,true,true,
                        //    true,true,true,true,
                        //    true,true
                    })
                    .addOnFailureListener(e -> {
                        switch (((ApiException) e).getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ((ResolvableApiException) e).startResolutionForResult(MaoHmsCommunication.this, 0);
                                } catch (IntentSender.SendIntentException sendIntentException) {
                                    MaoLogHelper.e(TAG, "startResolutionForResult -> SendIntentException " + sendIntentException.getMessage());
                                }
                                break;
                            default:
                                MaoLogHelper.e(TAG, "checkLocationSettings onFailure:" + e.getMessage());
                        }

                    });
        } catch (Exception e) {
            MaoLogHelper.e(TAG, "checkLocationSettings exception:" + e.getMessage());
        }
    }

    private void cancelLocationRefresh() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    .addOnSuccessListener(v -> MaoLogHelper.e(TAG, "removeLocationUpdates onSuccess"))
                    .addOnFailureListener(e -> MaoLogHelper.e(TAG, "removeLocationUpdates onFailure:" + e.getMessage()));
        } catch (Exception e) {
            MaoLogHelper.e(TAG, "checkLocationSettings exception:" + e.getMessage());
        }
    }

    private class MaoLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                HWLocation l = locationResult.getLastHWLocation();
                Map<String, Object> extra = l.getExtraInfo();
                Object type = extra.get("SourceType");
                if (type != null && ((Integer) type) == 19) {
//                    String data = String.format(Locale.CHINA,"%f, %f, %f, %f, %d\n" +
//                                    "%s, %s, %s, %s, %s, %s, %s\n" +
//                                    "%f, %f, %f\n" +
//                                    "%f, %f\n" +
//                                    "%d, %s, %s, %s, %d\n" +
//                                    "%s, %s\n",
//                            l.getAltitude(), l.getLatitude(), l.getLongitude(), l.getSpeed(), l.getTime(),
//                            l.getCountryCode(), l.getCountryName(), l.getCounty(), l.getCity(), l.getPostalCode(), l.getState(), l.getStreet(),
//                            l.getAccuracy(), l.getSpeedAccuracyMetersPerSecond(), l.getVerticalAccuracyMeters(),
//
//                            l.getBearing(), l.getBearingAccuracyDegrees(),
//
//                            l.describeContents(), l.getFeatureName(), l.getUrl(), l.getPhone(), l.getElapsedRealtimeNanos(),
//
//                            l.getProvider(), l.getExtraInfo().toString()
//                    );
                    String data = String.format(Locale.CHINA,
                            "" +
                                    "坐标：%f  %f\n" +
                                    "坐标精度：%f m\n" +
                                    "海拔：%f\n" +
                                    "海拔精度：%f m\n" +
                                    "速度：%f\n" +
                                    "速度精度：%f m/s\n" +
                                    "航向：%f °\n" +
                                    "航向精度：%f °\n" +
                                    "时间 | 来源：%s | %s\n",
                            l.getLatitude(), l.getLongitude(), l.getAccuracy(),
                            l.getAltitude(), l.getVerticalAccuracyMeters(),
                            l.getSpeed(), l.getSpeedAccuracyMetersPerSecond(),
                            l.getBearing(), l.getBearingAccuracyDegrees(),
                            localtimeFormat.format(new Date(l.getTime())), l.getProvider()
                    );

                    ((Activity) gpsLocationView.getContext()).runOnUiThread(() ->
                            gpsLocationView.setText(data + "===================================\n" + gpsLocationView.getText()));
                    MaoLogHelper.e(TAG, "HWLocation:\n" + data);
                }
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            if (locationAvailability != null) {
                MaoLogHelper.e(TAG, String.format(Locale.CHINA,"%b, %s", locationAvailability.isLocationAvailable(), locationAvailability.toString()));
            }
        }
    }
}