package com.redsytem.passwordapp.BaseDeDatos;

public class Constants {

    /* Nombre de la base de datos */
    public static final String BD_NAME = "PASSWORD_BD";

    /* Versión de la base de datos */
    public static final int BD_VERSION = 3;

    /* Nombre de la tabla */
    public static final String TABLE_NAME = "PASSWORD_TABLE";

    public static final String C_ID = "ID";
    public static final String C_TITULO = "TITULO"; // legacy columns kept for migration compatibility
    public static final String C_CUENTA = "CUENTA";
    public static final String C_NOMBRE_USUARIO = "NOMBRE_USUARIO";
    public static final String C_PASSWORD = "PASSWORD";
    public static final String C_SITIO_WEB = "SITIO_WEB";
    public static final String C_NOTA = "NOTA";
    public static final String C_TIEMPO_REGISTRO = "TIEMPO_REGISTRO";
    public static final String C_TIEMPO_ACTUALIZACION = "TIEMPO_ACTUALIZACION";
    public static final String C_REGISTRO_CIFRADO = "REGISTRO_CIFRADO";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
            + "("
            + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + C_TITULO + " TEXT," // legacy, intentionally unused after migration
            + C_CUENTA + " TEXT," // legacy, intentionally unused after migration
            + C_NOMBRE_USUARIO + " TEXT," // legacy, intentionally unused after migration
            + C_PASSWORD + " TEXT," // legacy, intentionally unused after migration
            + C_SITIO_WEB + " TEXT," // legacy, intentionally unused after migration
            + C_NOTA + " TEXT," // legacy, intentionally unused after migration
            + C_TIEMPO_REGISTRO + " TEXT,"
            + C_TIEMPO_ACTUALIZACION + " TEXT," 
            + C_REGISTRO_CIFRADO + " TEXT"
            + ")";
}
