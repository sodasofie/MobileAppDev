package com.example.q;
import java.util.Date;

public class HistoryItem {
    private String link;
    private String scannedDate;

    public HistoryItem(String link, String scannedDate) {
        this.link = link;
        this.scannedDate = scannedDate;
    }

    public String getLink() {
        return link;
    }

    public String getScannedDate() {
        return scannedDate;
    }

    // Method to save data in a format suitable for SharedPreferences
    public String toStorageString() {
        return link + "|" + scannedDate;
    }

    // Method to create an object from a stored string
    public static HistoryItem fromStorageString(String storageString) {
        String[] parts = storageString.split("\\|");
        if (parts.length == 2) {
            return new HistoryItem(parts[0], parts[1]);
        }
        return null; // Handle errors as needed
    }
}



