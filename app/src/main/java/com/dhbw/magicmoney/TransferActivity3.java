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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class TransferActivity3 extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {
    String transferValue ="";
    TextView tvShowCode = null;
    TextView tvNfcSignalSent = null;
    User u = null;

    private NfcAdapter mNfcAdapter;

    private ArrayList<String> dataToSendArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer3);

        tvShowCode = findViewById(R.id.transfer3_showCode);
        tvNfcSignalSent = findViewById(R.id.tvNfcSignalSent);

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

        if(!isNetworkAvailable()){
            File file = new File(getApplicationContext().getFilesDir(),"user.xml");

            try {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(false);
                factory.setValidating(false);
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document document = builder.parse(file);
                Element catalog = document.getDocumentElement();
                NodeList nodeList = catalog.getChildNodes();

                Node balanceNode = nodeList.item(13);

                //Letzten 2 Stellen abschneiden, um das Euro Zeichen zu entfernen
                String toTransferWithoutCurrency = transferValue.substring(0, transferValue.length() -2);

                //Komma mit Punkt ersetzen
                toTransferWithoutCurrency = toTransferWithoutCurrency.replace(",", ".");

                //String to Int
                double transferValueInt = Double.parseDouble(toTransferWithoutCurrency);

                double currentBalance = Double.parseDouble(getCharacterData(balanceNode));
                double newBalance = currentBalance - transferValueInt;


                file.delete();

                createUserXML(u.getID(), u.getUsername(), u.getEmail(), u.getPassword(), u.getName(), u.getForename(), newBalance);

                //finish();

            }catch (Exception e){
                System.out.println(e);
            }
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
