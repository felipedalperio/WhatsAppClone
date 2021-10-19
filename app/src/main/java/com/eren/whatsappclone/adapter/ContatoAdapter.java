package com.eren.whatsappclone.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eren.whatsappclone.R;
import com.eren.whatsappclone.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContatoAdapter extends RecyclerView.Adapter<ContatoAdapter.MyViewHolder> {

    private List<Usuario> contatos;
    private Context context;

    public ContatoAdapter(List<Usuario> contatos, Context context) {
        this.contatos = contatos;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos,parent,false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario usuario = contatos.get(position);
        holder.nome.setText(usuario.getNome());
        holder.email.setText(usuario.getEmail());

        if(usuario.getFoto() != null){
            Uri uri = Uri.parse(usuario.getFoto());
            Glide.with(context).load(uri).into(holder.foto);
        }else{
            holder.foto.setImageResource(R.drawable.padrao);
        }

    }

    @Override
    public int getItemCount() {
        return contatos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView foto;
        TextView nome, email;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.imageViewFotoContato);
            nome = itemView.findViewById(R.id.textNomeContato);
            email = itemView.findViewById(R.id.textEmailContato);

        }
    }
}
