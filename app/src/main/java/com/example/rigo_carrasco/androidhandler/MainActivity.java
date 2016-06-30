package com.example.rigo_carrasco.androidhandler;


import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;


import android.view.View;

import android.widget.Button;


import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;


import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;



import java.lang.ref.WeakReference;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity  {
    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    runButton.setEnabled(false);
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    runButton.setEnabled(false);
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    runButton.setEnabled(false);
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    runButton.setEnabled(false);
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    runButton.setEnabled(false);
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView textView;
    String[] parametersActivity;
    Button clearButton, runButton;
    TextView cycles,temperature,current_cycle;










    LineChart chart;




    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mHandler = new MyHandler(this);
        cycles = (TextView) findViewById(R.id.editTextNumberOfCycles);
        temperature = (TextView) findViewById(R.id.textViewCurrentTemperature);
        current_cycle = (TextView) findViewById(R.id.textViewCurrentCycle);




        //inital thermalcycling parameters
        parametersActivity = new String[28];
        for(int i = 0; i<11;i++){
            parametersActivity[i]="";
        }



        parametersActivity[11] = "30";
        parametersActivity[5] = "90";
        parametersActivity[6] = "0";
        parametersActivity[7]  = "68";
        parametersActivity [8] = "0";
        String kp = "10";
        String ki = "2";
        String kd = "1";
        for (int h=0;h<5; h++){
            //setting the initial PID parameters
            parametersActivity[12+3*h] = kp;
            parametersActivity[13+3*h] = ki;
            parametersActivity[14+3*h] = kd;
        }
        parametersActivity[27] = "Time(s):";














        //Chart
        chart = (LineChart) findViewById(R.id.lineChart);
        chart.setDescription("Time (s)");
        chart.setDescriptionTextSize(13f);
        chart.setNoDataTextDescription("No data yet.");
        chart.setBackgroundColor(Color.LTGRAY);

        //allowing touch interactions
        chart.setTouchEnabled(true);

        chart.setDragEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        //data

       // lineData.setValueTextColor(Color.WHITE);


        //chart
       chart.setData(new LineData());
       Legend leg = chart.getLegend();
        leg.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);



        //Xaxis
        XAxis xl = chart.getXAxis();
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);


        //Yaxis
        YAxis yl = chart.getAxisLeft();
        yl.setAxisMaxValue(100f);
        yl.setDrawGridLines(true);

        YAxis yl2 = chart.getAxisRight();
        yl2.setEnabled(false);




        textView = (TextView) findViewById(R.id.textView);
        clearButton = (Button) findViewById(R.id.buttonClear);
        runButton = (Button) findViewById(R.id.buttonRun);

        runButton.setEnabled(false);


    }
    private void removeDataSet() {

        chart.setData(new LineData());
        chart.setTouchEnabled(true);

        chart.notifyDataSetChanged();
        chart.invalidate();
    }




    public void pushcmd(String command) {
        usbService.write(command.getBytes());
    }

    public String encnum(Object value) { //encode command for serial communication
        int intval = (Integer) value;
        String strval = Integer.toString(intval);
        String cmdstr;
        if (intval < 10) {
            cmdstr = "0" + "0" + strval;
        } else if (intval < 100) {
            cmdstr = "0" + strval;
        } else
            cmdstr = strval;
        return cmdstr;
    }



    public void onClickClear(View view) {
        removeDataSet();
        textView.setText(" ");
        current_cycle.setText(" ");
        temperature.setText(" ");
    }

    public void onClickRun(View view) {
        List<Map<String, Object>> cmd = encodecmd(parametersActivity);//encode commands for the arduino
        tvSet(current_cycle,Integer.toString(1));
        Run(cmd);
        runplot();
    }
    public void onClickStop(View view) {
        pushcmd("P\n");
        tvAppend(textView,"\nStopping");
    }

    public void runplot() {
        {pushcmd("S\n");}
    }






    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    public void tvAppend(TextView tv, CharSequence text) { //This is to simply append to the textview
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }
    public void tvSet(TextView tv,CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread((new Runnable() {
            @Override
            public void run() {
                ftv.setText(ftext);
            }
        }));
    }


    private void addDataSet(ArrayList<Float> time,ArrayList<Float> temp ) {
        LineData data = chart.getData();
            if (data != null) {

                ILineDataSet set = data.getDataSetByIndex(0);
                if (set == null) {
                    set = createSet();
                    data.addDataSet(set);
                }

               for(int i = 0; i<time.size();i++) {
                   String xentry = Float.toString(time.get(i));
                   data.addXValue("" + xentry);
                   data.addEntry(new Entry(temp.get(i), set.getEntryCount()), 0);
                   if(i>0){
                   if(!temp.get(i).equals(temp.get(i-1))){
                   tvSet(temperature,temp.get(i).toString());
                       }
                   }
                   else{
                       tvSet(temperature,temp.get(i).toString());
                   }
               }
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
    }


    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null,"Temperatures");
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);


        return set;
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

       ArrayList<Float> dtime= new ArrayList<>(); //array of times
        ArrayList<Float> dtemp = new ArrayList<>(); //array of temperatures

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }


        //Array slicing method
        public  float[] sliceArray(float[] arr, int begin,int end, int spacing) {
            int curr = begin;
            float[] newArr = new float [((end - begin - 1) / spacing) + 1];
            for (int i = 0; i < newArr.length; i++) {
                newArr[i] = arr[curr];
                curr += spacing;
            }
            return newArr;
        }

       public void update_data(float[] times,float[] temps) {
           float dtime_recent; //Checks if data from the arduino is new
           ArrayList<Float> updatedtimes = new ArrayList<>();
           ArrayList<Float> updatedtemps = new ArrayList<>();
           if (dtime.size() == 0) {
               dtime_recent = 0;
           } else {
               dtime_recent = dtime.get(dtime.size() - 1);
           }
           for (int i = 0; i < times.length; i++) {
               if (times[i] > dtime_recent) {
                   dtime.add(times[i]);
                   dtemp.add(temps[i]);
                   updatedtimes.add(times[i]);
                   updatedtemps.add(temps[i]);

               }
           }
           mActivity.get().addDataSet(updatedtimes,updatedtemps);

       }
        public float [] logtimes(float[] log) { //slices the data from the arduino to retrieve time in seconds and milliseconds
            float[] log_time1 = sliceArray(log, 0,log.length, 3);
            float[] log_time2 = sliceArray(log, 1,log.length, 3);
            float[] log_time = new float[log_time1.length];
            for (int i = 0; i < log_time1.length; i++) {
                log_time[i] = log_time1[i] + log_time2[i]/1000;
            }
            return log_time;
        }


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                   String [] data = (String[]) msg.obj;
                    /*the if statement checks for what kind of data was sent from the arduino
                     either a pointer  that indicates that it is still thermocycling or if it is the temperature/time
                     array to be graphed
                     */
                    if (data.length<80) {
                        if (!data[1].equals(mActivity.get().current_cycle.getText().toString())) {
                            mActivity.get().current_cycle.setText(data[1]);
                        }
                        if (!data[0].equals("0")) { //if pointer isnt 0 prompt arduino to send data
                            mActivity.get().pushcmd("L\n");
                        } else { //if pointer is zero, clear data
                            dtime.clear();
                            dtemp.clear();
                            mActivity.get().current_cycle.setText("Done/ended");
                        }
                    }
                    else{
                        //handles the data from the arduino
                        float [] floats = new float[data.length-1]; //This is to make the data into floats
                       for (int i = 0; i<floats.length;i++) { //the -1 is to remove the "\n" in arduino's output
                           floats[i] = Float.parseFloat(data[i]);
                       }
                        float [] times =  logtimes(floats); //calls function to get time data
                        float [] temps = sliceArray(floats,2,floats.length,3); //similar to python's array[2::3]
                        update_data(times,temps); //checks if data is actually updated data
                        mActivity.get().pushcmd("S\n");
                    }
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    public void onClickParameters(View view) { //Takes user to another screen to set the parameters for thermal cycling
        Intent parametersScreenIntent = new Intent(this, ParametersScreen.class);
        final int result = 1;
        String [] theValues = parametersActivity;
        parametersScreenIntent.putExtra("values",theValues);
        /*First visit to the parameters screen
        are default parameters, once the parameters are set, they will be saved and the
        user will be able to refer to the screen*/
        startActivityForResult(parametersScreenIntent,result);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //sets the paramaters once the users goes back to this screen
        super.onActivityResult(requestCode, resultCode, data);
        parametersActivity = data.getStringArrayExtra("data"); //Using the same fields allow
        //for a "Save" feature in parameters

        runButton.setEnabled(true);
        tvAppend(textView, "\nParameters Set!");
        cycles.setText(parametersActivity[11]);

    }


    public List<Map<String, Object>> encodecmd(String[] parameters) {
        //Encode the paramaters into commands for arduino
        int loopcut = 1; //This is for the arduino to cycle through some commands, because there are some commands that only need to be done
        //once the program initiates such as preheat, precool...
        int[] encoded = new int[parameters.length-1];
        for (int i = 0; i < encoded.length; i++) {
            if (parameters[i].isEmpty()) {
                encoded[i] = -1; //for when the entries are null ie. the user doesnt have a box checked
            } else
                encoded[i] = Integer.parseInt(parameters[i]);
        }

        List<Map<String, Object>> command = new ArrayList<>();

        Map<String, Object> map1OfList = new HashMap<>();
        map1OfList.put("type", "reset");
        command.add(map1OfList);

        if(encoded[0]!=-1) { //is there a precool stage?
            Map<String,Object>mapPreCool = new HashMap<>();
            if(parametersActivity[27].equals("Time(s):")) {//did user want the fan on for a specific amount of time?
                mapPreCool.put("type","precool");
                mapPreCool.put("time",encoded[0]);
                loopcut++;
            }
            else{
                mapPreCool.put("type","cool");
                mapPreCool.put("target",encoded[0]);
                loopcut++;
            }
            command.add(mapPreCool);
        }

        if(encoded[1]!= -1) { //is there a preheat stage?
            Map<String,Object>mapPreheat1OfList = new HashMap<>();
            mapPreheat1OfList.put("type","heat");
            mapPreheat1OfList.put("target",encoded[1]);
            command.add(mapPreheat1OfList);
            loopcut++;
            if(encoded[2]> 0 ) { //Does the temperature for preheat 1 need to be maintained?
                Map<String,Object>mapPreheat1ContOfList = new HashMap<>();
                mapPreheat1ContOfList.put("type","cont");
                mapPreheat1ContOfList.put("target",encoded[1]);
                mapPreheat1ContOfList.put("time", encoded[2]); //preheat time
                mapPreheat1ContOfList.put("kp", encoded[12]);
                mapPreheat1ContOfList.put("ki", encoded[13]);
                mapPreheat1ContOfList.put("kd", encoded[14]);
                command.add(mapPreheat1ContOfList);
                loopcut++;
            }
        }

        if(encoded[3]!=-1) { //is there a second preheat stage?
            Map<String,Object>mapPreheat2OfList = new HashMap<>();
            mapPreheat2OfList.put("type","heat");
            mapPreheat2OfList.put("target",encoded[3]);
            command.add(mapPreheat2OfList);
            loopcut++;
            if(encoded[4]>0) {// Does the temperature for preheat 2 need to be maintained?
                Map<String,Object> mapPreheat2ContOfList = new HashMap<>();
                mapPreheat2ContOfList.put("type","cont");
                mapPreheat2ContOfList.put("target",encoded[3]);
                mapPreheat2ContOfList.put("time",encoded[4]);
                mapPreheat2ContOfList.put("kp", encoded[15]);
                mapPreheat2ContOfList.put("ki", encoded[16]);
                mapPreheat2ContOfList.put("kd", encoded[17]);
                command.add(mapPreheat2ContOfList);
                loopcut++;

            }
        }


        Map<String, Object> map2OfList = new HashMap<>();
        map2OfList.put("type", "heat");
        map2OfList.put("target", encoded[5]); //denature
        command.add(map2OfList);
        if (encoded[6] > 0) { //denature time > 0s ? go to PID to maintain the temp
            Map<String, Object> map3OfList = new HashMap<>();
            map3OfList.put("type", "cont");
            map3OfList.put("target", encoded[5]); //denature temp
            map3OfList.put("time", encoded[6]); //denature time
            map3OfList.put("kp", encoded[18]);
            map3OfList.put("ki", encoded[19]);
            map3OfList.put("kd", encoded[20]);
            command.add(map3OfList);
        }
        if (encoded[5] >= encoded[7]) { //denature temp >= annealing temp?
            Map<String, Object> map4OfList = new HashMap<>();
            map4OfList.put("type", "cool");
            map4OfList.put("target", encoded[7]);
            command.add(map4OfList);
        } else { //annealing temp is actually greater than denature temp
            Map<String, Object> map4OfList = new HashMap<>();
            map4OfList.put("type", "heat");
            map4OfList.put("target", encoded[7]); // annealing temp
            command.add(map4OfList);
        }
        if (encoded[8]>0) {//annealing time greater than 0? go to PID
            Map<String,Object>annealingPID = new HashMap<>();
            annealingPID.put("type","cont");
            annealingPID.put("target",encoded[7]);
            annealingPID.put("time",encoded[8]);
            annealingPID.put("kp",encoded[21]);
            annealingPID.put("ki",encoded[22]);
            annealingPID.put("kd",encoded[23]);
            command.add(annealingPID);
        }
        if(encoded[9]!=-1) {
            if (encoded[9] > encoded[7]) { //extension temp greater than annealing temp?
                Map<String, Object> extension = new HashMap<>();
                extension.put("type", "heat");
                extension.put("target", encoded[9]); //extension temp
                command.add(extension);
            } else {
                Map<String, Object> extension = new HashMap<>();
                extension.put("type", "cool");
                extension.put("target", encoded[9]); //extension temp
                command.add(extension);
            }
            if (encoded[10] > 0) { //Extension time greater than 0?
                Map<String, Object> extensionPID = new HashMap<>();
                extensionPID.put("type", "cont");
                extensionPID.put("target", encoded[9]);
                extensionPID.put("time", encoded[10]);
                extensionPID.put("kp", encoded[24]);
                extensionPID.put("ki", encoded[25]);
                extensionPID.put("kd", encoded[26]);
                command.add(extensionPID);
            }
        }

        Map<String, Object> map5OfList = new HashMap<>();
        map5OfList.put("type", "loopback");
        map5OfList.put("amount", command.size() - loopcut);
        map5OfList.put("cycle", encoded[11]);
        map5OfList.put("init", 1);
        command.add(map5OfList);

        Map<String, Object> map6OfList = new HashMap<>();
        map6OfList.put("type", "end");
        command.add(map6OfList);
        /**WARNING the size of the list is not always going to be the same
         * the size depends on the parameters chosen */
        return command;
    }



    public void Run(List<Map<String, Object>> cmd) { //this sends the commands to the arduino
        pushcmd( "R\n");
        for (int i = 0; i < cmd.size(); i++) {
            if (cmd.get(i).get("type").equals("reset")) {
                pushcmd("RC\n");
            }
            if (cmd.get(i).get("type").equals("end")) {
                pushcmd("EC\n");
            } else if (cmd.get(i).get("type").equals("loopback")) {
                pushcmd(encnum(cmd.get(i).get("init")) + encnum(cmd.get(i).get("cycle")) + encnum(cmd.get(i).get("amount")) + "LC\n");
            }else if(cmd.get(i).get("type").equals("precool")) {
                pushcmd(encnum(cmd.get(i).get("time"))+"KC\n");
            } else if (cmd.get(i).get("type").equals("heat")) {
                pushcmd(encnum(cmd.get(i).get("target")) + "HC\n");
            } else if (cmd.get(i).get("type").equals("cool")) {
                pushcmd(encnum(cmd.get(i).get("target")) + "CC\n");
            } else if (cmd.get(i).get("type").equals("cont")) {
                pushcmd( encnum(cmd.get(i).get("kd"))
                        + encnum(cmd.get(i).get("ki")) + encnum(cmd.get(i).get("kp")) +
                        encnum(cmd.get(i).get("time")) + encnum(cmd.get(i).get("target")) + "OC\n");
            }
        }
       pushcmd("X\n");
    }







}