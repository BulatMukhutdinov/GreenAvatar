package com.innopolis.greenavatar;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private NumberFormat formatter;
    private String address = "10.240.20.113";
    private int serverPort = 6666;
    private TextView textView;
    private DataInputStream in;
    private DataOutputStream out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        formatter = new DecimalFormat("#00.00");
        textView = (TextView) findViewById(R.id.consumption);
        InetAddress ipAddress = null; // создаем объект который отображает вышеописанный IP-адрес.
        try {
            ipAddress = InetAddress.getByName(address);
            Socket socket = new Socket(ipAddress, serverPort); // создаем сокет используя IP-адрес и порт сервера.
            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();
            in = new DataInputStream(sin);
            out = new DataOutputStream(sout);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                                  public void run() {
                                      try {
                                          out.writeUTF("");
                                          out.flush();
                                          textView.setText("Usage of consumption is " + formatter.format(in.readDouble()) + "% from normal");
                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }

                                  }
                              }
                );
            }
        }, 0, 1000);
    }

    public void someAction(View view) {
        try {
            out.writeUTF("");
            out.flush();
            textView.setText("Usage of consumption is " + formatter.format(in.readDouble()) + "%");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
