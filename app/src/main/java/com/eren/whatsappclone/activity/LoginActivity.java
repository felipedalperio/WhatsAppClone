package com.eren.whatsappclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eren.whatsappclone.R;
import com.eren.whatsappclone.config.ConfiguracaoFirebase;
import com.eren.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail,campoSenha;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        campoEmail = findViewById(R.id.editTextEmailLogin);
        campoSenha = findViewById(R.id.editTextSenhaLogin);
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

    }

    private void logarUsuario(Usuario usuario) {

        firebaseAuth.signInWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    abrirTelaPrincipal();
                }else{
                    String execao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        execao = "Esse usuário não está cadastrado! ";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        execao = "E-mail e senha não correspondem ao Usuário! ";
                    }catch (Exception e){
                    execao = "Erro ao cadastrar usuário "+e.getMessage();
                }
                    Toast.makeText(LoginActivity.this, "Email ou senha incorreta", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void abrirTelaPrincipal(){
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
        finish();
    }


    public void validarLogin(View view) {
        //Recuperando o texto:
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        //validando as informações

        if (!textoEmail.isEmpty()) {
            if (!textoSenha.isEmpty()) {

                Usuario usuario = new Usuario();

                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario(usuario);

            } else {
                Toast.makeText(this, "Prenchaa o campo senha", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Preencha o campo email", Toast.LENGTH_SHORT).show();
        }
    }

    public void abrirTelaCadastro(View view) {
        startActivity(new Intent(LoginActivity.this, CadastrarActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = firebaseAuth.getCurrentUser();
        if(usuarioAtual != null){
            abrirTelaPrincipal();
        }
    }
}