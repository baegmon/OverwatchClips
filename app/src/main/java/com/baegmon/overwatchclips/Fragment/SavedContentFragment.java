package com.baegmon.overwatchclips.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import com.baegmon.overwatchclips.Clip;
import com.baegmon.overwatchclips.R;

import java.util.ArrayList;

public class SavedContentFragment extends Fragment {

    private static ArrayList<Clip> list;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public VideoView video;
        public TextView name;
        public TextView description;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_card_saved, parent, false));

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

            //Picasso.with(_context).load(thumbnail).into(holder.picture);
            holder.description.setText(clipTitle);

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }


}