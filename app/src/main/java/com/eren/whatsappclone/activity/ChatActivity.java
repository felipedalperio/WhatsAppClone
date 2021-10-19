package com.eren.whatsappclone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.eren.whatsappclone.adapter.MensagensAdapter;
import com.eren.whatsappclone.config.ConfiguracaoFirebase;
import com.eren.whatsappclone.helper.Base64Custom;
import com.eren.whatsappclone.helper.UsuarioDigitando;
import com.eren.whatsappclone.helper.UsuarioFirebase;
import com.eren.whatsappclone.helper.UsuarioOnline;
import com.eren.whatsappclone.model.Conversa;
import com.eren.whatsappclone.model.Mensagem;
import com.eren.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eren.whatsappclone.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private TextView textViewNome;
    private CircleImageView circleImageFoto;
    private Usuario usuarioDestinatario;
    private EditText editMensagem;
    private ImageView imagemCamera;

    private String idUsuarioRementente;
    private String idUsuarioDestinatario;
    private String idUsuarioLogado;
    private RecyclerView recyclerMensagem;
    private DatabaseReference database;
    private DatabaseReference mensagemRef;
    private StorageReference storage;

    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    private ChildEventListener childEventListenerMensagens;
    private ChildEventListener childEventListenerUsuario;
    private LinearLayout linearLayoutVisualizarMarcado;
    private TextView textViewVisualizarMarcado;
    private TextView textViewStatus;

    public static Thread thread = null;
    static Handler handler = new Handler();
    private Usuario usuarioLogado;
    private DatabaseReference usuarioRef;
    private DatabaseReference usuarioRef2;
    private Mensagem mensagemRemetente;
    private TextView status;
    private Conversa conversaRemetente = new Conversa();;
    private Handler mHandler = new Handler();
    private String digitando = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();



        textViewNome = findViewById(R.id.textViewNomeChat);
        status = findViewById(R.id.textViewStatus);
        textViewStatus = findViewById(R.id.textViewStatus);
        circleImageFoto = findViewById(R.id.circleImageFoto);
        editMensagem = findViewById(R.id.editMensagem);
        linearLayoutVisualizarMarcado = findViewById(R.id.linearLayoutVisualizarMarcado);
        //recuperando informações do usuario remetente:
        idUsuarioRementente = UsuarioFirebase.getIdentificadorUsuario();
        recyclerMensagem = findViewById(R.id.recyclerMensagens);
        textViewVisualizarMarcado = findViewById(R.id.textViewVisualizarMarcado);
        imagemCamera = findViewById(R.id.imageCamera);
        mensagemRemetente= new Mensagem();

        //recuperando informações do usuario destinatario:
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
            textViewNome.setText(usuarioDestinatario.getNome());

            UsuarioOnline.statusOnlineOffline(true);

            String foto = usuarioDestinatario.getFoto();
            if (foto != null) {
                Uri url = Uri.parse(usuarioDestinatario.getFoto());
                Glide.with(ChatActivity.this).load(url).into(circleImageFoto);


            } else {
                circleImageFoto.setImageResource(R.drawable.padrao);
            }



            //recuperando dados do Usuario destinatario:
            idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            idUsuarioLogado = Base64Custom.codificarBase64(usuarioLogado.getEmail());
            usuarioDestinatario.setId(idUsuarioDestinatario);



            Usuario usuario = new Usuario();
            usuario.setIdDestinatario(idUsuarioDestinatario);
            usuario.atualizarIdDestinatario();

            database = ConfiguracaoFirebase.getFirebaseDataBase();
            //RECUPERANDO USUSARIOS
            usuarioRef = database.child("usuarios");
            usuarioRef2 = database.child("usuarios").child(idUsuarioDestinatario);

            recuperarUsuario();
            digitandoFirebase();
            digitando();

        }

        //ADAPTER:
        adapter = new MensagensAdapter(mensagens, getApplicationContext());
        //CONFIGURAÇÃO RECYCLERVIEW:
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagem.setLayoutManager(layoutManager);
        recyclerMensagem.setHasFixedSize(true);
        recyclerMensagem.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDataBase();
        storage = ConfiguracaoFirebase.getStorageReference();

        mensagemRef = database.child("mensagens")
                .child(idUsuarioRementente)
                .child(idUsuarioDestinatario);
