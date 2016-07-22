package com.baegmon.overwatchclips.Fragment;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.baegmon.overwatchclips.Clip;
import com.baegmon.overwatchclips.DetailActivity;
import com.baegmon.overwatchclips.MainActivity;
import com.baegmon.overwatchclips.MyOnItemClickListener;
import com.baegmon.overwatchclips.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SavedContentFragment extends Fragment {

    private static ArrayList<Clip> list;
    private MyOnItemClickListener _onItemClickListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        list = (ArrayList<Clip>) getArguments().getSerializable("FAVORITE");

        return recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView picture;
        public ImageView favorite;
        public TextView name;
        public TextView description;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_card, parent, false));
            picture = (ImageView) itemView.findViewById(R.id.card_image);
            description = (TextView) itemView.findViewById(R.id.card_text);
            favorite = (ImageView) itemView.findViewById(R.id.favorite_button);

            picture.setOnClickListener(this);
            favorite.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if(v == picture){

                Intent intent = new Intent(((MainActivity) getActivity()), DetailActivity.class);
                intent.putExtra("Clip", list.get(getAdapterPosition()));
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(((MainActivity) getActivity()));
                ActivityCompat.startActivity(((MainActivity) getActivity()), intent, options.toBundle());


            } else if (v == favorite){
                Clip clip = list.get(getAdapterPosition());

                // clip has already been favorited
                if(clip.isFavorited()){
                    clip.favorite();
                    DrawableCompat.setTint(favorite.getDrawable(), ContextCompat.getColor(getContext(), R.color.unfavorite));

                    ArrayList<Clip> clips = ((MainActivity) getActivity()).getClips();
                    for(int i = 0 ; i < clips.size(); i++){
                        if(clips.get(i).getTitle().equals(clip.getTitle())){
                            clips.set(i, clip);
                        }
                    }

                    ((MainActivity)getActivity()).updateClips(clips);
                    ((MainActivity)getActivity()).updateClipsFragment();
                    // remove the clip
                    list.remove(clip);
                    ((MainActivity)getActivity()).updateFavorites(list);
                    ((MainActivity)getActivity()).callUpdate();

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