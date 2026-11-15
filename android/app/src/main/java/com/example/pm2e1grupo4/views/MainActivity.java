package com.example.pm2e1grupo4.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.pm2e1grupo4.R;
import com.example.pm2e1grupo4.network.ApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etNombre, etTelefono, etLatitud, etLongitud;
    private TextView tvRutaVideo;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private File videoFile;

    private final ActivityResultLauncher<Intent> videoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    tvRutaVideo.setText("Video grabado: " + videoFile.getName());
                } else {
                    Toast.makeText(this, "No se ha grabado el video", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        tvRutaVideo = findViewById(R.id.tvRutaVideo);
        progressBar = findViewById(R.id.progressBar);
        Button btnGrabarVideo = findViewById(R.id.btnGrabarVideo);
        Button btnSubir = findViewById(R.id.btnSubir);
        Button btnIrLista = findViewById(R.id.btnIrLista);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGrabarVideo.setOnClickListener(v -> grabarVideo());
        btnSubir.setOnClickListener(v -> subirContacto());
        btnIrLista.setOnClickListener(v -> startActivity(new Intent(this, ListaContactosActivity.class)));

        solicitarPermisos();
    }

    private void solicitarPermisos() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            obtenerUbicacion();
        }
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    etLatitud.setText(String.valueOf(location.getLatitude()));
                    etLongitud.setText(String.valueOf(location.getLongitude()));
                } else {
                    Toast.makeText(this, "GPS no esta activo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void grabarVideo() {
        try {
            videoFile = File.createTempFile("video_", ".mp4", getExternalCacheDir());
            Uri videoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", videoFile);
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 20);
            videoLauncher.launch(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void subirContacto() {
        String nombre = etNombre.getText().toString();
        String telefono = etTelefono.getText().toString();
        String latitud = etLatitud.getText().toString();
        String longitud = etLongitud.getText().toString();

        if (nombre.isEmpty() || telefono.isEmpty() || latitud.isEmpty() || longitud.isEmpty() || videoFile == null) {
            Toast.makeText(this, "Complete todos los campos y grabe un video", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Nombre", nombre)
                .addFormDataPart("Telefono", telefono)
                .addFormDataPart("Latitud", latitud)
                .addFormDataPart("Longitud", longitud)
                .addFormDataPart("Archivo", videoFile.getName(),
                        RequestBody.create(videoFile, MediaType.parse("video/mp4")))
                .build();

        Request request = new Request.Builder()
                .url(ApiClient.getBaseUrl() + "/api/contactos")
                .post(requestBody)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(MainActivity.this, "Sin conexion con la API. Intente de nuevo.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Contacto subido", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al subir: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etTelefono.setText("");
        tvRutaVideo.setText("Video no grabado");
        videoFile = null;
        obtenerUbicacion();
    }
}