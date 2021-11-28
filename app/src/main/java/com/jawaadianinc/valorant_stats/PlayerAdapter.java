package com.jawaadianinc.valorant_stats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class PlayerAdapter extends BaseAdapter {
    private final Context context;
    private final String[] numbersInWords;
    private final int[] numberImage;
    private LayoutInflater layoutInflater;
    private ImageView imageView;
    private TextView textView;

    public PlayerAdapter(Context c, String[] numbersInWords, int[] numberImage) {
        context = c;
        this.numberImage = numberImage;
        this.numbersInWords = numbersInWords;
    }

    @Override
    public int getCount() {
        return numbersInWords.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (layoutInflater == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.player_row, null);
        }
        imageView = convertView.findViewById(R.id.grid_item_image);
        textView = convertView.findViewById(R.id.grid_item_title);
        imageView.setImageResource(numberImage[position]);
        textView.setText(numbersInWords[position]);
        return convertView;
    }
}