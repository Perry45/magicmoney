package com.dhbw.magicmoney;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Locale;

public class TransferActivity1 extends NavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Geld senden");

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

        final EditText transferValueNumber = (EditText) findViewById(R.id.transfer1_value);


        final Button confirmButton = (Button) findViewById(R.id.transfer1_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(TransferActivity1.this, TransferActivity2.class);
                myIntent.putExtra("transferValue", transferValueNumber.getText().toString());
                TransferActivity1.this.startActivity(myIntent);
            }
        });

        transferValueNumber.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    transferValueNumber.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("\\D", "");

                    double parsed = Double.parseDouble(cleanString);

                    String formatted = NumberFormat.getCurrencyInstance(Locale.GERMANY).format((parsed/100));

                    current = formatted;
                    transferValueNumber.setText(formatted);
                    transferValueNumber.setSelection(formatted.length());
                    transferValueNumber.addTextChangedListener(this);

                    if (parsed<=0){
                        confirmButton.setEnabled(false);
                    }else{
                        confirmButton.setEnabled(true);
                    }
                }
            }


            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        transferValueNumber.setText("0,00");



    }

}
