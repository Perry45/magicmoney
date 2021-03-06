package com.dhbw.magicmoney;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class HomeActivity extends NavigationActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private GetTransactionsTask getTransactionsTask = null;
    private GetBalanceTask getBalanceTask = null;
    private TransactionTask transactionTask = null;
    public static User user;
    private List<Transaction> transactionList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TransactionAdapter mAdapter;
    private TextView balanceView;
    List<Transaction> transactions;
    Map<Integer, String> nameList = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        balanceView = (TextView) findViewById(R.id.home_balance_view);

        if(isNetworkAvailable())
            checkOpenTransactions();

        user = (User) getApplication();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            user.setUsername((String) bundle.get("username"));
            user.setEmail((String) bundle.get("email"));
            user.setBalance((Double)bundle.get("balance"));
            user.setForename((String)bundle.get("forename"));
            user.setName((String) bundle.get("name"));
            user.setID(Integer.parseInt(bundle.get("id").toString()));

            if (isNetworkAvailable()) {

                balanceView.setText(user.getEURBalance());

                getBalanceTask = new GetBalanceTask();
                getBalanceTask.execute();
            } else{
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
                    String formattedBalance = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(Double.parseDouble(LoginActivity.getCharacterData(balanceNode)));
                    balanceView.setText(formattedBalance);
                    //finish();

                }catch (Exception e){
                    System.out.println(e);
                }
            }

        }

        getSupportActionBar().setTitle(user.getForename() + " " + user.getName());

        View hView =  navigationView.getHeaderView(0);
        TextView navHeaderName = (TextView)hView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = (TextView)hView.findViewById(R.id.nav_header_email);
        navHeaderName.setText(HomeActivity.user.getUsername());
        navHeaderEmail.setText(HomeActivity.user.getEmail());


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new TransactionAdapter(transactionList, nameList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        if (isNetworkAvailable()) {
            getTransactionsTask = new GetTransactionsTask();
            getTransactionsTask.execute();
        }


    }

    private void checkOpenTransactions() {
        //TODO test XML reading
        //TODO write to DB
        File file = new File(getApplicationContext().getFilesDir(),"transactions.xml");

        if(file.exists()) {

            try {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(false);
                factory.setValidating(false);
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document document = builder.parse(file);
                Element catalog = document.getDocumentElement();
                NodeList transactions = catalog.getChildNodes();

                Node idNode = transactions.item(1);
                int receiverID = Integer.parseInt(getCharacterData(idNode));

                Node usernameNode = transactions.item(3);
                int senderID = Integer.parseInt(getCharacterData(usernameNode));

                Node emailNode = transactions.item(5);
                double transferValue = Double.parseDouble(getCharacterData(emailNode));


                transactionTask = new TransactionTask(receiverID, senderID, transferValue);
                transactionTask.execute();


                //finish();

                file.delete();

            } catch (Exception e) {
                System.out.println(e);
            }
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
    public void onResume(){
        super.onResume();

        if(isNetworkAvailable()) {
            getBalanceTask = new GetBalanceTask();
            getBalanceTask.execute();
        } else{
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
                String formattedBalance = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(Double.parseDouble(LoginActivity.getCharacterData(balanceNode)));
                balanceView.setText(formattedBalance);


                //finish();

            }catch (Exception e){
                System.out.println(e);
            }
        }

    }


    private void generateTransactions() {

            Collections.reverse(transactions);

            for (Transaction t : transactions) {
                Transaction u = new Transaction(t.getSenderID(), t.getReceiverID(), t.getTransferValue(), t.getTimestamp());

                transactionList.add(u);
            }



        mAdapter.notifyDataSetChanged();
    }

    /* Task to get all Transcations from the user */
    public class GetTransactionsTask extends AsyncTask<Void, Void, Boolean> {

        boolean success;
        private Transaction sampleTransaction;


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            generateTransactions();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            User dbuser;
            ConnectionSource connectionSource = null;
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                // create our data-source for the database
                connectionSource = new JdbcConnectionSource("jdbc:mysql://den1.mysql2.gear.host:3306/magicmoney?autoReconnect=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "magicmoney", "magic!");
                // setup our database and DAOs
                Dao<Transaction, Integer> transactionDao = DaoManager.createDao(connectionSource, Transaction.class);
                Dao<User, Integer> userDao = DaoManager.createDao(connectionSource, User.class);
                sampleTransaction = new Transaction();

                QueryBuilder<Transaction, Integer> qb = transactionDao.queryBuilder();

                Where where = qb.where();
                where.eq("EmpfängerKundenID", user.getID());
                // and
                where.or();
                where.eq("SenderKundenID", user.getID());
                PreparedQuery<Transaction> preparedQuery = qb.prepare();

                transactions =  qb.query();

                HashSet<Integer> set = new HashSet<Integer>();
                for (int i = 0; i < transactions.size(); i++) {
                    if(transactions.get(i).receiverID != user.getID()){
                       set.add(transactions.get(i).receiverID);
                    }
                    if(transactions.get(i).senderID != user.getID()){
                        set.add(transactions.get(i).senderID);
                    }
                }
                for (int i : set) {
                    dbuser = userDao.queryForEq("ID", i).get(0);
                    nameList.put(i, dbuser.getForename() + " " + dbuser.getName());
                }

                Log.d("Result", Integer.toString(transactions.size()));

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


    }

    /* Task to get current Balance from DB */
    public class GetBalanceTask extends AsyncTask<Void, Void, Boolean> {

        boolean success;

        @Override
        protected Boolean doInBackground(Void... voids) {

            ConnectionSource connectionSource = null;
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                // create our data-source for the database
                connectionSource = new JdbcConnectionSource("jdbc:mysql://den1.mysql2.gear.host:3306/magicmoney?autoReconnect=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "magicmoney", "magic!");
                // setup our database and DAOs
                Dao<User, Integer> accountDao = DaoManager.createDao(connectionSource, User.class);

                QueryBuilder<User, Integer> qb = accountDao.queryBuilder();

                Where where = qb.where();
                where.eq("ID", user.getID());

                PreparedQuery<User> preparedQuery = qb.prepare();

                List<User> balance =  qb.query();

                user.setBalance(balance.get(0).getBalance());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        balanceView.setText(user.getEURBalance());
                    }
                });

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
        protected void onPostExecute(Boolean aBoolean){

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(getApplicationContext().openFileInput("user.xml"));

                Node node = doc.getElementById("balance");
                node.setNodeValue(Double.toString(user.balance));

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource dSource = new DOMSource(doc);
                StreamResult result = new StreamResult(getApplicationContext().openFileOutput("user.xml", Context.MODE_PRIVATE));
                transformer.transform(dSource, result);

                Log.d("XML", "Balance update successful");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

            transaction = new Transaction(senderID, receiverID, transferValue);

            User u = (User) getApplication();

            ConnectionSource connectionSource = null;
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                // create our data-source for the database
                connectionSource = new JdbcConnectionSource("jdbc:mysql://den1.mysql2.gear.host:3306/magicmoney?autoReconnect=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "magicmoney", "magic!");
                // setup our database and DAOs
                Dao<Transaction, Integer> transactionDao = DaoManager.createDao(connectionSource, Transaction.class);
                Dao<User, Integer> userDao = DaoManager.createDao(connectionSource, User.class);
                u = userDao.queryForEq("email", u.getEmail()).get(0);
                User u2 = userDao.queryForEq("ID", senderID).get(0);
                UpdateBuilder<User, Integer> updateBuilder = userDao.updateBuilder();
                updateBuilder.updateColumnValue("Kontostand", u.getBalance() + transferValue);
                updateBuilder.where().eq("email", u.getEmail());
                updateBuilder.update();
                updateBuilder.updateColumnValue("Kontostand", u2.getBalance() - transferValue);
                updateBuilder.where().eq("ID", senderID);
                updateBuilder.update();

                transactionDao.create(transaction);
                success = true;
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                success = false;
            } finally {
                // destroy the data source which should close underlying connections
                if (connectionSource != null) {
                    try {
                        connectionSource.close();
                    } catch (Exception e) {
                        System.out.println(e);
                        e.printStackTrace();
                    }
                }
            }

            return success;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isNetworkAvailable()) {
                    balanceView.setText("Berechne dein Guthaben...");
                }
            }
        });
    }
}
