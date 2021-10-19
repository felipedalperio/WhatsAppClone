package com.eren.whatsappclone.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.eren.whatsappclone.R;
import com.eren.whatsappclone.activity.ChatActivity;
import com.eren.whatsappclone.activity.MainActivity;
import com.eren.whatsappclone.adapter.ConversasAdapter;
import com.eren.whatsappclone.config.ConfiguracaoFirebase;
import com.eren.whatsappclone.helper.Base64Custom;
import com.eren.whatsappclone.helper.RecyclerItemClickListener;
import com.eren.whatsappclone.helper.UsuarioFirebase;
import com.eren.whatsappclone.model.Conversa;
import com.eren.whatsappclone.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private List<Conversa> listaConversa = new ArrayList<>();
    private Set<Conversa> listaSemRepeticao= new HashSet<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversaRef;
    private ChildEventListener childEventListenerConversas  ;
    private Handler mHandler = new Handler();
    private Usuario usuarioLogado;
    private String idUsuarioLogado;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_conversas, container, false);

        recyclerViewConversas = view.findViewById(R.id.recyclerViewListConversas);

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        idUsuarioLogado = Base64Custom.codificarBase64(usuarioLogado.getEmail());
        //adapter:
        adapter = new ConversasAdapter(listaConversa, getActivity());
        //RecyclerView:
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager(layoutManager);
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setAdapter(adapter);

        recyclerViewConversas.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerViewConversas, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Conversa conversaSelecionada = listaConversa.get(position);
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra("chatContato",conversaSelecionada.getUsuarioExibicao());
                startActivity(i);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));

        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDataBase();
        conversaRef = database.child("conversas").child(identificadorUsuario);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversa();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversaRef.removeEventListener(childEventListenerConversas);
    }

    private void recuperarConversa(){
        listaConversa.clear();
        childEventListenerConversas = conversaRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversa conversa = snapshot.getValue(Conversa.class);
                listaConversa.add(conversa);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                lerAtualizacaoBanco();

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

    private void lerAtualizacaoBanco(){
        conversaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaConversa.clear();
                for (DataSnapshot childSnapshot :snapshot.getChildren()) {
                    listaConversa.add(childSnapshot.getValue(Conversa.class));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}