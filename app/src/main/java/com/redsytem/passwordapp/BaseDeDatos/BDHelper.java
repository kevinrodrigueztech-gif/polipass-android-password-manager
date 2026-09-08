package com.redsytem.passwordapp.BaseDeDatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.redsytem.passwordapp.Encriptacion.Encrypt;
import com.redsytem.passwordapp.Modelo.Password;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class BDHelper extends SQLiteOpenHelper {

    public BDHelper(@Nullable Context context) {
        super(context, Constants.BD_NAME, null, Constants.BD_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + Constants.TABLE_NAME + " ADD COLUMN "
                    + Constants.C_REGISTRO_CIFRADO + " TEXT");

            migrateLegacyRecords(db);
        }
    }

    private void migrateLegacyRecords(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_NAME, null);
        try {
            int idIndex = cursor.getColumnIndexOrThrow(Constants.C_ID);
            int tituloIndex = cursor.getColumnIndexOrThrow(Constants.C_TITULO);
            int cuentaIndex = cursor.getColumnIndexOrThrow(Constants.C_CUENTA);
            int usuarioIndex = cursor.getColumnIndexOrThrow(Constants.C_NOMBRE_USUARIO);
            int passwordIndex = cursor.getColumnIndexOrThrow(Constants.C_PASSWORD);
            int sitioIndex = cursor.getColumnIndexOrThrow(Constants.C_SITIO_WEB);
            int notaIndex = cursor.getColumnIndexOrThrow(Constants.C_NOTA);
            int blobIndex = cursor.getColumnIndexOrThrow(Constants.C_REGISTRO_CIFRADO);

            while (cursor.moveToNext()) {
                String encryptedPassword = cursor.getString(passwordIndex);
                String password = encryptedPassword == null ? "" : Encrypt.encrypt(encryptedPassword, false);

                String blob = construirRegistroCifrado(
                        cursor.getString(tituloIndex),
                        cursor.getString(cuentaIndex),
                        cursor.getString(usuarioIndex),
                        password,
                        cursor.getString(sitioIndex),
                        cursor.getString(notaIndex));

                ContentValues values = new ContentValues();
                values.put(Constants.C_REGISTRO_CIFRADO, blob);
                values.putNull(Constants.C_TITULO);
                values.putNull(Constants.C_CUENTA);
                values.putNull(Constants.C_NOMBRE_USUARIO);
                values.putNull(Constants.C_PASSWORD);
                values.putNull(Constants.C_SITIO_WEB);
                values.putNull(Constants.C_NOTA);
                db.update(Constants.TABLE_NAME, values,
                        Constants.C_ID + " = ?", new String[]{cursor.getString(idIndex)});
            }
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible migrar la bóveda cifrada", e);
        } finally {
            cursor.close();
        }
    }

    public long insertarRegistro(String titulo, String cuenta, String nombre_usuario, String password,
                                 String sitio_web, String nota, String t_registro,
                                 String t_actualizacion) throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.C_REGISTRO_CIFRADO,
                    construirRegistroCifrado(titulo, cuenta, nombre_usuario, password, sitio_web, nota));
            values.put(Constants.C_TIEMPO_REGISTRO, t_registro);
            values.put(Constants.C_TIEMPO_ACTUALIZACION, t_actualizacion);
            return db.insertOrThrow(Constants.TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }

    public void actualizarRegistro(String id, String titulo, String cuenta, String nombre_usuario,
                                   String password, String sitio_web, String nota,
                                   String t_registro, String t_actualizacion) throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.C_REGISTRO_CIFRADO,
                    construirRegistroCifrado(titulo, cuenta, nombre_usuario, password, sitio_web, nota));
            values.put(Constants.C_TIEMPO_REGISTRO, t_registro);
            values.put(Constants.C_TIEMPO_ACTUALIZACION, t_actualizacion);
            db.update(Constants.TABLE_NAME, values,
                    Constants.C_ID + " = ?", new String[]{id});
        } finally {
            db.close();
        }
    }

    public ArrayList<Password> ObtenerTodosRegistros(String orderby) throws Exception {
        ArrayList<Password> passwordList = cargarTodosRegistros();
        ordenarRegistros(passwordList, orderby);
        return passwordList;
    }

    public ArrayList<Password> BuscarRegistros(String consulta) throws Exception {
        String normalizedQuery = consulta == null ? "" : consulta.trim().toLowerCase(Locale.ROOT);
        ArrayList<Password> all = cargarTodosRegistros();
        ArrayList<Password> result = new ArrayList<>();

        for (Password registro : all) {
            if (registro.getTitulo() != null
                    && registro.getTitulo().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                result.add(registro);
            }
        }
        return result;
    }

    public ArrayList<Password> BuscarRegistrosPorSitioWeb(String sitioWeb) throws Exception {
        String normalizedSite = sitioWeb == null ? "" : sitioWeb.trim();
        ArrayList<Password> all = cargarTodosRegistros();
        ArrayList<Password> result = new ArrayList<>();

        for (Password registro : all) {
            if (normalizedSite.equalsIgnoreCase(registro.getSitio_web())) {
                result.add(registro);
            }
        }
        return result;
    }

    public Pair<String, String> BuscarNombreUsuarioYContraseñaPorSitioWeb(String sitioWeb) throws Exception {
        ArrayList<Password> registros = BuscarRegistrosPorSitioWeb(sitioWeb);
        if (registros.isEmpty()) {
            return Pair.create(null, null);
        }
        Password registro = registros.get(0);
        return Pair.create(registro.getNombre_usuario(), registro.getPassword());
    }

    public Password ObtenerRegistroPorId(String id) throws Exception {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_NAME, null,
                Constants.C_ID + " = ?", new String[]{id}, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return convertirCursorARegistro(cursor);
        } finally {
            cursor.close();
            db.close();
        }
    }

    private ArrayList<Password> cargarTodosRegistros() throws Exception {
        ArrayList<Password> records = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_NAME, null);
        try {
            while (cursor.moveToNext()) {
                records.add(convertirCursorARegistro(cursor));
            }
        } finally {
            cursor.close();
            db.close();
        }
        return records;
    }

    private Password convertirCursorARegistro(Cursor cursor) throws Exception {
        int idIndex = cursor.getColumnIndexOrThrow(Constants.C_ID);
        int registroCifradoIndex = cursor.getColumnIndexOrThrow(Constants.C_REGISTRO_CIFRADO);
        int tiempoRegistroIndex = cursor.getColumnIndexOrThrow(Constants.C_TIEMPO_REGISTRO);
        int tiempoActualizacionIndex = cursor.getColumnIndexOrThrow(Constants.C_TIEMPO_ACTUALIZACION);

        String blob = cursor.getString(registroCifradoIndex);
        if (blob == null || blob.isEmpty()) {
            return convertirLegacyCursor(cursor);
        }

        JSONObject payload = new JSONObject(Encrypt.decryptRecord(blob));
        return new Password(
                String.valueOf(cursor.getInt(idIndex)),
                payload.optString("titulo", ""),
                payload.optString("cuenta", ""),
                payload.optString("nombre_usuario", ""),
                payload.optString("password", ""),
                payload.optString("sitio_web", ""),
                payload.optString("nota", ""),
                cursor.getString(tiempoRegistroIndex),
                cursor.getString(tiempoActualizacionIndex));
    }

    private Password convertirLegacyCursor(Cursor cursor) throws Exception {
        String password = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_PASSWORD));
        if (password != null && !password.isEmpty()) {
            password = Encrypt.encrypt(password, false);
        } else {
            password = "";
        }

        return new Password(
                String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.C_ID))),
                safe(cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_TITULO))),
                safe(cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CUENTA))),
                safe(cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_NOMBRE_USUARIO))),
                password,
                safe(cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_SITIO_WEB))),
                safe(cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_NOTA))),
                safe(cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_TIEMPO_REGISTRO))),
                safe(cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_TIEMPO_ACTUALIZACION))));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String construirRegistroCifrado(String titulo, String cuenta, String nombreUsuario,
                                                    String password, String sitioWeb, String nota) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("version", 1);
        payload.put("titulo", safe(titulo));
        payload.put("cuenta", safe(cuenta));
        payload.put("nombre_usuario", safe(nombreUsuario));
        payload.put("password", safe(password));
        payload.put("sitio_web", safe(sitioWeb));
        payload.put("nota", safe(nota));
        return Encrypt.encryptRecord(payload.toString());
    }

    private static void ordenarRegistros(ArrayList<Password> records, String orderby) {
        boolean descending = orderby != null && orderby.toLowerCase(Locale.ROOT).contains("desc");
        Collections.sort(records, new Comparator<Password>() {
            @Override
            public int compare(Password left, Password right) {
                String a = safe(left.getTitulo());
                String b = safe(right.getTitulo());
                int result = a.compareToIgnoreCase(b);
                return descending ? -result : result;
            }
        });
    }

    public void reemplazarTodosLosRegistros(ArrayList<Password> records) throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(Constants.TABLE_NAME, null, null);
            for (Password record : records) {
                ContentValues values = new ContentValues();
                values.put(Constants.C_REGISTRO_CIFRADO,
                        construirRegistroCifrado(
                                record.getTitulo(),
                                record.getCuenta(),
                                record.getNombre_usuario(),
                                record.getPassword(),
                                record.getSitio_web(),
                                record.getNota()));
                values.put(Constants.C_TIEMPO_REGISTRO, record.getT_registro());
                values.put(Constants.C_TIEMPO_ACTUALIZACION, record.getT_actualiacion());
                db.insertOrThrow(Constants.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public int ObtenerNumeroRegistros() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_NAME, null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
            db.close();
        }
    }

    public void EliminarRegistro(String id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(Constants.TABLE_NAME, Constants.C_ID + " = ?", new String[]{id});
        } finally {
            db.close();
        }
    }

    public void EliminarTodosRegistros() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.execSQL("DELETE FROM " + Constants.TABLE_NAME);
            Log.d("bdHelper", "Todos los registros han sido eliminados");
        } catch (Exception e) {
            Log.e("bdHelper", "Error al eliminar registros", e);
            throw new RuntimeException("Error al eliminar registros: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}
