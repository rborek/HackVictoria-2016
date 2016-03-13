package bike.stop;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BikeRack {
    public final double longitude;
    public final double latitude;

    public BikeRack(double longitude, double latitude ){
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
