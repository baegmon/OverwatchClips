package com.baegmon.overwatchclips;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;


import com.baegmon.overwatchclips.Fragment.CardContentFragment;
import com.baegmon.overwatchclips.Fragment.SavedContentFragment;
import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.retrieval.params.SubmissionSort;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

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
    private String KEY = "CLIPS";
    private Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clips = new ArrayList<>();
        favoriteClips = new ArrayList<>();

        visitSubreddit();

        //favoriteClips.add(clips.get(1));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        _context = this;

    }

    public void updateClips(ArrayList<Clip> toUpdate){
        clips = toUpdate;
    }

    public ArrayList<Clip> getClips() {
        return clips;
    }

    public void updateFavorites(ArrayList<Clip> toUpdate){
        favoriteClips = toUpdate;
    }

    private Adapter adapter;
    private CardContentFragment fragment;
    private SavedContentFragment favoriteFragment;

    public void callUpdate(){
        adapter.notifyDataSetChanged();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());

        fragment = new CardContentFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY, clips);
        fragment.setArguments(bundle);

        favoriteFragment = new SavedContentFragment();
        Bundle favoriteBundle = new Bundle();
        favoriteBundle.putSerializable("FAVORITE", favoriteClips);
        favoriteFragment.setArguments(favoriteBundle);

        adapter.addFragment(fragment, "Clips");
        adapter.addFragment(favoriteFragment, "Favorite");

        if(!clips.isEmpty()){

            MyOnItemClickListener onItemClickListener = new MyOnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {

                    if (view.findViewById(R.id.favorite_button) == view) {
                        Clip clip = clips.get(position);
                        ImageView favoriteButton = (ImageView) view.findViewById(R.id.favorite_button);

                        // if the clip is already favorited
                        if(clip.isFavorited()){
                            clip.favorite();

                            DrawableCompat.setTint(favoriteButton.getDrawable(), ContextCompat.getColor(_context, R.color.unfavorite));

                            // Update Clips Fragment
                            updateClipsFragment();
                            /*
                            // Update Favorite fragment
                            Bundle favoriteArgument = favoriteFragment.getArguments();
                            favoriteArgument.clear();
                            favoriteClips.remove(clip);
                            Bundle f = new Bundle();
                            f.putSerializable("FAVORITE", favoriteClips);
                            favoriteArgument.putAll(f);

                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.detach(favoriteFragment);
                            ft.attach(favoriteFragment);
                            ft.commit();
                            */
                            favoriteClips.remove(clip);


                        } else {
                            // otherwise the clip was not previously a favorite, change it to favorite
                            clip.favorite();

                            DrawableCompat.setTint(favoriteButton.getDrawable(), ContextCompat.getColor(_context, R.color.favorite));

                            // Update Clips Fragment
                            updateClipsFragment();
                            /*
                            // Update Favorite fragment
                            Bundle favoriteArgument = favoriteFragment.getArguments();
                            favoriteArgument.clear();
                            favoriteClips.add(clip);
                            Bundle f = new Bundle();
                            f.putSerializable("FAVORITE", favoriteClips);
                            favoriteArgument.putAll(f);

                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.detach(favoriteFragment);
                            ft.attach(favoriteFragment);
                            ft.commit();
                            */
                            favoriteClips.add(clip);


                        }

                        adapter.notifyDataSetChanged();

                    } else if (view.findViewById(R.id.card_image) == view){
                        openClip(position);
                    }

                }
            };

            fragment.setOnItemClickListener(onItemClickListener);
        }
        viewPager.setAdapter(adapter);

    }

    public void openClip(int position){
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("Clip", clips.get(position));
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
        ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
    }

    public void updateClipsFragment(){
        // Update Clips Fragment
        Bundle clipArgument = fragment.getArguments();
        clipArgument.clear();
        Bundle b = new Bundle();
        b.putSerializable(KEY, clips);
        clipArgument.putAll(b);
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
            return true;
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

            for(Submission s : submission){
                if(s.getDomain().contains("gfycat.com")){

                    char s_char = s.getURL().charAt(4);
                    if(Character.toString(s_char).equals("s")){
                        Clip clip = new Clip(s.getTitle(), s.getUrl().replaceAll(link, ""));
                        clips.add(clip);

                    } else {
                        Clip clip = new Clip(s.getTitle(), s.getUrl().replaceAll(link2, ""));
                        clips.add(clip);

                    }

                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }



}
