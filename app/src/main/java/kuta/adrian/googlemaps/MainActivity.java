package kuta.adrian.googlemaps;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import static com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnMapClickListener, Spinner.OnItemSelectedListener, GoogleMap.OnInfoWindowClickListener, OnMapLongClickListener {

    private GoogleMap map;
    private SpinnerAdapter spinnerAdapter;
    private List<Marker> markerList = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();
    private LatLng previousLatLng = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerAdapter = new SpinnerAdapter(this, android.R.layout.simple_spinner_item, markerList);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMapLongClickListener(this);
        new AddMarkers(map).execute();
    }

    private void moveCamera(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        map.animateCamera(cameraUpdate, 1500, null);
    }

    private void centerCamera() throws NullPointerException {
        if (map == null)
            throw new NullPointerException();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markerList) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();
        int padding = 80; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cu, 1500, null);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        centerCamera();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        moveCamera(spinnerAdapter.getLatLang(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (previousLatLng == null) {
            previousLatLng = marker.getPosition();
        } else {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.add(previousLatLng);
            polylineOptions.add(marker.getPosition());
            polylines.add(map.addPolyline(polylineOptions));
            previousLatLng = null;
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        for (Polyline polyline : polylines)
            polyline.remove();
        polylines.clear();
    }

    private class AddMarkers extends AsyncTask<String, MarkerOptions, String> {

        private WeakReference<GoogleMap> googleMapWeakReference;
        private List<Marker> markers;

        private AddMarkers(GoogleMap googleMap) {
            googleMapWeakReference = new WeakReference<>(googleMap);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            markers = new ArrayList<>();
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream inputStream = getResources().openRawResource(R.raw.miasta);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    String miasto[] = line.split(" ");
                    String nazwa = miasto[0];
                    String[] geo = miasto[1].split(",");
                    double lat = Double.parseDouble(geo[0]);
                    double lng = Double.parseDouble(geo[1]);
                    LatLng coords = new LatLng(lat, lng);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(coords)
                            .title(nazwa);
                    publishProgress(markerOptions);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(MarkerOptions... values) {
            super.onProgressUpdate(values);
            if (googleMapWeakReference != null) {
                GoogleMap googleMap = googleMapWeakReference.get();
                if (googleMap != null) {
                    markers.add(googleMap.addMarker(values[0]));
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (markerList.size() != 0)
                markerList.clear();
            markerList.addAll(markers);
            markers.clear();
            markers = null;
            googleMapWeakReference.clear();
            googleMapWeakReference = null;
            spinnerAdapter.notifyDataSetChanged();
        }
    }
}
