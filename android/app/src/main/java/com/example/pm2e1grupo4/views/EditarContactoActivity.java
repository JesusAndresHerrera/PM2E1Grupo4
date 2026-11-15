package com.example.pm2e1grupo4.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pm2e1grupo4.R;
import com.example.pm2e1grupo4.models.Contacto;
import com.example.pm2e1grupo4.network.ApiClient;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

public class EditarContactoActivity extends AppCompatActivity {

    private EditText etNombre, etTelefono, etLatitud, etLongitud;
    private ProgressBar progressBar;
    private Contacto contacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_contacto);

        contacto = (Contacto) getIntent().getSerializableExtra("contacto");

        etNombre = findViewById(R.id.etEditNombre);
        etTelefono = findViewById(R.id.etEditTelefono);
        etLatitud = findViewById(R.id.etEditLatitud);
        etLongitud = findViewById(R.id.etEditLongitud);
        progressBar = findViewById(R.id.progressBarEditar);
        Button btnGuardar = findViewById(R.id.btnGuardarCambios);

        if (contacto != null) {
            etNombre.setText(contacto.getNombre());
            etTelefono.setText(contacto.getTelefono());
            etLatitud.setText(String.valueOf(contacto.getLatitud()));
            etLongitud.setText(String.valueOf(contacto.getLongitud()));
        }

        btnGuardar.setOnClickListener(v -> actualizarContacto());
    }

    private void actualizarContacto() {
        String nombre = etNombre.getText().toString();
        String telefono = etTelefono.getText().toString();

        if (nombre.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Nombre y telefono son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        String json = "{\"nombre\":\"" + nombre + "\",\"telefono\":\"" + telefono + "\",\"latitud\":" + contacto.getLatitud() + ",\"longitud\":" + contacto.getLongitud() + "}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(ApiClient.getBaseUrl() + "/api/contactos/" + contacto.getId())
                .put(body)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(EditarContactoActivity.this, "Error de conexion", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(EditarContactoActivity.this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditarContactoActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}