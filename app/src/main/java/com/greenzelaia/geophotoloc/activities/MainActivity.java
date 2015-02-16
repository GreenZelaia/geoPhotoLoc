package com.greenzelaia.geophotoloc.activities;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.greenzelaia.geophotoloc.utils.ImageGetTask;
import com.greenzelaia.geophotoloc.utils.LocationGatherer;
import com.greenzelaia.geophotoloc.views.ListaFotosAdapter;
import com.greenzelaia.geophotoloc.objects.ListaFotosItem;
import com.greenzelaia.geophotoloc.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity implements ImageGetTask.ImageGetTaskCallback, ListaFotosAdapter.itemSelectedCallback, LocationGatherer.LocationGathererCallback {

	private static final String TAG = "geoPhotoLoc";
    private static final String TAG_DIALOG_FRAGMENT = "dialog_fragment";

    GridView gridView;
    ListaFotosAdapter mListaFotosAdapter;

    LocationGatherer locationGatherer;
	Location mLocation;
	
	SharedPreferences mPreferencias;
	
	int mRadio;
	int mCantidadFotos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

		mLocation = new Location("Me");

        gridView = (GridView) findViewById(R.id.gridView);

		mListaFotosAdapter = new ListaFotosAdapter(this, R.layout.itemlistafotos, new ArrayList<ListaFotosItem>());
		gridView.setAdapter(mListaFotosAdapter);
		
		mPreferencias =	getSharedPreferences("GeoPhotoLocPreferences",Context.MODE_PRIVATE);
		
		mRadio = mPreferencias.getInt("radio", 10);
		mCantidadFotos = mPreferencias.getInt("cantidad", 20);

        locationGatherer = LocationGatherer.getInstance();
        locationGatherer.requestLocation(this);
	}

    @Override
    public void locationObtained() {
        mLocation = locationGatherer.getLocation();
        mListaFotosAdapter.setLocation(mLocation);
        updateDisplay();
    }

    // Update display
    private void updateDisplay() {
        if(mLocation != null){ // de no haber aun un lonlat sacamos un mensaje para que lo intente mas adelante
            Log.d(TAG, "update display con location");
            double angulo = mRadio * 0.0089833458; // para crear los bounds del mapa en 5 km mas o menos -- angulo por kilometro = 360 / (2 * pi * 6378)
            new ImageGetTask(this).execute("http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to=" + mCantidadFotos + "&minx=" +
                    (mLocation.getLongitude() - angulo) + "&miny=" + (mLocation.getLatitude() - angulo) + "&maxx=" + (mLocation.getLongitude() + angulo) +
                    "&maxy=" + (mLocation.getLatitude() + angulo) + "&size=medium&mapfilter=true");
        }
        else{
            Log.d(TAG, "update display SIN location");
            CharSequence text = "No se encuentra la localizaci�n, activa la geolocalizaci�n y vuelve a intentarlo";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(MainActivity.this, text, duration);
            toast.show();
        }
    }

    @Override
    public void imageGetPostExecute(String result) {
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

    @Override
    public void itemSelected(ListaFotosItem itemLista) {
        Intent intent = new Intent(this, ActivitySeleccion.class);
        intent.putExtra("itemSeleccionado", itemLista);
        intent.putExtra("longitud", mLocation.getLongitude());
        intent.putExtra("latitud", mLocation.getLatitude());
        intent.putExtra("location", mLocation);
        startActivity(intent);
    }

	@Override
	protected void onResume() {
		super.onResume();
		mRadio = mPreferencias.getInt("radio", 10);
		mCantidadFotos = mPreferencias.getInt("cantidad", 20);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menuConfiguracion:
            Intent intent = new Intent(this, OptionsActivity.class);
            startActivity(intent);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

}
