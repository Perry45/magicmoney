package com.dhbw.magicmoney;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private List<Transaction> transactionList;
    private Map<Integer, String> nameList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, tvTransactionReceiver, tvTransactionVolume, tvTransactionDate;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            tvTransactionVolume = (TextView) view.findViewById(R.id.tvTransactionVolume);
            tvTransactionReceiver = (TextView) view.findViewById(R.id.tvTransactionReceiver);
            tvTransactionDate = (TextView) view.findViewById(R.id.tvTransactionDate);
        }
    }


    public TransactionAdapter(List<Transaction> pTransactionList, Map<Integer, String> nameList) {
        this.transactionList = pTransactionList;
        this.nameList = nameList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.tvTransactionVolume.setText(transaction.getEURBalance());

        if (nameList.get(transaction.getReceiverID()) != null){
            holder.tvTransactionVolume.setText("-"+transaction.getEURBalance());
            holder.tvTransactionVolume.setTextColor(Color.parseColor("#FF4081"));
        }

        String name = "Dich";
        if(nameList.get(transaction.getReceiverID()) != null) {
            name = nameList.get(transaction.getReceiverID());
        }
        holder.tvTransactionReceiver.setText("an: "+name);
        holder.tvTransactionDate.setText(transaction.getFormattedDate());
        //TODO Format-DatesString

    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}
