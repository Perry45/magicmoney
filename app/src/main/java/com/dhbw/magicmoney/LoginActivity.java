package com.dhbw.magicmoney;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Boolean isOnline = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.login_button);
        Button registerButton = (Button) findViewById(R.id.register_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()) {
                    System.out.println("ONLINE");
                    attemptLogin();
                } else{
                    System.out.println("OFFLINE");
                    attemptOfflineLogin();
                }
            }
        });

        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                LoginActivity.this.startActivity(myIntent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptOfflineLogin() {
//        if (mAuthTask != null) {
//            return;
//        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (password.isEmpty()) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!RegisterActivity.isValidEmailAddress(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else{
            showProgress(true);

            File file = new File(getApplicationContext().getFilesDir(),"user.xml");

            String idXML = null;
            String usernameXML = null;
            String emailXML = null;
            String passwordXML = null;
            String nameXML = null;
            String forenameXML = null;
            double balanceXML = 0;

            try {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(false);
                factory.setValidating(false);
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document document = builder.parse(file);
                Element catalog = document.getDocumentElement();
                NodeList nodeList = catalog.getChildNodes();

                Node idNode = nodeList.item(1);
                idXML = getCharacterData(idNode);

                Node usernameNode = nodeList.item(3);
                usernameXML = getCharacterData(usernameNode);

                Node emailNode = nodeList.item(5);
                emailXML = getCharacterData(emailNode);

                Node passwordNode = nodeList.item(7);
                passwordXML = getCharacterData(passwordNode);

                Node nameNode = nodeList.item(9);
                nameXML = getCharacterData(nameNode);

                Node forenameNode = nodeList.item(11);
                forenameXML = getCharacterData(forenameNode);

                Node balanceNode = nodeList.item(13);
                balanceXML = Double.parseDouble(getCharacterData(balanceNode));


                //finish();

            }catch (Exception e){
                System.out.println(e);
            }
            System.out.println(emailXML);
            System.out.println(passwordXML);
            if (email.equals(emailXML) && password.equals(passwordXML)) {
                finish();
                Intent myIntent = new Intent(LoginActivity.this, HomeActivity.class);
                myIntent.putExtra("id", idXML);
                myIntent.putExtra("username", usernameXML);
                myIntent.putExtra("email", emailXML);
                myIntent.putExtra("password", passwordXML);
                myIntent.putExtra("name", nameXML);
                myIntent.putExtra("forename", forenameXML);
                myIntent.putExtra("balance", balanceXML);
                LoginActivity.this.startActivity(myIntent);
            } else {
                showProgress(false);
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (password.isEmpty()) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!RegisterActivity.isValidEmailAddress(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private User user;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean sucess;

            ConnectionSource connectionSource = null;
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                // create our data-source for the database
                connectionSource = new JdbcConnectionSource("jdbc:mysql://den1.mysql2.gear.host:3306/magicmoney?autoReconnect=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "magicmoney", "magic!");
                // setup our database and DAOs
                Dao<User, Integer> accountDao = DaoManager.createDao(connectionSource, User.class);
                // read and write some data
                user = accountDao.queryForEq("email",mEmail).get(0);
                System.out.println(user.toString());
                if(user.getPassword().equals(mPassword)) {
                    sucess = true;
                } else {
                    sucess = false;
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                sucess = false;
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

            return sucess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();

                File file = new File(getApplicationContext().getFilesDir(),"user.xml");

                if(!file.exists()){
                    createUserXML(user.getID(), user.getUsername(), user.getEmail(), user.getPassword(), user.getName(), user.getForename(), user.getBalance());
                }


                Intent myIntent = new Intent(LoginActivity.this,HomeActivity.class);
                myIntent.putExtra("id", user.getID());
                myIntent.putExtra("username", user.getUsername());
                myIntent.putExtra("email", user.getEmail());
                myIntent.putExtra("password", user.getPassword());
                myIntent.putExtra("name", user.getName());
                myIntent.putExtra("forename", user.getForename());
                myIntent.putExtra("balance", user.getBalance());
                LoginActivity.this.startActivity(myIntent);
                }
                else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
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

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

