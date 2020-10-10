package com.mohammadsuhail.letschatencrypted;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    private ArrayList<Chat> chatlist;
    private Context context;

    public ChatAdapter(ArrayList<Chat> chatList, Context c) {
        this.chatlist = chatList;
        this.context = c;
    }


    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.contact_item_view, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {

        final Chat chat = chatlist.get(position);
        holder.setChatName(chat.getName());
        holder.setChatNumber(chat.getNumber());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ChatboxActivity.class);
                intent.putExtra("name",chat.getName());
                intent.putExtra("number",chat.getNumber());
                ((Activity)view.getContext()).startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatlist == null? 0: chatlist.size();
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private TextView txtNumber;

        public ChatHolder(View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txt_name);
            txtNumber = itemView.findViewById(R.id.txt_number);
        }

        public void setChatName(String name) {
            txtName.setText(name);
        }

        public void setChatNumber(String number) {
            txtNumber.setText(number);
        }
    }
}
