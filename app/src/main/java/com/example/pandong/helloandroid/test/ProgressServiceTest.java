package com.example.pandong.helloandroid.test;

import android.test.InstrumentationTestCase;

import com.example.pandong.helloandroid.ProgressService;

/**
 * Created by panda on 15-7-20.
 */
public class ProgressServiceTest extends InstrumentationTestCase {
    ProgressService progressService;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        progressService = new ProgressService();
    }
    public void test() throws Exception{
        Integer i = progressService.getCurrentProgerss(27,100);
        assertEquals(27,i.intValue());
    }
}
