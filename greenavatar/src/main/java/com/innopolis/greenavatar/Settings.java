package com.innopolis.greenavatar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class Settings extends Fragment {

    EditText ipaddress;
    EditText password;
    Button save;

    DBHelper dbHelper;
    SQLiteDatabase database;

    static Context context;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.settingslayout, container, false);

        ipaddress = (EditText) view.findViewById(R.id.input_ipaddress);
        password = (EditText) view.findViewById(R.id.input_password);
        save = (Button) view.findViewById(R.id.saveSettings);

        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();

        updateData();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean available = false;
                try {
                    available = new CheckSocketTask().execute(String.valueOf(ipaddress.getText())).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (available) {
                    Cursor cursor = database.query(DBHelper.TABLE2, null, null, null, null, null, null);
                    ContentValues data = new ContentValues();
                    data.put(DBHelper.COLUMN_IP, String.valueOf(ipaddress.getText()));
                    data.put(DBHelper.COLUMN_PASS, String.valueOf(password.getText()));
                    if (cursor.moveToFirst()) {
                        database.update(DBHelper.TABLE2, data, "_id=" + 1, null);
                        updateData();
                    } else {
                        database.insert(DBHelper.TABLE2, null, data);
                        updateData();
                        getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.remove(getActivity().getSupportFragmentManager().findFragmentByTag("Log In"));
                        ft.replace(R.id.content_frame, new Face(), "Face").addToBackStack(null).commit();
                    }
                } else {
                    Toast.makeText(context, "Wrong address. Try again", Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (database.isOpen());
            database.close();
    }

    public void updateData(){
        Cursor cursor = database.query(DBHelper.TABLE2, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int ipaddress, password;
            ipaddress = cursor.getColumnIndex(DBHelper.COLUMN_IP);
            password = cursor.getColumnIndex(DBHelper.COLUMN_PASS);
            this.ipaddress.setText(cursor.getString(ipaddress));
            this.password.setText(cursor.getString(password));
        }
    }

    private class CheckSocketTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            InetAddress ipAddress = null;
            Socket socket = new Socket();
            try {
                ipAddress = InetAddress.getByName(params[0]);
                socket.setSoTimeout(200);
                socket.connect(new InetSocketAddress(ipAddress, 6666), 200);
                if (socket.isConnected()){
                    socket.close();
                    return true;
                }
            } catch (SocketTimeoutException e) {
                return false;
            }catch (SocketException e) {
                return false;
            } catch (UnknownHostException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            return false;
        }
    }
}
