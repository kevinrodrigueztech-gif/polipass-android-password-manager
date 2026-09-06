package com.redsytem.passwordapp.OpcionesPassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.redsytem.passwordapp.BaseDeDatos.BDHelper;
import com.redsytem.passwordapp.MainActivity;
import com.redsytem.passwordapp.R;

import java.security.SecureRandom;

import android.widget.TextView;

public class Agregar_Actualizar_Registro extends AppCompatActivity {
    private static final String CHAR_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL_CHAR = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/";
    EditText EtTitulo, EtCuenta, EtNombreUsuario, EtPassword, EtSitioWeb, EtNota;
    String id, titulo, cuenta, nombre_usuario, password, sitio_web, nota, tiempo_registro, tiempo_actualizacion;
    private CheckBox checkboxLetters, checkboxNumbers, checkboxSpecialCharacters;
    private SeekBar seekBarLength;
    private TextView textViewLength;
    private boolean MODO_EDICION = false;
    private BDHelper bdHelper;
    private TextView passwordStrengthText;
    private LottieAnimationView lottieView;
    private ProgressBar passwordStrengthBar;
    private EditText passwordInput;
    private boolean isUpdatingText = false;

    private static final long CLIPBOARD_CLEAR_DELAY_MS = 2 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_agregar_password);
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        lottieView = findViewById(R.id.lottieView);
        passwordInput = findViewById(R.id.EtPassword);
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        passwordStrengthText = findViewById(R.id.passwordStrengthText);
        checkboxLetters = findViewById(R.id.checkbox_letters);
        checkboxNumbers = findViewById(R.id.checkbox_numbers);
        checkboxSpecialCharacters = findViewById(R.id.checkbox_special_characters);
        seekBarLength = findViewById(R.id.seekbar_length);
        textViewLength = findViewById(R.id.textview_length);

        seekBarLength.setMax(99);
        seekBarLength.setProgress(12);
        textViewLength.setText("Longitud de la Contraseña: 12");
        seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewLength.setText("Longitud de la Contraseña: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        lottieView.setOnClickListener(v -> {
            lottieView.playAnimation();
            int length = seekBarLength.getProgress(); // Obtener longitud del SeekBar
            String password = generateSecurePassword(length); // Generar contraseña con la longitud del SeekBar
            EtPassword.setText(password); // Mostrar la contraseña en el EditText
            Toast.makeText(Agregar_Actualizar_Registro.this, "Contraseña generada: " + password, Toast.LENGTH_SHORT).show();
        });

        checkboxLetters.setChecked(true);
        checkboxNumbers.setChecked(true);
        checkboxSpecialCharacters.setChecked(true);

        passwordInput.addTextChangedListener(new TextWatcher() {
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



        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    updatePasswordStrength(s.toString());
                } catch (Exception e) {
                    Log.e("Agregar_Actualizar_Registro", "Error al actualizar la fuerza de la contraseña", e);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        EtTitulo = findViewById(R.id.EtTitulo);
        EtCuenta = findViewById(R.id.EtCuenta);
        EtNombreUsuario = findViewById(R.id.EtNombreUsuario);
        EtSitioWeb = findViewById(R.id.EtSitioWeb);
        EtNota = findViewById(R.id.EtNota);

        setupContextMenu(EtTitulo);
        setupContextMenu(EtCuenta);
        setupContextMenu(EtNombreUsuario);
        setupContextMenu(passwordInput);
        setupContextMenu(EtSitioWeb);
        setupContextMenu(EtNota);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("");
        InicializarVariables();
        ObtenerInformacion();

    }

    private String generateSecurePassword(int length) {
        StringBuilder passwordChars = new StringBuilder();
        if (checkboxLetters.isChecked()) {
            passwordChars.append(CHAR_LOWERCASE).append(CHAR_UPPERCASE);
        }
        if (checkboxNumbers.isChecked()) {
            passwordChars.append(DIGIT);
        }
        if (checkboxSpecialCharacters.isChecked()) {
            passwordChars.append(SPECIAL_CHAR);
        }

        if (passwordChars.length() == 0) {
            return "";
        }

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(passwordChars.length());
            password.append(passwordChars.charAt(index));
        }

        return password.toString();
    }

    private void setupContextMenu(final EditText editText) {
        editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_contextual, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.menu_paste) {
                    pasteText(editText);
                    mode.finish();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private void pasteText(EditText editText) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item clipItem = clipboard.getPrimaryClip().getItemAt(0);
            if (clipItem != null) {
                editText.getText().insert(editText.getSelectionStart(), clipItem.getText());
            }
        }
    }

    private void pastePlainText(EditText editText) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item clipItem = clipboard.getPrimaryClip().getItemAt(0);
            if (clipItem != null && clipItem.getText() != null) {
                String plainText = clipItem.getText().toString().replaceAll("\\n", " ");
                editText.getText().insert(editText.getSelectionStart(), plainText);
            }
        }
    }

    private void cutText(EditText editText) {
        String selectedText = editText.getText().toString().substring(editText.getSelectionStart(), editText.getSelectionEnd());
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", selectedText);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
        editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
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
        passwordInput.setText(spannable);

        // Mantener el cursor al final del texto
        passwordInput.setSelection(s.length());
        isUpdatingText = false;
    }

    private void updatePasswordStrength(String password) {
        int length = password.length();
        try {
            if (length < 8) {
                passwordStrengthBar.setProgress(33);
                passwordStrengthBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.red));
                passwordStrengthText.setText("Seguridad baja");
                //passwordStrengthText.setTextColor(Color.RED);
            } else if (length >= 8 && length <= 11) {
                passwordStrengthBar.setProgress(66);
                passwordStrengthBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.yellow));
                passwordStrengthText.setText("Seguridad media");
                //passwordStrengthText.setTextColor(Color.YELLOW);
            } else if (length >= 12) {
                passwordStrengthBar.setProgress(100);
                passwordStrengthBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.green));
                passwordStrengthText.setText("Seguridad alta");
                //passwordStrengthText.setTextColor(Color.GREEN);
            }
        } catch (Exception e) {
            Log.e("barra y texto", "Error en updatePasswordStrength", e);
        }
    }

    private void InicializarVariables(){
        EtTitulo = findViewById(R.id.EtTitulo);
        EtCuenta = findViewById(R.id.EtCuenta);
        EtNombreUsuario = findViewById(R.id.EtNombreUsuario);
        EtPassword = findViewById(R.id.EtPassword);
        EtSitioWeb = findViewById(R.id.EtSitioWeb);
        EtNota = findViewById(R.id.EtNota);

        bdHelper = new BDHelper(this);
    }

    private void ObtenerInformacion(){
        Intent intent = getIntent();
        MODO_EDICION = intent.getBooleanExtra("MODO_EDICION", false);

        if (MODO_EDICION){
            //Verdadero
            id = intent.getStringExtra("ID");
            titulo = intent.getStringExtra("TITULO");
            cuenta = intent.getStringExtra("CUENTA");
            nombre_usuario = intent.getStringExtra("NOMBRE_USUARIO");
            password = intent.getStringExtra("PASSWORD");
            sitio_web = intent.getStringExtra("SITIO_WEB");
            nota = intent.getStringExtra("NOTA");
            tiempo_registro = intent.getStringExtra("T_REGISTRO");
            tiempo_actualizacion = intent.getStringExtra("T_ACTUALIZACION");

            /*Seteamos la información recuperada en las vistas*/
            EtTitulo.setText(titulo);
            EtCuenta.setText(cuenta);
            EtNombreUsuario.setText(nombre_usuario);
            EtPassword.setText(password);
            EtSitioWeb.setText(sitio_web);
            EtNota.setText(nota);


        }
        else {
            //Falso, se agrega un nuevo registro
        }
    }

    private void Agregar_Actualizar_R() throws Exception {
        /*Obtener datos de entrada*/
        titulo = EtTitulo.getText().toString().trim();
        cuenta = EtCuenta.getText().toString().trim();
        nombre_usuario = EtNombreUsuario.getText().toString().trim();
        password = EtPassword.getText().toString().trim();
        sitio_web = EtSitioWeb.getText().toString().trim();
        nota = EtNota.getText().toString().trim();

        if (MODO_EDICION){
            //Actualizar el registro
            /*Tiempo del dispositivo*/
            String tiempo_actual = ""+ System.currentTimeMillis();
            bdHelper.actualizarRegistro(
                    ""+ id,
                    ""+ titulo,
                    ""+ cuenta,
                    ""+ nombre_usuario,
                    ""+ password,
                    ""+ sitio_web,
                    ""+ nota,
                    ""+ tiempo_registro,
                    ""+ tiempo_actual
            );

            Toast.makeText(this, "Actualizado con éxito", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Agregar_Actualizar_Registro.this, MainActivity.class));
            finish();

        }

        else {
            //Agregar un nuev registro
            if (!titulo.equals("")){
                /*Obtener el tiempo del dispositivo*/
                String tiempo = ""+System.currentTimeMillis();
                long id = bdHelper.insertarRegistro(
                        "" + titulo,
                        "" + cuenta,
                        "" + nombre_usuario,
                        ""+ password,
                        ""+ sitio_web,
                        ""+ nota,
                        "" + tiempo,
                        "" + tiempo
                );

                Toast.makeText(this, "Se ha guardado con éxito: ", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Agregar_Actualizar_Registro.this, MainActivity.class));
                finish();
            }
            else {
                EtTitulo.setError("Campo obligatorio");
                EtTitulo.setFocusable(true);
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_guardar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.Guardar_Password){
            try {
                Agregar_Actualizar_R();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return super.onOptionsItemSelected(item);
    }

}