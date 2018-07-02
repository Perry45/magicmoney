package com.dhbw.magicmoney;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import org.xmlpull.v1.XmlSerializer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private UserRegisterTask registerTask = null;

    private EditText usernameView;
    private EditText nameView;
    private EditText forenameView;
    private EditText emailView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Registrieren");
        setSupportActionBar(toolbar);


        usernameView = (EditText) findViewById(R.id.register_username);
        nameView = (EditText) findViewById(R.id.register_name);
        forenameView = (EditText) findViewById(R.id.register_forename);
        emailView = (EditText) findViewById(R.id.register_email);
        passwordView = (EditText) findViewById(R.id.register_password);

        Button registerButton = (Button) findViewById(R.id.register_register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        Button backButton = (Button) findViewById(R.id.register_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                RegisterActivity.this.startActivity(myIntent);
            }
        });
    }

    private void attemptRegister() {
        if (registerTask != null) {
            return;
        }

        usernameView.setError(null);
        nameView.setError(null);
        forenameView.setError(null);
        emailView.setError(null);
        passwordView.setError(null);

        String username = usernameView.getText().toString();
        String name = nameView.getText().toString();
        String forename = forenameView.getText().toString();
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        if (!name.matches("[a-zA-Z]+$") || name.isEmpty()) {
            nameView.setError(getString(R.string.error_invalid_name));
            nameView.requestFocus();
        } else if(!forename.matches("[a-zA-Z]+$") || forename.isEmpty()){
            forenameView.setError(getString(R.string.error_invalid_name));
            forenameView.requestFocus();
        } else if(!username.matches("[a-zA-Z0-9]+$") || username.isEmpty()){
            usernameView.setError(getString(R.string.error_invalid_name));
            usernameView.requestFocus();
        } else if(!isValidEmailAddress(email) || email.isEmpty()){
            emailView.setError(getString(R.string.error_invalid_email));
            emailView.requestFocus();
        } else if(password.length() < 6){
            passwordView.setError(getString(R.string.error_invalid_password));
            passwordView.requestFocus();
        } else {
            //showProgress(true);
            registerTask = new UserRegisterTask(username, name, forename, email, password);
            registerTask.execute((Void) null);
        }
    }

    public static boolean isValidEmailAddress(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return  matcher.matches();
    }


    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String name;
        private final String forename;
        private final String email;
        private final String password;
        private User user;

        UserRegisterTask(String username, String name, String forename, String email, String password) {
            this.username = username;
            this.name = name;
            this.forename = forename;
            this.email = email;
            this.password = password;

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean sucess;

            user = new User(username,email,password,name,forename, 0);

            ConnectionSource connectionSource = null;
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                // create our data-source for the database
                connectionSource = new JdbcConnectionSource("jdbc:mysql://den1.mysql2.gear.host:3306/magicmoney?autoReconnect=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "magicmoney", "magic!");
                // setup our database and DAOs
                Dao<User, Integer> accountDao = DaoManager.createDao(connectionSource, User.class);
                // read and write some data
                System.out.println(user.toString());
                accountDao.create(user);
                System.out.println("\n\nIt seems to have worked\n\n");
                sucess = true;
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
            registerTask = null;
            //showProgress(false);

            if (success) {
                finish();
                Intent myIntent = new Intent(RegisterActivity.this, HomeActivity.class);
                myIntent.putExtra("id", user.getID());
                myIntent.putExtra("username", user.getUsername());
                myIntent.putExtra("email", user.getEmail());
                myIntent.putExtra("password", user.getPassword());
                myIntent.putExtra("name", user.getName());
                myIntent.putExtra("forename", user.getForename());
                myIntent.putExtra("balance", user.getBalance());
                RegisterActivity.this.startActivity(myIntent);

                createUserXML(user.getID(), user.getUsername(), user.getEmail(), user.getPassword(), user.getName(), user.getForename(), user.getBalance());
            } else {
                emailView.setError(getString(R.string.error_alreadyInUse_email));
                emailView.requestFocus();
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
            registerTask = null;
            //showProgress(false);
        }
    }

}
