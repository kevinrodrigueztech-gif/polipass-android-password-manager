package com.redsytem.passwordapp.Login_usuario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.redsytem.passwordapp.BaseDeDatos.BDHelper;
import com.redsytem.passwordapp.Fragmentos.F_Ajustes;
import com.redsytem.passwordapp.MainActivity;
import com.redsytem.passwordapp.R;
import com.redsytem.passwordapp.Seguridad.MasterPasswordStore;

public class Logeo_usuario extends AppCompatActivity {

    EditText EtPasswordU;
    Button BtnIngresar, BtnInicioSesionBiometrico;
    SharedPreferences sharedPreferences;
    ImageButton Ib_Aviso;
    BDHelper bdHelper;
    TextView Btn_Recuperar;
    Dialog dialog;

    private static final String SHARED_PREF = MasterPasswordStore.SHARED_PREF;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private int failedAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_logeo_usuario);
        InicializarVariables();

        FragmentManager fragmentManager = getSupportFragmentManager();
        F_Ajustes fragment = (F_Ajustes) fragmentManager.findFragmentById(R.id.f_ajustes);
        bdHelper = new BDHelper(this);
        dialog = new Dialog(Logeo_usuario.this);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean HabPregSecure = sharedPreferences.getBoolean("question_enabled", false);

        if (HabPregSecure) {
            Btn_Recuperar.setVisibility(View.VISIBLE);
        } else {
            Btn_Recuperar.setVisibility(View.GONE);
        }


        if (fragment == null) {
            fragment = new F_Ajustes();
            fragmentManager.beginTransaction()
                    .add(fragment, "F_Ajustes")
                    .commit();
        }

        Btn_Recuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Recuperacion();
            }
        });

        BtnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String S_password = EtPasswordU.getText().toString().trim();
                if (S_password.isEmpty()) {
                    Toast.makeText(Logeo_usuario.this, "Campo es obligatorio", Toast.LENGTH_SHORT).show();
                } else if (!MasterPasswordStore.verify(sharedPreferences, S_password)) {
                    handleFailedAttempt();
                } else {
                    loginSuccess();
                }
            }
        });

        biometricPrompt = new BiometricPrompt(Logeo_usuario.this, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(Logeo_usuario.this, "Error de autenticación: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                loginSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                handleFailedAttempt();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación biométrica")
                .setNegativeButtonText("Cancelar")
                .build();

        BtnInicioSesionBiometrico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
            }
        });

        Ib_Aviso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBiometricWarningDialog();
            }
        });
    }

    private void Dialog_Recuperacion() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cuadro_dialogo_preguntas, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(Logeo_usuario.this);
        builder.setView(dialogView);

        TextView TvPreguntaUno = dialogView.findViewById(R.id.TvPreguntaUno);
        TextView TvPreguntaDos = dialogView.findViewById(R.id.TvPreguntaDos);
        TextView TvPreguntaTres = dialogView.findViewById(R.id.TvPreguntaTres);
        EditText Et_Respuesta_Uno = dialogView.findViewById(R.id.Et_Respuesta_Uno);
        EditText Et_Respuesta_Dos = dialogView.findViewById(R.id.Et_Respuesta_Dos);
        EditText Et_Respuesta_Tres = dialogView.findViewById(R.id.Et_Respuesta_Tres);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Obtener y establecer las preguntas en los TextView correspondientes
        String PreguntaU = sharedPreferences.getString("PreguntaUno", "¿Nombre de tu mascota?");
        String PreguntaD = sharedPreferences.getString("PreguntaDos", "¿Comida favorita?");
        String PreguntaT = sharedPreferences.getString("PreguntaTres", "¿Tu lugar favorito?");

        TvPreguntaUno.setText(PreguntaU);
        TvPreguntaDos.setText(PreguntaD);
        TvPreguntaTres.setText(PreguntaT);

        // Obtener el estado de las preguntas seleccionadas
        boolean seleccionadoUno = sharedPreferences.getBoolean("seleccionadoUno", false);
        boolean seleccionadoDos = sharedPreferences.getBoolean("seleccionadoDos", false);
        boolean seleccionadoTres = sharedPreferences.getBoolean("seleccionadoTres", false);

        // Configurar visibilidad de acuerdo a la pregunta seleccionada
        if (seleccionadoUno) {
            TvPreguntaUno.setVisibility(View.VISIBLE);
            Et_Respuesta_Uno.setVisibility(View.VISIBLE);
            TvPreguntaDos.setVisibility(View.GONE);
            Et_Respuesta_Dos.setVisibility(View.GONE);
            TvPreguntaTres.setVisibility(View.GONE);
            Et_Respuesta_Tres.setVisibility(View.GONE);
        } else if (seleccionadoDos) {
            TvPreguntaUno.setVisibility(View.VISIBLE);
            Et_Respuesta_Uno.setVisibility(View.VISIBLE);
            TvPreguntaDos.setVisibility(View.VISIBLE);
            Et_Respuesta_Dos.setVisibility(View.VISIBLE);
            TvPreguntaTres.setVisibility(View.GONE);
            Et_Respuesta_Tres.setVisibility(View.GONE);
        } else if (seleccionadoTres) {
            TvPreguntaUno.setVisibility(View.VISIBLE);
            Et_Respuesta_Uno.setVisibility(View.VISIBLE);
            TvPreguntaDos.setVisibility(View.VISIBLE);
            Et_Respuesta_Dos.setVisibility(View.VISIBLE);
            TvPreguntaTres.setVisibility(View.VISIBLE);
            Et_Respuesta_Tres.setVisibility(View.VISIBLE);
        }

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String respuestaUno = Et_Respuesta_Uno.getText().toString().trim();
                String respuestaDos = Et_Respuesta_Dos.getText().toString().trim();
                String respuestaTres = Et_Respuesta_Tres.getText().toString().trim();

                boolean respuestasCorrectas = false;

                if (seleccionadoUno) {
                    String respuestaAlmacenadaUno = sharedPreferences.getString("RespuestaUno", "");
                    respuestasCorrectas = respuestaUno.equals(respuestaAlmacenadaUno);
                } else if (seleccionadoDos) {
                    String respuestaAlmacenadaUno = sharedPreferences.getString("RespuestaUno", "");
                    String respuestaAlmacenadaDos = sharedPreferences.getString("RespuestaDos", "");
                    respuestasCorrectas = respuestaUno.equals(respuestaAlmacenadaUno) &&
                            respuestaDos.equals(respuestaAlmacenadaDos);
                } else if (seleccionadoTres) {
                    String respuestaAlmacenadaUno = sharedPreferences.getString("RespuestaUno", "");
                    String respuestaAlmacenadaDos = sharedPreferences.getString("RespuestaDos", "");
                    String respuestaAlmacenadaTres = sharedPreferences.getString("RespuestaTres", "");
                    respuestasCorrectas = respuestaUno.equals(respuestaAlmacenadaUno) &&
                            respuestaDos.equals(respuestaAlmacenadaDos) &&
                            respuestaTres.equals(respuestaAlmacenadaTres);
                }

                if (respuestasCorrectas) {
                    loginSuccess();
                } else {
                    Toast.makeText(Logeo_usuario.this, "Respuestas incorrectas, intenta de nuevo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }




    private void InicializarVariables() {
        EtPasswordU = findViewById(R.id.EtPasswordU);
        BtnIngresar = findViewById(R.id.BtnIngresar);
        BtnInicioSesionBiometrico = findViewById(R.id.BtnInicioSesionBiometrico);
        sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        Ib_Aviso = findViewById(R.id.Ib_Aviso);
        Btn_Recuperar = findViewById(R.id.Btn_Recuperar);
    }

    private void handleFailedAttempt() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isAutoDestructionEnabled = sharedPreferences.getBoolean("auto_destruction_enabled", false);
        boolean isExportarEnabled = sharedPreferences.getBoolean("export_csv_enabled", false);
        int maxAttempts = sharedPreferences.getInt("max_attempts", 3);

        if (failedAttempts >= maxAttempts && isAutoDestructionEnabled) {
            Toast.makeText(Logeo_usuario.this, "¡Intentos Máximos Superados Autodestruyendo!", Toast.LENGTH_SHORT).show();
            try {
                if(isExportarEnabled){
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    F_Ajustes fragment = (F_Ajustes) fragmentManager.findFragmentByTag("F_Ajustes");
                    if (fragment != null) {
                        try {
                            if (ContextCompat.checkSelfPermission(Logeo_usuario.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                fragment.ExportarRegistros();
                            } else {
                                Toast.makeText(Logeo_usuario.this, "Permiso denegado para escribir en el almacenamiento externo", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("Logeo_usuario", "Error al exportar registros", e);
                            Toast.makeText(Logeo_usuario.this, "Error al exportar registros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Logeo_usuario", "El fragmento F_Ajustes no está disponible");
                    }
                }
                bdHelper.EliminarTodosRegistros();
            } catch (Exception e) {
                Log.e("Logeo_usuario", "Error al eliminar o exportar registros: " + e.getMessage(), e);
                Toast.makeText(Logeo_usuario.this, "Error al eliminar o exportar registros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(Logeo_usuario.this, "Intento fallido", Toast.LENGTH_SHORT).show();
        }

        failedAttempts++;
    }

    private void loginSuccess() {
        Intent intent = new Intent(Logeo_usuario.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showBiometricWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Logeo_usuario.this);
        builder.setTitle("Importante");
        builder.setMessage("Esta funcionalidad solo está disponible con huellas dactilares previamente registradas en su dispositivo.");
        builder.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}