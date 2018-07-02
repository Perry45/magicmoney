package com.dhbw.magicmoney;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class AccountActivity extends NavigationActivity {

    private PassChangeTask passChangeTask = null;

    private User user;

    private EditText oldpassView;
    private EditText newpass1View;
    private EditText newpass2View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        user = (User) getApplication();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Accountinformationen");



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView =  navigationView.getHeaderView(0);
        TextView navHeaderName = (TextView)hView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = (TextView)hView.findViewById(R.id.nav_header_email);
        navHeaderName.setText(HomeActivity.user.getUsername());
        navHeaderEmail.setText(HomeActivity.user.getEmail());

        TextView username = (TextView) findViewById(R.id.acc_user);
        TextView forename = (TextView) findViewById(R.id.acc_forename);
        TextView name = (TextView) findViewById(R.id.acc_name);
        TextView email = (TextView) findViewById(R.id.acc_email);
        username.setText(user.getUsername());
        forename.setText(user.getForename());
        name.setText(user.getName());
        email.setText(user.getEmail());

        oldpassView = (EditText) findViewById(R.id.account_pass);
        newpass1View = (EditText) findViewById(R.id.account_passNeu1);
        newpass2View = (EditText) findViewById(R.id.account_passNeu2);

        Button changePass = (Button) findViewById(R.id.account_changePass);

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptPassChange();
            }
        });


    }

    private void attemptPassChange() {
        if (passChangeTask != null) {
            return;
        }

        oldpassView.setError(null);
        newpass1View.setError(null);
        newpass2View.setError(null);

        String oldpass = oldpassView.getText().toString();
        String newpass1 = newpass1View.getText().toString();
        String newpass2 = newpass2View.getText().toString();

        if(newpass1.length() < 6){
            newpass1View.setError(getString(R.string.error_invalid_password));
            newpass1View.requestFocus();
        } else if (!newpass1.equals(newpass2)){
            newpass2View.setError(getString(R.string.error_notsame_password));
            newpass2View.requestFocus();
        } else {
            passChangeTask = new PassChangeTask(oldpass, newpass1);
            passChangeTask.execute((Void) null);
        }

    }

    public class PassChangeTask extends AsyncTask<Void, Void, Boolean> {

        private final String oldpass;
        private final String newpass;

        PassChangeTask(String oldpass, String newpass) {
            this.oldpass= oldpass;
            this.newpass= newpass;
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
                user = accountDao.queryForEq("email",user.getEmail()).get(0);
                System.out.println(user.toString());
                System.out.println("\n\nIt seems to have worked\n\n");
                if(user.getPassword().equals(oldpass)) {
                    sucess = true;
                    try {
                        UpdateBuilder<User, Integer> updateBuilder = accountDao.updateBuilder();
                        updateBuilder.updateColumnValue("password", newpass);
                        updateBuilder.where().eq("email",user.getEmail());
                        updateBuilder.update();
                    } catch (Exception e) {
                        System.out.println(e);
                        e.printStackTrace();
                        sucess = false;
                    }
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
            passChangeTask = null;

            if (success) {
                oldpassView.setText("");
                newpass1View.setText("");
                newpass2View.setText("");
                // TODO alert success
            } else {
                oldpassView.setError(getString(R.string.error_incorrect_password));
                oldpassView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            passChangeTask = null;
        }
    }

}
