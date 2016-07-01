package com.example.rigo_carrasco.androidthermocycler;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Set;


/**
 *This is the control window for PCR, here the user will test all of the functions of the arduino:
 * turn fan on/off turn LED on/off check the temperature
 */
public class Controls extends AppCompatActivity {
    Button mainButton, parButton, pIDButton,controlButton, fanOnButton,fanOffButton,lEDOnButton,lEDOffButton,
    getTempButton;
    TextView currentTemperature;

    String[] values;

    private UsbService usbService;
    private MyHandler2 mHandler2;


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler2);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


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



    @Override
    protected void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.controls_screen);

        mHandler2 = new MyHandler2(this);

        mainButton = (Button) findViewById(R.id.buttonBackToMainActivity);
        parButton = (Button) findViewById(R.id.buttonToParameters);
        pIDButton = (Button) findViewById(R.id.buttonToPID);
        controlButton = (Button) findViewById(R.id.buttonToControl);


        fanOnButton = (Button) findViewById(R.id.buttonFanOn);
        fanOffButton = (Button) findViewById(R.id.buttonFanOff);
        lEDOnButton = (Button) findViewById(R.id.buttonLEDOn);
        lEDOffButton = (Button) findViewById(R.id.buttonLEDOff);
        getTempButton = (Button) findViewById(R.id.buttonCheckTemp);



        currentTemperature = (TextView) findViewById(R.id.EditTextGetTemp);



        Intent theValues = getIntent();
        values = theValues.getStringArrayExtra("values");






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

    public void pushcmd(String command) {
        usbService.write(command.getBytes());
    }


   public void onClickFanOn(View view) {
       pushcmd("F\n");
   }
    public void onClickFanOff(View view) {
        pushcmd("H\n");
    }
    public void onClickCheckTemp(View view) {
        pushcmd("T\n");
    }
    public void onClickLEDOff(View view) {
        pushcmd("0\n");
    }
    public void onClickLEDOn(View view) {
        pushcmd(encnum(230)+"1\n");
    }

    private static class MyHandler2 extends Handler {
        private final WeakReference<Controls> mActivity;

        public MyHandler2(Controls activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String[] data = (String[]) msg.obj;
                    mActivity.get().currentTemperature.setText(data[0]);
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


    public void onClickBackToMain(View view) {
        Intent backToMainIntent = new Intent(this,MainActivity.class);
        String [] theValues = values;
        backToMainIntent.putExtra("values",theValues);
        setResult(RESULT_OK,backToMainIntent);
        finish();
    }
    public void onBackPressed() {
        Intent backToMainIntent = new Intent(this,MainActivity.class);
        String [] theValues = values;
        backToMainIntent.putExtra("values",theValues);
        setResult(RESULT_OK,backToMainIntent);
        finish();
    }






}
