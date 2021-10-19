package com.eren.whatsappclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eren.whatsappclone.R;
import com.eren.whatsappclone.config.ConfiguracaoFirebase;
import com.eren.whatsappclone.helper.Base64Custom;
import com.eren.whatsappclone.helper.Permissao;
import com.eren.whatsappclone.helper.UsuarioFirebase;
import com.eren.whatsappclone.helper.UsuarioOnline;
import com.eren.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracaoActivity extends AppCompatActivity {
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonGaleria;
    private CircleImageView circleImageViewPerfil;
    private StorageReference storageReference;
    private String identificadorUsuario;
    private EditText editPerfilNome;
    private FirebaseUser usuario;
    private Usuario usuarioLogado;
    private ProgressBar progressBarFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        Permissao.validarPermissao(permissoesNecessarias,this,1);
        imageButtonGaleria = findViewById(R.id.imageButtonCamera);
        circleImageViewPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        storageReference = ConfiguracaoFirebase.getStorageReference();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        editPerfilNome = findViewById(R.id.editPerfilNome);
        progressBarFoto = findViewById(R.id.progressBarFoto);
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configuração");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperando dados do usuario:
        usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        //colocando a imagem de perfil do usuario:
        if(url != null){
            Glide.with(ConfiguracaoActivity.this)
                    .load(url)
                    .into(circleImageViewPerfil);
        }else{
            circleImageViewPerfil.setImageResource(R.drawable.padrao);
        }

        //colocando o nome
        editPerfilNome.setText(usuario.getDisplayName());

        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(ConfiguracaoActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap imagem = null;
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                try {
                    Uri resultUri = result.getUri();
                    imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    if(imagem != null) {
                        progressBarFoto.setVisibility(View.VISIBLE);
                        circleImageViewPerfil.setImageBitmap(imagem);
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this,""+error, Toast.LENGTH_SHORT).show();
            }

            if(imagem != null) {
                //RECUPERANDO DADOS DO FIREBASE (IMAGEM)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] dadosImagem = baos.toByteArray();

                //SALVANDO NO FIREBASE:
                StorageReference imagemRef = storageReference
                        .child("imagens")
                        .child("perfil")
                        .child(identificadorUsuario)
                        .child("perfil.jpg");
                //aqui ele salva no firebase:
                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ConfiguracaoActivity.this, "Erro ao fazer upload de imagem", Toast.LENGTH_SHORT).show();
                        progressBarFoto.setVisibility(View.GONE);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(ConfiguracaoActivity.this, "Sucesso ao fazer upload de imagem", Toast.LENGTH_SHORT).show();
                        progressBarFoto.setVisibility(View.GONE);
                    }
                });

                //RECUPERANDO A URL DA FOTO
                imagemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri url = uri;
                        atualizarFotoUsuario(url);
                    }
                });
            }
        }

    }

    private void atualizarFotoUsuario(Uri url) {
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if(retorno) {
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualizar();
        }else{
            Toast.makeText(this, "sua foto não foi alterada!", Toast.LENGTH_SHORT).show();
        }
        progressBarFoto.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for ( int permissaoResultado:grantResults ){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValdiacaoPermissao();
            }
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        UsuarioOnline.statusOnlineOffline(true);



    }


    @Override
    protected void onPause() {
        super.onPause();
        UsuarioOnline.statusOnlineOffline(false);
    }


    private void alertaValdiacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o App é necessário aceitar todas as Permissões ");
        builder.setCancelable(false); // nao tem como cancelar
        builder.setPositiveButton("Confimar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String nome = editPerfilNome.getText().toString();
        if(!nome.equals(usuario.getDisplayName())) {
            boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);
            if(retorno){
               usuarioLogado.setNome(nome);
               usuarioLogado.atualizar();
            }else{
                Toast.makeText(this, "erro ao atualizar nome. ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}