package kuta.adrian.googlemaps;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * author:  Adrian Kuta
 * index:   204423
 * date:    18.04.15
 */
public class SpinnerAdapter extends ArrayAdapter {

    private List<Marker> markers;

    public SpinnerAdapter(Context context, int resource, List<Marker> markers) {
        super(context, resource, markers);
        this.markers = markers;
    }

    public LatLng getLatLang(int position){
        return markers.get(position).getPosition();
    }

    @Override
    public String getItem(int position) {
        return markers.get(position).getTitle();
    }

    @Override
    public int getCount() {
        return markers.size();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        textView.setTextSize(30);
        textView.setText(getItem(position));
        return textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getView(position, convertView, parent);
        textView.setTextSize(30);
        textView.setText(getItem(position));
        return textView;
    }
}