//=-=-=-=-=-=-=

        //EVENTO DE CLICK NA CAMERA:
        imagemCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(ChatActivity.this);
            }
        });



        marcado();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap imagem = null;
        mensagens.clear();
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            try {
                Uri resultUri = result.getUri();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),resultUri);

                if(imagem != null){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //CRIANDO ID PARA A IMAGEM:
                    final String nomeImagem = UUID.randomUUID().toString();
                    //REFERENCIAS DO FIREBASE:
                    StorageReference imagemRef = storage
                            .child("imagens")
                            .child("fotos")
                            .child(idUsuarioRementente)
                            .child(nomeImagem);
                    //SALVANDO IMAGEM:



                    //gerando Url
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.continueWithTask(
                            new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task)
                                        throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return imagemRef.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri downloadUri = task.getResult();
                                final String downloadUrl = downloadUri.toString();

                                final Mensagem mensagem = new Mensagem();
                                mensagem.setIdUsuario(idUsuarioRementente);
                                mensagem.setMensagem("imagem.jpeg");
                                mensagem.setImagem(downloadUrl);

                                // Salvar mensagem remetente
                                salvarMensagem(idUsuarioRementente, idUsuarioDestinatario, mensagem);

                                // Salvar mensagem destinatario
                                salvarMensagem(idUsuarioDestinatario, idUsuarioRementente, mensagem);

                                imagemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        mensagem.setImagem(downloadUrl);
                                        Toast.makeText(ChatActivity.this, "Sucesso ao enviar foto!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ChatActivity.this, "Erro ao enviar foto!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }else {

                                Toast.makeText(ChatActivity.this,
                                        "Erro ao fazer upload da foto!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
            Toast.makeText(this, "error" , Toast.LENGTH_SHORT).show();
        }
    }

    public void enviarMensagem(View view){
        //REINCIANDO O PADRÃO
        linearLayoutVisualizarMarcado.setVisibility(View.GONE);


        String textomensagem = editMensagem.getText().toString();
        if(!textomensagem.isEmpty()){
            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario( idUsuarioRementente );
            mensagem.setMensagem(textomensagem);
            mensagem.setMensagemMarcada(MensagensAdapter.mensagemMarcada);

            mensagemRemetente = mensagem;

            //Salvar mensagem para remetente:
            salvarMensagem(idUsuarioRementente,idUsuarioDestinatario,mensagem);

            //Salvar mensagem para remetente:
            salvarMensagem(idUsuarioDestinatario,idUsuarioRementente,mensagem);

            //salvando conversa:
            salvarConversa(mensagem);

        }else{
            Toast.makeText(ChatActivity.this,"Digite uma mensagem para enviar!",Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarConversa(Mensagem msg) {

        salvarConversa(idUsuarioRementente,idUsuarioDestinatario,usuarioDestinatario,msg);

        usuarioLogado.setId(idUsuarioLogado);
        salvarConversa(idUsuarioDestinatario,idUsuarioRementente,usuarioLogado,msg);

    }

    private void salvarConversa(String idRemetente,String idDestinatario,Usuario usuario,Mensagem msg){
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference conversaRef = database.child("conversas");
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUsuarioExibicao(usuario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        conversaRef.child(idRemetente).child(idDestinatario).setValue(conversaRemetente);


    }

    private void digitando(){
        editMensagem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!editMensagem.getText().toString().isEmpty()){
                    UsuarioDigitando.statusOnlineOffline(true);
                }else{
                    UsuarioDigitando.statusOnlineOffline(false);
                }
            }
        });

    }


    private void salvarMensagem(String idRemetente,String idDestinatario,Mensagem msg){
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference mensagemRef = database.child("mensagens");
        mensagemRef.child(idRemetente).child(idDestinatario).push().setValue(msg);

        editMensagem.setText("");
        MensagensAdapter.mensagemMarcada = "";


    }

    @Override
    protected void onStart() {
        super.onStart();
        UsuarioOnline.statusOnlineOffline(true);
        recuperarMensagens();

    }


    @Override
    protected void onPause() {
        super.onPause();
        UsuarioOnline.statusOnlineOffline(false);
        UsuarioDigitando.statusOnlineOffline(false);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mensagemRef.removeEventListener(childEventListenerMensagens);
        usuarioRef.removeEventListener(childEventListenerUsuario);
        UsuarioDigitando.statusOnlineOffline(false);
    }

    private void recuperarMensagens(){
        mensagens.clear();
        childEventListenerMensagens = mensagemRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                recyclerMensagem.scrollToPosition(mensagens.size() -1);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void recuperarUsuario(){
        childEventListenerUsuario = usuarioRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                String idUsuarioRecuperado = Base64Custom.codificarBase64(usuario.getEmail());

                if (idUsuarioRecuperado.equals(idUsuarioDestinatario)) {
                    textViewStatus.setText(usuario.getStatus());
                }



            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                String idUsuarioRecuperado = Base64Custom.codificarBase64(usuario.getEmail());

                if (idUsuarioRecuperado.equals(idUsuarioDestinatario)) {
                    UsuarioOnline.statusOnlineOffline(true);
                    if (usuarioDestinatario.getStatusDigitando().equals("")) {
                        textViewStatus.setText(usuario.getStatus());
                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void digitandoFirebase(){
        usuarioRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 Usuario usuario = snapshot.getValue(Usuario.class);
                 usuarioDestinatario = usuario;
               try {
                   if(usuario.getIdDestinatario().equals(idUsuarioLogado)) {
                       if (!usuario.getStatusDigitando().equals("")) {
                           textViewStatus.setText(usuario.getStatusDigitando());
                       } else {
                           textViewStatus.setText(usuario.getStatus());
                       }
                   }
               }catch (Exception e){

               }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    private void marcado(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            thread = new Thread(new Runnable() {
                @Override
                public void run(){
                    synchronized(this){
                        while (true) {
                            try {
                                Thread.sleep(340);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //update ui on UI thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!MensagensAdapter.mensagemMarcada.equals("")) {
                                        linearLayoutVisualizarMarcado.setVisibility(View.VISIBLE);
                                        textViewVisualizarMarcado.setText(MensagensAdapter.mensagemMarcada);
                                    }
                                }

                            });
                        }
                    }
                }
            });

        }

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MensagensAdapter.mensagemMarcada = "";
        thread.interrupt();

    }



}