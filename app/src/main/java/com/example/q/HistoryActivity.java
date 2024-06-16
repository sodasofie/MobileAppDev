package com.example.q;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
    private Set<Integer> selectedItems;

    private static final String PREF_NAME = "scan_history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.history_list_view);
        selectedItems = new HashSet<>();

        scanHistory = getScanHistory();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scanHistory);
        historyListView.setAdapter(adapter);

        historyListView.setOnItemLongClickListener((parent, view, position, id) -> {
            toggleSelection(position);
            return true;
        });

        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = scanHistory.get(position);
            copyToClipboard(selectedText);
        });

        Button clearSelectedButton = findViewById(R.id.clear_selected_button);
        clearSelectedButton.setOnClickListener(v -> clearSelectedHistory());

        Button clearAllButton = findViewById(R.id.clear_all_button);
        clearAllButton.setOnClickListener(v -> clearAllHistory());
    }

    private ArrayList<String> getScanHistory() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("history", new HashSet<>());
        return new ArrayList<>(set);
    }

    private void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
            historyListView.getChildAt(position).setBackgroundColor(Color.TRANSPARENT);
        } else {
            selectedItems.add(position);
            historyListView.getChildAt(position).setBackgroundColor(Color.LTGRAY);
        }
    }

    private void clearSelectedHistory() {
        ArrayList<String> newScanHistory = new ArrayList<>(scanHistory);

        for (int position : selectedItems) {
            newScanHistory.remove(position);
        }

        saveScanHistory(newScanHistory);
        scanHistory.clear();
        scanHistory.addAll(newScanHistory);
        adapter.notifyDataSetChanged();
        selectedItems.clear();
    }

    private void clearAllHistory() {
        scanHistory.clear();
        saveScanHistory(scanHistory);
        adapter.notifyDataSetChanged();
        selectedItems.clear();
    }

    private void saveScanHistory(ArrayList<String> history) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> set = new HashSet<>(history);
        editor.putStringSet("history", set);
        editor.apply();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Скопійовано в буфер обміну", Toast.LENGTH_SHORT).show();
    }
}
