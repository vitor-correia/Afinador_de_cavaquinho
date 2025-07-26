package com.twobrotherscompany.afinadordecavaquinho;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class RegistroFirebase {

    private static RegistroFirebase instancia;
    private FirebaseAnalytics firebaseAnalytics;

    private RegistroFirebase(Context contexto) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(contexto);
    }

    public static synchronized RegistroFirebase getInstance(Context contexto) {
        if (instancia == null) {
            instancia = new RegistroFirebase(contexto.getApplicationContext());
        }
        return instancia;
    }

    public void registrarVisualizacaoTela(String nomeTela, String classeTela) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, nomeTela);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, classeTela);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    public void registrarEvento(String nomeEvento, Bundle parametros) {
        firebaseAnalytics.logEvent(nomeEvento, parametros);
    }

    public void registrarItemSelecionado(String idItem, String nomeItem, String categoriaItem) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, idItem);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, nomeItem);
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, categoriaItem);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void registrarCompra(String idItem, String nomeItem, String moeda, double valor) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, idItem);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, nomeItem);
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, moeda);
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, valor);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle);
    }

    public void definirIdUsuario(String idUsuario) {
        firebaseAnalytics.setUserId(idUsuario);
    }

    public void definirPropriedadeUsuario(String nome, String valor) {
        firebaseAnalytics.setUserProperty(nome, valor);
    }

    /**

     Registrar Visualização de Tela
     RegistroFirebase registroFirebase = RegistroFirebase.getInstance(getContext());
    registroFirebase.registrarVisualizacaoTela("TelaPrincipal", "MainActivity");

    Registrar Evento Genérico
    Bundle parametros = new Bundle();
    parametros.putString("parametro_personalizado", "valor");
    registroFirebase.registrarEvento("evento_personalizado", parametros);

     Registrar Item Selecionado
    registroFirebase.registrarItemSelecionado("id_item_123", "Nome do Item", "Categoria do Item");

     Registrar Compra
    registroFirebase.registrarCompra("id_item_123", "Nome do Item", "BRL", 9.99);

     Definir ID de Usuário
     registroFirebase.definirIdUsuario("usuario_123");

     Definir Propriedade de Usuário
    registroFirebase.definirPropriedadeUsuario("cor_favorita", "azul");

    */
}