package com.dhbw.magicmoney;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class ChargeActivity extends NavigationActivity {

    private User u;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Geld aufladen");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        u = (User) getApplication();

        final TextView balanceView = (TextView) findViewById(R.id.charge_balance_view);
        balanceView.setText(u.getEURBalance());

        final Button fiveButton = (Button) findViewById(R.id.charge_charge5_button);
        final Button tenButton = (Button) findViewById(R.id.charge_charge10_button);
        final Button twentyButton = (Button) findViewById(R.id.charge_charge20_button);
        final Button fiftyButton = (Button) findViewById(R.id.charge_charge50_button);

        final Activity cont=this;
        fiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()) {
                    new ChargeBalanceAsync(cont).execute(5);
                    balanceView.setText(u.getEURBalance());
                } else{
                    fiveButton.setError(getString(R.string.error_no_network));
                    fiveButton.requestFocus();
                }
            }
        });
        tenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()) {
                    new ChargeBalanceAsync(cont).execute(10);
                    balanceView.setText(u.getEURBalance());
                } else{
                    tenButton.setError(getString(R.string.error_no_network));
                    tenButton.requestFocus();
                }
            }
        });
        twentyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()) {
                     new ChargeBalanceAsync(cont).execute(20);
                     balanceView.setText(u.getEURBalance());
                } else{
                    twentyButton.setError(getString(R.string.error_no_network));
                    twentyButton.requestFocus();
                }

            }
        });
        fiftyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()) {
                    new ChargeBalanceAsync(cont).execute(50);
                    balanceView.setText(u.getEURBalance());
                } else{
                    fiftyButton.setError(getString(R.string.error_no_network));
                    fiftyButton.requestFocus();
                }
            }
        });

        View hView =  navigationView.getHeaderView(0);
        TextView navHeaderName = (TextView)hView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = (TextView)hView.findViewById(R.id.nav_header_email);
        navHeaderName.setText(HomeActivity.user.getUsername());
        navHeaderEmail.setText(HomeActivity.user.getEmail());

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}
