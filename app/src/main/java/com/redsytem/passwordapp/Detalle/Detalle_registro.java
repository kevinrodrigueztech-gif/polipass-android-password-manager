package com.redsytem.passwordapp.Detalle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.redsytem.passwordapp.BaseDeDatos.BDHelper;
import com.redsytem.passwordapp.BaseDeDatos.Constants;
import com.redsytem.passwordapp.Encriptacion.Encrypt;
import com.redsytem.passwordapp.OpcionesPassword.Agregar_Actualizar_Registro;
import com.redsytem.passwordapp.R;

import java.util.Calendar;
import java.util.Locale;


public class Detalle_registro extends AppCompatActivity {

    TextView D_Titulo, D_Cuenta, D_NombreUsuario, D_SitioWeb, D_Nota, D_Tiempo_registro, D_Tiempo_actualizacion;
    EditText D_Password;
    ImageButton Im_Ir_Web;
    String id_registro;

    BDHelper helper;
    private boolean isUpdatingText = false;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_registro);

        D_Password = findViewById(R.id.D_Password);

        // Agregar OnLongClickListener para copiar texto
        setOnLongClickListener(D_Password);

        D_Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingText) {
                    applyColorToText(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        ActionBar actionBar = getSupportActionBar();

        Button btnCopyUsuario = findViewById(R.id.Btn_Copy_Usuario);

        Button btnCopyPassword = findViewById(R.id.Btn_Copy_Password);

        D_Titulo = findViewById(R.id.D_Titulo);
        D_Cuenta = findViewById(R.id.D_Cuenta);
        D_NombreUsuario = findViewById(R.id.D_NombreUsuario);
        D_SitioWeb = findViewById(R.id.D_SitioWeb);
        D_Nota = findViewById(R.id.D_Nota);

        setOnLongClickListener(D_Titulo);
        setOnLongClickListener(D_Cuenta);
        setOnLongClickListener(D_NombreUsuario);
        setOnLongClickListener(D_SitioWeb);
        setOnLongClickListener(D_Nota);

        btnCopyUsuario.setOnClickListener(v -> {
            String usuario = D_NombreUsuario.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("usuario", usuario);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(Detalle_registro.this, "Nombre De Usuario Copiado Al Portapapeles", Toast.LENGTH_SHORT).show();

            // Programar la tarea para borrar la contraseña del portapapeles después de 2 minutos
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Aquí se ejecuta la acción de borrar del portapapeles
                    clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
                    Toast.makeText(Detalle_registro.this, "Contraseña borrada del portapapeles", Toast.LENGTH_SHORT).show();
                }
            }, 2 * 60 * 1000); // 2 minutos en milisegundos
        });

        btnCopyPassword.setOnClickListener(v -> {
            String password = D_Password.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", password);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(Detalle_registro.this, "Contraseña copiada al portapapeles", Toast.LENGTH_SHORT).show();

            // Programar la tarea para borrar la contraseña del portapapeles después de 2 minutos
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Aquí se ejecuta la acción de borrar del portapapeles
                    clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
                    Toast.makeText(Detalle_registro.this, "Contraseña borrada del portapapeles", Toast.LENGTH_SHORT).show();
                }
            }, 2 * 60 * 1000); // 2 minutos en milisegundos
        });


        Intent intent = getIntent();
        id_registro = intent.getStringExtra("Id_registro");
        Toast.makeText(this, "Id del registro: " + id_registro, Toast.LENGTH_SHORT).show();

        helper = new BDHelper(this);

        InicializarVariables();
        try {
            MostrarInformacionRegistro();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String titulo_registro = D_Titulo.getText().toString();
        assert actionBar != null;
        actionBar.setTitle(titulo_registro);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Im_Ir_Web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url_pagina_web = D_SitioWeb.getText().toString().trim();
                if (!url_pagina_web.equals("")) {
                    abrirPaginaWeb(url_pagina_web);
                } else {
                    Toast.makeText(Detalle_registro.this, "No existe una url", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void copyTextToClipboard() {
        String textToCopy = D_Password.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Password", textToCopy);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show();
    }

    private void applyColorToText(Editable s) {
        if (s == null || s.length() == 0) return;

        SpannableStringBuilder spannable = new SpannableStringBuilder(s.toString());

        // Definir colores
        int blackColor = Color.parseColor("#A57C00");
        int darkBlueColor = Color.parseColor("#800040"); // Azul oscuro
        int darkPurpleColor = Color.parseColor("#006400"); // Morado oscuro

        // Aplicar color a cada tipo de carácter
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                spannable.setSpan(new ForegroundColorSpan(blackColor), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (Character.isDigit(c)) {
                spannable.setSpan(new ForegroundColorSpan(darkPurpleColor), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannable.setSpan(new ForegroundColorSpan(darkBlueColor), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        isUpdatingText = true;
        // Establecer el texto coloreado en el EditText
        D_Password.setText(spannable);

        // Mantener el cursor al final del texto
        D_Password.setSelection(s.length());
        isUpdatingText = false;
    }

    private void setOnLongClickListener(TextView textView) {
        textView.setOnLongClickListener(v -> {
            String textToCopy = textView.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", textToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(Detalle_registro.this, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setOnLongClickListener(EditText editText) {
        editText.setOnLongClickListener(v -> {
            String textToCopy = editText.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", textToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(Detalle_registro.this, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void InicializarVariables(){
        D_Titulo = findViewById(R.id.D_Titulo);
        D_Cuenta = findViewById(R.id.D_Cuenta);
        D_NombreUsuario = findViewById(R.id.D_NombreUsuario);
        D_Password = findViewById(R.id.D_Password);
        D_SitioWeb = findViewById(R.id.D_SitioWeb);
        D_Nota = findViewById(R.id.D_Nota);
        D_Tiempo_registro = findViewById(R.id.D_Tiempo_registro);
        D_Tiempo_actualizacion = findViewById(R.id.D_Tiempo_actualizacion);

        Im_Ir_Web = findViewById(R.id.Im_Ir_Web);

        dialog = new Dialog(this);
    }

    private void MostrarInformacionRegistro() throws Exception {
        String consulta = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.C_ID + " =\"" + id_registro+"\"";

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(consulta, null);

        if (cursor.moveToFirst()){
            do {
                @SuppressLint("Range") String id = ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID));
                @SuppressLint("Range") String titulo = ""+cursor.getString(cursor.getColumnIndex(Constants.C_TITULO));
                @SuppressLint("Range") String cuenta = ""+cursor.getString(cursor.getColumnIndex(Constants.C_CUENTA));
                @SuppressLint("Range") String nombre_usuario = ""+cursor.getString(cursor.getColumnIndex(Constants.C_NOMBRE_USUARIO));
                @SuppressLint("Range") String password = ""+ Encrypt.encrypt(cursor.getString(cursor.getColumnIndex(Constants.C_PASSWORD)), false);
                @SuppressLint("Range") String sitio_web = ""+cursor.getString(cursor.getColumnIndex(Constants.C_SITIO_WEB));
                @SuppressLint("Range") String nota = ""+cursor.getString(cursor.getColumnIndex(Constants.C_NOTA));
                @SuppressLint("Range") String t_registro = ""+cursor.getString(cursor.getColumnIndex(Constants.C_TIEMPO_REGISTRO));
                @SuppressLint("Range") String t_actualizacion = ""+cursor.getString(cursor.getColumnIndex(Constants.C_TIEMPO_ACTUALIZACION));

                /*Convertir tiempo a dia/mes/anio 12:00 am-pm*/

                /*Tiempo registro*/
                Calendar calendar_t_r = Calendar.getInstance(Locale.getDefault());
                calendar_t_r.setTimeInMillis(Long.parseLong(t_registro));
                String tiempo_registro = ""+ DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar_t_r);

                /*Tiempo actualización*/
                Calendar calendar_t_a = Calendar.getInstance(Locale.getDefault());
                calendar_t_a.setTimeInMillis(Long.parseLong(t_actualizacion));
                String tiempo_actualizacion = ""+ DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar_t_a);


                /*Setear la información en las vistas*/
                D_Titulo.setText(titulo);
                D_Cuenta.setText(cuenta);
                D_NombreUsuario.setText(nombre_usuario);
                D_Password.setText(password);
                D_Password.setEnabled(false);
                D_Password.setBackgroundColor(Color.TRANSPARENT);
                D_Password.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                D_SitioWeb.setText(sitio_web);
                D_Nota.setText(nota);
                D_Tiempo_registro.setText(tiempo_registro);
                D_Tiempo_actualizacion.setText(tiempo_actualizacion);


            }while (cursor.moveToNext());

        }

        db.close();

    }

    private void abrirPaginaWeb(String url_pagina_web) {
        Intent navegar = new Intent(Intent.ACTION_VIEW, Uri.parse("https://"+url_pagina_web));
        startActivity(navegar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}