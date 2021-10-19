package com.eren.whatsappclone.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eren.whatsappclone.R;
import com.eren.whatsappclone.activity.ChatActivity;
import com.eren.whatsappclone.helper.UsuarioFirebase;
import com.eren.whatsappclone.model.Mensagem;
import com.eren.whatsappclone.model.Usuario;

import java.util.List;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;

    private List<Mensagem> mensagens;
    private Context context;
    public static boolean marcado =  false;
    public static String mensagemMarcada =  "";

    public MensagensAdapter(List<Mensagem> lista, Context c){
        this.mensagens = lista;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;
        if(viewType ==TIPO_REMETENTE){
            item = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.adapter_mensagem_remetente,
                    parent,
                    false);
        }else if(viewType == TIPO_DESTINATARIO){
                item = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.adapter_mensagem_destinatario,
                        parent,
                        false);
        }
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Mensagem mensagem = mensagens.get(position);
        String msg = mensagem.getMensagem();
        String imagem = mensagem.getImagem();
        String msgMarcada = mensagem.getMensagemMarcada();
        holder.mensagem.setVisibility(View.VISIBLE);
        holder.imagem.setVisibility(View.VISIBLE);

        if(msgMarcada != null) {
            if (!msgMarcada.equalsIgnoreCase("")) {
                holder.linearMarcado.setVisibility(View.VISIBLE);
                holder.marcado.setText(msgMarcada);
            } else {
                holder.linearMarcado.setVisibility(View.GONE);
            }
        }

        if(imagem != null){
            holder.linearMarcado.setVisibility(View.GONE);
            Uri url =Uri.parse(imagem);
            Glide.with(context).load(url).into(holder.imagem);
            holder.mensagem.setVisibility(View.GONE);
        }else{
            holder.mensagem.setText(msg);
            holder.imagem.setVisibility(View.GONE);
        }

        holder.linearLayoutMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensagemM = msg;
                mensagemMarcada = mensagemM;
                try {
                    if(!ChatActivity.thread.isAlive()){
                        ChatActivity.thread.start();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    @Override
    public int getItemViewType(int position) {
       Mensagem mensagem = mensagens.get(position);
       String idUsuario = UsuarioFirebase.getIdentificadorUsuario();
       if(idUsuario.equals(mensagem.getIdUsuario())){
           return TIPO_REMETENTE;
       }else {
           return TIPO_DESTINATARIO;
       }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView mensagem;
        TextView marcado;
        LinearLayout linearMarcado,linearLayoutMensagem;
        ImageView imagem;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mensagem = itemView.findViewById(R.id.textMensagemTexto);
            imagem = itemView.findViewById(R.id.imagemMensagemFoto);
            marcado = itemView.findViewById(R.id.textViewMensagemMarcada);
            linearMarcado = itemView.findViewById(R.id.linearLayoutMensagemMarcada);
            linearLayoutMensagem = itemView.findViewById(R.id.linearLayoutMensagem);
        }
    }


}
