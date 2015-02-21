package com.greenzelaia.geophotoloc.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.greenzelaia.geophotoloc.R;
import com.greenzelaia.geophotoloc.objects.ListaFotosItem;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends ActionBarActivity implements MapEventsReceiver, LocationListener, SensorEventListener {

    protected static int START_INDEX=-2, DEST_INDEX=-1;

    double lat;
    double lon;
    ListaFotosItem itemSeleccionado;

    protected MapView mapView;
    DirectedLocationOverlay myLocationOverlay;
    protected boolean mTrackingMode;
    float mAzimuthAngleSpeed = 0.0f;
    protected Marker markerStart, markerDestination;
    protected GeoPoint startPoint, destinationPoint;
    protected FolderOverlay itineraryMarkers;
    public static Road mRoad;
    protected ArrayList<GeoPoint> viaPoints;
    protected Polyline roadOverlay;
    protected FolderOverlay roadNodeMarkers;

    private final NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
    long mLastTime = 0; // milliseconds
    double mSpeed = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

        Intent intent = getIntent();
        lat = intent.getDoubleExtra("latitud", 0);
        lon = intent.getDoubleExtra("longitud", 0);
        itemSeleccionado = (ListaFotosItem) intent.getSerializableExtra("itemSeleccionado");

        mapView = (MapView) findViewById(R.id.mapview);

        //Inicializar Mapa
        MapBoxTileSource.retrieveMapBoxMapId(this);
        MapBoxTileSource mapBoxTileSource = new MapBoxTileSource();
        mapView.setTileSource(mapBoxTileSource);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        //Configurar Mapa
        GeoPoint initialPOV = new GeoPoint(lat, lon);
        IMapController mapController = mapView.getController();
        mapController.setZoom(13);
        mapController.setCenter(initialPOV);


        //Pintar nuestra localizaci�n actual
        myLocationOverlay = new DirectedLocationOverlay(this);
        mapView.getOverlays().add(myLocationOverlay);

        viaPoints = new ArrayList<>();

        roadNodeMarkers = new FolderOverlay(this);
        mapView.getOverlays().add(roadNodeMarkers);
        itineraryMarkers = new FolderOverlay(this);

        startPoint = new GeoPoint(lat, lon);
        markerStart = updateItineraryMarker(null, startPoint, START_INDEX,
                R.string.departure, R.drawable.marker_departure, -1);


        destinationPoint = new GeoPoint(itemSeleccionado.getLatitud(), itemSeleccionado.getLongitud());
        markerDestination = updateItineraryMarker(null, destinationPoint, DEST_INDEX,
                R.string.destination, R.drawable.marker_destination, -1);

        getRoadAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_image:
                Intent intent = new Intent(this, ImageActivity.class);
                intent.putExtra("itemSeleccionado", itemSeleccionado);
                intent.putExtra("longitud", lon);
                intent.putExtra("latitud", lat);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLocationChanged(final Location pLoc) {
        long currentTime = System.currentTimeMillis();
        if (mIgnorer.shouldIgnore(pLoc.getProvider(), currentTime))
            return;
        double dT = currentTime - mLastTime;
        if (dT < 100.0){
            //Toast.makeText(this, pLoc.getProvider()+" dT="+dT, Toast.LENGTH_SHORT).show();
            return;
        }
        mLastTime = currentTime;

        GeoPoint newLocation = new GeoPoint(pLoc);
        if (!myLocationOverlay.isEnabled()){
            //we get the location for the first time:
            myLocationOverlay.setEnabled(true);
            mapView.getController().animateTo(newLocation);
        }

        GeoPoint prevLocation = myLocationOverlay.getLocation();
        myLocationOverlay.setLocation(newLocation);
        myLocationOverlay.setAccuracy((int)pLoc.getAccuracy());

        if (prevLocation != null && pLoc.getProvider().equals(LocationManager.GPS_PROVIDER)){
			/*
			double d = prevLocation.distanceTo(newLocation);
			mSpeed = d/dT*1000.0; // m/s
			mSpeed = mSpeed * 3.6; //km/h
			*/
            mSpeed = pLoc.getSpeed() * 3.6;
            long speedInt = Math.round(mSpeed);
            //TODO Activar la visualizaci�n de la velocidad?
            //TextView speedTxt = (TextView)findViewById(R.id.speed);
            //speedTxt.setText(speedInt + " km/h");

            //TODO: check if speed is not too small
            if (mSpeed >= 0.1){
                //mAzimuthAngleSpeed = (float)prevLocation.bearingTo(newLocation);
                mAzimuthAngleSpeed = (float)pLoc.getBearing();
                myLocationOverlay.setBearing(mAzimuthAngleSpeed);
            }
        }

        if (mTrackingMode){
            //keep the map view centered on current location:
            mapView.getController().animateTo(newLocation);
            mapView.setMapOrientation(-mAzimuthAngleSpeed);
        } else {
            //just redraw the location overlay:
            mapView.invalidate();
        }

    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }


    final OnItineraryMarkerDragListener mItineraryListener = new OnItineraryMarkerDragListener();
    /** Update (or create if null) a marker in itineraryMarkers. */
    public Marker updateItineraryMarker(Marker item, GeoPoint p, int index,
                                        int titleResId, int markerResId, int imageResId) {
        Drawable icon = getResources().getDrawable(markerResId);
        String title = getResources().getString(titleResId);
        if (item == null){
            item = new Marker(mapView);
            item.setAnchor(Marker.ANCHOR_CENTER, 1.0f);
            item.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble, mapView));
            item.setDraggable(true);
            item.setOnMarkerDragListener(mItineraryListener);
            itineraryMarkers.add(item);
        }
        item.setTitle(title);
        item.setPosition(p);
        item.setIcon(icon);
        if (imageResId != -1){
            item.setImage(getResources().getDrawable(imageResId));
        }

        item.setRelatedObject(index);
        mapView.invalidate();
        //Start geocoding task to update the description of the marker with its address:
        //TODO obtener la direcci�n del punto a�adido
        //new GeocodingTask().execute(item);
        return item;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    class OnItineraryMarkerDragListener implements Marker.OnMarkerDragListener {
        @Override public void onMarkerDrag(Marker marker) {}
        @Override public void onMarkerDragEnd(Marker marker) {
//			int index = (Integer)marker.getRelatedObject();
//			if (index == START_INDEX)
//				startPoint = marker.getPosition();
//			else if (index == DEST_INDEX)
//				destinationPoint = marker.getPosition();
//			else
//				viaPoints.set(index, marker.getPosition());
//			//update route:
//			getRoadAsync();
        }
        @Override public void onMarkerDragStart(Marker marker) {}
    }

    public void getRoadAsync(){
        mRoad = null;
        GeoPoint roadStartPoint = null;
        if (startPoint != null){
            roadStartPoint = startPoint;
        } else if (myLocationOverlay.isEnabled() && myLocationOverlay.getLocation() != null){
            //use my current location as itinerary start point:
            roadStartPoint = myLocationOverlay.getLocation();
        }
        if (roadStartPoint == null || destinationPoint == null){
            updateUIWithRoad(mRoad);
            return;
        }
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
        waypoints.add(roadStartPoint);
        //add intermediate via points:
        for (GeoPoint p:viaPoints){
            waypoints.add(p);
        }
        waypoints.add(destinationPoint);
        new UpdateRoadTask().execute(waypoints);
    }

    /**
     * Async task to get the road in a separate thread.
     */
    private class UpdateRoadTask extends AsyncTask<Object, Void, Road> {
        protected Road doInBackground(Object... params) {
            @SuppressWarnings("unchecked")
            ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
            RoadManager roadManager = null;
            Locale locale = Locale.getDefault();
            roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
            roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
            //TODO A�adir opci�n de elegir tipo de ruta
//			switch (whichRouteProvider){
//			case OSRM:
//				roadManager = new OSRMRoadManager();
//				break;
//			case MAPQUEST_FASTEST:
//				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
//				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
//				break;
//			case MAPQUEST_BICYCLE:
//				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
//				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
//				roadManager.addRequestOption("routeType=bicycle");
//				break;
//			case MAPQUEST_PEDESTRIAN:
//				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
//				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
//				roadManager.addRequestOption("routeType=pedestrian");
//				break;
//			case GOOGLE_FASTEST:
//				roadManager = new GoogleRoadManager();
//				//roadManager.addRequestOption("mode=driving"); //default
//				break;
//			default:
//				return null;
//			}
            return roadManager.getRoad(waypoints);
        }
        protected void onPostExecute(Road result) {
            mRoad = result;
            updateUIWithRoad(result);
            //getPOIAsync(poiTagText.getText().toString());
        }
    }

    //Cargar la ruta en el mapa
    void updateUIWithRoad(Road road){
        roadNodeMarkers.getItems().clear();
        //TextView textView = (TextView)findViewById(R.id.routeInfo);
        //textView.setText("");
        List<Overlay> mapOverlays = mapView.getOverlays();
        if (roadOverlay != null){
            mapOverlays.remove(roadOverlay);
        }
        if (road == null)
            return;
        if (road.mStatus == Road.STATUS_TECHNICAL_ISSUE)
            Toast.makeText(mapView.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
        roadOverlay = RoadManager.buildRoadOverlay(road, mapView.getContext());
        Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
        //we set the road overlay at the "bottom", just above the MapEventsOverlay,
        //to avoid covering the other overlays.
        mapOverlays.add(removedOverlay);
        putRoadNodes(road);
        mapView.invalidate();
        //Set route info in the text view:
        //textView.setText(road.getLengthDurationText(-1));
    }

    private void putRoadNodes(Road road){
        roadNodeMarkers.getItems().clear();
        Drawable icon = getResources().getDrawable(R.drawable.marker_node);
        int n = road.mNodes.size();
        MarkerInfoWindow infoWindow = new MarkerInfoWindow(R.layout.bonuspack_bubble, mapView);
        TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
        for (int i=0; i<n; i++){
            RoadNode node = road.mNodes.get(i);
            String instructions = (node.mInstructions==null ? "" : node.mInstructions);
            Marker nodeMarker = new Marker(mapView);
            nodeMarker.setTitle("Step " + (i+1));
            nodeMarker.setSnippet(instructions);
            nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
            nodeMarker.setIcon(icon);
            nodeMarker.setPosition(node.mLocation);

            nodeMarker.setInfoWindow(infoWindow); //use a shared infowindow.
            int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
            if (iconId != R.drawable.ic_empty){
                Drawable image = getResources().getDrawable(iconId);
                nodeMarker.setImage(image);
            }
            roadNodeMarkers.add(nodeMarker);
        }
        iconIds.recycle();
    }
}
