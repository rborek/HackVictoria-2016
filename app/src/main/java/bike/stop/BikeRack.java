package bike.stop;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BikeRack {
    public final double longitude;
    public final double latitude;

    public BikeRack(double longitude, double latitude ){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static ArrayList<BikeRack> getBikeRacks(){
        ArrayList<BikeRack> bikeRacks = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader("BikeRacks.csv"));
            String line;
            line = in.readLine();
            while ((line = in.readLine()) != null) {
                String[] ar = line.split(",");
                bikeRacks.add(new BikeRack(Double.parseDouble(ar[3]), Double.parseDouble(ar[4])));
            }
            in.close();
        } catch (IOException e) {
            System.out.println("File Read Error");
        }
        return bikeRacks;
    }
}
