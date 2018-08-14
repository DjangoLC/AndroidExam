package com.example.eperez.androidexam.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eperez.androidexam.Modelos.Ruta;
import com.example.eperez.androidexam.R;

import java.util.List;

public class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.VHClientList> {

    private List<Ruta> data;
    private Context context;
    Ruta current_ruta=null;
    private listener listener;

    public RutasAdapter(List<Ruta> data, Context context, listener listener){
        this.data = data;
        this.context = context;
        this.listener = listener;

    }


    public interface listener{
        void onClick(Ruta article);
    }


    @NonNull
    @Override
    public RutasAdapter.VHClientList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_ruta, parent, false);

        return new RutasAdapter.VHClientList(itemView);
    }



    @Override
    public void onBindViewHolder(@NonNull RutasAdapter.VHClientList holder, int position) {

        if (data!=null){

            if (data.get(0) instanceof Ruta)
                current_ruta  = (Ruta) data.get(position);

            holder.ruta_distance.setText(current_ruta.getDistance());
            holder.ruta_hora.setText(String.valueOf(current_ruta.getHora()));

        } else{
            Log.i("asd","data is null here");
        }
        holder.bind(current_ruta,listener);



    }

    @Override
    public int getItemCount() {
        return this.data == null ? 0 : this.data.size();
    }

    public class VHClientList extends RecyclerView.ViewHolder{

        private TextView ruta_hora,ruta_distance;

        public VHClientList(View itemView) {
            super(itemView);

            ruta_hora     = (TextView)  itemView.findViewById(R.id.tv_hora);
            ruta_distance     = (TextView)  itemView.findViewById(R.id.tv_distancia);

        }

        public void bind(final Ruta item, final listener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onClick(item);
                }
            });
        }


    }

}