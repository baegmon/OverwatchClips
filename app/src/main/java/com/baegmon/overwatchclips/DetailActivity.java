package com.baegmon.overwatchclips;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.danikula.videocache.HttpProxyCacheServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    private VideoView video;
    private MediaController mediaController;
    private int setting = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        SharedPreferences preferences = getSharedPreferences("QualityPreference", MODE_PRIVATE);
        setting = preferences.getInt("QUALITY", 0);
        System.out.println("QUALITY: " + setting);

        video = (VideoView) findViewById(R.id.video);
        Clip clip = (Clip) getIntent().getSerializableExtra("Clip");
        String query = "https://gfycat.com/cajax/get/" + clip.getCode();

        RetrieveJSONTask task = new RetrieveJSONTask();
        task.execute(query);

        TextView title = (TextView) findViewById(R.id.clip_title);
        title.setText(clip.getTitle());

    }

    private void startClip(JSONObject jsonObject) throws JSONException {

        // 0 = High 1 = Medium 2 = Low
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

        String url = jsonObject.getString(quality);
        HttpProxyCacheServer proxy = new HttpProxyCacheServer(getApplicationContext());
        String proxyURL = proxy.getProxyUrl(url);
        video.setVideoPath(proxyURL);

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaController = new MediaController(DetailActivity.this);
                mp.setLooping(true);
                video.setMediaController(mediaController);
                mediaController.setAnchorView(video);

            }
        });

        video.start();

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
        protected void onPostExecute(JSONObject jsonObject) {
            try{
                if(jsonObject.has("gfyItem")){
                    startClip(jsonObject.getJSONObject("gfyItem"));
                }
            } catch (Exception e){
            }
        }
    }


}