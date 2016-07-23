package com.baegmon.overwatchclips;

import android.content.Context;

import java.util.ArrayList;

public class OverwatchResource {

    private ArrayList<Clip> _clips;
    private ArrayList<Clip> _favorites;
    private Context _context;


    public OverwatchResource(Context context, ArrayList<Clip> clips, ArrayList<Clip> favorites){
        _context = context;
        _clips = clips;
        _favorites = favorites;

    }

    public Context getContext(){
        return _context;
    }

    public ArrayList<Clip> getClips(){
        return _clips;
    }

    public ArrayList<Clip> getFavorites(){
        return _favorites;
    }


}
