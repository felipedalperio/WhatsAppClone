package com.eren.whatsappclone.helper;

import com.eren.whatsappclone.model.Usuario;

public class UsuarioOnline {
   public static Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

    public static void statusOnlineOffline(boolean status){
        try {
            if(status){
                usuarioLogado.setStatus("online");
                usuarioLogado.atualizarStatus();
            }else{
                usuarioLogado.setStatus("offline");
                usuarioLogado.atualizarStatus();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}


