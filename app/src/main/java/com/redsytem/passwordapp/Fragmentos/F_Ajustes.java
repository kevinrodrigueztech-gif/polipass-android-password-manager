package com.redsytem.passwordapp.Fragmentos;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.redsytem.passwordapp.BaseDeDatos.BDHelper;
import com.redsytem.passwordapp.BaseDeDatos.Constants;
import com.redsytem.passwordapp.Login_usuario.Logeo_usuario;
import com.redsytem.passwordapp.MainActivity;
import com.redsytem.passwordapp.Modelo.Password;
import com.redsytem.passwordapp.R;

import java.util.ArrayList;


import com.redsytem.passwordapp.Seguridad.MasterPasswordStore;
import com.redsytem.passwordapp.Seguridad.RecoveryAnswerStore;
import com.redsytem.passwordapp.Seguridad.SecurityTaskRunner;
import com.redsytem.passwordapp.Seguridad.VaultBackupManager;

public class F_Ajustes extends Fragment {

    TextView Eliminar_Todos_Registros, PreguntaSafe, Autodestruccion, Exportar_Archivo
            , Importar_Archivo, Cambiar_password_maestra, Compartir;

    Dialog dialog, dialog_p_m;

    private SwitchCompat HabAutoDes;
    private SharedPreferences sharedPreferences;

    private  BDHelper bdHelper;

    String ordenarTituloAsc = Constants.C_TITULO + " ASC";

    private static final String SHARED_PREF = MasterPasswordStore.SHARED_PREF;

    private ActivityResultLauncher<Intent> crearBackupLauncher;
    private ActivityResultLauncher<Intent> abrirBackupLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE , WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        bdHelper = new BDHelper(getContext());
        View view = inflater.inflate(R.layout.fragment_f__ajustes, container, false);

        Eliminar_Todos_Registros = view.findViewById(R.id.Eliminar_Todos_Registros);
        PreguntaSafe = view.findViewById(R.id.PreguntaSafe);
        Autodestruccion = view.findViewById(R.id.Autodestruccion);
        Exportar_Archivo = view.findViewById(R.id.Exportar_Archivo);
        Importar_Archivo = view.findViewById(R.id.Importar_Archivo);
        Cambiar_password_maestra = view.findViewById(R.id.Cambiar_password_maestra);

        dialog = new Dialog(getActivity());
        dialog_p_m = new Dialog(getActivity());



