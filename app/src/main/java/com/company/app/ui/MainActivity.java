package com.company.app.ui;

import android.graphics.Color;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.situm.sdk.SitumSdk;
import es.situm.sdk.directions.DirectionsRequest;
import es.situm.sdk.error.Error;
import es.situm.sdk.location.util.CoordinateConverter;
import es.situm.sdk.model.cartography.Building;
import es.situm.sdk.model.cartography.Floor;
import es.situm.sdk.model.cartography.Poi;
import es.situm.sdk.model.cartography.Point;
import es.situm.sdk.model.directions.Route;
import es.situm.sdk.model.directions.RouteSegment;
import es.situm.sdk.model.location.Angle;
import es.situm.sdk.model.location.CartesianCoordinate;
import es.situm.sdk.model.location.Coordinate;
import es.situm.sdk.utils.Handler;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = MainActivity.class.getSimpleName();

    ArrayList<Building> buildsMoreOneFloor;
    private Building buildingSelected;
    private int buildingSize;
    private int count;

    private GoogleMap mMap;
    private AppCompatSpinner spBuildings;
    private ArrayList<Marker> buildingMakers = new ArrayList<>();
    private ArrayList<Marker> markerSelected = new ArrayList<>();

    private List<Polyline> polylines = new ArrayList<>();

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
        buildingSelected = building;
        if (mMap != null) {
            SitumSdk.communicationManager().fetchIndoorPOIsFromBuilding(building, new Handler<Collection<Poi>>() {
                @Override
                public void onSuccess(Collection<Poi> pois) {
                    clearMap();
                    if (pois.isEmpty()) {
                        Toast.makeText(MainActivity.this, "There isn't any poi in the building: " + building.getName() + ". Go to the situm dashboard and create at least one poi before execute again this example", Toast.LENGTH_LONG).show();
                    } else {
                        Timber.d("onSuccess: Your pois: %s", pois.toString());
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

    private void clearMap() {
        mMap.clear();
        buildingMakers.clear();
        markerSelected.clear();
    }

    private void clearPolyLine(){
        for(Polyline line: polylines){
            line.remove();
        }
        polylines.clear();
    }

    private void drawPoi(Poi poi) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng latLng = new LatLng(poi.getCoordinate().getLatitude(),
                poi.getCoordinate().getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(poi.getName());

        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag(poi);
        buildingMakers.add(marker);

        mMap.setOnMarkerClickListener(this::logicOnClickMaker);
        builder.include(latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    private boolean logicOnClickMaker(Marker marker) {
        if (!markerSelected.contains(marker)) {
            if (markerSelected.size() >= 2) {
                Marker markerToRemove = markerSelected.get(0);
                int indexMakerToRemove = buildingMakers.indexOf(markerSelected.get(0));
                buildingMakers.get(indexMakerToRemove).setIcon(BitmapDescriptorFactory.defaultMarker());
                markerSelected.remove(markerToRemove);
            }
            markerSelected.add(marker);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            if (markerSelected.size() == 2) {
                calculateRoute();
            }
        }
        Timber.d("Makers selected: %s", markerSelected.toString());
        return false;
    }

    private Point createPoint(Marker marker, CoordinateConverter coordinateConverter) {
        Coordinate coordinate = new Coordinate(marker.getPosition().latitude, marker.getPosition().longitude);
        CartesianCoordinate cartesianCoordinate = coordinateConverter.toCartesianCoordinate(coordinate);
        Poi tag = (Poi) marker.getTag();
        String floorIdentifier = tag.getFloorIdentifier();
        return new Point(buildingSelected.getIdentifier(), floorIdentifier, coordinate, cartesianCoordinate);
    }

    private void calculateRoute() {
        if (markerSelected.size() == 2) {
            clearPolyLine();
            CoordinateConverter coordinateConverter = new CoordinateConverter(buildingSelected.getDimensions(), buildingSelected.getCenter(), buildingSelected.getRotation());
            Point origin = createPoint(markerSelected.get(0), coordinateConverter);
            Point destination = createPoint(markerSelected.get(1), coordinateConverter);
            DirectionsRequest directionsRequest = new DirectionsRequest.Builder()
                    .from(origin, Angle.EMPTY)
                    .to(destination)
                    .build();
            SitumSdk.directionsManager().requestDirections(directionsRequest, new Handler<Route>() {
                @Override
                public void onSuccess(Route route) {
                    drawRoute(route);
                    centerCamera(route);

                }

                @Override
                public void onFailure(Error error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Timber.w("You need to select two points to receive a route");
        }
    }

    private void centerCamera(Route route) {
        Coordinate from = route.getFrom().getCoordinate();
        Coordinate to = route.getTo().getCoordinate();

        LatLngBounds.Builder builder = new LatLngBounds.Builder()
                .include(new LatLng(from.getLatitude(), from.getLongitude()))
                .include(new LatLng(to.getLatitude(), to.getLongitude()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    private void drawRoute(Route route) {
        for (RouteSegment segment : route.getSegments()) {
            //For each segment you must draw a polyline
            //Add an if to filter and draw only the current selected floor
            List<LatLng> latLngs = new ArrayList<>();
            for (Point point : segment.getPoints()) {
                latLngs.add(new LatLng(point.getCoordinate().getLatitude(), point.getCoordinate().getLongitude()));
            }

            PolylineOptions polyLineOptions = new PolylineOptions()
                    .color(Color.BLUE)
                    .width(4f)
                    .addAll(latLngs);
            polylines.add(mMap.addPolyline(polyLineOptions));

        }
    }

    public interface OnCallbackBuildings {
        void onFinish();   //method, which can have parameters
    }
}