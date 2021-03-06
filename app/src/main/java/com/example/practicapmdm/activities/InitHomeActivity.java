package com.example.practicapmdm.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.practicapmdm.R;
import com.example.practicapmdm.apiRest.ApiLocationMadridData;
import com.example.practicapmdm.constants.Constants;
import com.example.practicapmdm.domain.JsonResponse;
import com.example.practicapmdm.impl.ViewAdapter;
import com.example.practicapmdm.models.Location;
import com.example.practicapmdm.models.Pool;
import com.example.practicapmdm.services.GpsService;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.practicapmdm.constants.Constants.INTENT_LOCALIZATION_ACTION;
import static com.example.practicapmdm.constants.Constants.LATITUDE;
import static com.example.practicapmdm.constants.Constants.LONGITUDE;
import static com.example.practicapmdm.constants.Constants.NAME;

public class InitHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    public final String TAG = getClass().getName();
    public static final String TITLE_KEY = "TITLE_KEY";
    public static final String TITLE = "My location";
    public static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    public static final String DESCRIPTION = "This is my location";
    public static Double latitude;
    public static Double longitude;
    public String name;
    private ArrayList<Pool> mPools;
    public Double latitudReceive = null;
    public Double longitudReceive = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        setToolbar();

        drawerLayout = findViewById(R.id.drawer_layout);


        NavigationView navigationView = findViewById(R.id.navview);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(InitHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(InitHomeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Log.d(TAG, "Empezando el servicio");
            startService();
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter(INTENT_LOCALIZATION_ACTION));
        Intent getIntent = getIntent();
        double latitudeReceive = getIntent.getDoubleExtra(LATITUDE, 0);
        double longitudeReceive = getIntent.getDoubleExtra(LONGITUDE, 0);
        String nameReceive = getIntent.getStringExtra(NAME);


        Log.d(TAG, "Latitude " + String.valueOf(latitudeReceive));
        Log.d(TAG, "Longitude " + String.valueOf(longitudeReceive));
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.btn_moreinfo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = intent.getDoubleExtra(LATITUDE, 0);
            longitude = intent.getDoubleExtra(LONGITUDE, 0);
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), R.string.gps_granted, Toast.LENGTH_SHORT).show();
                startService();
            } else {
                Toast.makeText(getApplicationContext(), R.string.gps_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_item_one:
                Intent intentMaps = new Intent(InitHomeActivity.this, MapsActivity.class);
                intentMaps.putExtra(LATITUDE, latitude);
                intentMaps.putExtra(LONGITUDE, longitude);
                intentMaps.putExtra(NAME, name);
                startActivity(intentMaps);
                break;
            case R.id.nav_item_three:
                getPoolsNear();
                break;
            case R.id.nav_item_four:
                break;
            case R.id.nav_item_five:
                break;
            default:
                Log.d(TAG, "No he entrado por el onNavigationGetItemSelected");
                break;

        }
        return false;
    }



    public void startService () {
        Intent intentService = new Intent(getApplicationContext(), GpsService.class);
        startService(intentService);
    }
    public void getPoolsNear () {

        Retrofit retrofit = new Retrofit.Builder().
                baseUrl(Constants.HEADER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiLocationMadridData mApi = retrofit.create(ApiLocationMadridData.class);
        Log.d(TAG, "Parametros de entrada: " + latitude + " " + longitude + " " + Constants.DISTANCE);
        mApi.getPools(latitude, longitude, Constants.DISTANCE).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(Call<JsonResponse> call, Response<JsonResponse> response) {
                if (response != null && response.body() != null) {
                    mPools = (ArrayList<Pool>) response.body().results;
                    Intent sendPools = new Intent(getApplicationContext(), ActivityViewAdapter.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("LIST", mPools);
                    sendPools.putExtras(bundle);
                    startActivity(sendPools);
                    for (Pool mPool : mPools) {
                        Log.d(TAG, mPool.getName() == null ? "" : mPool.getName()); //e.getLocalizedMessage() == null ? "" : e.getLocalizedMessage()
                        Log.d(TAG, String.valueOf(mPool.getLocation().getLatitude()==0 ? "" : mPool.getLocation().getLatitude()));
                        Log.d(TAG, String.valueOf(mPool.getLocation().getLongitude() == 0 ? "" : mPool.getLocation().getLongitude()));
                    }
                    Log.d(TAG, "Parametros de salida: " + latitude + " " + longitude + " " + Constants.DISTANCE);
                }

            }

            @Override
            public void onFailure(Call<JsonResponse> call, Throwable t) {

            }
        });
    }

}