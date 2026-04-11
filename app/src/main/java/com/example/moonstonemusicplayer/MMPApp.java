package com.example.moonstonemusicplayer;

import timber.log.Timber;
import android.util.Log;
import android.content.Context;
import android.widget.Toast;

import com.example.moonstonemusicplayer.controller.Utility.logging.*;

public class MMPApp extends android.app.Application {

    private static Context applicationContext;

    public static void logUncaughtException(Throwable t){
        Timber.log(PersistentLogTree.UNHANDLED_LOG_PRIO, t, "uncaughtException: "+t.getStackTrace());
        Toast.makeText(applicationContext, "Unhandled Exception! View internal logs ...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(){
        super.onCreate();
        applicationContext = getApplicationContext();
        Timber.plant(new PersistentLogTree(getApplicationContext()));
    }
}

/**
 * Catches all Unhandled Exceptions
 * Documentation: https://developer.android.com/reference/java/lang/Thread.UncaughtExceptionHandler
 */
class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**
     * Method invoked when the given thread terminates due to the given uncaught exception. 
     */
    public void uncaughtException(Thread t, Throwable e){
        MMPApp.logUncaughtException(e);
    }
}
