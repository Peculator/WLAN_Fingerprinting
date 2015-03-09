package de.peculator.wlan_fingerprinting;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by sven on 02.03.15.
 */
public class MyScanResults {
    public int placeID;
    public List<ScanResult> results;

    public MyScanResults(int placeID, List<ScanResult> results) {
        this.placeID = placeID;
        this.results = results;
    }

    public int getPlaceID() {
        return placeID;
    }

    public void setPlaceID(int placeID) {
        this.placeID = placeID;
    }

    public List<ScanResult> getResults() {
        return results;
    }

    public void setResults(List<ScanResult> results) {
        this.results = results;
    }
}
