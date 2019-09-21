package com.company.app.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.company.app.R;
import com.company.app.commons.utils.SpinnerExtensions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collection;

import es.situm.sdk.SitumSdk;
import es.situm.sdk.error.Error;
import es.situm.sdk.model.cartography.Building;
import es.situm.sdk.model.cartography.Floor;
import es.situm.sdk.model.cartography.Poi;
import es.situm.sdk.utils.Handler;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = MainActivity.class.getSimpleName();

    ArrayList<Building> buildsMoreOneFloor;
    int buildingSize;
    int count;

    private GoogleMap mMap;
    private AppCompatSpinner spBuildings;
    private ArrayList<Marker> buildingPois = new ArrayList<>();
    private ArrayList<Marker> selectedMakers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spBuildings = findViewById(R.id.sp_buildings);

        initSupportMapFragment();
        getBuildings(() -> buildSpinnerBuildings(buildsMoreOneFloor));
    }

    private void initSupportMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void getBuildings(OnCallbackBuildings onCallbackBuildings) {
        SitumSdk.communicationManager().fetchBuildings(new Handler<Collection<Building>>() {
            @Override
            public void onSuccess(Collection<Building> buildings) {
                Timber.d("onSuccess: Your buildings: ");
                ArrayList<Building> arrayListBuildings = new ArrayList<>(buildings);
                for (Building building : arrayListBuildings) {
                    Timber.i("onSuccess: %s - %s", building.getIdentifier(), building.getName());
                }

                if (arrayListBuildings.isEmpty()) {
                    Timber.e("onSuccess: you have no buildings. Create one in the Dashboard");
                    return;
                }

                filterFloors(arrayListBuildings, onCallbackBuildings);
            }

            @Override
            public void onFailure(Error error) {
                Timber.e("onFailure: %s", error);
            }
        });
    }

    private void filterFloors(ArrayList<Building> buildings, OnCallbackBuildings callbackFinish) {
        buildsMoreOneFloor = buildings;
        buildingSize = buildings.size();
        count = 0;
        for (Building building : buildings) {
            SitumSdk.communicationManager().fetchFloorsFromBuilding(building, new Handler<Collection<Floor>>() {
                @Override
                public void onSuccess(Collection<Floor> floors) {
                    Timber.d("onSuccess: Your building floors: %s", floors.size());
                    if (floors.size() <= 1) {
                        buildsMoreOneFloor.remove(building);
                        Timber.d("Remove building, Total: %s", buildsMoreOneFloor.size());
                    }
                    notifyCallback(callbackFinish);
                }

                @Override
                public void onFailure(Error error) {
                    Timber.e("onFailure: %s", error);
                    notifyCallback(callbackFinish);
                }
            });
        }
    }

    private void notifyCallback(OnCallbackBuildings callbackFinish) {
        if (++count == buildingSize) {
            Timber.d("Finish Handlers");
            callbackFinish.onFinish();
        }
    }


    private void buildSpinnerBuildings(ArrayList<Building> buildings) {
        SpinnerExtensions.INSTANCE.setSpinnerBuildings(spBuildings, buildings);
        SpinnerExtensions.INSTANCE.setSpinnerItemSelectedListener(spBuildings, item -> getPoisFromBuilding((Building) item));
    }

    private void getPoisFromBuilding(Building building) {
        if (mMap != null) {
            SitumSdk.communicationManager().fetchIndoorPOIsFromBuilding(building, new Handler<Collection<Poi>>() {
                @Override
                public void onSuccess(Collection<Poi> pois) {
                    if (pois.isEmpty()) {
                        Toast.makeText(MainActivity.this, "There isn't any poi in the building: " + building.getName() + ". Go to the situm dashboard and create at least one poi before execute again this example", Toast.LENGTH_LONG).show();
                    } else {
                        Timber.d("onSuccess: Your pois: %s", pois.toString());
                        mMap.clear();
                        buildingPois.clear();
                        selectedMakers.clear();
                        for (Poi poi : pois) {
                            drawPoi(poi);
                        }
                    }
                }

                @Override
                public void onFailure(Error error) {
                    Timber.e("onFailure: %s", error);
                }
            });
        }

    }

    private void drawPoi(Poi poi) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng latLng = new LatLng(poi.getCoordinate().getLatitude(),
                poi.getCoordinate().getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(poi.getName());
        buildingPois.add(mMap.addMarker(markerOptions));
        mMap.setOnMarkerClickListener(this::logicOnClickMaker);
        builder.include(latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    private boolean logicOnClickMaker(Marker marker) {
        if (!selectedMakers.contains(marker)) {
            if (selectedMakers.size() >= 2) {
                Marker markerToRemove = selectedMakers.get(0);
                int indexMakerToRemove = buildingPois.indexOf(selectedMakers.get(0));
                buildingPois.get(indexMakerToRemove).setIcon(BitmapDescriptorFactory.defaultMarker());
                selectedMakers.remove(markerToRemove);
            }
            selectedMakers.add(marker);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
        Timber.d("Makers selected: %s", selectedMakers.toString());
        return false;
    }

    public interface OnCallbackBuildings {
        void onFinish();   //method, which can have parameters
    }
}