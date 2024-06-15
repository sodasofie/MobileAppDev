package com.example.q;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    }

    private ArrayList<String> getScanHistory() {
        SharedPreferences prefs = getSharedPreferences("scan_history", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("history", new HashSet<>());
        return new ArrayList<>(set);
    }
}
