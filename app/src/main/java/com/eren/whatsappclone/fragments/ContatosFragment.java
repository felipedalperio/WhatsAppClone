package com.eren.whatsappclone.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.eren.whatsappclone.R;
import com.eren.whatsappclone.activity.ChatActivity;
import com.eren.whatsappclone.adapter.ContatoAdapter;
import com.eren.whatsappclone.config.ConfiguracaoFirebase;
import com.eren.whatsappclone.helper.RecyclerItemClickListener;
import com.eren.whatsappclone.helper.UsuarioFirebase;
import com.eren.whatsappclone.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatoAdapter adapter;
    private ArrayList<Usuario> listaContato = new ArrayList<>();
    private DatabaseReference usuarioRef;
    private ChildEventListener childEventListenerContatos;
    private ProgressBar progressBarContatos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewContatos);
        progressBarContatos = view.findViewById(R.id.progressBarContatos);

        usuarioRef = ConfiguracaoFirebase.getFirebaseDataBase().child("usuarios");

        //adapter:
        adapter = new ContatoAdapter(listaContato,getActivity());
        //Configuracao RecyclerView:
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaContatos.setLayoutManager(layoutManager);
        recyclerViewListaContatos.setHasFixedSize(true);
        recyclerViewListaContatos.setAdapter( adapter );


        //evento de click no recyclerView:
        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuariSelecionado  = listaContato.get(position);
                                Intent i = new Intent(getActivity(), ChatActivity.class);
                                i.putExtra("chatContato",usuariSelecionado);
                                startActivity(i);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        })
        );

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioRef.removeEventListener(childEventListenerContatos);
    }

    public void recuperarContatos(){
        listaContato.clear();
        progressBarContatos.setVisibility(View.VISIBLE);
        childEventListenerContatos = usuarioRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    listaContato.add(usuario);
                    adapter.notifyDataSetChanged();
                    progressBarContatos.setVisibility(View.INVISIBLE);
                }catch (Exception e){

                }

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



}