package com.redsytem.passwordapp.Fragmentos;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.redsytem.passwordapp.BaseDeDatos.BDHelper;
import com.redsytem.passwordapp.BaseDeDatos.Constants;
import com.redsytem.passwordapp.Encriptacion.Encrypt;
import com.redsytem.passwordapp.Login_usuario.Logeo_usuario;
import com.redsytem.passwordapp.MainActivity;
import com.redsytem.passwordapp.Modelo.Password;
import com.redsytem.passwordapp.R;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Objects;


import com.redsytem.passwordapp.R;

public class F_Ajustes extends Fragment {

    TextView Eliminar_Todos_Registros, PreguntaSafe, Autodestruccion, Exportar_Archivo
            , Importar_Archivo, Cambiar_password_maestra, Compartir;

    Dialog dialog, dialog_p_m;

    private SwitchCompat HabAutoDes;
    private SharedPreferences sharedPreferences;

    private  BDHelper bdHelper;

    String ordenarTituloAsc = Constants.C_TITULO + " ASC";

    private static final String SHARED_PREF = "mi_pref";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_C_PASSWORD = "c_password";

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

        Exportar_Archivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Exportar archivo", Toast.LENGTH_SHORT).show();
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    try {
                        ExportarRegistros();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }else {
                    SolicitudPermisoExportar.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

            }
        });

        Importar_Archivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("¿Importar CSV?");
                builder.setMessage("Se eliminarán todos los registros actuales.");
                builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                            bdHelper.EliminarTodosRegistros();
                            ImportarRegistros();
                        }else {
                            SolicitudPermisoImportar.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }

                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Importación cancelada", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.create().show();

            }
        });

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
        String Carpeta_Archivo = Environment.getExternalStorageDirectory() + "/Documents/Password App/Registros.csv";
        File file = new File(Carpeta_Archivo);
        if (!file.exists()) {
            Toast.makeText(getActivity(), "El archivo no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Compartir archivo"));
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
        String RespuestaU = sharedPreferences.getString("RespuestaUno", "");
        EtRespuestaUno.setText(String.valueOf(RespuestaU));
        String RespuestaD = sharedPreferences.getString("RespuestaDos", "");
        EtRespuestaDos.setText(String.valueOf(RespuestaD));
        String RespuestaT = sharedPreferences.getString("RespuestaTres", "");
        EtRespuestaTres.setText(String.valueOf(RespuestaT));
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

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String inputPU = EtPreguntaUno.getText().toString().trim();
            String inputRU = EtRespuestaUno.getText().toString().trim();
            String inputPD = EtPreguntaDos.getText().toString().trim();
            String inputRD = EtRespuestaDos.getText().toString().trim();
            String inputPT = EtPreguntaTres.getText().toString().trim();
            String inputRT = EtRespuestaTres.getText().toString().trim();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("question_enabled", HabPregSecure.isChecked());
            if (pregunta_uno.isChecked()) {
                if (!inputPU.isEmpty() && !inputRU.isEmpty()) {
                    editor.putString("PreguntaUno", inputPU);
                    editor.putString("RespuestaUno", inputRU);
                    editor.putBoolean("seleccionadoUno", true);
                    editor.putBoolean("seleccionadoDos", false);
                    editor.putBoolean("seleccionadoTres", false);
                    Toast.makeText(getActivity(), "Pregunta y Respuesta Uno Guardadas", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error al guardar pregunta y respuesta uno", Toast.LENGTH_SHORT).show();
                }
            } else if (pregunta_dos.isChecked()) {
                if (!inputPU.isEmpty() && !inputRU.isEmpty() && !inputPD.isEmpty() && !inputRD.isEmpty()) {
                    editor.putString("PreguntaUno", inputPU);
                    editor.putString("RespuestaUno", inputRU);
                    editor.putString("PreguntaDos", inputPD);
                    editor.putString("RespuestaDos", inputRD);
                    editor.putBoolean("seleccionadoUno", false);
                    editor.putBoolean("seleccionadoDos", true);
                    editor.putBoolean("seleccionadoTres", false);
                    Toast.makeText(getActivity(), "Preguntas y Respuestas Uno y Dos Guardadas", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error al guardar preguntas y respuestas uno y dos", Toast.LENGTH_SHORT).show();
                }
            } else if (pregunta_tres.isChecked()) {
                if (!inputPU.isEmpty() && !inputRU.isEmpty() && !inputPD.isEmpty() && !inputRD.isEmpty() && !inputPT.isEmpty() && !inputRT.isEmpty()) {
                    editor.putString("PreguntaUno", inputPU);
                    editor.putString("RespuestaUno", inputRU);
                    editor.putString("PreguntaDos", inputPD);
                    editor.putString("RespuestaDos", inputRD);
                    editor.putString("PreguntaTres", inputPT);
                    editor.putString("RespuestaTres", inputRT);
                    editor.putBoolean("seleccionadoUno", false);
                    editor.putBoolean("seleccionadoDos", false);
                    editor.putBoolean("seleccionadoTres", true);
                    Toast.makeText(getActivity(), "Preguntas y Respuestas Guardadas", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error al guardar todas las preguntas y respuestas", Toast.LENGTH_SHORT).show();
                }
            }
            editor.apply();
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
        SwitchCompat HabExportCSV = dialogView.findViewById(R.id.HabExportCSV);

        // Configurar el EditText con el número máximo de intentos actual
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int maxAttempts = sharedPreferences.getInt("max_attempts", 3);
        Et_Numero_Intentos.setText(String.valueOf(maxAttempts));

        // Configurar el SwitchCompat según el estado actual de la autodestrucción
        boolean isAutoDestructionEnabled = sharedPreferences.getBoolean("auto_destruction_enabled", false);
        boolean isExportarEnabled = sharedPreferences.getBoolean("export_csv_enabled", false);
        HabAutoDes.setChecked(isAutoDestructionEnabled);
        HabExportCSV.setChecked(isExportarEnabled);

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
        HabExportCSV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("export_csv_enabled", isChecked);
                editor.apply();
            }
        });

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

    public void ExportarRegistros() throws Exception {
        Log.d("ExportCSV", "Starting exportToCSV method...");

        //Nombre de la carpeta
        File carpeta = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Password App");

        boolean carpetaCreada = false;

        if (!carpeta.exists()){
            //Si la carpeta no existe, creamos una nueva
            carpetaCreada = carpeta.mkdirs();
        }

        //Nombre del archivo
        String csvnombreArchivo = "Registros.csv";
        //Concatenar el nombre de la carpeta y del archivo
        String Carpeta_Archivo = carpeta + "/" + csvnombreArchivo;

        /*Obtener el registro que vamos a exportar*/
        ArrayList<Password> registroList = new ArrayList<>();
        registroList = bdHelper.ObtenerTodosRegistros(ordenarTituloAsc);

        try (FileWriter fileWriter = new FileWriter(Carpeta_Archivo)) {
            for (Password registro : registroList) {
                fileWriter.append("" + registro.getId()).append(",");
                fileWriter.append("" + registro.getTitulo().replace("\n", " ")).append(",");
                fileWriter.append("" + registro.getCuenta().replace("\n", " ")).append(",");
                fileWriter.append("" + registro.getNombre_usuario().replace("\n", " ")).append(",");
                fileWriter.append("" + Encrypt.encrypt(registro.getPassword(), true).trim().replace("\n", " ")).append(",");
                fileWriter.append("" + registro.getSitio_web().replace("\n", " ")).append(",");
                fileWriter.append("" + registro.getNota().replace("\n", " ")).append(",");
                fileWriter.append("" + registro.getT_registro()).append(",");
                fileWriter.append("" + registro.getT_actualiacion()).append("\n");
            }
            Toast.makeText(getActivity(), "Se ha exportado el archivo CSV con éxito", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("Export", "Error al exportar CSV: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void ImportarRegistros() {
        //Establecer la ruta
        String Carpeta_Archivo = Environment.getExternalStorageDirectory()+ "/Documents/" + "/Password App/" + "Registros.csv";
        File file = new File(Carpeta_Archivo);
        if (file.exists()){
            // Si el respaldo existe
            try {
                CSVReader csvReader = new CSVReader(new FileReader(file.getAbsoluteFile()));
                String [] nextLine;
                while ((nextLine = csvReader.readNext())!=null){
                    String ids = nextLine[0];
                    String titulo = nextLine[1].replace("\n", " ");
                    String cuenta = nextLine[2].replace("\n", " ");
                    String nombre_usuario = nextLine[3].replace("\n", " ");
                    String password = nextLine[4].replace("\n", " ");
                    String sitio_web = nextLine[5].replace("\n", " ");
                    String nota = nextLine[6].replace("\n", " ");
                    String tiempoR = nextLine[7];
                    String tiempoA = nextLine[8];

                    password = Encrypt.encrypt(password, false);

                    long id = bdHelper.insertarRegistro(
                            ""+titulo,
                            ""+ cuenta,
                            ""+ nombre_usuario,
                            ""+password,
                            ""+sitio_web,
                            ""+ nota,
                            ""+tiempoR,
                            ""+tiempoA);
                }
                Toast.makeText(getActivity(), "Archivo CSV importado con éxito", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "No existe un respaldo", Toast.LENGTH_SHORT).show();
        }

    }

    private void CuadroDialogoPasswordMaestra() {
        //Establecer las vistas
        EditText Password_maestra;
        EditText Et_nuevo_password_maestra, Et_C_nuevo_password_maestra;
        Button Btn_cambiar_password_maestra, Btn_cancelar_password_maestra;

        String password_maestra_recuperada = sharedPreferences.getString(KEY_PASSWORD, null);

        //Hacemos la conexión con el cuadro de diálogo
        dialog_p_m.setContentView(R.layout.cuadro_dialogo_password_maestra);

        //Inicializar las vistas
        Password_maestra = dialog_p_m.findViewById(R.id.Password_maestra);
        Et_nuevo_password_maestra = dialog_p_m.findViewById(R.id.Et_nuevo_password_maestra);
        Et_C_nuevo_password_maestra = dialog_p_m.findViewById(R.id.Et_C_nuevo_password_maestra);
        Btn_cambiar_password_maestra = dialog_p_m.findViewById(R.id.Btn_cambiar_password_maestra);
        Btn_cancelar_password_maestra = dialog_p_m.findViewById(R.id.Btn_cancelar_password_maestra);

        Btn_cambiar_password_maestra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtener los datos de los edittext
                String S_nuevo_password = Et_nuevo_password_maestra.getText().toString().trim();
                String S_c_nuevo_password = Et_C_nuevo_password_maestra.getText().toString().trim();

                /*Validación de datos*/
                if (S_nuevo_password.equals("")){
                    Toast.makeText(getActivity(), "Ingrese nueva contraseña", Toast.LENGTH_SHORT).show();
                }
                else if (S_c_nuevo_password.equals("")){
                    Toast.makeText(getActivity(), "Confirme nueva contraseña", Toast.LENGTH_SHORT).show();
                }
                else if (S_nuevo_password.length()<6){
                    Toast.makeText(getActivity(), "La contraseña debe tener más de 6 caracteres", Toast.LENGTH_SHORT).show();
                }
                else if (!S_nuevo_password.equals(S_c_nuevo_password)){
                    Toast.makeText(getActivity(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                }else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    /*Pasar los nuevos datos a las llaves*/
                    editor.putString(KEY_PASSWORD, S_nuevo_password);
                    editor.putString(KEY_C_PASSWORD, S_c_nuevo_password);
                    editor.apply();
                    /*Salir de la aplicación, para iniciar sesión con la nueva contraseña*/
                    startActivity(new Intent(getActivity(), Logeo_usuario.class));
                    getActivity().finish();
                    Toast.makeText(getActivity(), "La contraseña maestra se ha cambiado", Toast.LENGTH_SHORT).show();
                    dialog_p_m.dismiss();
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

        Password_maestra.setText(password_maestra_recuperada);
        Password_maestra.setEnabled(false);
        Password_maestra.setBackgroundColor(Color.TRANSPARENT);
        Password_maestra.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        dialog_p_m.show();
        dialog_p_m.setCancelable(false);
    }


    //Permiso exportar registro
    private ActivityResultLauncher<String> SolicitudPermisoExportar =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), Concede_permiso_exportar -> {

                int permiso = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permiso == PackageManager.PERMISSION_GRANTED){
                    try {
                        ExportarRegistros();
                        Toast.makeText(getActivity(), "Permiso consedido", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }else {
                    Toast.makeText(getActivity(), "Permiso denegado", Toast.LENGTH_SHORT).show();
                }
            });

    //Permiso importar registro
    private ActivityResultLauncher<String> SolicitudPermisoImportar =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), Concede_permiso_importar -> {
                int permiso = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permiso == PackageManager.PERMISSION_GRANTED){
                    ImportarRegistros();
                    Toast.makeText(getActivity(), "Permiso consedido", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getActivity(), "Permiso denegado", Toast.LENGTH_SHORT).show();
                }
            });
}