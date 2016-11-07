package uk.me.hallam.neopixel;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by phallam on 04/11/16.
 */

public class SendCommandTask extends AsyncTask<URL, Void, String> {

    public MainActivity delegate = null;

    @Override
    protected String doInBackground(URL... url) {

        String response = "";
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url[0].openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return response;
        }

        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processHttpResponse(result);
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 512);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

}
