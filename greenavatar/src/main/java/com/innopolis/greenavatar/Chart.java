package com.innopolis.greenavatar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Chart extends Fragment {

    LineChart chart;

    DBHelper dbHelper;
    SQLiteDatabase database;
    private Typeface mTf;
    protected static Map <String,String> dataSetMap;

    static Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.chartlayout, container, false);
        chart = (LineChart) view.findViewById(R.id.chart);
        dbHelper = new DBHelper(context);
        database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE2, null, null, null, null, null, null);
        cursor.moveToFirst();
        String address = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IP));
        try {
            dataSetMap = new ChartTask().execute(address).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        mTf = Typeface.createFromAsset(context.getAssets(), "OpenSans-Regular.ttf");
        database = dbHelper.getWritableDatabase();
        try {
            setData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        chart.invalidate();
        return view;
    }

    private void setData() throws ParseException {
        ArrayList<String> xVals = new ArrayList<String>();

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        int i = 1;
        for(Map.Entry<String,String> entry: dataSetMap.entrySet()){
            i++;
            xVals.add(new Utils().hm.format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entry.getKey())));
            yVals1.add(new Entry(Float.parseFloat(entry.getValue()), i));
        }

        LineDataSet set1 = new LineDataSet(yVals1, "Consumption");

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);

        LineData data = new LineData(xVals, dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(mTf);

        chart.setData(data);
    }

    protected class ChartTask extends AsyncTask<String, Void, Map<String, String>> {

        protected Map<String, String> doInBackground(String... params) {
            InetAddress ipAddress = null;
            Socket socket = new Socket();
            try {
                ipAddress = InetAddress.getByName(params[0]);
                socket.connect(new InetSocketAddress(ipAddress, 6666));
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.flush();
                outStream.writeUTF("graph");
                outStream.flush();
                ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                Map<String, String> yourMap = (Map<String, String>) inStream.readObject();
                socket.close();
                return yourMap;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
