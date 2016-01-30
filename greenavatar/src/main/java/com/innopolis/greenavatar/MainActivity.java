package com.innopolis.greenavatar;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private NumberFormat formatter;
    private int serverPort = 6666;
    private TextView textView;
    private EditText editText;
    private Timer timer;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        formatter = new DecimalFormat("#00.00");
        textView = (TextView) findViewById(R.id.consumption);
        editText = (EditText) findViewById(R.id.address);
    }

    public void stop(View view) {
        timer.cancel();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView.setText("Consumption");
        editText.setVisibility(View.VISIBLE);
        TextView text = (TextView) findViewById(R.id.textView1);
        text.setVisibility(View.VISIBLE);
        Button connect = (Button) findViewById(R.id.connect);
        connect.setVisibility(View.VISIBLE);
        Button stop = (Button) findViewById(R.id.stop);
        stop.setVisibility(View.INVISIBLE);
    }

    public void connect(View view) {
        //10.91.43.113
        String address = editText.getText().toString();
        InetAddress ipAddress; // создаем объект который отображает вышеописанный IP-адрес.
        DataInputStream in = null;
        DataOutputStream out = null;
        TextView invalidAddress = (TextView) findViewById(R.id.invalidAddress);
        try {
            ipAddress = InetAddress.getByName(address);
            socket = new Socket(ipAddress, serverPort); // создаем сокет используя IP-адрес и порт сервера.
            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            in = new DataInputStream(sin);
            out = new DataOutputStream(sout);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            invalidAddress.setVisibility(View.VISIBLE);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            invalidAddress.setVisibility(View.VISIBLE);
            return;
        }
        invalidAddress.setVisibility(View.INVISIBLE);
        editText.setVisibility(View.INVISIBLE);
        TextView text = (TextView) findViewById(R.id.textView1);
        text.setVisibility(View.INVISIBLE);
        Button connect = (Button) findViewById(R.id.connect);
        connect.setVisibility(View.INVISIBLE);
        Button stop = (Button) findViewById(R.id.stop);
        stop.setVisibility(View.VISIBLE);

        final DataOutputStream finalOut = out;
        final DataInputStream finalIn = in;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                                  public void run() {
                                      try {
                                          finalOut.writeUTF("");
                                          finalOut.flush();
                                          textView.setText("Usage of consumption is " + formatter.format(finalIn.readDouble()) + "% from normal");
                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }

                                  }
                              }
                );
            }
        }, 0, 1000);

    }
}
