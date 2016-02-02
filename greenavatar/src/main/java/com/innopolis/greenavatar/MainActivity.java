package com.innopolis.greenavatar;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private NumberFormat formatter;
    private int serverPort = 6666;
    private TextView textView;
    private EditText editText;
    private Timer timer;
    private Socket socket;
    protected SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private CircularImageView imageView;
    private GradientDrawable circleBg;
    private ImageView circle;

    DBHelper dbHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        formatter = new DecimalFormat("#00.00");
        textView = (TextView) findViewById(R.id.consumption);
        imageView = (CircularImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.address);
        circleBg = (GradientDrawable) findViewById(R.id.imageViewCircle).getBackground();
        circle = (ImageView) findViewById(R.id.imageViewCircle);

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Outputs the database content in the log
     * Used for debugging
     */
    protected void readConsumptionDB() {
        Cursor cursor = database.query(DBHelper.TABLE, null, null, null, null, null, null);
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
        imageView.setVisibility(View.INVISIBLE);
        circle.setVisibility(View.INVISIBLE);
    }

    /**
     * Animates face, based on the 10% threshold
     * @param cons - Consumption
     * @param imageView - the face
     * @param circleBg - background shape
     */
    protected void animateFace(Double cons, ImageView imageView, GradientDrawable circleBg){
        if (cons >= 140) {
            imageView.setImageResource(R.drawable.dead);
            circleBg.setColor(Color.rgb(207, 0, 0));
            Toast.makeText(MainActivity.this, "Do you even care?", Toast.LENGTH_SHORT).show();
        } else if (cons >= 130 && cons < 140) {
            imageView.setImageResource(R.drawable.cry);
            circleBg.setColor(Color.rgb(140, 5, 5));
            Toast.makeText(MainActivity.this, "Please, stop that!", Toast.LENGTH_SHORT).show();
        } else if (cons >= 120 && cons < 130) {
            imageView.setImageResource(R.drawable.curious);
            circleBg.setColor(Color.rgb(138, 48, 48));
            Toast.makeText(MainActivity.this, "You oughtta think about the planet", Toast.LENGTH_SHORT).show();
        } else if (cons >= 110 && cons < 120) {
            imageView.setImageResource(R.drawable.annulled3);
            circleBg.setColor(Color.rgb(168, 79, 115));
            Toast.makeText(MainActivity.this, "Reduce the electricity wastage", Toast.LENGTH_SHORT).show();
        } else if (cons >= 100 && cons < 110) {
            imageView.setImageResource(R.drawable.sad);
            circleBg.setColor(Color.rgb(135, 214, 161));
            Toast.makeText(MainActivity.this, "Maybe there's no need to turn on the TV?", Toast.LENGTH_SHORT).show();
        } else if (cons >= 90 && cons < 100) {
            imageView.setImageResource(R.drawable.happy);
            circleBg.setColor(Color.rgb(60, 207, 109));
            Toast.makeText(MainActivity.this, "Stay this way!", Toast.LENGTH_SHORT).show();
        } else if (cons >= 80 && cons < 90) {
            imageView.setImageResource(R.drawable.ultrahappy);
            circleBg.setColor(Color.rgb(0, 199, 66));
            Toast.makeText(MainActivity.this, "The planet Earth loves you!", Toast.LENGTH_SHORT).show();
        } else {
            imageView.setImageResource(R.drawable.dead);
            circleBg.setColor(Color.rgb(123, 87, 255));
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public void connect(View view) {
        String address = editText.getText().toString();
        InetAddress ipAddress;
        DataInputStream in = null;
        DataOutputStream out = null;
        TextView invalidAddress = (TextView) findViewById(R.id.invalidAddress);
        try {
            ipAddress = InetAddress.getByName(address);
            socket = new Socket(ipAddress, serverPort);
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
        imageView.setVisibility(View.VISIBLE);
        circle.setVisibility(View.VISIBLE);

        final Date[] date = new Date[1]; //this is for the db date field

        final DataOutputStream finalOut = out;
        final DataInputStream finalIn = in;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                                  public void run() {
                                      try {
                                          date[0] = new Date();
                                          String dateTime = s.format(date[0]);

                                          finalOut.writeUTF("");
                                          finalOut.flush();
                                          Double cons = finalIn.readDouble();

                                          textView.setText("Usage of consumption is " + formatter.format(cons) + "% from normal");

                                          ContentValues cv = new ContentValues();
                                          cv.put(DBHelper.COLUMN_DATETIME, dateTime);
                                          cv.put(DBHelper.COLUMN_CONSPERC, cons);
                                          database.insert(DBHelper.TABLE, null, cv);

                                          readConsumptionDB();
                                          animateFace(cons, imageView, circleBg);
                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }
                                  }
                              }
                );
            }
        }, 0, 10000); //10 seconds interval

    }
}
