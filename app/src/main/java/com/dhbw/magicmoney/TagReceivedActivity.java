package com.dhbw.magicmoney;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class TagReceivedActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {

    private TransactionTask transactionTask = null;

    private ArrayList<String> dataToSendArray = new ArrayList<>();
    private ArrayList<String> dataReceivedArray = new ArrayList<>();

    private String insertedCode = null;

    private String transferValue = null;
    private String code = null;
    private String name = null;
    private String transactionID = null;
    private int senderID;

    private NfcAdapter mNfcAdapter;
    TextView tvShowText = null;
    Button btnConfirmCode = null;
    EditText etCode = null;

    private User u;

    private User u2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_received);

        handleNfcIntent(getIntent());

        u = (User) getApplication();

        tvShowText = findViewById(R.id.tagReceived_textView);
        etCode = findViewById(R.id.tagReceived_code);

        btnConfirmCode = (Button) findViewById(R.id.btnConfirmCode);
        btnConfirmCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertedCode = etCode.getText().toString();


                Log.d("Code to put in", code);
                Log.d("Inserted Code", insertedCode);

                if (insertedCode.equals(code)){
                    Log.d("Code", "confirmed");

                    attemptTransaction();


                    Intent myIntent = new Intent(TagReceivedActivity.this, TransactionFeedbackActivity.class);
                    TagReceivedActivity.this.startActivity(myIntent);

                }
                else{
                    Log.d("Code","wrong");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //TODO ErrorMessage if there is time left
                            etCode.setText("");
                        }
                    });

                }
            }
        });
        tvShowText.setText(name + " möchte dir " + transferValue + " schicken. Um dies zu bestätigen trage in das Feld den vierstelligen Code ein, welcher auf " + name + "s Gerät angezeigt wird.");


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(mNfcAdapter != null) {
            //Handle some NFC initialization here
        }
        else {
            Toast.makeText(this, "NFC not available on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void attemptTransaction(){

        if (transactionTask != null) {
            return;
        }

        //Letzten 2 Stellen abschneiden, um das Euro Zeichen zu entfernen
        String toTransferWithoutCurrency = transferValue.substring(0, transferValue.length() -2);

        //Komma mit Punkt ersetzen
        toTransferWithoutCurrency = toTransferWithoutCurrency.replace(",", ".");

        //String to Int
        double transferValueInt = Double.parseDouble(toTransferWithoutCurrency);

        transactionTask = new TransactionTask(HomeActivity.user.getID(), senderID, transferValueInt);
        transactionTask.execute((Void) null);

    }

    //not used here actually
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        return null;
    }

    //not used here actually
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Toast.makeText(this, "NFC signal sent!", Toast.LENGTH_SHORT);

    }


    private void handleNfcIntent(Intent NfcIntent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(receivedArray != null) {
                dataReceivedArray.clear();
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();

                for (NdefRecord record:attachedRecords) {
                    String string = new String(record.getPayload());

                    if (string.equals(getPackageName())) { continue; }
                    dataReceivedArray.add(string);
                }

                transferValue = dataReceivedArray.get(0);
                code = dataReceivedArray.get(1);
                name = dataReceivedArray.get(2);
                senderID = Integer.parseInt(dataReceivedArray.get(3));

            }
            else {
                Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handleNfcIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }

    /* Task to update Account Balance and to create a new Transaction into the Transaction DB */
    public class TransactionTask extends AsyncTask<Void, Void, Boolean> {

        private final int receiverID;
        private final int senderID;
        private final double transferValue;
        private Transaction transaction;

        TransactionTask(int receiverID, int senderID, double transferValue) {
            this.receiverID = receiverID;
            this.senderID = senderID;
            this.transferValue = transferValue;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean success;

            transaction = new Transaction(senderID,receiverID,transferValue);

            ConnectionSource connectionSource = null;
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                // create our data-source for the database
                connectionSource = new JdbcConnectionSource("jdbc:mysql://den1.mysql2.gear.host:3306/magicmoney?autoReconnect=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "magicmoney", "magic!");
                // setup our database and DAOs
                Dao<Transaction, Integer> transactionDao = DaoManager.createDao(connectionSource, Transaction.class);
                Dao<User, Integer> userDao = DaoManager.createDao(connectionSource, User.class);
                u = userDao.queryForEq("email",u.getEmail()).get(0);
                u2 = userDao.queryForEq("ID",senderID).get(0);
                UpdateBuilder<User, Integer> updateBuilder = userDao.updateBuilder();
                updateBuilder.updateColumnValue("Kontostand", u.getBalance() + transferValue);
                updateBuilder.where().eq("email",u.getEmail());
                updateBuilder.update();
                updateBuilder.updateColumnValue("Kontostand", u2.getBalance() - transferValue);
                updateBuilder.where().eq("ID",senderID);
                updateBuilder.update();

                transactionDao.create(transaction);
                success = true;
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                success = false;
            }
            finally {
                // destroy the data source which should close underlying connections
                if (connectionSource != null) {
                    try {
                        connectionSource.close();
                    } catch (Exception e){
                        System.out.println(e);
                        e.printStackTrace();
                    }
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            transactionTask = null;
            //showProgress(false);

            if (success) {
                HomeActivity.user.riseBalance(transferValue);
            } else {
            }
        }

        @Override
        protected void onCancelled() {
            transactionTask = null;
        }
    }
}
