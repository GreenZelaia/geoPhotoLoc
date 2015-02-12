package com.greenzelaia.geophotoloc.views;

import java.text.NumberFormat;
import java.util.ArrayList;
  
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.greenzelaia.geophotoloc.objects.ListaFotosItem;
import com.greenzelaia.geophotoloc.R;
import com.greenzelaia.geophotoloc.activities.MainActivity;
import com.squareup.picasso.Picasso;

public class ListaFotosAdapter extends ArrayAdapter<ListaFotosItem> {

    public interface itemSelectedCallback {
        void itemSelected(ListaFotosItem itemLista);
    }

    private ArrayList<ListaFotosItem> mArrayItems;
    private Location mLocation;
    itemSelectedCallback callback;
  
    public ListaFotosAdapter(Activity a, int itemViewResourceId, ArrayList<ListaFotosItem> entries) {
        super(a, itemViewResourceId, entries);
        this.mArrayItems = entries;
        this.callback = (itemSelectedCallback) a;
    }
  
    public static class ViewHolder {
        public ImageView imagen;
        public TextView distancia;
    }
      
    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getView(position, convertView, parent);
    }
  
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.itemlistafotos, null);
            holder = new ViewHolder();
            holder.distancia = (TextView) v.findViewById(R.id.txtMetros);
            holder.imagen = (ImageView) v.findViewById(R.id.imvImagen);
            v.setTag(holder);
        }
        else{
            holder = (ViewHolder) v.getTag();
        }
  
        final ListaFotosItem itemLista = mArrayItems.get(position);

        itemLista.setDistancia(mLocation); // Desde aqui llamamos al main que le devuelve la location conseguida, luego el objeto LOitem hace el calculo de la distancia (ponia activity, no mActivity :( )

        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        String output = format.format(itemLista.getDistancia());

        holder.distancia.setText( output + " Km");

        Picasso.with(getContext()).load(itemLista.getUrl()).fit().centerCrop().into(holder.imagen);

        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                callback.itemSelected(itemLista);
            }

        });

        return v;
    }

    public void setLocation(Location location){
        mLocation = location;
    }
  
} 