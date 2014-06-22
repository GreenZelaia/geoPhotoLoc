package com.example.geophotoloc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	private static final String TAG = "geoPhotoLoc";
	
	protected static final String EXTRA_RES_ID = "POS";
	
	ScheduledFuture<?> mTimer;
	
	LocationManager mLocationManager;
	LocationListener mLocationListener;
	Location mLocation;
	ListaFotosAdapter mListaFotosAdapter;
	
	SharedPreferences mPreferencias;
	
	int mRadio;
	int mCantidadFotos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mLocation = new Location("Me");
		
		mLocation.setLongitude(intent.getDoubleExtra("longitud", 0));
		mLocation.setLatitude(intent.getDoubleExtra("latitud", 0));
		
		
		mListaFotosAdapter = new ListaFotosAdapter(this, R.layout.itemlistafotos, new ArrayList<ListaFotosItem>());
		getListView().setEmptyView(findViewById( R.layout.listavacia ));
		getListView().setAdapter(mListaFotosAdapter);
		
		mPreferencias =	getSharedPreferences("GeoPhotoLocPreferences",Context.MODE_PRIVATE);
		
		mRadio = mPreferencias.getInt("radio", 10);
		mCantidadFotos = mPreferencias.getInt("cantidad", 20);
		
		updateDisplay(mLocation);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mRadio = mPreferencias.getInt("radio", 10);
		mCantidadFotos = mPreferencias.getInt("cantidad", 20);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menuConfiguracion:
	    	dialogoConfiguracion();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void dialogoConfiguracion() {
		
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialogogeolocalizar);
		
		final TextView txtFotosNum = (TextView) dialog.findViewById(R.id.txtFotosNum);
		final TextView txtRadioNum = (TextView) dialog.findViewById(R.id.txtRadioNum);
		
		txtFotosNum.setText(String.valueOf(mCantidadFotos));
		txtRadioNum.setText(String.valueOf(mRadio));
		
		final SeekBar skbFotos = (SeekBar) dialog.findViewById(R.id.skbFotos);
		
		skbFotos.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				txtFotosNum.setText(String.valueOf(seekBar.getProgress()));
				mCantidadFotos = seekBar.getProgress();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				txtFotosNum.setText(String.valueOf(progress));
			}
			
		});
		
		skbFotos.setProgress(mCantidadFotos);
		
		final SeekBar skbKm = (SeekBar) dialog.findViewById(R.id.skbKm);
		
		skbKm.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				txtRadioNum.setText(String.valueOf(seekBar.getProgress()));
				mRadio = seekBar.getProgress();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				txtRadioNum.setText(String.valueOf(progress));
			}
			
		});
		
		skbKm.setProgress(mRadio);
		
		final Button btnAceptarDialog = (Button) dialog.findViewById(R.id.btnAceptar);
		
		btnAceptarDialog.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = mPreferencias.edit();
				editor.putInt("radio", mRadio);
				editor.putInt("cantidad", mCantidadFotos);
				editor.commit();
				dialog.dismiss();
			}
			
		});
		
		dialog.show();
		
	}

	// Update display
	private void updateDisplay(Location location) {
		
		if(location != null){ // de no haber aun un lonlat sacamos un mensaje para que lo intente mas adelante
			Log.d(TAG, "update display con location");
    		double angulo = mRadio * 0.0089833458; // para crear los bounds del mapa en 5 km mas o menos -- angulo por kilometro = 360 / (2 * pi * 6378)
    		new HttpAsyncTask().execute("http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to="+mCantidadFotos+"&minx="+(location.getLongitude() - angulo)+"&miny="+(location.getLatitude() - angulo)+"&maxx="+(location.getLongitude() + angulo)+"&maxy="+(location.getLatitude() + angulo)+"&size=medium&mapfilter=true");
    	}
    	else{
    		Log.d(TAG, "update display SIN location");
    		CharSequence text = "No se encuentra la localización, activa la geolocalización y vuelve a intentarlo";
	    	int duration = Toast.LENGTH_SHORT;
	    	Toast toast = Toast.makeText(MainActivity.this, text, duration);
	    	toast.show();
    	}
	}
	
	public Location getLocation() {
		return this.mLocation;
	}

	public void refreshLista() {
		mListaFotosAdapter.notifyDataSetChanged();
	}
	
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
 
            return GET(urls[0]);
        }
        
        @Override
        protected void onPostExecute(String result) {
        	try {
        		mListaFotosAdapter.clear();
				JSONObject json = new JSONObject(result);
				JSONArray fotos = json.getJSONArray("photos");
				for(int i = 0; i < fotos.length(); i++){
					JSONObject foto = fotos.getJSONObject(i);
					mListaFotosAdapter.add(new ListaFotosItem(foto.getString("photo_title"),foto.getString("owner_name"),foto.getString("photo_file_url"),
							Double.parseDouble(foto.getString("longitude")),Double.parseDouble(foto.getString("latitude")),500));
				}
				mListaFotosAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				e.printStackTrace();
			}
        	
       }
    }

	public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
 
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
 
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
 
        return result;
    }
	
	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

	public void itemSeleccionado(ListaFotosItem itemLista) {
		Intent intent = new Intent(this, ActivitySeleccion.class);
        intent.putExtra("itemSeleccionado", itemLista);
        intent.putExtra("longitud", mLocation.getLongitude());
        intent.putExtra("latitud", mLocation.getLatitude());
        intent.putExtra("location", mLocation);
        startActivity(intent);
	}

}
