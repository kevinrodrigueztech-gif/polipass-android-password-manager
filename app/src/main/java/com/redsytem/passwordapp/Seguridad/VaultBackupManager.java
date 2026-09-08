package com.redsytem.passwordapp.Seguridad;

import android.content.Context;
import android.net.Uri;

import com.redsytem.passwordapp.BaseDeDatos.BDHelper;
import com.redsytem.passwordapp.Modelo.Password;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/** Coordinates JSON serialization and Android document storage for portable backups. */
public final class VaultBackupManager {

    public static final int MIN_BACKUP_PASSWORD_LENGTH = VaultBackupCrypto.MIN_PASSWORD_LENGTH;

    private VaultBackupManager() {
    }

    public static void writeBackup(Context context, Uri destination, String backupPassword) throws Exception {
        byte[] json = buildPayload(context).toString().getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = null;
        try {
            encrypted = VaultBackupCrypto.encrypt(json, backupPassword);
            try (OutputStream out = context.getContentResolver().openOutputStream(destination)) {
                if (out == null) {
                    throw new IllegalStateException("No se pudo abrir el archivo de respaldo");
                }
                out.write(encrypted);
                out.flush();
            }
        } finally {
            java.util.Arrays.fill(json, (byte) 0);
            if (encrypted != null) java.util.Arrays.fill(encrypted, (byte) 0);
        }
    }

    public static void restoreBackup(Context context, Uri source, String backupPassword) throws Exception {
        byte[] encrypted = readAll(context, source);
        byte[] plaintext = null;
        try {
            plaintext = VaultBackupCrypto.decrypt(encrypted, backupPassword);
            JSONObject payload = new JSONObject(new String(plaintext, StandardCharsets.UTF_8));
            validatePayload(payload);
            ArrayList<Password> records = parseRecords(payload.getJSONArray("records"));
            new BDHelper(context.getApplicationContext()).reemplazarTodosLosRegistros(records);
        } finally {
            java.util.Arrays.fill(encrypted, (byte) 0);
            if (plaintext != null) java.util.Arrays.fill(plaintext, (byte) 0);
        }
    }

    private static JSONObject buildPayload(Context context) throws Exception {
        ArrayList<Password> records = new BDHelper(context.getApplicationContext())
                .ObtenerTodosRegistros("TITULO ASC");
        JSONObject root = new JSONObject();
        root.put("format", "polipass-backup");
        root.put("version", VaultBackupCrypto.FORMAT_VERSION);
        root.put("createdAt", System.currentTimeMillis());

        JSONArray array = new JSONArray();
        for (Password record : records) {
            JSONObject item = new JSONObject();
            item.put("titulo", safe(record.getTitulo()));
            item.put("cuenta", safe(record.getCuenta()));
            item.put("nombre_usuario", safe(record.getNombre_usuario()));
            item.put("password", safe(record.getPassword()));
            item.put("sitio_web", safe(record.getSitio_web()));
            item.put("nota", safe(record.getNota()));
            item.put("t_registro", safe(record.getT_registro()));
            item.put("t_actualizacion", safe(record.getT_actualiacion()));
            array.put(item);
        }
        root.put("records", array);
        return root;
    }

    private static ArrayList<Password> parseRecords(JSONArray array) throws Exception {
        ArrayList<Password> records = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            records.add(new Password(
                    "0",
                    item.optString("titulo", ""),
                    item.optString("cuenta", ""),
                    item.optString("nombre_usuario", ""),
                    item.optString("password", ""),
                    item.optString("sitio_web", ""),
                    item.optString("nota", ""),
                    item.optString("t_registro", ""),
                    item.optString("t_actualizacion", "")));
        }
        return records;
    }

    private static void validatePayload(JSONObject payload) throws Exception {
        if (!"polipass-backup".equals(payload.optString("format"))) {
            throw new IllegalArgumentException("El archivo no es un respaldo válido de PoliPass");
        }
        if (payload.optInt("version", -1) != VaultBackupCrypto.FORMAT_VERSION) {
            throw new IllegalArgumentException("Versión de respaldo no compatible");
        }
        if (!(payload.get("records") instanceof JSONArray)) {
            throw new IllegalArgumentException("El respaldo no contiene registros válidos");
        }
    }

    private static byte[] readAll(Context context, Uri source) throws Exception {
        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            if (in == null) {
                throw new IllegalStateException("No se pudo abrir el archivo de respaldo");
            }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                if (out.size() > 25_000_000) {
                    throw new IllegalArgumentException("El archivo de respaldo es demasiado grande");
                }
            }
            java.util.Arrays.fill(buffer, (byte) 0);
            return out.toByteArray();
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
