package com.baegmon.overwatchclips.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baegmon.overwatchclips.Clip;
import com.baegmon.overwatchclips.R;
import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.retrieval.params.SubmissionSort;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Provides UI for the view with Cards.
 */
public class CardContentFragment extends Fragment {

    private static ArrayList<Clip> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        list = new ArrayList<>();
        visitSubreddit();

        return recyclerView;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView picture;
        public TextView name;
        public TextView description;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_card, parent, false));
            picture = (ImageView) itemView.findViewById(R.id.card_image);
            description = (TextView) itemView.findViewById(R.id.card_text);
        }
    }
    /**
     * Adapter to display recycler view.
     */
    public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private Context _context;
        public ContentAdapter(Context context) {
            _context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            String clipTitle = list.get(position).getTitle();
            String linkStart = "https://thumbs.gfycat.com/";
            String linkEnd = "-poster.jpg";
            String thumbnail = linkStart + list.get(position).getCode() + linkEnd;

            Picasso.with(_context).load(thumbnail).into(holder.picture);
            holder.description.setText(clipTitle);

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
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
                        list.add(clip);

                    } else {
                        Clip clip = new Clip(s.getTitle(), s.getUrl().replaceAll(link2, ""));
                        list.add(clip);

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