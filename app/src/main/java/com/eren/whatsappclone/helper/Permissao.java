package com.eren.whatsappclone.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissao(String[] permissoes, Activity activity,int requestCode){
        if(Build.VERSION.SDK_INT >= 23){
            List<String> listaPermissao = new ArrayList<>();

            for (String permissao: permissoes) {
               Boolean tempermissao = ContextCompat.checkSelfPermission(activity,permissao) == PackageManager.PERMISSION_GRANTED;

               if(!tempermissao) listaPermissao.add(permissao);

            }
            
            if(listaPermissao.isEmpty()) return false;

            String[] novasPermissoes = new String[listaPermissao.size()];
            listaPermissao.toArray(novasPermissoes);

           ActivityCompat.requestPermissions(activity,novasPermissoes,requestCode);

        }
        return true;
    }

}
