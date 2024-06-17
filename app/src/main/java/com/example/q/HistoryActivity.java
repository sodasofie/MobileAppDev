package com.example.q;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class HistoryActivity extends AppCompatActivity {
    private Map<String, List<String>> scanHistoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false); // Сховати стандартний заголовок
        TextView historyTitleTextView = findViewById(R.id.history_title);
        historyTitleTextView.setText("ІСТОРІЯ СКАНУВАНЬ");

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());


        scanHistoryMap = getScanHistory();

            Button clearSelectedButton = findViewById(R.id.clear_selected_button);
            clearSelectedButton.setOnClickListener(v -> clearSelectedHistory());

            Button clearAllButton = findViewById(R.id.clear_all_button);
            clearAllButton.setOnClickListener(v -> clearAllHistory());

            displayHistory();
        }



        private Map<String, List<String>> getScanHistory() {
        SharedPreferences prefs = getSharedPreferences("scan_history", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        Map<String, List<String>> historyMap = new LinkedHashMap<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String date = entry.getKey();
            Set<String> set = (Set<String>) entry.getValue();
            List<String> links = new ArrayList<>(set);
            historyMap.put(date, links);
        }

        return historyMap;
    }

    private void displayHistory() {
        LinearLayout buttonsContainer = findViewById(R.id.buttons_container);
        buttonsContainer.removeAllViews();

        for (String date : scanHistoryMap.keySet()) {
            // Add date TextView
            TextView dateTextView = new TextView(this);
            dateTextView.setText(date);
            // Add styling for dateTextView as needed
            buttonsContainer.addView(dateTextView);

            // Add buttons with links for the date
            List<String> links = scanHistoryMap.get(date);
            if (links != null && !links.isEmpty()) {
                for (String link : links) {
                    Button button = createLinkButton(link);
                    if (selectedLinks.contains(link)) {
                        button.setBackgroundColor(Color.LTGRAY);  // Set background color for selected link
                    }
                    buttonsContainer.addView(button);
                }
            }
        }
    }



    private void refreshButtons() {
        displayHistory();
    }

    private void clearSelectedHistory() {
        for (List<String> links : scanHistoryMap.values()) {
            links.removeAll(selectedLinks);
        }

        selectedLinks.clear();
        saveScanHistory();

        Toast.makeText(this, "Вибрані посилання видалено", Toast.LENGTH_SHORT).show();

        refreshButtons();
    }


    private void clearAllHistory() {
        scanHistoryMap.clear();

        saveScanHistory();

        Toast.makeText(this, "Всю історію видалено", Toast.LENGTH_SHORT).show();

        refreshButtons();
    }

    private void saveScanHistory() {
        SharedPreferences prefs = getSharedPreferences("scan_history", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear();

        for (Map.Entry<String, List<String>> entry : scanHistoryMap.entrySet()) {
            Set<String> linksSet = new HashSet<>(entry.getValue());
            editor.putStringSet(entry.getKey(), linksSet);
        }

        editor.apply();
    }


    private Set<String> selectedLinks = new HashSet<>();

    private Button createLinkButton(String link) {
        Button button = new Button(this);
        button.setText(link);
        button.setOnClickListener(v -> {
            if (selectedLinks.contains(link)) {

                selectedLinks.remove(link);
                button.setBackgroundColor(Color.TRANSPARENT);
            } else {
                selectedLinks.add(link);
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.faderer_light_vio));  // Set background color for selection
            }
        });
        return button;
    }


    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }
}