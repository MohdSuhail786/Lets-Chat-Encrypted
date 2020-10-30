package com.mohammadsuhail.letschatencrypted;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<Message> messagelist;


    public MessageListAdapter(Context context,ArrayList<Message> ml) {
        this.context = context;
        this.messagelist = ml;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagelist.get(position);
        if (message.getStatus().equals("TO")) return 1;
        return 2;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            view = layoutInflater.inflate(R.layout.item_message_send, parent, false);
            return new SendMessageHolder(view);
        }
        else if (viewType == 2) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            view = layoutInflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messagelist.get(position);
        switch (holder.getItemViewType()) {
            case 1: ((SendMessageHolder)holder).bind(message); break;
            case 2: ((ReceivedMessageHolder)holder).bind(message); break;
        }
    }

    @Override
    public int getItemCount() {
        return messagelist.size();
    }

    private static class ReceivedMessageHolder extends  RecyclerView.ViewHolder {
        TextView messageText, timeText;
        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.item_rmessage_id);
            timeText = itemView.findViewById(R.id.item_rtime_id);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            timeText.setText(message.getTime());
        }
    }

    private static class SendMessageHolder extends  RecyclerView.ViewHolder {
        TextView messageText, timeText;
        public SendMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.item_smessage_id);
            timeText = itemView.findViewById(R.id.item_stime_id);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            timeText.setText(message.getTime());
        }
    }

}
