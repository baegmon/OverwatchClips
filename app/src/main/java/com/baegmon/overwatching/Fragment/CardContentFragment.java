package com.baegmon.overwatching.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baegmon.overwatching.Clip;
import com.baegmon.overwatching.DetailActivity;
import com.baegmon.overwatching.MainActivity;
import com.baegmon.overwatching.Utility.Resource;
import com.baegmon.overwatching.R;
import com.baegmon.overwatching.Utility.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;


public class CardContentFragment extends Fragment {

    private static ArrayList<Clip> list;
    private Resource resource;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);
        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        resource = ((MainActivity)getActivity()).getResource();
        list = resource.getClips();

        return recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView picture;
        private ImageButton favorite;
        private ImageButton share;
        private ImageButton download;
        private Button source;
        private TextView name;
        private TextView description;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_card, parent, false));
            picture = (ImageView) itemView.findViewById(R.id.card_image);
            description = (TextView) itemView.findViewById(R.id.card_text);
            favorite = (ImageButton) itemView.findViewById(R.id.favorite_button);
            share = (ImageButton) itemView.findViewById(R.id.share_button);
            download = (ImageButton) itemView.findViewById(R.id.download_button);
            source = (Button) itemView.findViewById(R.id.source_button);


            picture.setOnClickListener(this);
            favorite.setOnClickListener(this);
            source.setOnClickListener(this);
            share.setOnClickListener(this);
            download.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {

            if(v == favorite){
                Clip clip = resource.getClips().get(getAdapterPosition());

                if(clip.isFavorited()){
                    clip.favorite();

                    DrawableCompat.setTint(favorite.getDrawable(), ContextCompat.getColor(resource.getContext(), R.color.unfavorite));

                    for(int i = 0 ; i < resource.getFavorites().size(); ++i){
                        if(resource.getFavorites().get(i).getCode().equals(clip.getCode())){
                            resource.getFavorites().remove(i);
                        }

                    }

                } else {
                    clip.favorite();
                    DrawableCompat.setTint(favorite.getDrawable(), ContextCompat.getColor(resource.getContext(), R.color.favorite));
                    resource.getFavorites().add(clip);

                }

                ((MainActivity)getActivity()).callUpdate();


            } else if (v == picture){
                Clip clip = resource.getClips().get(getAdapterPosition());
                Intent intent = new Intent(resource.getContext(), DetailActivity.class);
                intent.putExtra("Clip", clip);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

            } else if (v == source){
                Clip clip = resource.getClips().get(getAdapterPosition());
                Uri uri = Uri.parse(clip.getSource());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            } else if (v == share){
                Clip clip = resource.getClips().get(getAdapterPosition());
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, clip.getURL());

                startActivity(Intent.createChooser(intent, "Share clip"));

            } else if (v == download){
                Clip clip = resource.getClips().get(getAdapterPosition());
                Task task = new Task();
                try {
                    task.downloadClip(clip, getActivity());
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }

        }

    }

    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
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

            String thumbnail = "https://thumbs.gfycat.com/" + list.get(position).getCode() + "-poster.jpg";
            Picasso.with(_context).load(thumbnail).into(holder.picture);
            holder.description.setText(list.get(position).getTitle());

            if(list.get(position).isFavorited()){
                DrawableCompat.setTint(holder.favorite.getDrawable(), ContextCompat.getColor(_context, R.color.favorite));
            } else {
                DrawableCompat.setTint(holder.favorite.getDrawable(), ContextCompat.getColor(_context, R.color.unfavorite));
            }

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }


}