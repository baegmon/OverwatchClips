package com.baegmon.overwatchclips.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.baegmon.overwatchclips.Clip;
import com.baegmon.overwatchclips.DetailActivity;
import com.baegmon.overwatchclips.MainActivity;
import com.baegmon.overwatchclips.Utility.Resource;
import com.baegmon.overwatchclips.R;
import com.baegmon.overwatchclips.Utility.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;

public class SavedContentFragment extends Fragment {

    private static ArrayList<Clip> list;
    private Resource resource;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        resource = ((MainActivity)getActivity()).getResource();
        list = resource.getFavorites();
        verifyPermission();

        return recyclerView;
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public void verifyPermission(){
        int writePermission = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView picture;
        public ImageButton favorite;
        public ImageButton share;
        public ImageButton download;
        public Button source;
        public TextView name;
        public TextView description;

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
            share.setOnClickListener(this);
            source.setOnClickListener(this);
            download.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v == picture){

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("Clip", list.get(getAdapterPosition()));
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());


            } else if (v == favorite){
                Clip clip = list.get(getAdapterPosition());

                // clip has already been favorited
                if(clip.isFavorited()){
                    clip.favorite();
                    DrawableCompat.setTint(favorite.getDrawable(), ContextCompat.getColor(getContext(), R.color.unfavorite));

                    ArrayList<Clip> clips = resource.getClips();
                    for(int i = 0 ; i < clips.size(); i++){
                        if(clips.get(i).getCode().equals(clip.getCode())){
                            clips.set(i, clip);
                        }
                    }

                    resource.getFavorites().remove(clip);
                    ((MainActivity)getActivity()).callUpdate();
                }

            } else if (v == share){
                Clip clip = resource.getFavorites().get(getAdapterPosition());
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, clip.getURL());

                startActivity(Intent.createChooser(intent, "Share clip"));
            } else if (v == source){
                Clip clip = resource.getFavorites().get(getAdapterPosition());
                Uri uri = Uri.parse(clip.getSource());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } else if (v == download){
                Clip clip = resource.getFavorites().get(getAdapterPosition());
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