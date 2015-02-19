package com.greenzelaia.geophotoloc.objects;

import java.io.Serializable;

import android.location.Location;

public class ListaFotosItem implements Serializable{
	
	private static final long serialVersionUID = 1L;
      
    private String mTitulo;
    private String mAutor;
    private String mUrl;
    private double mLongitud;
    private double mLatitud;
    private double mDistancia;

	public ListaFotosItem(String titulo, String autor, String url,
			double longitud, double latitud, double distancia) {
		super();
		this.mTitulo = titulo;
		this.mAutor = autor;
		this.mUrl = url;
		this.mLongitud = longitud;
		this.mLatitud = latitud;
		this.mDistancia = distancia;
	}

    
	public String getTitulo() {
		return mTitulo;
	}

	public void setTitulo(String titulo) {
		this.mTitulo = titulo;
	}

	public String getAutor() {
		return mAutor;
	}

	public void setAutor(String autor) {
		this.mAutor = autor;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		this.mUrl = url;
	}

	public double getLongitud() {
		return mLongitud;
	}

	public void setLongitud(double longitud) {
		this.mLongitud = longitud;
	}

	public double getLatitud() {
		return mLatitud;
	}

	public void setLatitud(double latitud) {
		this.mLatitud = latitud;
	}

	public double getDistancia() {
		return mDistancia;
	}

	public void setDistancia(Location location) {
		Location locationPropia = new Location("");
		locationPropia.setLongitude(this.mLongitud);
		locationPropia.setLatitude(this.mLatitud);
		this.mDistancia = locationPropia.distanceTo(location) / 1000;
	}
      
} 