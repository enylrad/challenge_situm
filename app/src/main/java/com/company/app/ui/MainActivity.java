package com.company.app.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.company.app.R;
import com.company.app.ui.base.BaseActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.Collection;

import es.situm.sdk.SitumSdk;
import es.situm.sdk.error.Error;
import es.situm.sdk.model.cartography.Building;
import es.situm.sdk.model.cartography.Floor;
import es.situm.sdk.utils.Handler;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    public static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSupportMapFragment();

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

                filterFloors(arrayListBuildings);
            }

            @Override
            public void onFailure(Error error) {
                Timber.e("onFailure: %s", error);
            }
        });

    }

    private void filterFloors(ArrayList<Building> buildings) {
        for (Building building : buildings) {
            SitumSdk.communicationManager().fetchFloorsFromBuilding(building, new Handler<Collection<Floor>>() {
                @Override
                public void onSuccess(Collection<Floor> floors) {
                    Timber.d("onSuccess: Your building floors: %s", floors.size());
                    if (floors.size() <= 1) {
                        buildings.remove(building);
                    }
                    Timber.d("Buildings: %s", buildings.size());
                }

                @Override
                public void onFailure(Error error) {
                    Timber.e("onFailure: %s", error);
                }
            });
        }

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

        //TODO
    }
}