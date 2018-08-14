package com.example.eperez.androidexam;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.eperez.androidexam.Adapter.RutasAdapter;
import com.example.eperez.androidexam.Modelos.DrawPolilyne;
import com.example.eperez.androidexam.Modelos.PicassoMarker;
import com.example.eperez.androidexam.Modelos.Ruta;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                   View.OnClickListener,
                                                   GoogleApiClient.ConnectionCallbacks,
                                                   GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private PlaceAutocompleteFragment placeAutoCompleteFrom, placeAutoCompleteTo;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button btn_go;

    private LinearLayout l_map;
    private LinearLayout l_recycler;

    private GoogleApiClient googleApiClient;
    private double longitude;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private double latitude;
    private Marker marker, mRoadTo;
    private String TAG = getClass().getSimpleName();
    private double currentLat, currentnLong, goToLat, goToLong;
    private String BASE_URL;
    private float currentZoom = 11f;
    private String mUrl ="";
    private final String APIKEY = "AIzaSyAx1ZnIYdrIsdQiBuI3KQEcn3uDcDKEEfM";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if(!isGpsEnabled()){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        btn_go = findViewById(R.id.btn_go);
        btn_go.setOnClickListener(this);
        l_map = findViewById(R.id.content_map);
        l_recycler = findViewById(R.id.content_recycler);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);


        //AutoComplete
        placeAutoCompleteFrom = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocompleteFrom);
        placeAutoCompleteTo = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocompleteTo);
        placeAutoCompleteFrom.setHint("Mi ubicación");
        placeAutoCompleteTo.setHint("Destino");
        placeAutoCompleteFrom.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                Log.d("Maps", "Place selected From: " + place.getName());
                Log.d("Maps", "LatLong From: " + place.getLatLng());

            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        placeAutoCompleteTo.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                Log.d("Maps", "Place selectedTo: " + place.getName());
                Log.d("Maps", "LatLong To: " + place.getLatLng());
                LatLng goTo= place.getLatLng();

                goToLat = goTo.latitude;
                goToLong = goTo.longitude;

            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);


        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom != currentZoom) {
                    currentZoom = cameraPosition.zoom;
                }
            }
        });

        if (marker == null) {
            Criteria criteria = new Criteria();

            try {
                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                currentLat = latitude;
                currentnLong = longitude;
            } catch (Exception e) {
                e.getMessage();
                Log.i(TAG, "Something was worng, sorry :/");
            }

            marker = mMap.addMarker(new MarkerOptions()
                    .title("You are here!")
                    .snippet("LEt's go to drink some beers!")
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.person_pin_circle_black_192x192)));

            LatLng currentLocation = new LatLng(latitude, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            geocoder = new Geocoder(this, Locale.getDefault());
        }
    }

    private void buildRecycler(){
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RutasAdapter(builsItems(),this, new RutasAdapter.listener() {
            @Override
            public void onClick(Ruta article) {
                //Toast.makeText(MapsActivity.this, "I am the: "+article.getTipo(), Toast.LENGTH_SHORT).show();
                l_map.setVisibility(View.VISIBLE);

                BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?origin="+currentLat+","+currentnLong+"&destination="+goToLat+","+goToLong+"&sensor=true&mode=driving&key="+APIKEY;
                Log.i("TAG",BASE_URL);
                DrawPolilyne draw = new DrawPolilyne(MapsActivity.this,mMap,BASE_URL);
                draw.DrawnPolyline();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btn_go:
                l_map.setVisibility(View.GONE);
                l_recycler.setVisibility(View.VISIBLE);
                buildRecycler();

                break;

                default:
                    Toast.makeText(this, "Action unknown", Toast.LENGTH_SHORT).show();
                    break;

        }
    }

    public List<Ruta> builsItems(){

        List<Ruta> l_ruta = new ArrayList<>();

        Ruta r = new Ruta();

        for (int i=0; i<=10; i++){
            r.setHora(12.00);
            r.setTipo("Carro");
            l_ruta.add(r);
        }

        return l_ruta;
    }

    public void showMap(boolean val){
        /*if (val ==true)

            else*/

    }

    public void showList(boolean val){
        /*if (val ==true)

            else*/
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean isGpsEnabled() {
        try{
            int gpsSignal = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE);

            if(gpsSignal == 0){
                return false;
            }else{
                return true;
            }
        }catch(Settings.SettingNotFoundException e){
            return false;
        }
    }
}
