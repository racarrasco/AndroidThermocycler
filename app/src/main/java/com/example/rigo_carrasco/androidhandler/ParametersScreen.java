package com.example.rigo_carrasco.androidhandler;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import android.widget.CompoundButton;
import android.widget.EditText;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;


/**
 * Created by Rigo_Carrasco on 6/18/2016.
 * This Activity is the user interface to set the parameters, some edit texts will be enabled once
 * the checkbox is checked
 */
public class ParametersScreen extends AppCompatActivity {
   Switch firstPreheatSwitch,extensionSwitch, secondPreheatSwitch, preCoolSwitch;
    Button setParametersButton;

    ToggleButton timeTempToggle;







    EditText [] dataET;
    String [] data = new String [28];
    TextView timeTempTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameters);


        timeTempTextView = (TextView) findViewById(R.id.textViewTimeTemp);
        firstPreheatSwitch = (Switch) findViewById(R.id.switchFirstPreheat);
        secondPreheatSwitch = (Switch) findViewById(R.id.switchSecondPreheat);
        extensionSwitch = (Switch) findViewById(R.id.switchExtension);
        setParametersButton = (Button) findViewById(R.id.buttonParameters);
        preCoolSwitch = (Switch) findViewById(R.id.switchPrecool);


        timeTempToggle = (ToggleButton) findViewById(R.id.toggleButtonTimeTemp);
        timeTempToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    timeTempTextView.setText("Temp(C):");
                   dataET[0].setText("");
                } else{
                    timeTempTextView.setText("Time(s):");
                    dataET[0].setText("");
                }
            }
        });



        dataET = new EditText [] {(EditText) findViewById(R.id.editTextPreCool),
                                 (EditText) findViewById(R.id.editTextFirstPreheat0), (EditText) findViewById(R.id.editTextFirstPreheat1), //0 is temperature 1 is time
                                 (EditText) findViewById(R.id.editTextSecondPreheat0), (EditText) findViewById(R.id.editTextSecondPreheat1) ,
                                 (EditText) findViewById(R.id.editTextDenature0),      (EditText) findViewById(R.id.editTextDenature1),
                                 (EditText) findViewById(R.id.editTextAnnealing0),     (EditText) findViewById(R.id.editTextAnnealing1),
                                 (EditText) findViewById(R.id.editTextExtension0),     (EditText) findViewById(R.id.editTextExtension1),
                (EditText) findViewById(R.id.editTextNumberOfCycles), //EditText[11],
                (EditText) findViewById(R.id.editTextFirstPreheatPID0), (EditText) findViewById(R.id.editTextFirstPreheatPID1), (EditText) findViewById(R.id.editTextFirstPreheatPID2),
                (EditText) findViewById(R.id.editTextSecondPreheatPID0),(EditText) findViewById(R.id.editTextSecondPreheatPID1),(EditText) findViewById(R.id.editTextSecondPreheatPID2),
                (EditText) findViewById(R.id.editTextDenaturePID0), (EditText) findViewById(R.id.editTextDenaturePID1),(EditText) findViewById(R.id.editTextDenaturePID2),
                (EditText) findViewById(R.id.editTextAnnealingPID0), (EditText) findViewById(R.id.editTextAnnealingPID1),(EditText) findViewById(R.id.editTextAnnealingPID2),
                (EditText) findViewById(R.id.editTextExtensionPID0), (EditText) findViewById(R.id.editTextExtensionPID1),(EditText) findViewById(R.id.editTextExtensionPID2)};
        String [] values;

        Intent mainactivityvalues = getIntent();
        values = mainactivityvalues.getStringArrayExtra("values");
        setUiEnabled(false,1);
        setUiEnabled(false,3);
        setUiEnabled(false,9);
        dataET[0].setEnabled(false);
        timeTempToggle.setEnabled(false);
        timeTempTextView.setText(values[27]);


        for(int i = 0; i<dataET.length;i++){
            if(i==0) {
                if(timeTempTextView.getText().toString().equals("Temp(C):")) {
                    timeTempToggle.setChecked(true);
                }
                if(!values[i].isEmpty()) {
                    preCoolSwitch.setChecked(true);
                    dataET[i].setEnabled(true);
                    timeTempToggle.setEnabled(true);
                }
            }
            else if(i==1) {
                if(!values[i].isEmpty()) {
                    firstPreheatSwitch.setChecked(true);
                    setUiEnabled(true,1);
                }
            }
            else if(i==3) {
                if(!values[i].isEmpty()) {
                    secondPreheatSwitch.setChecked(true);
                    setUiEnabled(true,3);
                }
            }
            else if(i==9) {
                if(!values[i].isEmpty()) {
                    extensionSwitch.setChecked(true);
                    setUiEnabled(true,9);
                }

            }
            dataET[i].setText(values[i]);
        }


    }

    public void setUiEnabled(boolean bool, int j ) {
        //enables text editing when a box is checked
        //deletes the text editing when a box is unchecked
            dataET[j].setEnabled(bool);
            dataET[j+1].setEnabled(bool);
    }
    public void clearText(int index) {
        //Clears the text of time and temp
        dataET[index].setText("");
        dataET[index+1].setText("");
    }

        public void onSwitchOn (View view){
            boolean checked = ((Switch) view).isChecked();
            switch (view.getId()) {
                case R.id.switchFirstPreheat:
                    if (checked) {
                        setUiEnabled(true,1);
                    }
                    else {
                        setUiEnabled(false, 1);
                        clearText(1);
                    }
                    break;
                case R.id.switchSecondPreheat:
                    if(checked){
                        setUiEnabled(true,3);
                    }
                    else{
                        setUiEnabled(false,3);
                        clearText(3);
                    }
                    break;
                case R.id.switchExtension:
                    if (checked) {
                        setUiEnabled(true,9);
                    }
                    else{
                        setUiEnabled(false,9);
                        clearText(9);
                    }
                    break;
                case R.id.switchPrecool:
                    if(checked) {
                        dataET[0].setEnabled(true);
                        timeTempToggle.setEnabled(true);
                    }else {
                        dataET[0].setEnabled(false);
                        timeTempToggle.setEnabled(false);
                        dataET[0].setText("");
                    }
                    break;
            }

        }

    public void onClickSetParameters(View view) { //Goes back to the run activity with the set parameters
        for (int i=0; i<dataET.length; i++) {
            data[i] = String.valueOf(dataET[i].getText());
        }
        data[27] = String.valueOf(timeTempTextView.getText());
        String [] numbers = data;
        Intent mainActivityIntent = new Intent();
        mainActivityIntent.putExtra("data",numbers);
        setResult(RESULT_OK,mainActivityIntent);
        finish();
    }

    public void onBackPressed(){
        for (int i=0; i<dataET.length; i++) {
            data[i] = String.valueOf(dataET[i].getText());

        }
        String [] numbers = data;
        Intent a = new Intent();
        a.putExtra("data",numbers);
        setResult(RESULT_OK,a);
        finish();
    }
}
