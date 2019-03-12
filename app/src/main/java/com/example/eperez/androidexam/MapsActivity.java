package com.example.eperez.androidexam;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.eperez.androidexam.Adapter.RutasAdapter;
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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                   View.OnClickListener,
                                                   GoogleApiClient.ConnectionCallbacks,
                                                   GoogleApiClient.OnConnectionFailedListener,
                                                   RoutingListener {

    private GoogleMap mMap;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private LinearLayout l_map;
    private LinearLayout l_recycler;

    private Marker marker;
    private String TAG = getClass().getSimpleName();
    private double currentLat, currentnLong, goToLat, goToLong;
    private float currentZoom = 11f;
    private LatLng goTo,current;
    private List<Ruta> l_ruta;
    private ProgressBar progressBar;
    private final int MY_LOCATION_REQUEST_CODE = 1;

    //Animation

    private List<LatLng> polyLineList;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition;
    private int index, next;
    private Polyline blackPolyline;
    private int valueSeekbar;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light, R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        checkPermissions();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polyLineList = new ArrayList<>();

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        SeekBar seekBar = (SeekBar) findViewById(R.id.SeekBar);
        polylines = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                /*Toast.makeText(MapsActivity.this, "Seek bar progress is :" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();*/
                valueSeekbar = progressChangedValue;
            }
        });


        if (!isGpsEnabled()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        Button btn_go = findViewById(R.id.btn_go);
        btn_go.setOnClickListener(this);
        l_map = findViewById(R.id.content_map);
        l_recycler = findViewById(R.id.content_recycler);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);


        //AutoComplete
        PlaceAutocompleteFragment placeAutoCompleteFrom = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocompleteFrom);
        PlaceAutocompleteFragment placeAutoCompleteTo = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocompleteTo);
        placeAutoCompleteFrom.setHint("Mi ubicación");
        placeAutoCompleteTo.setHint("Destino");
        placeAutoCompleteFrom.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                Log.d("Maps", "Place selected From: " + place.getName());
                Log.d("Maps", "LatLong From: " + place.getLatLng());
                current = place.getLatLng();

                currentLat = current.latitude;
                currentnLong = current.longitude;

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
                goTo = place.getLatLng();

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
            checkPermissions();
            return;
        } else {
            mMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

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
                    currentLat =  location.getLatitude();
                    currentnLong = location.getLongitude();
                } catch (Exception e) {
                    e.getMessage();
                    Log.i(TAG, "Algo salio mal al colocar tu marcador");
                }

                marker = mMap.addMarker(new MarkerOptions()
                        .title("Estas aqui!")
                        .snippet("Vamos a un lugar divertido!")
                        .position(new LatLng(currentLat, currentnLong))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.person_pin_circle_black_192x192)));

                LatLng currentLocation = new LatLng(currentLat, currentnLong);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(true);
            }
        }

    }

    private void buildRecycler() {
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecyclerView.Adapter mAdapter = new RutasAdapter(l_ruta, this, new RutasAdapter.listener() {
            @Override
            public void onClick(Ruta article) {
                //Toast.makeText(MapsActivity.this, "I am the: "+article.getTipo(), Toast.LENGTH_SHORT).show();
                l_map.setVisibility(View.VISIBLE);

                if (polylines.size() > 0)
                    clearPolylines();

                LatLng latLng = new LatLng(article.getLat(), article.getLgn());
                drwSingleRoute(latLng);


            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_go:
                if (goToLat != 0.0 && goToLong != 0.0) {
                    l_map.setVisibility(View.GONE);
                    l_recycler.setVisibility(View.VISIBLE);
                    drwRoute(goTo);

                } else
                    Toast.makeText(this, "Debes selecionar un destino primero", Toast.LENGTH_SHORT).show();


                break;

            default:
                Toast.makeText(this, "Accion desconocida unknown", Toast.LENGTH_SHORT).show();
                break;

        }
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
        try {
            int gpsSignal = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE);

            if (gpsSignal == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Algo salio mal al trazar la ruta "+e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onRoutingFailure: "+e.getMessage() );
    }

    @Override
    public void onRoutingStart() {

        Toast.makeText(this, "Trazando ruta espera un mometo porfavor", Toast.LENGTH_SHORT).show();
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRoutingSuccess(final ArrayList<Route> route, int i) {
        progressBar.setVisibility(View.INVISIBLE);
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        polyLineList = route.get(0).getPoints();
        l_ruta = new ArrayList<>();
        //add route(s) to the map.
        Ruta ruta = new Ruta();
        for (int a = 0; a < route.size(); a++) {

            PolylineOptions blackPolylineOptions = new PolylineOptions();
            blackPolylineOptions.width(5);
            blackPolylineOptions.color(Color.BLACK);
            blackPolylineOptions.startCap(new SquareCap());
            blackPolylineOptions.endCap(new SquareCap());
            blackPolylineOptions.jointType(JointType.ROUND);
            blackPolyline = mMap.addPolyline(blackPolylineOptions);


            Toast.makeText(getApplicationContext(), "Route " + (a + 1) + ": distance - " + route.get(a).getDistanceValue() +
                    ": duration - " + route.get(a).getDurationValue(), Toast.LENGTH_SHORT).show();

            LatLngBounds latLng = route.get(a).getLatLgnBounds();

            ruta.setHora(route.get(a).getDurationText());
            ruta.setDistance(String.valueOf(route.get(a).getEndAddressText()));
            ruta.setLat(latLng.getCenter().latitude);
            ruta.setLgn(latLng.getCenter().longitude);

            l_ruta.add(ruta);

        }
        buildRecycler();

        //Animator  polylines.get(0).getPoints()


        Toast.makeText(this, "Data convert in JSONFormat", Toast.LENGTH_SHORT).show();
        Task task = new Task(route.get(0).getPoints(), new Task.listener() {
            @Override
            public void onFinish(StringBuilder builder) {
                Log.i(TAG,builder.toString());
                sendFirebase(builder.toString());
            }
        });
        task.execute();
            

        if (route.get(0).getPoints()!=null) {
            final ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
            polylineAnimator.setDuration(2000);
            polylineAnimator.setInterpolator(new LinearInterpolator());
            polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    List<LatLng> points = route.get(0).getPoints();
                    int percentValue = (int) valueAnimator.getAnimatedValue();
                    int size = points.size();
                    int newPoints = (int) (size * (percentValue / 100.0f));
                    List<LatLng> p = points.subList(0, newPoints);
                    blackPolyline.setPoints(p);
                }
            });
            polylineAnimator.start();
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLat,currentnLong))
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car2)));
            handler = new Handler();
            index = -1;
            next = 1;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (index < polyLineList.size() - 1) {
                        index++;
                        next = index + 1;
                    }
                    if (index < polyLineList.size() - 1) {
                        startPosition = polyLineList.get(index);
                        endPosition = polyLineList.get(next);
                    }
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                    valueAnimator.setDuration(1000);
                    valueAnimator.setInterpolator(new LinearInterpolator());
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            v = valueAnimator.getAnimatedFraction();
                            lng = v * endPosition.longitude + (1 - v)
                                    * startPosition.longitude;
                            lat = v * endPosition.latitude + (1 - v)
                                    * startPosition.latitude;
                            LatLng newPos = new LatLng(lat, lng);
                            marker.setPosition(newPos);
                            marker.setAnchor(0.5f, 0.5f);
                            marker.setRotation(getBearing(startPosition, newPos));
                            mMap.moveCamera(CameraUpdateFactory
                                    .newCameraPosition
                                            (new CameraPosition.Builder()
                                                    .target(newPos)
                                                    .zoom(15.5f)
                                                    .build()));





                        }

                    });
                    valueAnimator.start();
                    handler.postDelayed(this, 1000);
                }
            }, 1000);
        }
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    @Override
    public void onRoutingCancelled() {

    }

    public void drwRoute(LatLng dir) {
        Routing routing = new Routing.Builder()
                .key(getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(new LatLng(currentLat, currentnLong), dir)
                .build();
        routing.execute();
    }

    public void drwSingleRoute(LatLng dir) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(new LatLng(currentLat, currentnLong), dir)
                .build();
        routing.execute();
    }

    private void clearPolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    public boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "permission granted!", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            // Show rationale and request permission.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_LOCATION_REQUEST_CODE);
                return false;

            } else{
                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_LOCATION_REQUEST_CODE);
                return false;
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length > 0 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {
                // Permission was denied.
                Toast.makeText(this, "Debes aceptar los permisos solicitados", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void sendFirebase(String str){
        FirebaseFirestore database = FirebaseFirestore.getInstance();


        Map<String,String> data = new HashMap<>();
        data.put("routes",str);
        data.put("seekbar",String.valueOf(valueSeekbar));

        database.collection("ruta").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(MapsActivity.this, "Datos enviados", Toast.LENGTH_SHORT).show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity.this, "Algo salio mal al enviar datos", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
