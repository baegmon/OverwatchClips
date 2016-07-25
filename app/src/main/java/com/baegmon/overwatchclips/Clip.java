package com.baegmon.overwatchclips;

import java.io.Serializable;

public class Clip implements Serializable{

    private String _title;
    private String _url;
    private String _source;
    private String _code;
    private String _uploader;
    private Boolean _favorited;

    public Clip(String title, String url, String code, String source, String uploader){
        _title = title;
        _url = url;
        _source = source;
        _code = code;
        _uploader = uploader;
        _favorited = false;
    }

    public String getTitle(){
        return _title;
    }

    public String getURL(){
        return _url;
    }

    public String getCode() { return _code; }

    public String getSource() { return _source; }

    public String getUploader() { return _uploader; }

    public Boolean isFavorited(){ return _favorited; }

    public void favorite(){
        _favorited = !_favorited;
    }
}
