package com.example.PlanIT;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Message;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {

    public DBhelper mydb;
    private ListView obj;

    private BluetoothService bluetoothService;

    private String messageToBeSentFromClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mydb = new DBhelper(this);

        final Button exit = (Button) findViewById(R.id.exi);
        exit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == exit) {
                    finish();
                }
            }
        });

        this.refreshView();

        if (Constants.IS_SERVER) {
            this.setTitle("PlanIT (Server)");
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth is OFF. IMP : Switch on bluetooth and restart the app!!",
                        Toast.LENGTH_LONG).show();
            } else {
                this.bluetoothService = new BluetoothService(this, mHandler);
                // Start listening for incoming connections.
                this.bluetoothService.start();
            }
        } else {
            this.setTitle("PlanIT (Client)");
        }

    }

    private void refreshView() {
        ArrayList array_list = mydb.getTableNames();
        ArrayAdapter arrayAdapter =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1, array_list);

        //adding it to the list view.
        obj = (ListView) findViewById(R.id.listView1);
        obj.setAdapter(arrayAdapter);
        obj.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position,
                                    long id) {
                String item = ((TextView) view).getText().toString();
                Intent intent = new Intent(MainActivity.this, EditTable.class);
                intent.putExtra(Constants.TIME_TABLE_NAME, item);
                startActivity(intent);
            }
        });
    }

    private void addTimetable(String timeTablename) {
        mydb.addTimeTable(timeTablename);
        refreshView();
    }


    public void tablename(View v) {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.new_table, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompt_subject.xml to be the layout file of the alertdialog builder
        alertDialogBuilder.setView(promptView);
        final EditText input = (EditText) promptView.findViewById(R.id.userInput);

        // setup a dialog window
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get user input and set it to result
                        String text = input.getText().toString();
                        addTimetable(text);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alertD = alertDialogBuilder.create();

        alertD.show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_settings:
                versionItems();
                break;
            case R.id.info:
                infoItems();
                break;
            case R.id.about_us:
                aboutItems();
                break;
            case R.id.add_table:
                tablename(null);
                break;
            case R.id.sync:
                syncTimeTableWithServer();
                break;
        }
        return true;

    }

    private void versionItems() {
        new Builder(this)
                .setTitle("Version")
                .setMessage("This is the first version")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).show();
    }

    private void infoItems() {
        new Builder(this)
                .setTitle(" APP Info")
                .setMessage("Used for managing your weekly class time table schedule.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).show();
    }

    private void aboutItems() {
        new Builder(this)
                .setTitle("About Us")
                .setMessage("Credits:\n" + "1.Ashish Kumar Singh\n2.Naresh Choudhary\n3.Rohan")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).show();
    }


    @Override
    protected void onDestroy() {
        mydb.close();
        super.onDestroy();
    }

    // BLUETOOTH FUNCTIONS:

    private void syncTimeTableWithServer() {
        if (Constants.IS_SERVER) {
            Toast.makeText(getApplicationContext(), "This is server. Try this on client.",
                    Toast.LENGTH_LONG).show();
        } else {

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth is OFF. IMP : Switch on bluetooth and restart the app!!",
                        Toast.LENGTH_LONG).show();
            } else {
                // Get a set of currently paired devices
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                // If there are paired devices, add each one to the ArrayAdapter
                if (pairedDevices.size() > 0) {

                    for (BluetoothDevice device : pairedDevices) {
                        if (this.bluetoothService == null)
                            this.bluetoothService = new BluetoothService(this, mHandler);
                        // Connect to server :
                        if (this.bluetoothService.getState() == BluetoothService.STATE_CONNECTED)
                            sendMessageViaBluetooth(Constants.CMD_FETCH_DATA_FROM_SERVER);
                        else {
                            this.bluetoothService.connect(device, true);
                            // Device not connected yet. So, save the message and sent it when we get a CONNECTED callback in mHandler.
                            messageToBeSentFromClient = Constants.CMD_FETCH_DATA_FROM_SERVER;
                        }
                        break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No paired devices found!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void sendMessageViaBluetooth(String message) {

        if (this.bluetoothService != null) {
            // Check that we're actually connected before trying anything
            if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                Toast.makeText(this, "Remote device is not connected!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                bluetoothService.write(message);
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            if (messageToBeSentFromClient != null) {
                                sendMessageViaBluetooth(messageToBeSentFromClient);
                                messageToBeSentFromClient = null;
                            }
                    }
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    if (Constants.IS_SERVER) {
                        Toast.makeText(MainActivity.this, "RECEIVED FROM CLIENT : "
                                + readMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        // This is client. Saving data from server.
                        DBhelper dBhelper = new DBhelper(MainActivity.this);
                        dBhelper.insertDataUsingJsonString(readMessage);
                        refreshView();
                        Toast.makeText(MainActivity.this, "RECEIVED FROM SERVER : "
                                + readMessage, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != MainActivity.this) {
                        Toast.makeText(MainActivity.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
}
