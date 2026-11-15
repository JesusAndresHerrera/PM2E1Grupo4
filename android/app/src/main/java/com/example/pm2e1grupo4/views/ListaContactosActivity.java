package com.example.pm2e1grupo4.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pm2e1grupo4.R;
import com.example.pm2e1grupo4.models.Contacto;
import com.example.pm2e1grupo4.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ListaContactosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactoAdapter adapter;
    private List<Contacto> listaContactos = new ArrayList<>();
    private ProgressBar progressBar;
    private EditText etBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBarLista);
        etBuscar = findViewById(R.id.etBuscar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactoAdapter(listaContactos, contacto -> {
            Intent intent = new Intent(ListaContactosActivity.this, DetalleContactoActivity.class);
            intent.putExtra("contacto", contacto);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarContactos(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        obtenerContactos("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        obtenerContactos(etBuscar.getText().toString());
    }

    private void obtenerContactos(String buscar) {
        progressBar.setVisibility(View.VISIBLE);
        String url = ApiClient.getBaseUrl() + "/api/contactos?buscar=" + buscar;

        Request request = new Request.Builder().url(url).get().build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ListaContactosActivity.this, "Error de conexion", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Type listType = new TypeToken<List<Contacto>>(){}.getType();
                    List<Contacto> contactos = new Gson().fromJson(json, listType);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        listaContactos.clear();
                        listaContactos.addAll(contactos);
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }

    private void buscarContactos(String texto) {
        obtenerContactos(texto);
    }

    static class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ViewHolder> {
        private final List<Contacto> contactos;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(Contacto contacto);
        }

        ContactoAdapter(List<Contacto> contactos, OnItemClickListener listener) {
            this.contactos = contactos;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacto, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Contacto contacto = contactos.get(position);
            holder.tvNombre.setText(contacto.getNombre());
            holder.tvTelefono.setText(contacto.getTelefono());
            holder.itemView.setOnClickListener(v -> listener.onItemClick(contacto));
        }

        @Override
        public int getItemCount() {
            return contactos.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvTelefono;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombreItem);
                tvTelefono = itemView.findViewById(R.id.tvTelefonoItem);
            }
        }
    }
}