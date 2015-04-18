package com.example.PlanIT;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class EditTable extends Activity {

    DBhelper db;

    String timeTableName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nitk_table);

        db = new DBhelper(EditTable.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.timeTableName = extras.getString(Constants.TIME_TABLE_NAME);
            TextView tv = (TextView) findViewById(R.id.index);
            tv.setText(this.timeTableName);
        }

        refreshView();
    }

    private void refreshView() {
        Button v;
        ArrayList<ContentValues> array_list = db.getItems(this.timeTableName);
        for (ContentValues cv : array_list) {
            int rowId = cv.getAsInteger(Constants.ROW_ID);
            if (rowId > 0) {
                v = (Button) findViewById(rowId);
                if (v != null)
                    v.setText(cv.getAsString(Constants.SUBJECT));
            }
        }
    }

    public void back(View v) {
        finish();
    }

    public void edit(View v) {

        final Button editbtn = (Button) v;
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.prompt_subject, null);
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
                        editbtn.setText(text);
                        int i = editbtn.getId();
                        db.insertSubject(i, text, timeTableName);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.etinfo:
                infoItems();
                break;
        }
        return true;

    }

    private void infoItems() {
        new Builder(this)
                .setTitle("Info")
                .setMessage("Used to edit the existing table and manage your weekly schedule.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
