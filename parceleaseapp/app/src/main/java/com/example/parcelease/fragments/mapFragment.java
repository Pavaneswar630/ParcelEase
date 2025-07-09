package com.example.parcelease.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.parcelease.R;
import java.util.List;

public class mapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> routePoints;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (routePoints != null) {
            drawRouteOnMap();
        }
    }

    public void updateRoute(List<LatLng> points) {
        this.routePoints = points;
        if (mMap != null) {
            getActivity().runOnUiThread(this::drawRouteOnMap);
        }
    }

    private void drawRouteOnMap() {
        if (mMap == null || routePoints == null || routePoints.isEmpty()) return;

        PolylineOptions polylineOptions = new PolylineOptions()
                .color(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .width(8)
                .addAll(routePoints);
        mMap.addPolyline(polylineOptions);

        // Move camera to first location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routePoints.get(0), 14));

        // Add drop location marker
        mMap.addMarker(new MarkerOptions().position(routePoints.get(routePoints.size() - 1))
                .title("Drop Location"));
    }
}
