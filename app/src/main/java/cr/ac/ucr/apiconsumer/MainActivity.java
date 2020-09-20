package cr.ac.ucr.apiconsumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cr.ac.ucr.apiconsumer.api.RetrofitBuilder;
import cr.ac.ucr.apiconsumer.api.WeatherService;
import cr.ac.ucr.apiconsumer.models.Main;
import cr.ac.ucr.apiconsumer.models.Sys;
import cr.ac.ucr.apiconsumer.models.Weather;
import cr.ac.ucr.apiconsumer.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private final int REQUEST_CODE = 36;
    private final String TAG = "MainActivity";
    private final int LOCATION_CODE_REQUEST = 1;

    private ConstraintLayout clContainer;
    private TextView tvGreeting;
    private TextView tvTemperature;
    private TextView tvDescription;
    private ImageView ivImage;
    private TextView tvCity;
    private TextView tvMimMax;
    private String day;
    private Location location;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private Toolbar tToolbar;
    private AppBarLayout ablAppBarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitude = 9.994474;
        longitude = -84.66466;

        clContainer = findViewById(R.id.cl_container);
        tvGreeting = findViewById(R.id.tv_greeting);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvDescription = findViewById(R.id.tv_description);
        tvCity = findViewById(R.id.tv_city);
        tvMimMax = findViewById(R.id.tv_minmax);
        ivImage = findViewById(R.id.iv_image);
        tToolbar = findViewById(R.id.t_toolbar);
        ablAppBarLayout = findViewById(R.id.abl_appbar);

        ablAppBarLayout.setOutlineProvider(null);
        ablAppBarLayout.setElevation(0);

        tToolbar.setTitle("");
        setSupportActionBar(tToolbar);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkPermissions();

        setBackgroundAndGreeting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.im_open_search:
                openSearchActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                String city = data.getStringExtra(SearchActivity.KEY);
                Log.i(TAG, "city " + city);

                LatLng latlon = getLocationFromAddress(city);

                if (latlon != null) {
                    Log.i(TAG, latlon.latitude + " ----- " + latlon.longitude);
                    getWeather(latlon.latitude, latlon.longitude);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage().toString());
        }
    }

    private LatLng getLocationFromAddress(String city) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        LatLng latLng = null;
        try {
            addressList = geocoder.getFromLocationName(city, 5);

            if (addressList == null) {
                return latLng;
            }
            Address location = addressList.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage().toString());
        }
        return latLng;
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                        },
                        LOCATION_CODE_REQUEST
                );
                return;
            }
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                onLocationChanged(location);
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Para una mejor funcionalidad, activa el GPS")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
                getWeather(latitude, longitude);
            }
        } catch (Exception e) {
            getWeather(latitude, longitude);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_CODE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            } else {
                getWeather(latitude, longitude);
            }
        }
    }

    public void setBackgroundAndGreeting() {
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay > 5 && timeOfDay < 12) {
            tvGreeting.setText(R.string.day);
            clContainer.setBackgroundResource(R.drawable.background_day);
        } else if (timeOfDay >= 12 && timeOfDay < 19) {
            tvGreeting.setText(R.string.afternoon);
            clContainer.setBackgroundResource(R.drawable.background_afternoon);
        } else {
            tvGreeting.setText(R.string.night);
            clContainer.setBackgroundResource(R.drawable.background_night);
        }

        day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }

    private void getWeather(double latitude, double longitude) {
        WeatherService service = RetrofitBuilder.createService(WeatherService.class);
        Call<WeatherResponse> response = service.getWeatherByCoordinates(latitude, longitude);
        final AppCompatActivity activity = this;

        response.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {

                Log.i(TAG, String.valueOf(call.request().url()));

                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();

                    Main main = weatherResponse.getMain();
                    List<Weather> weatherList = weatherResponse.getWeather();
                    Sys sys = weatherResponse.getSys();

                    String temperature = getString(R.string.temperature, String.valueOf(Math.round(main.getTemp())));

                    tvTemperature.setText(temperature);

                    String minMax = getString(R.string.minmax, String.valueOf(Math.round(main.getTemp_min())), String.valueOf(Math.round(main.getTemp_max())));

                    tvMimMax.setText(minMax);

                    if (weatherList.size() > 0) {
                        Weather weather = weatherList.get(0);
                        tvDescription.setText(String.format("%s, %s", day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase(), weather.getDescription()));

                        String imageUrl = String.format("https://openweathermap.org/img/wn/%s@2x.png", weather.getIcon());

                        Log.d(TAG, "onResponse: " + imageUrl);
                        RequestOptions options = new RequestOptions()
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);

                        Glide.with(activity)
                                .load(imageUrl)
                                .apply(options)
                                .into(ivImage);
                    }

                    tvCity.setText(String.format("%s, %s", weatherResponse.getName(), sys.getCountry()));

                    //TODO: terminar de cargar el weather
                } else {
                    Log.e(TAG, "OnError" + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        getWeather(latitude, longitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}