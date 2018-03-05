package com.example.guybi.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    private static final int PORT = 1337;
    private static final String IP  = "192.168.43.198";
    private static InetAddress address;
    DatagramSocket socket;
    SensorManager sensorManager;
    Sensor sensor;
    TextView textX;
    TextView textY;
    TextView textZ;
    int x_global;
    int y_global;
    BUTTON_TYPE global_button;
    BUTTON_ACTION global_action;

    class myMove extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ByteBuffer msg = ByteBuffer.allocateDirect(10);
            msg.order(ByteOrder.LITTLE_ENDIAN);
            msg.put((byte) 'M');
            msg.putInt(x_global);
            msg.putInt(y_global);
            msg.flip();

            byte[] buf = new byte[msg.remaining()];
            msg.get(buf);

            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
                address = InetAddress.getByName(IP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class myClick extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ByteBuffer msg = ByteBuffer.allocateDirect(10);
            msg.order(ByteOrder.LITTLE_ENDIAN);
            msg.put((byte) 'C');
            msg.putShort(global_button.getVal());
            msg.put((byte) (global_action == BUTTON_ACTION.PRESS ? 1 : 0));
            msg.flip();

            byte[] buf = new byte[msg.remaining()];
            msg.get(buf);

            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
                address = InetAddress.getByName(IP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void send_click(BUTTON_TYPE button, BUTTON_ACTION action) throws IOException {
        global_button = button;
        global_action = action;
//        ByteBuffer msg = ByteBuffer.allocateDirect(10);
//        msg.order(ByteOrder.LITTLE_ENDIAN);
//        msg.put((byte) 'C');
//        msg.putShort(button.getVal());
//        msg.put((byte) (action == BUTTON_ACTION.PRESS ? 1 : 0));
//        msg.flip();
//
//        byte[] buf = new byte[msg.remaining()];
//        msg.get(buf);
//
//        socket = new DatagramSocket();
//        address = InetAddress.getByName(IP);
//        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
//        socket.send(packet);
        new myClick().execute();
    }

    public void send_move(int x, int y) throws IOException {
        x_global = x;
        y_global = y;
//        ByteBuffer msg = ByteBuffer.allocateDirect(10);
//        msg.order(ByteOrder.LITTLE_ENDIAN);
//        msg.put((byte) 'M');
//        msg.putInt(x);
//        msg.putInt(y);
//        msg.flip();
//
//        byte[] buf = new byte[msg.remaining()];
//        msg.get(buf);
//
//        socket = new DatagramSocket();
//        address = InetAddress.getByName(IP);
//        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
//        socket.send(packet);

        new myMove().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final Button left_b = (Button) findViewById(R.id.left_b);

        left_b.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("Pressed", "Button pressed");
                    try {
                        send_click(BUTTON_TYPE.LEFT,BUTTON_ACTION.PRESS);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    Log.d("Released", "Button released");

                    try {
                        send_click(BUTTON_TYPE.LEFT,BUTTON_ACTION.RELEASE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        Button right_b = (Button) findViewById(R.id.right_b);
        right_b.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("Pressed", "Button pressed");
                    try {
                        send_click(BUTTON_TYPE.RIGHT,BUTTON_ACTION.PRESS);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    Log.d("Released", "Button released");

                    try {
                        send_click(BUTTON_TYPE.RIGHT,BUTTON_ACTION.RELEASE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        textX = (TextView) findViewById(R.id.textX);
        textY = (TextView) findViewById(R.id.textY);
        textZ = (TextView) findViewById(R.id.textZ);

       // ViewPager pad = (ViewPager) findViewById(R.id.pad);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        sensorManager.registerListener(acceloListener, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(acceloListener);
    }

    public SensorEventListener acceloListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {
            float x = 100*event.values[0] ;
            float y = 100*event.values[1] ;
            float z = 100*event.values[2] ;

            textX.setText("X : " +   Math.cos(x));
            textY.setText("Y : " +   Math.sin(y));
            textZ.setText("Z : " +   Math.sin(z));

            try {
               send_move((int)z*(-1),(int)y*(-1));
           } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
