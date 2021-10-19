package com.eren.whatsappclone.model;

import com.eren.whatsappclone.config.ConfiguracaoFirebase;
import com.eren.whatsappclone.helper.UsuarioFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {

    private String Id;
    private String Nome;
    private String Email;
    private String senha;
    private String foto;
    private String status;
    private String statusDigitando;
    private String idDestinatario;

    public Usuario(){

    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference usuario = firebaseRef.child("usuarios").child(getId());
        usuario.setValue(this);
    }

    public void atualizar(){
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference usuarioRef = database.child("usuarios")
                .child(identificadorUsuario);

        Map<String,Object> valoresUsuario = converterParaMap();
        usuarioRef.updateChildren(valoresUsuario);
    }

    public void atualizarIdDestinatario(){
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference usuarioRef = database.child("usuarios")
                .child(identificadorUsuario);

        Map<String,Object> valoresUsuario =   converterParaMapIdDestinatario();
        usuarioRef.updateChildren(valoresUsuario);
    }

    public void atualizarStatus(){
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference usuarioRef = database.child("usuarios")
                .child(identificadorUsuario);

        Map<String,Object> statusAtualizar = converterParaMapStatus();
        usuarioRef.updateChildren(statusAtualizar);
    }

    public void atualizarDigitar(){
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference usuarioRef = database.child("usuarios")
                .child(identificadorUsuario);

        Map<String,Object> statusAtualizar = converterParaMapDigitar();
        usuarioRef.updateChildren(statusAtualizar);
    }

    @Exclude
    public Map<String ,Object> converterParaMapStatus(){
        HashMap<String,Object> usuarioMap =  new HashMap<>();
        usuarioMap.put("status",getStatus());
        return usuarioMap;
    }

    @Exclude
    public Map<String ,Object> converterParaMapDigitar(){
        HashMap<String,Object> usuarioMap =  new HashMap<>();
        usuarioMap.put("statusDigitando",getStatusDigitando());
        return usuarioMap;
    }

    @Exclude
    public Map<String ,Object> converterParaMap(){
        HashMap<String,Object> usuarioMap =  new HashMap<>();
        usuarioMap.put("email",getEmail());
        usuarioMap.put("nome",getNome());
        usuarioMap.put("foto",getFoto());
        return usuarioMap;
    }

    @Exclude
    public Map<String ,Object> converterParaMapIdDestinatario(){
        HashMap<String,Object> usuarioMap =  new HashMap<>();
        usuarioMap.put("idDestinatario",getIdDestinatario());
        return usuarioMap;
    }



    @Exclude
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String nome) {
        Nome = nome;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDigitando() {
        return statusDigitando;
    }

    public void setStatusDigitando(String statusDigitando) {
        this.statusDigitando = statusDigitando;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }
}
