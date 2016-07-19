package com.baegmon.overwatchclips;

public class Clip {

    private String _title;
    private String _code;

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
