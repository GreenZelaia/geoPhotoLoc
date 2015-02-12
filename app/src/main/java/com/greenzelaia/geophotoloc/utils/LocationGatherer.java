package com.greenzelaia.geophotoloc.utils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

public class LocationGatherer implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public interface LocationGathererCallback{
        void locationObtained();
    }

    private static volatile LocationGatherer instance = new LocationGatherer();

    public static LocationGatherer getInstance(){
        if (instance == null) {
            synchronized (LocationGatherer.class) {
                instance = new LocationGatherer();
            }
        }
        return instance;
    }

    private LocationGatherer()
    {

    }

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Context context;

    public Location getLocation(){
        return mCurrentLocation;
    }

	public void requestLocation(Context context) {
        this.context = context;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        mGoogleApiClient.connect();
	}

    @Override
    public void onConnected(Bundle arg0) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        ((LocationGathererCallback)context).locationObtained();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO well...
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO Send message back to the activity ;)
    }


}
