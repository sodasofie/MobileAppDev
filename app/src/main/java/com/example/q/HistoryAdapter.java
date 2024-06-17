package com.example.q;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HistoryAdapter extends ArrayAdapter<HistoryItem> {

    private Context mContext;
    private ArrayList<HistoryItem> mHistoryList;
    private Set<Integer> selectedItems;

    public HistoryAdapter(Context context, ArrayList<HistoryItem> historyList) {
        super(context, 0, historyList);
        mContext = context;
        mHistoryList = historyList;
        selectedItems = new HashSet<>(); // Ініціалізуємо selectedItems
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        HistoryItem currentItem = mHistoryList.get(position);

        TextView linkTextView = listItem.findViewById(android.R.id.text1);
        linkTextView.setText(currentItem.getLink());

        TextView dateTextView = listItem.findViewById(android.R.id.text2);
        dateTextView.setText(currentItem.getScannedDate());

        return listItem;
    }

    // Метод для оновлення вибраних елементів
    public void updateSelectedItems(Set<Integer> items) {
        selectedItems = items;
        notifyDataSetChanged(); // Оновлюємо адаптер
    }
}
