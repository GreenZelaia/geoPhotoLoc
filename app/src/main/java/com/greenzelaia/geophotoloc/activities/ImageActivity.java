package com.greenzelaia.geophotoloc.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.greenzelaia.geophotoloc.R;
import com.greenzelaia.geophotoloc.objects.ListaFotosItem;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoView;

public class ImageActivity extends ActionBarActivity {

    PhotoView photoView;
    ListaFotosItem itemSeleccionado;
    double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //Publicidad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.getMenu().clear();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        photoView = (PhotoView) findViewById(R.id.photoView);

        Intent intent = getIntent();

        lat = intent.getDoubleExtra("latitud", 0);
        lon = intent.getDoubleExtra("longitud", 0);
        itemSeleccionado = (ListaFotosItem) intent.getSerializableExtra("itemSeleccionado");

        Picasso.with(this).load(itemSeleccionado.getUrl()).into(photoView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("itemSeleccionado", itemSeleccionado);
                intent.putExtra("longitud", lon);
                intent.putExtra("latitud", lat);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
