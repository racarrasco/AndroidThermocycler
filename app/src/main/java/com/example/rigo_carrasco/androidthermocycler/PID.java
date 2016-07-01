package com.example.rigo_carrasco.androidthermocycler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Rigo_Carrasco on 6/30/2016.
 */
public class PID extends AppCompatActivity {
    EditText[] pids;
    String[] values;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pid_screen);

        pids = new EditText[]{(EditText) findViewById(R.id.editTextFirstPreheatPID0), (EditText) findViewById(R.id.editTextFirstPreheatPID1), (EditText) findViewById(R.id.editTextFirstPreheatPID2),
                (EditText) findViewById(R.id.editTextSecondPreheatPID0), (EditText) findViewById(R.id.editTextSecondPreheatPID1), (EditText) findViewById(R.id.editTextSecondPreheatPID2),
                (EditText) findViewById(R.id.editTextDenaturePID0), (EditText) findViewById(R.id.editTextDenaturePID1), (EditText) findViewById(R.id.editTextDenaturePID2),
                (EditText) findViewById(R.id.editTextAnnealingPID0), (EditText) findViewById(R.id.editTextAnnealingPID1), (EditText) findViewById(R.id.editTextAnnealingPID2),
                (EditText) findViewById(R.id.editTextExtensionPID0), (EditText) findViewById(R.id.editTextExtensionPID1), (EditText) findViewById(R.id.editTextExtensionPID2)};

        Intent parametersIntent=getIntent();
        values = parametersIntent.getStringArrayExtra("values");

        for (int i =0;i<pids.length;i++) {
            pids[i].setText(values[i+12]);
        }



    }

    public void onClickBackToMain(View view) { //Goes back to the run activity with the set parameters
        for (int i=0; i<pids.length; i++) {
            values[i+12] = String.valueOf(pids[i].getText());
        }
        String [] numbers = values;
        Intent mainActivityIntent = new Intent(this,MainActivity.class);
        mainActivityIntent.putExtra("values",numbers);
        setResult(RESULT_OK,mainActivityIntent);
        finish();

    }
    @Override
    public void onPause() {
        super.onPause();
        Intent gotomain = new Intent(this,MainActivity.class);
        gotomain.putExtra("values",values);
        finish();
    }


    public void onBackPressed() {
        for (int i=0; i<pids.length; i++) {
            values[i+12] = String.valueOf(pids[i].getText());
        }
        String [] numbers = values;
        Intent mainActivityIntent = new Intent(this,MainActivity.class);
        mainActivityIntent.putExtra("values",numbers);
        setResult(RESULT_OK,mainActivityIntent);
        finish();


    }
    public void onClickToPID(View view) {

    }






}
