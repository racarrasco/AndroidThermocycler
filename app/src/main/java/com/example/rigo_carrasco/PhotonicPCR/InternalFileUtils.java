package com.example.rigo_carrasco.PhotonicPCR;

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
public class InternalFileUtils {


    public static File createInternalFile(Context context, String fileName,
                                        String content) throws IOException{
        File file = new File(context.getFilesDir()+
                File.separator+fileName);
        file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF8");
        PrintWriter pw = new PrintWriter(osw);

        pw.println(content);
        pw.flush();
        pw.close();
        return file;


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

        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://"+ InternalFileProvider.AUTHORITY + "/"
                                                             +filename));
        return emailIntent;
    }




}

