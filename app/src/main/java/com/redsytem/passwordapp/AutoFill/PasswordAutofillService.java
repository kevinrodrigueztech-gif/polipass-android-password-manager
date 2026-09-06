package com.redsytem.passwordapp.AutoFill;

import android.app.assist.AssistStructure;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.util.Pair;

import android.os.CancellationSignal;
import android.widget.RemoteViews;
import com.redsytem.passwordapp.BaseDeDatos.BDHelper;
import com.redsytem.passwordapp.BaseDeDatos.Constants;
import com.redsytem.passwordapp.Encriptacion.Encrypt;
import com.redsytem.passwordapp.Modelo.Password;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.View;
import android.os.CancellationSignal;
import android.widget.RemoteViews;

import com.redsytem.passwordapp.BaseDeDatos.BDHelper;
import com.redsytem.passwordapp.Modelo.Password;
import com.redsytem.passwordapp.R;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


public class PasswordAutofillService extends AutofillService {

BDHelper bdHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        bdHelper = new BDHelper(this);
    }

    @Override
    public void onFillRequest(@NonNull FillRequest fillRequest, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback fillCallback) {

    }


    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
        // Implementar lógica para manejar solicitudes de guardado de datos de autofill
    }

    private String obtenerSitioWebDelRequest(FillRequest request) {
        List<FillContext> contexts = request.getFillContexts();
        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        FillContext context = contexts.get(contexts.size() - 1);
        AssistStructure.ViewNode rootViewNode = context.getStructure().getWindowNodeAt(0).getRootViewNode();

        // Asumiendo que tienes una forma de identificar el campo que contiene la URL del sitio web
        for (int i = 0; i < rootViewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = rootViewNode.getChildAt(i);
            AutofillId id = childNode.getAutofillId();
            AutofillValue value = childNode.getAutofillValue();

            if (value != null && value.isText()) {
                return value.getTextValue().toString();
            }
        }

        return null;
    }







}
