package com.example.q;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private ArrayList<String> scanHistory;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.history_list_view);

        // Отримуємо історію сканувань
        scanHistory = getScanHistory();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scanHistory);
        historyListView.setAdapter(adapter);

        // Очищення історії при кліку на кнопку
        Button clearSelectedButton = findViewById(R.id.clear_selected_button);
        clearSelectedButton.setOnClickListener(v -> clearSelectedHistory());

        // Очищення всієї історії при кліку на кнопку
        Button clearAllButton = findViewById(R.id.clear_all_button);
        clearAllButton.setOnClickListener(v -> clearAllHistory());

        // Копіювання вибраного елементу історії при кліку на нього
        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = scanHistory.get(position);
            copyToClipboard(selectedText);
        });
    }

    private ArrayList<String> getScanHistory() {
        SharedPreferences prefs = getSharedPreferences("scan_history", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("history", new HashSet<>());
        return new ArrayList<>(set);
    }

    private void clearSelectedHistory() {
        SharedPreferences prefs = getSharedPreferences("scan_history", MODE_PRIVATE);
        prefs.edit().remove("history").apply();
        scanHistory.clear();
        adapter.notifyDataSetChanged();
    }

    private void clearAllHistory() {
        SharedPreferences prefs = getSharedPreferences("scan_history", MODE_PRIVATE);
        prefs.edit().clear().apply();
        scanHistory.clear();
        adapter.notifyDataSetChanged();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
