package com.baegmon.overwatchclips;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.baegmon.overwatchclips.Fragment.CardContentFragment;
import com.baegmon.overwatchclips.Fragment.SavedContentFragment;
import com.baegmon.overwatchclips.Utility.Resource;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Clip> clips;
    private ArrayList<Clip> favoriteClips;
    private Adapter adapter;
    private Resource resource;
    private AdView adview;
    private RedditClient redditClient;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createLoadingProgressDialog();

        clips = new ArrayList<>();
        favoriteClips = new ArrayList<>();

        resource = new Resource(this, clips, favoriteClips);
        retrieveFavorites();


        adview = (AdView) findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder().build();
        adview.loadAd(request);


        // OAUTH2 AUTHORIZATION
        UserAgent myUserAgent = UserAgent.of("Android", "com.baegmon.overwatchclips", "1.0", "/u/baegmon");
        redditClient = new RedditClient(myUserAgent);
        final Credentials credentials = Credentials.userlessApp("RbGH0I3I2hBtjw", UUID.randomUUID());
        authorize(credentials);

    }

    public void createLoadingProgressDialog(){
        pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Loading Clips");
        pDialog.show();
    }

    public void createView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    public void authorize(Credentials credentials) {

        new AsyncTask<Credentials, Void, OAuthData>(){
            @Override
            protected OAuthData doInBackground(Credentials... credentials) {
                try {
                    return redditClient.getOAuthHelper().easyAuth(credentials[0]);
                } catch (OAuthException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(OAuthData oAuthData) {
                redditClient.authenticate(oAuthData);
                visitSubreddit();
            }
        }.execute(credentials);
    }

    public void visitSubreddit() {

        new AsyncTask<Void, Void, ArrayList<String>>() {

            @Override
            protected ArrayList<String> doInBackground(Void... params) {

                int settings = getSubredditSettings();

                SubredditPaginator paginator = new SubredditPaginator(redditClient);
                paginator.setSubreddit("overwatch");
                paginator.setLimit(50);

                switch(settings){
                    case 0:
                        paginator.setSorting(Sorting.HOT);
                        paginator.setTimePeriod(TimePeriod.DAY);

                        break;
                    case 1:
                        paginator.setSorting(Sorting.TOP);
                        paginator.setTimePeriod(TimePeriod.ALL);

                        break;
                    case 2:
                        paginator.setSorting(Sorting.RISING);
                        paginator.setTimePeriod(TimePeriod.DAY);

                        break;
                    default:
                        paginator.setSorting(Sorting.HOT);
                        paginator.setTimePeriod(TimePeriod.DAY);

                        break;
                }


                Listing<Submission> submissions = paginator.next();

                String link = "https://gfycat.com/";
                String link2 = "http://gfycat.com/";
                String source = "https://www.reddit.com";

                for(Submission s : submissions){

                    if(s.getUrl().contains("gfycat.com")){
                        char s_char = s.getUrl().charAt(4);

                        if(Character.toString(s_char).equals("s")){
                            Clip clip = new Clip(s.getTitle(), s.getUrl(), s.getUrl().replaceAll(link, ""), source + s.getPermalink(), s.getAuthor());

                            if(checkFavorite(clip)){
                                clip.favorite();
                            }

                            resource.getClips().add(clip);

                        } else {
                            Clip clip = new Clip(s.getTitle(), s.getUrl(), s.getUrl().replaceAll(link2, ""), source + s.getPermalink(), s.getAuthor());

                            if(checkFavorite(clip)){
                                clip.favorite();
                            }

                            resource.getClips().add(clip);
                        }

                    }

                }

                pDialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createView();
                    }
                });


                return null;
            }
        }.execute();
    }

    public Resource getResource(){
        return resource;
    }

    private void createSettingsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Select Quality");
        builder.setNegativeButton("CANCEL", null);
        final String[] choices = { "High",
                             "Medium",
                             "Low"   };

        int quality = getQuality();
        builder.setSingleChoiceItems(choices, quality, null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                setQuality(selectedPosition);
            }
        });


        builder.show();

    }

    private void createAboutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("About");
        builder.setMessage("This application was built using various tools:" +
                "\n - JRAW: To access data from Reddit" +
                "\n - AndroidVideoCache: To cache videos" +
                "\n - GSON: To convert Java Objects" +
                "\n - Picasso: To load thumbnail images"
                + "\n \n"
                + "Please email any questions, suggestions or bugs to: baegmon@gmail.com"

        );

        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void createSubredditSettingsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Select Sorting");
        builder.setNegativeButton("CANCEL", null);
        final String[] choices = { "Hot","Top","Rising" };

        int subreddit = getSubredditSettings();
        builder.setSingleChoiceItems(choices, subreddit, null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                setSubredditSettings(selectedPosition);
                resource.getClips().clear();
                createLoadingProgressDialog();
                visitSubreddit();
                callUpdate();

            }
        });


        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveFavorites();

    }

    public void setSubredditSettings(int setting){
        SharedPreferences preferences = getSharedPreferences("RetrievalPreference", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("SUBREDDIT", setting);
        editor.commit();
    }

    public int getSubredditSettings(){
        SharedPreferences preferences = getSharedPreferences("RetrievalPreference" , MODE_PRIVATE);
        int setting = preferences.getInt("SUBREDDIT", 0);
        return setting;
    }

    public void setQuality(int setting){
        SharedPreferences preferences = getSharedPreferences("QualityPreference", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("QUALITY", setting);
        editor.commit();
    }

    public int getQuality(){
        SharedPreferences preferences = getSharedPreferences("QualityPreference" , MODE_PRIVATE);
        int setting = preferences.getInt("QUALITY", 0);
        return setting;
    }

    public void saveFavorites(){
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String favoritesJson = new Gson().toJson(resource.getFavorites());
        editor.putString("FAVORITES", favoritesJson);
        editor.commit();
    }

    private void retrieveFavorites(){
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if(preferences.contains("FAVORITES")){
            String json = preferences.getString("FAVORITES", "");
            Type type = new TypeToken<ArrayList<Clip>>(){}.getType();
            ArrayList<Clip> retrievedFavorites = new Gson().fromJson(json, type);
            resource.setFavorites(retrievedFavorites);
        }

    }

    public void callUpdate(){
        adapter.notifyDataSetChanged();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new CardContentFragment(), "Clips");
        adapter.addFragment(new SavedContentFragment(), "Favorite");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public int getItemPosition(Object item) {
            return POSITION_NONE;
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            createSettingsDialog();
        } else if (id == R.id.action_sorting){
            createSubredditSettingsDialog();
        } else if (id == R.id.action_about){
            createAboutDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean checkFavorite(Clip clip){

        if(resource.getFavorites() == null){
            return false;
        }

        if(resource.getFavorites().isEmpty()){
            return false;
        }

        for(int i = 0 ; i < resource.getFavorites().size(); i++){
            if(clip.getCode().equals(resource.getFavorites().get(i).getCode())){
                return true;
            }
        }

        return false;
    }



}
