package com.dhbw.magicmoney;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

public class TransferActivity3 extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {
    String transferValue ="";
    TextView tvShowCode = null;
    TextView tvNfcSignalSent = null;

    private NfcAdapter mNfcAdapter;

    private ArrayList<String> dataToSendArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer3);

        tvShowCode = findViewById(R.id.transfer3_showCode);
        tvNfcSignalSent = findViewById(R.id.tvNfcSignalSent);


        //Generate a 4-Digit-Code
        Random random = new Random();
        String generatedCode = String.format("%04d", random.nextInt(10000));

        tvShowCode.setText(generatedCode);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null)
        {
            transferValue = (String) bundle.get("transferValue");
        }

        //Generate data which is going to be sent
        dataToSendArray.add(transferValue);
        dataToSendArray.add(generatedCode);
        dataToSendArray.add(HomeActivity.user.getForename() + " " + HomeActivity.user.getName());
        dataToSendArray.add(Integer.toString(HomeActivity.user.getID()));

        //Check if NFC is available on device
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(mNfcAdapter != null) {
            //This will refer back to createNdefMessage for what it will send
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            //This will be called if the message is sent successfully
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }        else {
            Toast.makeText(this, "NFC not available on this device", Toast.LENGTH_LONG).show();
        }

        Button abbortButton = findViewById(R.id.transfer3_button_abbort);
        abbortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(TransferActivity3.this, HomeActivity.class);
                TransferActivity3.this.startActivity(myIntent);
            }
        });
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        //This is called when the system detects that our NdefMessage was successfully sent

        Log.d("PushComplete", "reached");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvNfcSignalSent.setText("NFC Nachricht gesendet. Überprüfen Sie den Erfolg der Transaktion bei dem Empfänger.");
            }
        });

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        //This will be called when another NFC capable device is detected.

        if (dataToSendArray.size() == 0) {
            return null;
        }

        NdefRecord[] recordsToAttach = createRecords();

        return new NdefMessage(recordsToAttach);
    }

    public NdefRecord[] createRecords(){
        NdefRecord[] records = new NdefRecord[dataToSendArray.size() + 1];


        //To Create Messages Manually if API is less than
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (int i = 0; i < dataToSendArray.size(); i++){
                byte[] payload = dataToSendArray.get(i).
                        getBytes(Charset.forName("UTF-8"));
                NdefRecord record = new NdefRecord(
                        NdefRecord.TNF_WELL_KNOWN,      //Our 3-bit Type name format
                        NdefRecord.RTD_TEXT,            //Description of our payload
                        new byte[0],                    //The optional id for our Record
                        payload);                       //Our payload for the Record

                records[i] = record;
            }
        }
        //API is high enough that we can use createMime, which is preferred.
        else {
            for (int i = 0; i < dataToSendArray.size(); i++){
                byte[] payload = dataToSendArray.get(i).
                        getBytes(Charset.forName("UTF-8"));

                NdefRecord record = NdefRecord.createMime("text/plain",payload);
                records[i] = record;
            }
        }
        records[dataToSendArray.size()] =
                NdefRecord.createApplicationRecord(getPackageName());
        return records;
    }
}
