package com.baegmon.overwatchclips;

import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.retrieval.params.SubmissionSort;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Clip> clips;
    private ArrayList<Clip> favoriteClips;
    private Adapter adapter;
    private Resource resource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        clips = new ArrayList<>();
        favoriteClips = new ArrayList<>();

        resource = new Resource(this, clips, favoriteClips);
        retrieveFavorites();
        visitSubreddit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

    }

    public Resource getResource(){
        return resource;
    }

    private void createDialog(){
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

    @Override
    protected void onPause() {
        super.onPause();
        saveFavorites();

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
        String json = preferences.getString("FAVORITES", "");
        Type type = new TypeToken<ArrayList<Clip>>(){}.getType();
        ArrayList<Clip> retrievedFavorites = new Gson().fromJson(json, type);
        resource.setFavorites(retrievedFavorites);
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
            createDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void visitSubreddit(){
        List<Submission> submission;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletionService<List<Submission>> completionService = new ExecutorCompletionService<>(executorService);

        completionService.submit(new Callable<List<Submission>>() {
            @Override
            public List<Submission> call() throws Exception {
                RestClient restClient = new PoliteHttpRestClient();
                restClient.setUserAgent("OverwatchClips/1.0 by Simon Baeg");

                // Handle to Submissions, which offers the basic API submission functionality
                Submissions subms = new Submissions(restClient);

                // Retrieve submissions of a submission
                return subms.ofSubreddit("overwatch", SubmissionSort.HOT, -1, 100, null, null, true);
            }
        });

        try {
            final Future<List<Submission>> completedFuture = completionService.take();
            submission = completedFuture.get();

            String link = "https://gfycat.com/";
            String link2 = "http://gfycat.com/";
            String source = "https://www.reddit.com";

            for(Submission s : submission){
                if(s.getDomain().contains("gfycat.com")){
                    char s_char = s.getURL().charAt(4);
                    if(Character.toString(s_char).equals("s")){
                        Clip clip = new Clip(s.getTitle(), s.getUrl(), s.getUrl().replaceAll(link, ""), source + s.getPermalink());

                        if(checkFavorite(clip)){
                            clip.favorite();
                        }

                        resource.getClips().add(clip);


                    } else {
                        Clip clip = new Clip(s.getTitle(), s.getUrl(), s.getUrl().replaceAll(link2, ""), source + s.getPermalink());

                        if(checkFavorite(clip)){
                            clip.favorite();
                        }

                        resource.getClips().add(clip);
                    }
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
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