        SharedPreferences sharedPreferencesT = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isDarkMode = sharedPreferencesT.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Compartir = view.findViewById(R.id.Compartir);
        Compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFile();
            }
        });

        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);

        crearBackupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            } catch (Exception ignored) {
                                // Some providers do not support persisted permissions.
                            }
                            mostrarDialogoExportarBackup(uri);
                        }
                    }
                });

        abrirBackupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignored) {
                                // Some providers do not support persisted permissions.
                            }
                            mostrarDialogoImportarBackup(uri);
                        }
                    }
                });

        SwitchCompat themeSwitch = view.findViewById(R.id.themeSwitch);
        themeSwitch.setChecked(isDarkMode);
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferencesT.edit();
                editor.putBoolean("dark_mode", isChecked);
                editor.apply();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });

        PreguntaSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Preguntas_Recuperacion();
            }
        });

        Eliminar_Todos_Registros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Eliminar_Registros();
            }
        });

        Autodestruccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Auto_Destruccion();
            }
        });

        Exportar_Archivo.setOnClickListener(v -> lanzarCrearBackup());

        Importar_Archivo.setOnClickListener(v -> lanzarAbrirBackup());

        Cambiar_password_maestra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Cambiar contraseña maestra", Toast.LENGTH_SHORT).show();
                CuadroDialogoPasswordMaestra();
            }
        });

        return view;
    }

    private void shareFile() {
        String storedUri = sharedPreferences.getString("last_backup_uri", null);
        if (storedUri == null || storedUri.isEmpty()) {
            Toast.makeText(getActivity(), "Primero crea un respaldo cifrado", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(storedUri);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/octet-stream");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(shareIntent, "Compartir respaldo cifrado"));
        } catch (Exception e) {
            Toast.makeText(getActivity(), "No se pudo compartir el respaldo", Toast.LENGTH_SHORT).show();
        }
    }

    private void lanzarCrearBackup() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "PoliPass-backup.ppbk");
        crearBackupLauncher.launch(intent);
    }

    private void lanzarAbrirBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        abrirBackupLauncher.launch(intent);
    }

    private void mostrarDialogoExportarBackup(Uri destination) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText password = new EditText(requireContext());
        password.setHint("Contraseña de respaldo (mín. 8)");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(password);

        EditText confirm = new EditText(requireContext());
        confirm.setHint("Confirmar contraseña de respaldo");
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirm);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Crear respaldo cifrado")
                .setMessage("Esta contraseña será necesaria para restaurar la bóveda. PoliPass no la guarda.")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Crear respaldo", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String first = password.getText().toString();
            String second = confirm.getText().toString();
            if (first.length() < VaultBackupManager.MIN_BACKUP_PASSWORD_LENGTH) {
                password.setError("Mínimo " + VaultBackupManager.MIN_BACKUP_PASSWORD_LENGTH + " caracteres");
                return;
            }
            if (!first.equals(second)) {
                confirm.setError("Las contraseñas no coinciden");
                return;
            }

            dialog.dismiss();
            ejecutarExportacionBackup(destination, first);
        }));
        dialog.show();
    }

    private void ejecutarExportacionBackup(Uri destination, String password) {
        Toast.makeText(requireContext(), "Creando respaldo cifrado...", Toast.LENGTH_SHORT).show();
        SecurityTaskRunner.execute(() -> {
            try {
                VaultBackupManager.writeBackup(requireContext(), destination, password);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    sharedPreferences.edit().putString("last_backup_uri", destination.toString()).apply();
                    Toast.makeText(requireContext(), "Respaldo cifrado creado correctamente", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                        "No se pudo crear el respaldo: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void mostrarDialogoImportarBackup(Uri source) {
        EditText password = new EditText(requireContext());
        password.setHint("Contraseña de respaldo");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        password.setPadding(pad, 0, pad, 0);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Restaurar respaldo cifrado")
                .setMessage("La bóveda actual será reemplazada únicamente si el archivo y la contraseña son válidos.")
                .setView(password)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restaurar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pass = password.getText().toString();
            if (pass.length() < VaultBackupManager.MIN_BACKUP_PASSWORD_LENGTH) {
                password.setError("Mínimo " + VaultBackupManager.MIN_BACKUP_PASSWORD_LENGTH + " caracteres");
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("¿Confirmar restauración?")
                    .setMessage("Se sustituirán los registros actuales por el contenido del respaldo.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sí, restaurar", (confirmDialog, which) -> {
                        dialog.dismiss();
                        ejecutarImportacionBackup(source, pass);
                    })
                    .show();
        }));
        dialog.show();
    }

    private void ejecutarImportacionBackup(Uri source, String password) {
        Toast.makeText(requireContext(), "Validando y restaurando respaldo...", Toast.LENGTH_SHORT).show();
        SecurityTaskRunner.execute(() -> {
            try {
                VaultBackupManager.restoreBackup(requireContext(), source, password);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Respaldo restaurado correctamente", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(requireContext(), MainActivity.class));
                    requireActivity().finish();
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                        "No se pudo restaurar el respaldo: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void Dialog_Eliminar_Registros() {

        Button Btn_Si, Btn_Cancelar;

        dialog.setContentView(R.layout.cuadro_dialogo_eliminar_todos_registros);

        Btn_Si = dialog.findViewById(R.id.Btn_Si);
        Btn_Cancelar = dialog.findViewById(R.id.Btn_Cancelar);

        Btn_Si.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bdHelper.EliminarTodosRegistros();
                startActivity(new Intent(getActivity(), MainActivity.class));
                Toast.makeText(getActivity(), "Se ha eliminado todos los registros", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        Btn_Cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.setCancelable(false);

    }

    private void Dialog_Preguntas_Recuperacion(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cuadro_dialogo_preguntas_recuperacion, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        SwitchCompat HabPregSecure = dialogView.findViewById(R.id.HabPregSecure);
        EditText EtPreguntaUno = dialogView.findViewById(R.id.EtPregunaUno);
        EditText EtRespuestaUno = dialogView.findViewById(R.id.EtRespuestaUno);
        EditText EtPreguntaDos = dialogView.findViewById(R.id.EtPreguntaDos);
        EditText EtRespuestaDos = dialogView.findViewById(R.id.EtRespuestaDos);
        EditText EtPreguntaTres = dialogView.findViewById(R.id.EtPreguntaTres);
        EditText EtRespuestaTres = dialogView.findViewById(R.id.EtRespuestaTres);
        RadioButton pregunta_uno = dialogView.findViewById(R.id.pregunta_uno);
        RadioButton pregunta_dos = dialogView.findViewById(R.id.pregunta_dos);
        RadioButton pregunta_tres = dialogView.findViewById(R.id.pregunta_tres);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String PreguntaU = sharedPreferences.getString("PreguntaUno", "¿Nombre de tu mascota?");
        EtPreguntaUno.setText(String.valueOf(PreguntaU));
        String PreguntaD = sharedPreferences.getString("PreguntaDos", "¿Comida favorita?");
        EtPreguntaDos.setText(String.valueOf(PreguntaD));
        String PreguntaT = sharedPreferences.getString("PreguntaTres", "¿Tu lugar favorito?");
        EtPreguntaTres.setText(String.valueOf(PreguntaT));
        boolean habilitarPreguntas = sharedPreferences.getBoolean("question_enabled", false);
        HabPregSecure.setChecked(habilitarPreguntas);
        boolean seleccionadoUno = sharedPreferences.getBoolean("seleccionadoUno", false);
        boolean seleccionadoDos = sharedPreferences.getBoolean("seleccionadoDos", false);
        boolean seleccionadoTres = sharedPreferences.getBoolean("seleccionadoTres", false);
        pregunta_uno.setChecked(seleccionadoUno);
        pregunta_dos.setChecked(seleccionadoDos);
        pregunta_tres.setChecked(seleccionadoTres);

        updateVisibility(seleccionadoUno, seleccionadoDos, seleccionadoTres, EtPreguntaUno, EtRespuestaUno, EtPreguntaDos, EtRespuestaDos, EtPreguntaTres, EtRespuestaTres);

        // Añadir listeners a los RadioButtons
        pregunta_uno.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                pregunta_dos.setChecked(false);
                pregunta_tres.setChecked(false);
                updateVisibility(true, false, false, EtPreguntaUno, EtRespuestaUno, EtPreguntaDos, EtRespuestaDos, EtPreguntaTres, EtRespuestaTres);
            }
        });

        pregunta_dos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                pregunta_uno.setChecked(false);
                pregunta_tres.setChecked(false);
                updateVisibility(false, true, false, EtPreguntaUno, EtRespuestaUno, EtPreguntaDos, EtRespuestaDos, EtPreguntaTres, EtRespuestaTres);
            }
        });

        pregunta_tres.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                pregunta_uno.setChecked(false);
                pregunta_dos.setChecked(false);
                updateVisibility(false, false, true, EtPreguntaUno, EtRespuestaUno, EtPreguntaDos, EtRespuestaDos, EtPreguntaTres, EtRespuestaTres);
            }
        });

        builder.setPositiveButton("Guardar", null);

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setOnClickListener(v -> {
            String inputPU = EtPreguntaUno.getText().toString().trim();
            String inputRU = EtRespuestaUno.getText().toString().trim();
            String inputPD = EtPreguntaDos.getText().toString().trim();
            String inputRD = EtRespuestaDos.getText().toString().trim();
            String inputPT = EtPreguntaTres.getText().toString().trim();
            String inputRT = EtRespuestaTres.getText().toString().trim();

            boolean enabled = HabPregSecure.isChecked();
            if (!enabled) {
                RecoveryAnswerStore.clearAnswer(sharedPreferences, 0);
                RecoveryAnswerStore.clearAnswer(sharedPreferences, 1);
                RecoveryAnswerStore.clearAnswer(sharedPreferences, 2);
                sharedPreferences.edit()
                        .putBoolean("question_enabled", false)
                        .putBoolean("seleccionadoUno", false)
                        .putBoolean("seleccionadoDos", false)
                        .putBoolean("seleccionadoTres", false)
                        .apply();
                Toast.makeText(getActivity(), "Recuperación deshabilitada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            final int selectedCount;
            if (pregunta_uno.isChecked()) selectedCount = 1;
            else if (pregunta_dos.isChecked()) selectedCount = 2;
            else if (pregunta_tres.isChecked()) selectedCount = 3;
            else selectedCount = 0;

            if (selectedCount == 0) {
                Toast.makeText(getActivity(), "Selecciona al menos una pregunta", Toast.LENGTH_SHORT).show();
                return;
            }
            if (inputPU.isEmpty() || inputRU.isEmpty() || (selectedCount >= 2 && (inputPD.isEmpty() || inputRD.isEmpty()))
                    || (selectedCount == 3 && (inputPT.isEmpty() || inputRT.isEmpty()))) {
                Toast.makeText(getActivity(), "Completa las preguntas y respuestas seleccionadas", Toast.LENGTH_SHORT).show();
                return;
            }

            saveButton.setEnabled(false);
            saveButton.setText("Guardando...");

            SecurityTaskRunner.execute(() -> {
                try {
                    sharedPreferences.edit()
                            .putBoolean("question_enabled", true)
                            .putString("PreguntaUno", inputPU)
                            .putString("PreguntaDos", inputPD)
                            .putString("PreguntaTres", inputPT)
                            .putBoolean("seleccionadoUno", selectedCount == 1)
                            .putBoolean("seleccionadoDos", selectedCount == 2)
                            .putBoolean("seleccionadoTres", selectedCount == 3)
                            .apply();

                    RecoveryAnswerStore.saveAnswer(sharedPreferences, 0, inputRU);
                    if (selectedCount >= 2) RecoveryAnswerStore.saveAnswer(sharedPreferences, 1, inputRD);
                    else RecoveryAnswerStore.clearAnswer(sharedPreferences, 1);
                    if (selectedCount == 3) RecoveryAnswerStore.saveAnswer(sharedPreferences, 2, inputRT);
                    else RecoveryAnswerStore.clearAnswer(sharedPreferences, 2);

                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        dialog.dismiss();
                        Toast.makeText(requireContext(), "Preguntas de recuperación guardadas de forma segura", Toast.LENGTH_SHORT).show();
                    });
                } catch (RuntimeException e) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        saveButton.setEnabled(true);
                        saveButton.setText("Guardar");
                        Toast.makeText(requireContext(), "No se pudieron guardar las respuestas de recuperación", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void updateVisibility(boolean uno, boolean dos, boolean tres, EditText etPreguntaUno, EditText etRespuestaUno, EditText etPreguntaDos, EditText etRespuestaDos, EditText etPreguntaTres, EditText etRespuestaTres) {
        if (uno) {
            etPreguntaUno.setVisibility(View.VISIBLE);
            etRespuestaUno.setVisibility(View.VISIBLE);
            etPreguntaDos.setVisibility(View.GONE);
            etRespuestaDos.setVisibility(View.GONE);
            etPreguntaTres.setVisibility(View.GONE);
            etRespuestaTres.setVisibility(View.GONE);
        } else if (dos) {
            etPreguntaUno.setVisibility(View.VISIBLE);
            etRespuestaUno.setVisibility(View.VISIBLE);
            etPreguntaDos.setVisibility(View.VISIBLE);
            etRespuestaDos.setVisibility(View.VISIBLE);
            etPreguntaTres.setVisibility(View.GONE);
            etRespuestaTres.setVisibility(View.GONE);
        } else if (tres) {
            etPreguntaUno.setVisibility(View.VISIBLE);
            etRespuestaUno.setVisibility(View.VISIBLE);
            etPreguntaDos.setVisibility(View.VISIBLE);
            etRespuestaDos.setVisibility(View.VISIBLE);
            etPreguntaTres.setVisibility(View.VISIBLE);
            etRespuestaTres.setVisibility(View.VISIBLE);
        }
    }

    private void Dialog_Auto_Destruccion(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cuadro_dialogo_autodestruccion, null);

        // Crear el cuadro de diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        EditText Et_Numero_Intentos = dialogView.findViewById(R.id.Et_Numero_Intentos);
        SwitchCompat HabAutoDes = dialogView.findViewById(R.id.HabAutoDes);

        // Configurar el EditText con el número máximo de intentos actual
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int maxAttempts = sharedPreferences.getInt("max_attempts", 3);
        Et_Numero_Intentos.setText(String.valueOf(maxAttempts));

        // Configurar el SwitchCompat según el estado actual de la autodestrucción
        boolean isAutoDestructionEnabled = sharedPreferences.getBoolean("auto_destruction_enabled", false);
        HabAutoDes.setChecked(isAutoDestructionEnabled);

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Guardar el número máximo de intentos ingresado
                String input = Et_Numero_Intentos.getText().toString().trim();
                if (!input.isEmpty()) {
                    int newMaxAttempts = Integer.parseInt(input);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("max_attempts", newMaxAttempts);
                    editor.apply();
                    Toast.makeText(getActivity(), "Número de intentos guardado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Ingrese un número válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Manejar el cambio en el estado del SwitchCompat
        HabAutoDes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("auto_destruction_enabled", isChecked);
                editor.apply();
            }
        });

        // Manejar el cambio en el estado del SwitchCompat

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Mostrar el cuadro de diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void CuadroDialogoPasswordMaestra() {
        EditText Et_nuevo_password_maestra, Et_C_nuevo_password_maestra;
        Button Btn_cambiar_password_maestra, Btn_cancelar_password_maestra;

        dialog_p_m.setContentView(R.layout.cuadro_dialogo_password_maestra);

        Et_nuevo_password_maestra = dialog_p_m.findViewById(R.id.Et_nuevo_password_maestra);
        Et_C_nuevo_password_maestra = dialog_p_m.findViewById(R.id.Et_C_nuevo_password_maestra);
        Btn_cambiar_password_maestra = dialog_p_m.findViewById(R.id.Btn_cambiar_password_maestra);
        Btn_cancelar_password_maestra = dialog_p_m.findViewById(R.id.Btn_cancelar_password_maestra);

        Btn_cambiar_password_maestra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String S_nuevo_password = Et_nuevo_password_maestra.getText().toString().trim();
                String S_c_nuevo_password = Et_C_nuevo_password_maestra.getText().toString().trim();

                if (S_nuevo_password.equals("")) {
                    Toast.makeText(getActivity(), "Ingrese nueva contraseña", Toast.LENGTH_SHORT).show();
                } else if (S_c_nuevo_password.equals("")) {
                    Toast.makeText(getActivity(), "Confirme nueva contraseña", Toast.LENGTH_SHORT).show();
                } else if (S_nuevo_password.length() < 6) {
                    Toast.makeText(getActivity(), "La contraseña debe tener más de 6 caracteres", Toast.LENGTH_SHORT).show();
                } else if (!S_nuevo_password.equals(S_c_nuevo_password)) {
                    Toast.makeText(getActivity(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                } else {
                    Btn_cambiar_password_maestra.setEnabled(false);
                    Btn_cambiar_password_maestra.setText("Guardando...");
                    SecurityTaskRunner.execute(() -> {
                        try {
                            MasterPasswordStore.save(sharedPreferences, S_nuevo_password);
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                if (!isAdded()) return;
                                startActivity(new Intent(requireContext(), Logeo_usuario.class));
                                requireActivity().finish();
                                Toast.makeText(requireContext(), "La contraseña maestra se ha cambiado", Toast.LENGTH_SHORT).show();
                                dialog_p_m.dismiss();
                            });
                        } catch (RuntimeException e) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                Btn_cambiar_password_maestra.setEnabled(true);
                                Btn_cambiar_password_maestra.setText("Cambiar");
                                Toast.makeText(requireContext(), "No se pudo guardar la contraseña", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            }
        });

        Btn_cancelar_password_maestra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
                dialog_p_m.dismiss();
            }
        });

        dialog_p_m.show();
        dialog_p_m.setCancelable(false);
    }


}
