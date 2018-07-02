package com.dhbw.magicmoney;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TransferActivity2 extends AppCompatActivity {
    String transferValue ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bestätigung");

        TextView transferValueNumberDisplay = (TextView) findViewById(R.id.transfer2_value);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null)
        {
            transferValue = (String) bundle.get("transferValue");
            transferValueNumberDisplay.setText(transferValue);
        }

        Button confirmButton = findViewById(R.id.transfer2_button_confirm);
        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent myIntent = new Intent(TransferActivity2.this, TransferActivity3.class);
                myIntent.putExtra("transferValue", transferValue);
                TransferActivity2.this.startActivity(myIntent);
            }
        });

        Button backButton = findViewById(R.id.transfer2_button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(TransferActivity2.this, TransferActivity1.class);
                TransferActivity2.this.startActivity(myIntent);
            }
        });


    }

}
