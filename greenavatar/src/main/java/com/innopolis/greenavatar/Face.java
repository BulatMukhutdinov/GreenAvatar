package com.innopolis.greenavatar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class Face extends Fragment{

    private NumberFormat formatter;
    private int serverPort = 6666;
    public Timer timer;
    private Socket socket;
    protected SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private TextView textView;
    private ImageView bulb;

    DBHelper dbHelper;
    SQLiteDatabase database;

    static Context context;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.facelayout, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        formatter = new DecimalFormat("#00.00");

        textView = (TextView) view.findViewById(R.id.consumption_text);
        bulb = (ImageView) view.findViewById(R.id.bulb);

        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        connect(view);
        return view;
    }
    @Override
    public void onDestroy(){
        if (timer != null && !socket.isClosed())
        stop();
        super.onDestroy();
    }

    /**
     * Outputs the database content in the log
     * Used for debugging
     */
    protected void readConsumptionDB() {
        Cursor cursor = database.query(DBHelper.TABLE1, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int _id, dateTime, consPerc;
            _id = cursor.getColumnIndex(DBHelper.COLUMN_ID);
            dateTime = cursor.getColumnIndex(DBHelper.COLUMN_DATETIME);
            consPerc = cursor.getColumnIndex(DBHelper.COLUMN_CONSPERC);
            do {
                Log.d("mLog", "_id = " + cursor.getString(_id) +
                        ", dateTime = " + cursor.getString(dateTime) +
                        ", consPerc = " + cursor.getString(consPerc));
            } while (cursor.moveToNext());
        } else
            Log.d("mLog", "0 rows");
        cursor.close();
    }

    public void stop() {
        timer.cancel();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Animates face, based on the 10% threshold
     * @param cons - Consumption
     * @param imageView - the face
     */
    protected void animateFace(Double cons, ImageView imageView){
        if (cons >= 140) {
            imageView.setImageResource(R.drawable.grumpy);
            Toast.makeText(context, "Do you even care?", Toast.LENGTH_LONG).show();
        } else if (cons >= 130 && cons < 140) {
            imageView.setImageResource(R.drawable.mad);
            Toast.makeText(context, "Please, stop that!", Toast.LENGTH_LONG).show();
        } else if (cons >= 120 && cons < 130) {
            imageView.setImageResource(R.drawable.warning);
            Toast.makeText(context, "You oughtta think about the planet", Toast.LENGTH_LONG).show();
        } else if (cons >= 110 && cons < 120) {
            imageView.setImageResource(R.drawable.sad);
            Toast.makeText(context, "Reduce the electricity wastage", Toast.LENGTH_LONG).show();
        } else if (cons >= 100 && cons < 110) {
            imageView.setImageResource(R.drawable.happy);
            Toast.makeText(context, "Maybe there's no need to turn on the TV?", Toast.LENGTH_LONG).show();
        } else if (cons >= 95 && cons < 100) {
            imageView.setImageResource(R.drawable.fun);
            Toast.makeText(context, "Stay this way!", Toast.LENGTH_LONG).show();
        } else if (cons >= 85 && cons < 95) {
            imageView.setImageResource(R.drawable.happier);
            Toast.makeText(context, "The planet Earth loves you!", Toast.LENGTH_LONG).show();
        } else if (cons >= 80 && cons < 85) {
            imageView.setImageResource(R.drawable.megahappy);
            Toast.makeText(context, "Green as grass!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    public void connect(View view) {
        Cursor cursor = database.query(DBHelper.TABLE2, null, null, null, null, null, null);
        cursor.moveToFirst();
        String address = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IP));
        InetAddress ipAddress;
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            ipAddress = InetAddress.getByName(address);
            socket = new Socket();
            socket.setSoTimeout(200);
            socket.connect(new InetSocketAddress(ipAddress, serverPort), 200);
            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();
            in = new DataInputStream(sin);
            out = new DataOutputStream(sout);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            Toast.makeText(context,"Unable to establish connection",Toast.LENGTH_LONG).show();
            return;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Toast.makeText(context,"Unable to establish connection",Toast.LENGTH_LONG).show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"Something went wrong",Toast.LENGTH_LONG).show();
            return;
        }

        if(getActivity() == null)
            return;

        final Date[] date = new Date[1]; //this is for the db date field

        final DataOutputStream finalOut = out;
        final DataInputStream finalIn = in;
        timer = new Timer();
        try {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(getActivity() == null)
                        return;
                    getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        try {
                                                            date[0] = new Date();
                                                            String dateTime = s.format(date[0]);

                                                            finalOut.writeUTF("");
                                                            finalOut.flush();
                                                            Double cons = finalIn.readDouble();

                                                            textView.setText("Consumption percentage is " + formatter.format(cons) + "% from regular");

                                                            ContentValues cv = new ContentValues();
                                                            cv.put(DBHelper.COLUMN_DATETIME, dateTime);
                                                            cv.put(DBHelper.COLUMN_CONSPERC, cons);
                                                            database.insert(DBHelper.TABLE1, null, cv);

                                                            animateFace(cons, bulb);
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                    );
                }
            }, 0, 5000);
        } catch (IllegalStateException e) {
        }
    }
}
