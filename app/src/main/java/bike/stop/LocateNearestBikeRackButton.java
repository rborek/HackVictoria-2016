package bike.stop;

import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.List;

public class LocateNearestBikeRackButton implements View.OnClickListener {
    private MapActivity mapActivity;
    private GoogleMap map;
    private List<BikeRack> bikeRacks;

    public LocateNearestBikeRackButton(GoogleMap map, List<BikeRack> bikeRacks, MapActivity mapActivity) {
        this.map = map;
        this.bikeRacks = bikeRacks;
        this.mapActivity = mapActivity;
    }
    @Override
    public void onClick(View v) {
        LatLng nearestRack = null;
        double closestDistance = Double.MAX_VALUE;
        for (BikeRack rack : bikeRacks) {
            LatLng curRack = new LatLng(rack.latitude, rack.longitude);
            double curDist = SphericalUtil.computeDistanceBetween(mapActivity.getMyCoords(), curRack);
            if (curDist < closestDistance) {
                closestDistance = curDist;
                nearestRack = curRack;
            }
        }
        if (nearestRack != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(nearestRack, 18));
        }
    }
}
