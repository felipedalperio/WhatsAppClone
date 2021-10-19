package com.eren.whatsappclone.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eren.whatsappclone.R;
import com.eren.whatsappclone.activity.ChatActivity;
import com.eren.whatsappclone.helper.UsuarioFirebase;
import com.eren.whatsappclone.model.Mensagem;
import com.eren.whatsappclone.model.Usuario;

import java.util.List;

public class DigitandoAdapter extends RecyclerView.Adapter<DigitandoAdapter.MyViewHolder> {

    private Usuario usuario;
    private Context context;

    public DigitandoAdapter(Usuario usuario, Context c){
        this.usuario = usuario;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat,parent,false);
        return new DigitandoAdapter.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario user= usuario;
//        Boolean status = user.getStatusDigitando();
//        holder.textViewStatus.setText(status + "");

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);

        }
    }




}
