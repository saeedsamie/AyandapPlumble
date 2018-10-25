package com.morlunk.mumbleclient;

import android.os.AsyncTask;
import android.util.Log;

import com.morlunk.mumbleclient.app.LoginActivity;
import com.morlunk.mumbleclient.app.RecentChatsFragment;
import com.morlunk.mumbleclient.app.SignupActivity;
import com.morlunk.mumbleclient.app.VerificationActivity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ServerFetchAsync extends AsyncTask<Void, Void, JSONObject> {
    List<NameValuePair> nameValuePairs;
    String jsonResponse;
    RecentChatsFragment recentChatsFragment;
    OnTaskCompletedListener listener;
    private SignupActivity signupActivity;
    private VerificationActivity verificationActivity;
    private LoginActivity loginActivity;

    public ServerFetchAsync(List<NameValuePair> nameValuePairs,OnTaskCompletedListener onTaskCompletedListener) {
        this.nameValuePairs = nameValuePairs;
        this.listener = onTaskCompletedListener;
    }


    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }


    @Override
    protected JSONObject doInBackground(Void... voids) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://192.168.2.18/SqliteTest/sqlite-vps.php");
        try {

            UrlEncodedFormEntity form;
            form = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            Log.e("requests elements", "request -----" + nameValuePairs.toString());

            // Use UrlEncodedFormEntity to send in proper format which we need
            httpPost.setEntity(form);

            HttpResponse response = httpClient.execute(httpPost);
            InputStream inputStream = response.getEntity().getContent();
            jsonResponse = getStringFromInputStream(inputStream);
            Log.e("response", "response -----" + jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject s) {
        try {
            listener.onTaskCompleted(new JSONObject(jsonResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPostExecute(s);
    }
}
