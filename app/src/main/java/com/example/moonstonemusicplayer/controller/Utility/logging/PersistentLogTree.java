package com.example.moonstonemusicplayer.controller.Utility.logging;

import timber.log.Timber;
import androidx.annotation.NonNull;
import android.content.Context;

import java.io.IOException;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import android.util.Log;
import static java.util.Map.entry;    
import java.util.Date;
import java.text.SimpleDateFormat;

public class PersistentLogTree extends Timber.Tree {

    public final static int UNHANDLED_LOG_PRIO = 1000;

    private Context context;
    private final static String Logdir = "MMP_logs";

    private static final Map<Integer, String> prioToLogFileMap = Map.ofEntries(
        entry(-1, "additional_log.txt"),
        entry(Log.VERBOSE, "verbose_log.txt"),
        entry(Log.DEBUG, "debug_log.txt"),
        entry(Log.INFO, "info_log.txt"),
        entry(Log.ERROR, "error_log.txt"),
        entry(UNHANDLED_LOG_PRIO, "unhandled_error.txt")
    );    

    public PersistentLogTree(Context context){
        this.context = context;
    }

    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        String timestamp = s.format(new Date());

        writeLogToFile(timestamp+": "+tag+" - "+message, prioToLogFileMap.get(priority));
    }

    private void writeLogToFile(@NonNull String message, @NonNull String logfile) {
        try {
            //make sure file (and parents) exists
            File file = new File(context.getFilesDir(), Logdir+"/"+logfile);
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            //append to file
            //NOTE: using android file api does not work fully
            FileWriter fw = new FileWriter(file.getPath(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(message);
            bw.newLine();
            bw.close();
        }
        catch (Exception e) {
            Log.e(PersistentLogTree.class.getName(), "Writing Logs failed: " + e.toString());
        } 
    }
}
