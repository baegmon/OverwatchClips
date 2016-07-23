package com.baegmon.overwatchclips.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.baegmon.overwatchclips.Clip;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Task {

    private JSONObject _object;
    private String code = "";
    private Activity _activity;

    public Task(){
        _object = new JSONObject();
    }

    public void downloadClip(Clip clip, Activity activity) throws JSONException{
        String query = "https://gfycat.com/cajax/get/" + clip.getCode();
        code = clip.getCode();
        _activity = activity;
        RetrieveJSONTask task = new RetrieveJSONTask();
        task.execute(query);


    }

    public void downloadFile(String url){
        new DownloadFileFromURL().execute(url);
    }


    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //pDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... params) {
            int count;
            try {
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Overwatch");

                // if the Overwatch directory doesn't exist, create it
                if(!directory.exists()){
                    directory.mkdirs();
                    System.out.println("DIRECTORY CREATED");
                }

                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/Overwatch/" + code + ".mp4");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage

        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            //pDialog.dismiss();
            System.out.println("DOWNLOAD FINISHED");

        }

    }

    private class RetrieveJSONTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection;

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);

                urlConnection.setDoOutput(true);

                urlConnection.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                String jsonString = sb.toString();
                return new JSONObject(jsonString);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try{
                if(result.has("gfyItem")){
                    _object = result.getJSONObject("gfyItem");
                    SharedPreferences preferences = _activity.getSharedPreferences("QualityPreference", _activity.MODE_PRIVATE);
                    int setting = preferences.getInt("QUALITY", 0);

                    String quality = "";

                    switch(setting){
                        case 0:
                            quality = "mp4Url";
                            break;
                        case 1:
                            quality = "mobileUrl";
                            break;
                        case 2:
                            quality = "thumb360Url";
                            break;

                    }

                    System.out.println("URL: " + _object.getString(quality));

                    downloadFile(_object.getString(quality));


                }
            } catch (Exception e){
            }
        }
    }





}
