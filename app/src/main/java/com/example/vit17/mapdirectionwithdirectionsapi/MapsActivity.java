package com.example.vit17.mapdirectionwithdirectionsapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private List<LatLng> places = new ArrayList<>();
    private String mapsApiKey = "AIzaSyBJmZyRSGDaqHRAoEy165hvcG9bPeHZowU";

    private static final int LOCATION_REQUEST = 500;

    private ArrayList<LatLng> listPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        places.add(new LatLng(55.754724, 37.621380));
        places.add(new LatLng(55.760133, 37.618697));
        places.add(new LatLng(55.764753, 37.591313));
        places.add(new LatLng(55.728466, 37.604155));
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


        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new  String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST);
            return;
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(listPoints.size()==2){
                    listPoints.clear();
                    mMap.clear();
                }
                listPoints.add(latLng);
                MarkerOptions marker = new MarkerOptions();
                marker.position(latLng);
                if(listPoints.size() == 1){
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }else if(listPoints.size() == 2){
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(marker);
                // make a directions between markers
                if(listPoints.size() == 2){
                    GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(mapsApiKey).build();
                    DirectionsResult result = null;
                    try{
                        result = DirectionsApi.newRequest(geoApiContext)
                                .mode(TravelMode.DRIVING)
                                .origin(new com.google.maps.model.LatLng(listPoints.get(0).latitude, listPoints.get(0).longitude))
                                .destination(new com.google.maps.model.LatLng(listPoints.get(1).latitude, listPoints.get(1).longitude))
                                .await();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(result.routes.length>0) {
                        List<com.google.maps.model.LatLng> path = result.routes[0].overviewPolyline.decodePath();
                        PolylineOptions line = new PolylineOptions();
                        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                        for (int i = 0; i < path.size(); i++) {
                            line.add(new com.google.android.gms.maps.model.LatLng(path.get(i).lat, path.get(i).lng));
                            latLngBuilder.include(new com.google.android.gms.maps.model.LatLng(path.get(i).lat, path.get(i).lng));
                        }
                        line.width(14f).color(R.color.colorPrimary);
                        mMap.addPolyline(line);

                        LatLngBounds latLngBounds = latLngBuilder.build();
                        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds,
                                getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels, 25);
                        mMap.moveCamera(track);
                    }
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(), "Way not found", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST:
                if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }

    }
}
