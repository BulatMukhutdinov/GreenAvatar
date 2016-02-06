package com.innopolis.greenavatar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Chart extends Fragment {

    LineChart chart;

    DBHelper dbHelper;
    SQLiteDatabase database;
    private Typeface mTf;

    static Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.chartlayout, container, false);
        chart = (LineChart) view.findViewById(R.id.chart);
        dbHelper = new DBHelper(context);

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

        Cursor cursor = database.query(DBHelper.TABLE1, new String[]{DBHelper.COLUMN_CONSPERC,DBHelper.COLUMN_DATETIME},null,null,null,null,null);
            int cons = cursor.getColumnIndex(DBHelper.COLUMN_CONSPERC);
            int datetime = cursor.getColumnIndex(DBHelper.COLUMN_DATETIME);
            for (int i = 1; i < cursor.getCount(); i+=10) {
                cursor.moveToNext();
                xVals.add(Utils.hm.format(Utils.s.parse(String.valueOf(cursor.getString(datetime)))));
                yVals1.add(new Entry(Float.parseFloat(cursor.getString(cons)), i));
            }

        LineDataSet set1 = new LineDataSet(yVals1, "Consumption");

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);

        LineData data = new LineData(xVals, dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(mTf);

        chart.setData(data);
    }

}
