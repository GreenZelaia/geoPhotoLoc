package com.greenzelaia.geophotoloc.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.greenzelaia.geophotoloc.R;
import com.greenzelaia.geophotoloc.activities.MainActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

public class GetLocation extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	TextView mTextViewLocation;
	
	LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
	Location mCurrentLocation;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.location);
		
		mTextViewLocation = (TextView) findViewById(R.id.textViewLocation);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
	}

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        mTextViewLocation.setText("Conectado a GPlayServices para localización");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mTextViewLocation.setText(mCurrentLocation.toString());


        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("latitud", mCurrentLocation.getLatitude());
        intent.putExtra("longitud", mCurrentLocation.getLongitude());
        startActivity(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private boolean servicesConnected() {
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "Conectado correctamente a play services");
            return true;
        }
        else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
	       case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
	           switch (resultCode) {
	               case Activity.RESULT_OK:
	                   Log.d(TAG, "error resuelto por play services");
	                   mTextViewLocation.setText("Conectado. Error resuelto por GPlay services");
	                   break;
	               default:
	                   Log.d(TAG, "el error no ha podido ser resuelto por play services");
	                   mTextViewLocation.setText("Desconectado. Error no ha sido resuelto por GPlay services");
                       break;
	           }
	       default:
                Log.d(TAG,"error desconocido");
                break;
	   }
	 }

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            }
            catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
        else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }



    private final static String TAG = "geoPhotoLoc - Location";

    public static class ErrorDialogFragment extends DialogFragment {

        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    protected class GetAddressTask extends AsyncTask<Location, Void, String> {

        Context localContext;

        public GetAddressTask(Context context) {
            super();
            localContext = context;
        }

        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());
            Location location = params[0];
            List <Address> addresses = null;

            try {

                addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1
                );

                }
            catch (IOException exception1) {
                    Log.e(TAG, "ioexception");
                    exception1.printStackTrace();
                    return ("ioexception");
                }
            catch (IllegalArgumentException exception2) {

                    String errorString = "illegal argument exception";

                    Log.e(TAG, errorString);
                    exception2.printStackTrace();

                    return errorString;
                }
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);

                    String addressText = getString(R.string.address_output_string,
                            address.getMaxAddressLineIndex() > 0 ?
                                    address.getAddressLine(0) : "",
                            address.getLocality(),
                            address.getCountryName()
                    );
                    return addressText;
                }
                else {
                  return "No address found";
                }
        }

        @Override
        protected void onPostExecute(String address) {
            mTextViewLocation.setText(address);
        }
    }
    

    private void showErrorDialog(int errorCode) {
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
        if (errorDialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
        }
    }
}
