package com.greenzelaia.geophotoloc.views;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
  
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

public class ListaFotosAdapter extends ArrayAdapter<ListaFotosItem> {
       
    private ArrayList<ListaFotosItem> mArrayItems;
    private Activity mActivity;
  
    public ListaFotosAdapter(Activity a, int itemViewResourceId, ArrayList<ListaFotosItem> entries) {
        super(a, itemViewResourceId, entries);
        this.mArrayItems = entries;
        this.mActivity = a;
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
            LayoutInflater vi = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.itemlistafotos, null);
            holder = new ViewHolder();
            holder.distancia = (TextView) v.findViewById(R.id.txtMetros);
            holder.imagen = (ImageView) v.findViewById(R.id.imvImagen);
            v.setTag(holder);
        } else
            holder = (ViewHolder) v.getTag();
  
        final ListaFotosItem itemLista = mArrayItems.get(position);
        if (itemLista != null) {
        	
        	itemLista.setDistancia(((MainActivity)mActivity).getLocation()); // Desde aqui llamamos al main que le devuelve la location conseguida, luego el objeto LOitem hace el calculo de la distancia (ponia activity, no mActivity :( )
        	
        	NumberFormat format = NumberFormat.getNumberInstance();
        	format.setMinimumFractionDigits(2);
        	format.setMaximumFractionDigits(2);
        	String output = format.format(itemLista.getDistancia());
        	
            holder.distancia.setText("Distancia: "+ output + " Km");
            
            if(itemLista.getImagen() == null){
            	new DownloadImageTask(itemLista).execute(itemLista.getUrl());
            }
            else{
            	holder.imagen.setImageBitmap(itemLista.getImagen());
            }
            
            v.setOnClickListener(new OnClickListener() {
                  
                @Override
                public void onClick(View v) {
                	//((ActivityListaFotos)activity).stopTracking(); Ya no se usa! jonanvc
                	((MainActivity)mActivity).itemSeleccionado(itemLista);
                }
                  
            });
            
        }
        return v;
    }

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ListaFotosItem item;

        public DownloadImageTask(ListaFotosItem item) {
            this.item = item;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
        	item.setImagen(result);
        	((MainActivity)mActivity).refreshLista();
        }
    }
  
} 