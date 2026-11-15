package com.example.pm2e1grupo4.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.pm2e1grupo4.R;
import com.example.pm2e1grupo4.models.Contacto;
import com.example.pm2e1grupo4.network.ApiClient;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class DetalleContactoActivity extends FragmentActivity {

    private MapView mapView;
    private Contacto contacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getApplicationContext().getPackageName());

        setContentView(R.layout.activity_detalle_contacto);

        contacto = (Contacto) getIntent().getSerializableExtra("contacto");

        TextView tvNombre = findViewById(R.id.tvDetalleNombre);
        TextView tvTelefono = findViewById(R.id.tvDetalleTelefono);
        VideoView videoView = findViewById(R.id.videoView);
        Button btnActualizar = findViewById(R.id.btnActualizar);
        Button btnEliminar = findViewById(R.id.btnEliminar);
        Button btnReproducir = findViewById(R.id.btnReproducir);
        mapView = findViewById(R.id.map);

        mapView.setMultiTouchControls(true);

        if (contacto != null) {
            tvNombre.setText(contacto.getNombre());
            tvTelefono.setText(contacto.getTelefono());

            String urlVideo = ApiClient.getBaseUrl() + contacto.getUrlArchivo();
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse(urlVideo));

            mostrarMapa();

            btnActualizar.setOnClickListener(v -> {
                Intent intent = new Intent(DetalleContactoActivity.this, EditarContactoActivity.class);
                intent.putExtra("contacto", contacto);
                startActivity(intent);
            });

            btnReproducir.setOnClickListener(v -> {
                Intent intent = new Intent(DetalleContactoActivity.this, ReproductorActivity.class);
                intent.putExtra("urlVideo", urlVideo);
                startActivity(intent);
            });

            btnEliminar.setOnClickListener(v -> eliminarContacto());
        }
    }

    private void mostrarMapa() {
        String[] urls = {"https://a.tile.opentopomap.org/", "https://b.tile.opentopomap.org/", "https://c.tile.opentopomap.org/"};
        XYTileSource topoSource = new XYTileSource("OpenTopoMap", 0, 17, 256, ".png", urls, "© OpenStreetMap contributors, SRTM | Map style: © OpenTopoMap");
        mapView.setTileSource(topoSource);

        double lat = contacto.getLatitud();
        double lon = contacto.getLongitud();

        GeoPoint punto = new GeoPoint(lat, lon);

        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(punto);

        Marker marcador = new Marker(mapView);
        marcador.setPosition(punto);
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcador.setTitle(contacto.getNombre());

        mapView.getOverlays().add(marcador);
        mapView.invalidate();
    }

    private void eliminarContacto() {
        Request request = new Request.Builder()
                .url(ApiClient.getBaseUrl() + "/api/contactos/" + contacto.getId())
                .delete()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(DetalleContactoActivity.this, "Error de conexion", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful() || response.code() == 204) {
                        Toast.makeText(DetalleContactoActivity.this, "Eliminado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(DetalleContactoActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}