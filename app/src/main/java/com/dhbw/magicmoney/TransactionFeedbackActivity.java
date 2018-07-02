package com.dhbw.magicmoney;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class TransactionFeedbackActivity extends AppCompatActivity {

    private ImageView ivContinue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_feedback);

        ivContinue = findViewById(R.id.ivContinue);
        ivContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(TransactionFeedbackActivity.this, HomeActivity.class);
                TransactionFeedbackActivity.this.startActivity(myIntent);
            }
        });
    }
}
