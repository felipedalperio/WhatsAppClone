package com.eren.whatsappclone.helper;

import com.eren.whatsappclone.model.Usuario;

public class UsuarioDigitando {

    public static Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

    public static void statusOnlineOffline(boolean status){
        try {
            if(status){
                usuarioLogado.setStatusDigitando("digitando...");
                usuarioLogado.atualizarDigitar();
            }else{
                usuarioLogado.setStatusDigitando("");
                usuarioLogado.atualizarDigitar();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
