package com.dhbw.magicmoney;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@DatabaseTable(tableName = "db_transaktion")
public class Transaction {

    @DatabaseField(columnName = "TransaktionsID", generatedId = true)
    private int ID;
    @DatabaseField(columnName = "SenderKundenID", canBeNull = false)
    public int senderID;
    @DatabaseField(columnName = "EmpfängerKundenID", canBeNull = false)
    public int receiverID;
    @DatabaseField(columnName = "Betrag", canBeNull = false)
    private double transferValue;
    @DatabaseField(columnName = "Datum", dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date timestamp;

    public Transaction(int senderID, int receiverID, double transferValue) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.transferValue = transferValue;
    }


    public Transaction(int senderID, int receiverID, double transferValue, Date time) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.transferValue = transferValue;
        this.timestamp = time;
    }

    public  Transaction(){

    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }

    public double getTransferValue() {
        return transferValue;
    }

    public void setTransferValue(double transferValue) {
        this.transferValue = transferValue;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getEURBalance(){
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format((this.getTransferValue()));
    }

    public String getFormattedDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(this.getTimestamp());
    }

}
