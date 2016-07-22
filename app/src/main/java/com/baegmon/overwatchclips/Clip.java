package com.baegmon.overwatchclips;

import java.io.Serializable;

public class Clip implements Serializable{

    private String _title;
    private String _code;
    private String _source;
    private String _size;
    private Boolean _favorited;

    public Clip(String title, String code, String source){
        _title = title;
        _code = code;
        _source = source;
        _favorited = false;
    }

    public String getTitle(){
        return _title;
    }

    public String getCode(){
        return _code;
    }

    public String getSource() { return _source; }

    public Boolean isFavorited(){ return _favorited; }

    public void favorite(){
        _favorited = !_favorited;
    }
}
