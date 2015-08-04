package com.example.pandong.helloandroid;

/**
 * Created by panda on 15-7-20.
 */
public class ProgressService {
    public ProgressService(){

    }
    public Integer getCurrentProgerss(double current, double max) {
        Integer i=(int)((current / max) * 100) ;
        return i;
    }
}
