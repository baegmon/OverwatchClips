package com.baegmon.overwatchclips;

import java.io.Serializable;

public class Clip implements Serializable{

    private String _title;
    private String _code;
    private String _source;
    private String _size;

    public Clip(String title, String code){
        _title = title;
        _code = code;
    }

    public String getTitle(){
        return _title;
    }

    public String getCode(){
        return _code;
    }
}
