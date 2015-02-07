package com.greenzelaia.geophotoloc.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ImageGetTask extends AsyncTask<String, Void, String> {

    public interface ImageGetTaskCallback {
        void imageGetPostExecute(String result);
    }

    ImageGetTaskCallback callback;

    public ImageGetTask(ImageGetTaskCallback callback){
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... urls) {
        return GET(urls[0]);
    }

    private String GET(String url){
        InputStream inputStream;
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
            Log.d("Get", e.getLocalizedMessage());
        }

        return result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        callback.imageGetPostExecute(result);
    }

}