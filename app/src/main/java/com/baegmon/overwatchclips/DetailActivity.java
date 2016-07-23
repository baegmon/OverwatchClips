package com.baegmon.overwatchclips;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION = "position";
    public static String TITLE = "";
    private VideoView video;
    private MediaController mediaController;
    private static final String TAG_LINK = "mp4Url";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);

        video = (VideoView) findViewById(R.id.video);

        StringBuffer link = new StringBuffer("");
        Clip clip = (Clip) getIntent().getSerializableExtra("Clip");
        String query = "https://gfycat.com/cajax/get/" + clip.getCode();
        RetrieveJSONTask task = new RetrieveJSONTask();
        task.execute(query);
        getSupportActionBar().setTitle(TITLE);

        TextView title = (TextView) findViewById(R.id.clip_title);
        title.setText(clip.getTitle());

        //TextView videoSize = (TextView) findViewById(R.id.video_size);


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

    private void startClip(JSONObject jsonObject) throws JSONException {
        Uri uri = Uri.parse(jsonObject.getString(TAG_LINK));

        video.setVideoURI(uri);
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


}