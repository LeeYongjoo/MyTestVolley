package com.example.jeemee.mytestvolley;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String REQUEST_TAG = "VolleyActivity";
    private TextView mTextView;
    private Button mButton;
    private RequestQueue mQueue;
    private LocationManager mLocation;
    LocationListener mLocationListener;
    private static final int REQUEST_CODE_FIND_LOCATION = 2;
    private static final int REQUEST_CODE_COARSE_LOCATION = 3;
    LocationListener locationListener;
    double mLat;
    double mLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.tv_result);
        mButton = (Button) findViewById(R.id.btn_http);

        mLocation = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLat = location.getLatitude();
                mLng = location.getLongitude();

                //mTextView.setText("lat: "+lat);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_FIND_LOCATION);
            //return;
        } else {
            mLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            mLocation.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            setKnownPosition();
        }

        //mLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_FIND_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                REQUEST_CODE_COARSE_LOCATION);

                        Log.d(REQUEST_TAG, "Granted");
                        mLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        mLocation.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    } else {
                        mLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        mLocation.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    }

                    setKnownPosition();
                }
                break;
            case REQUEST_CODE_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    mLocation.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mQueue = PodVolleyRequestQueue.getInstance(this.getApplicationContext()).getRequestQueue();
        String url = "http://api.openweathermap.org/data/2.5/weather?q=Seoul,kr&appid=7c86e9d42f39de8c1166f431b541f2ba";

        //String geo = mLat + ";" + mLng;
        //String air_quality_url = "https://api.waqi.info/feed/geo:"+geo /*37.56667;126.97806*/+"/?token=f7a99bf19fe7c09a9041cb37fd32be969776551d";
        //final PodJsonRequest jsonRequest = new PodJsonRequest(Request.Method.GET
                //, air_quality_url, new JSONObject(), MainActivity.this, MainActivity.this);
        //jsonRequest.setTag(REQUEST_TAG);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String geo = mLat + ";" + mLng;
                String air_quality_url = "https://api.waqi.info/feed/geo:"+geo /*37.56667;126.97806*/+"/?token=f7a99bf19fe7c09a9041cb37fd32be969776551d";
                final PodJsonRequest jsonRequest = new PodJsonRequest(Request.Method.GET
                        , air_quality_url, new JSONObject(), MainActivity.this, MainActivity.this);
                jsonRequest.setTag(REQUEST_TAG);
                mQueue.add(jsonRequest);
            }
        });
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        mTextView.setText(error.getMessage());
    }

    @Override
    public void onResponse(JSONObject response) {
        mTextView.setText("Response is: " + response);
        //parserWeather(response);
        fineDustParser(response);
    }

    private void fineDustParser(JSONObject fineDust) {

    }

    private void parserWeather(JSONObject weather) {
        try {
            String city = weather.getString("name");
            //Log.d(REQUEST_TAG, "city: " + city);
            mTextView.append("\n\ncity: " + city);
            int current_temp = (int) (weather.getJSONObject("main").getInt("temp") - 273.15);
            mTextView.append("\n현재기온: " + current_temp);
            Date sunrise = new Date(1000 * (weather.getJSONObject("sys").getLong("sunrise")));
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
            mTextView.append("\nSunrise: " + timeFormat.format(sunrise));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //첫 위치값 구하기
    private void setKnownPosition() {
        String locationProvider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider
            //    ActivityCompat#requestPermissionscalling
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = mLocation.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            mLat = lastKnownLocation.getLatitude();
            mLng = lastKnownLocation.getLatitude();
            //Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
        }
    }
}
