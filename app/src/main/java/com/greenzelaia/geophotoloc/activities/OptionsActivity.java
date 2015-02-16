package com.greenzelaia.geophotoloc.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.greenzelaia.geophotoloc.R;

public class OptionsActivity extends ActionBarActivity {

    private TextView txtRadioNum;
    private TextView txtFotosNum;
    private SeekBar skbFotos;
    private SeekBar skbKm;

    private SharedPreferences mPreferencias;

    private int mRadio;
    private int mCantidadFotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

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

        mPreferencias =	getSharedPreferences("GeoPhotoLocPreferences", Context.MODE_PRIVATE);

        mRadio = mPreferencias.getInt("radio", 10);
        mCantidadFotos = mPreferencias.getInt("cantidad", 20);

        txtFotosNum = (TextView) findViewById(R.id.txtFotosNum);
        txtRadioNum = (TextView) findViewById(R.id.txtRadioNum);
        skbKm = (SeekBar) findViewById(R.id.skbKm);
        skbFotos = (SeekBar) findViewById(R.id.skbFotos);

        skbFotos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        skbKm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mPreferencias.edit();
        editor.putInt("radio", mRadio);
        editor.putInt("cantidad", mCantidadFotos);
        editor.commit();
    }

}
