package bike.stop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private List<BikeRack> bikeRacks;
    private LatLng myCoords;
    private LocationManager locationManager;
    private static double nextTo = 100;

    private void initBikeRackMarkers() {
        ArrayList<Double> closestDists = new ArrayList<>();
        ArrayList<BikeRack> closestRacks = new ArrayList<>();
        bikeRacks = getBikeRacks();
        for (BikeRack rack : bikeRacks) {
            double curDist = SphericalUtil.computeDistanceBetween(myCoords, new LatLng(rack.latitude, rack.longitude));
            if (closestDists.size() < 10) {
                closestRacks.add(rack);
                closestDists.add(curDist);
            } else {
                for (int i = 0; i < closestDists.size(); i++) {
                    if (curDist < closestDists.get(i)) {
                        closestDists.remove(i);
                        closestRacks.remove(i);
                        closestDists.add(curDist);
                        closestRacks.add(rack);
                        break;
                    }
                }
            }
        }
        for (BikeRack rack : closestRacks) {
            int resource = 0;
            if (rack.number == 1) {
                resource = R.drawable.bike_1;
            } else if (rack.number == 2) {
                resource = R.drawable.bike_2;
            } else {
                resource = R.drawable.bike_3;
            }

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(resource);
            mMap.addMarker(new MarkerOptions().position(new LatLng(rack.latitude, rack.longitude)).icon(icon));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, 1,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
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
        enableMyLocation();
        // Add a marker in Sydney
        Criteria criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Getting Current Location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        LatLng latLng = new LatLng(locationManager.getLastKnownLocation(provider).getLatitude(), locationManager.getLastKnownLocation(provider).getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        myCoords = latLng;
        initBikeRackMarkers();
        Button button = new Button(this);
        button.setText("Find nearest bike rack");
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        addContentView(button, params);
        button.getBackground().setColorFilter(Color.argb(255, 220, 0, 0), PorterDuff.Mode.ADD);
        button.setOnClickListener(new LocateNearestBikeRackButton(mMap, bikeRacks, this));
    }


    public ArrayList<BikeRack> getBikeRacks() {
        ArrayList<BikeRack> bikeRacks = new ArrayList<>();
        ArrayList<LatLng> closeBikeRacks = new ArrayList<>();
        BikeRack bikeRack;
        LatLng rack;
        double averageLatitude = 0;
        double averageLongitude = 0;
        BikeRack averageRack;
        double distance;
        try {
            InputStream stream = getResources().openRawResource(R.raw.bikeracks);
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String line;
            line = in.readLine();
            while ((line = in.readLine()) != null) {
                String[] ar = line.split(",");
                rack = new LatLng(Double.parseDouble(ar[4]), Double.parseDouble(ar[3]));
                if (closeBikeRacks.isEmpty()) {
                    closeBikeRacks.add(rack);
                } else {
                    distance = SphericalUtil.computeDistanceBetween(closeBikeRacks.get(closeBikeRacks.size() - 1), rack);
                    if (distance > nextTo) { // if they are not close by
                        for (LatLng rack1 : closeBikeRacks) { // get the average value
                            averageLatitude += rack1.latitude;
                            averageLongitude += rack1.longitude;
                        }
                        averageRack = new BikeRack(averageLatitude / closeBikeRacks.size(), averageLongitude / closeBikeRacks.size(), closeBikeRacks.size());
                        bikeRacks.add(averageRack); // add the average value of all the close by bikeRacks and the number of them
                        averageLatitude = 0;
                        averageLongitude = 0;
                        closeBikeRacks.clear(); // clear closebyRack
                        closeBikeRacks.add(rack);
                    } else {
                        closeBikeRacks.add(rack);
                    }
                }
            }
            for (LatLng rack1 : closeBikeRacks) { // get the average value
                averageLatitude += rack1.latitude;
                averageLongitude += rack1.longitude;
            }
            averageRack = new BikeRack(averageLatitude / closeBikeRacks.size(), averageLongitude / closeBikeRacks.size(), closeBikeRacks.size());
            bikeRacks.add(averageRack); // add the average value of all the close by bikeRacks and the number of them
            in.close();

        } catch (IOException e) {
            System.out.println("File Read Error");
        }
        return bikeRacks;
    }

    public LatLng getMyCoords() {
        return myCoords;
    }


    @Override
    public void onLocationChanged(Location location) {
        myCoords = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
