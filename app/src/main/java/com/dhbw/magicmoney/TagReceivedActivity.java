package com.dhbw.magicmoney;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.util.Xml;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

        File file = new File(getApplicationContext().getFilesDir(),"user.xml");

        try {


            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(file);
            Element catalog = document.getDocumentElement();
            NodeList nodeList = catalog.getChildNodes();

            Node passwordNode = nodeList.item(7);
            u.setPassword(getCharacterData(passwordNode));
        } catch (Exception e){
            System.out.print(e.toString());
        }

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

                    if(isNetworkAvailable()) {
                        attemptTransaction();
                    } else{
                        attemptOfflineTransaction();
                    }

                    Intent myIntent = new Intent(TagReceivedActivity.this, TransactionFeedbackActivity.class);
                    TagReceivedActivity.this.startActivity(myIntent);

                }
                else{
                    Log.d("Code","wrong");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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

    private void attemptOfflineTransaction(){
        //Letzten 2 Stellen abschneiden, um das Euro Zeichen zu entfernen
        String toTransferWithoutCurrency = transferValue.substring(0, transferValue.length() -2);

        //Komma mit Punkt ersetzen
        toTransferWithoutCurrency = toTransferWithoutCurrency.replace(",", ".");

        //String to Int
        double transferValueInt = Double.parseDouble(toTransferWithoutCurrency);

        saveTransaction(HomeActivity.user.getID(), senderID, transferValueInt);

        File file = new File(getApplicationContext().getFilesDir(),"user.xml");

        try {
            //TODO test this
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(file);
            Element catalog = document.getDocumentElement();
            NodeList nodeList = catalog.getChildNodes();

            Node balanceNode = nodeList.item(13);

            double currentBalance = Double.parseDouble(getCharacterData(balanceNode));
            double newBalance = currentBalance + transferValueInt;

            file.delete();

            createUserXML(u.getID(), u.getUsername(), u.getEmail(), u.getPassword(), u.getName(), u.getForename(), newBalance);


            //finish();

        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void createUserXML(int id, String username, String email, String password, String name, String forename, double balance) {

        String filename = "user.xml";

        try {

            FileOutputStream fos = null;

            fos = openFileOutput(filename, Context.MODE_APPEND);



            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, "user");


            serializer.startTag(null, "id");

            serializer.text(Integer.toString(id));

            serializer.endTag(null, "id");


            serializer.startTag(null, "username");

            serializer.text(username);

            serializer.endTag(null, "username");


            serializer.startTag(null, "email");

            serializer.text(email);

            serializer.endTag(null, "email");


            serializer.startTag(null, "password");

            serializer.text(password);

            serializer.endTag(null, "password");


            serializer.startTag(null, "name");

            serializer.text(name);

            serializer.endTag(null, "name");


            serializer.startTag(null, "forename");

            serializer.text(forename);

            serializer.endTag(null, "forename");


            serializer.startTag(null, "balance");

            serializer.text(Double.toString(balance));

            serializer.endTag(null, "balance");


            serializer.endTag(null, "user");

            serializer.endDocument();

            serializer.flush();

            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    static public String getCharacterData(Node parent)
    {
        StringBuilder text = new StringBuilder();
        if ( parent == null )
            return text.toString();
        NodeList children = parent.getChildNodes();
        for (int k = 0, kn = children.getLength() ; k < kn ; k++) {
            Node child = children.item(k);
            if ( child.getNodeType() != Node.TEXT_NODE )
                break;
            text.append(child.getNodeValue());
        }
        return text.toString();
    }

    private void saveTransaction(int receiverID, int senderID, double transferValue){
        String filename = "transactions.xml";

        try {

            FileOutputStream fos = null;

            fos = openFileOutput(filename, Context.MODE_APPEND);



            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, "transaction");


            serializer.startTag(null, "receiverId");

            serializer.text(Integer.toString(receiverID));

            serializer.endTag(null, "receiverId");


            serializer.startTag(null, "senderId");

            serializer.text(Integer.toString(senderID));

            serializer.endTag(null, "senderId");


            serializer.startTag(null, "transferValue");

            serializer.text(Double.toString(transferValue));

            serializer.endTag(null, "transferValue");


            serializer.endTag(null, "transaction");

            serializer.endDocument();

            serializer.flush();

            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        return null;
    }

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
