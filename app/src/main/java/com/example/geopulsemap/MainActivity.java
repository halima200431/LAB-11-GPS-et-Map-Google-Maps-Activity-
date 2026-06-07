package com.example.geopulsemap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 200;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Marker currentMarker;
    private LatLng lastKnownPosition;

    private TextView txtStatus;
    private TextView txtCoordinates;
    private TextView txtProvider;
    private AppCompatButton btnCenter; // Utilisation du type corrigé

    private final LocationListener gpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            updateUserPosition(location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            txtStatus.setText("Statut : localisation activée");
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            txtStatus.setText("Statut : localisation désactivée");
            showLocationSettingsDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        txtCoordinates = findViewById(R.id.txtCoordinates);
        txtProvider = findViewById(R.id.txtProvider);
        btnCenter = findViewById(R.id.btnCenter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnCenter.setOnClickListener(v -> {
            if (mMap != null && lastKnownPosition != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownPosition, 17f));
            } else {
                Toast.makeText(this, "Aucune position disponible pour le moment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        configureMapDesign();

        // Point de départ par défaut (Marrakech) avant le premier fix GPS
        LatLng defaultStart = new LatLng(31.6295, -7.9811);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultStart, 12f));

        if (hasLocationPermission()) {
            startLocationTracking();
        } else {
            requestLocationPermission();
        }
    }

    // Annotation ajoutée pour éviter les erreurs strictes de compilation sur les permissions
    @SuppressLint("MissingPermission")
    private void configureMapDesign() {
        if (mMap == null) return;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (hasLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }

    @SuppressLint("MissingPermission")
    private void startLocationTracking() {
        if (!hasLocationPermission()) return;

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            showLocationSettingsDialog();
            return;
        }

        txtStatus.setText("Statut : recherche de position...");

        // Écoute du réseau (rapide et efficace en intérieur)
        if (networkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, gpsListener);
        }

        // Écoute du module GPS (ultra précis en extérieur)
        if (gpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, gpsListener);
        }

        // Chargement instantané de la dernière coordonnée en cache pour éviter l'écran vide
        Location lastLocation = getBestLastKnownLocation();
        if (lastLocation != null) {
            updateUserPosition(lastLocation);
        }
    }

    @SuppressLint("MissingPermission")
    private Location getBestLastKnownLocation() {
        if (!hasLocationPermission()) return null;

        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        return (gpsLocation != null) ? gpsLocation : networkLocation;
    }

    private void updateUserPosition(Location location) {
        if (location == null || mMap == null) return;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng userPosition = new LatLng(latitude, longitude);
        lastKnownPosition = userPosition;

        txtStatus.setText("Statut : position détectée");
        txtCoordinates.setText("Latitude : " + latitude + " | Longitude : " + longitude);
        txtProvider.setText("Source : " + location.getProvider());

        // Logique de mise à jour ou de création d'un marqueur unique Rose/Magenta
        if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(userPosition)
                    .title("Ma position")
                    .snippet("Mise à jour en direct")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))); // Correction de la couleur ici
        } else {
            currentMarker.setPosition(userPosition);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 17f));
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Localisation désactivée")
                .setMessage("Pour utiliser GeoPulseMap, veuillez activer le GPS ou les services réseau.")
                .setCancelable(false)
                .setPositiveButton("Paramètres", (dialog, id) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Annuler", (dialog, id) -> {
                    dialog.cancel();
                    txtStatus.setText("Statut : localisation non activée");
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
                configureMapDesign();
                startLocationTracking();
            } else {
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_LONG).show();
                txtStatus.setText("Statut : permission refusée");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null && hasLocationPermission()) {
            startLocationTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Coupe le tracking pour économiser la batterie du téléphone
        if (locationManager != null) {
            locationManager.removeUpdates(gpsListener);
        }
    }
}