package com.greenzelaia.geophotoloc.views;

import android.app.Activity;
import android.os.Bundle;
import android.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.greenzelaia.geophotoloc.R;

public class OptionsDialog extends DialogFragment {

    public interface OptionsDialogCallback {
        void onFinishOptionsDialog(int mRadio, int mCantidadFotos);
    }

    private TextView txtRadioNum;
    private TextView txtFotosNum;
    private SeekBar skbFotos;
    private SeekBar skbKm;
    private Button btnAceptarDialog;
    private OptionsDialogCallback callback;

    private int mRadio;
    private int mCantidadFotos;

    public static OptionsDialog newInstance(int mRadio, int mCantidadFotos) {
        OptionsDialog dialog = new OptionsDialog();

        Bundle args = new Bundle();
        args.putInt("radio", mRadio);
        args.putInt("fotos", mCantidadFotos);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (OptionsDialogCallback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRadio = getArguments().getInt("radio");
        mCantidadFotos = getArguments().getInt("fotos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogogeolocalizar, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        txtFotosNum = (TextView) view.findViewById(R.id.txtFotosNum);
        txtRadioNum = (TextView) view.findViewById(R.id.txtRadioNum);
        skbKm = (SeekBar) view.findViewById(R.id.skbKm);
        skbFotos = (SeekBar) view.findViewById(R.id.skbFotos);
        btnAceptarDialog = (Button) view.findViewById(R.id.btnAceptar);

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

        btnAceptarDialog.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                callback.onFinishOptionsDialog(mRadio, mCantidadFotos);
                getDialog().dismiss();
            }
        });

        return view;
    }

}