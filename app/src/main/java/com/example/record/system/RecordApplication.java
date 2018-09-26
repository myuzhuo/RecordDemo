package com.example.record.system;

import android.app.Application;

public class RecordApplication extends Application {
    private static RecordApplication INSTANCE;

    public RecordApplication() {
        super();
        INSTANCE=this;
    }

    public static synchronized RecordApplication getInstance(){
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
