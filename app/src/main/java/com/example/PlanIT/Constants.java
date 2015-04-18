package com.example.PlanIT;

public class Constants {
    public static final Boolean IS_SERVER = false;
    public static final String TIME_TABLE_NAME = "time_tbl_name";
    public static final String ROW_ID = "row_id";
    public static final String SUBJECT = "subject";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final String CMD_FETCH_DATA_FROM_SERVER = "FETCH_DATA";

    public static final String END_OF_STRING = "*=!=END=!=*";
}
