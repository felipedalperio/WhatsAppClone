package com.eren.whatsappclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.eren.whatsappclone.R;
import com.eren.whatsappclone.config.ConfiguracaoFirebase;
import com.eren.whatsappclone.helper.Base64Custom;
import com.eren.whatsappclone.helper.UsuarioFirebase;
import com.eren.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastrarActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);

        campoNome = findViewById(R.id.editTextNameCadastrar);
        campoEmail = findViewById(R.id.editTextEmailCadastrar);
        campoSenha = findViewById(R.id.editTextSenhaCadastrar);
    }

    public void cadastrarUsuario(Usuario usuario){
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        firebaseAuth.createUserWithEmailAndPassword(usuario.getEmail(),usuario.getSenha()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){


                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                    finish();
                    try {
                        String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId(identificadorUsuario);
                        usuario.salvar();
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(CadastrarActivity.this, "algo deu errado", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    String execao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        execao = "Digite uma senha mais forte! ";
                    }catch (FirebaseAuthInvalidCredentialsException e ){
                        execao = "Por favor, digite um email válido! ";
                    }catch (FirebaseAuthUserCollisionException e){
                        execao = "Essa conta já foi cadastrada";
                    }catch (Exception e){
                        execao = "Erro ao cadastrar usuário: "+e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastrarActivity.this,execao,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void validandoCadastro(View view){
        //Recuperando o texto:
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        //validando as informações
        if(!textoNome.isEmpty()){
            if(!textoEmail.isEmpty()){
                if(!textoSenha.isEmpty()){

                    Usuario usuario= new Usuario();

                    usuario.setEmail(textoEmail);
                    usuario.setNome(textoNome);
                    usuario.setSenha(textoSenha);

                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText(this, "Prenchaa o campo senha", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Preencha o campo email", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Preencha o campo nome", Toast.LENGTH_SHORT).show();
        }
    }

    public void fecharCadastro(View view){
        finish();
    }
}