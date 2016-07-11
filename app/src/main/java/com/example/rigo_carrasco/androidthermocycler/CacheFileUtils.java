package com.example.rigo_carrasco.androidthermocycler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;


/**
 * Created by Rigo_Carrasco on 7/11/2016.
 * creates a cached file with the time and temperature data, and when called, opens mail
 */
public class CacheFileUtils {


    public static File createCachedFile(Context context, String fileName,
                                        String content) throws IOException{
        File cacheFile = new File(context.getCacheDir()+
                File.separator+fileName);
        cacheFile.createNewFile();

        FileOutputStream fos = new FileOutputStream(cacheFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF8");
        PrintWriter pw = new PrintWriter(osw);

        pw.println(content);
        pw.flush();
        pw.close();
        return cacheFile;


    }
    public static Intent getSendEmailIntent(Context context, String email,
                                            String subject,String body,String filename) {


        final Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setClassName("com.google.android.gm",
                "com.google.android.gm.ComposeActivityGmail");


        emailIntent.setType("plain/text");

        emailIntent.putExtra(Intent.EXTRA_EMAIL,new String [] {email});

        emailIntent.putExtra(Intent.EXTRA_SUBJECT,subject);

        emailIntent.putExtra(Intent.EXTRA_TEXT,body);

        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://"+ CachedFileProvider.AUTHORITY + "/"
                                                             +filename));
        return emailIntent;
    }




}

