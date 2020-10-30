package com.mohammadsuhail.letschatencrypted;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    private ArrayList<Contact> chatlist;
    private Context context;

    public ChatAdapter(ArrayList<Contact> chatList, Context c) {
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onBindViewHolder(@NonNull final ChatHolder holder, int position) {
        Contact contact = chatlist.get(position);
        holder.txtName.setText(contact.getName());
        holder.txtNumber.setText(contact.getNumber());
        FirebaseDatabase.getInstance().getReference().child("Users").child(contact.getNumber()).child("image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.setImageView(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (contact.getUnread() == 1) holder.dot.setVisibility(View.VISIBLE);


        holder.itemView.setOnClickListener(view -> {

            Intent intent = new Intent(context, ChatboxActivity.class);
            intent.putExtra("name", contact.getName());
            intent.putExtra("number", contact.getNumber());
            intent.putExtra("profileurl", contact.getImage());
            holder.dot.setVisibility(View.INVISIBLE);
            DatabaseHandler db = new DatabaseHandler(context);
            db.removeUnread(contact);
            ArrayList<Contact> temp = db.getAllChats();
            for (Contact c:temp) {
                Log.i("xxxx",c.getUnread()+ " D");
            }
            view.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }

    public class ChatHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        private TextView txtNumber;
        private CircleImageView imageView;
        private ImageView dot;

        public ChatHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileID);
            txtName = itemView.findViewById(R.id.txt_name);
            txtNumber = itemView.findViewById(R.id.txt_number);
            dot = itemView.findViewById(R.id.dot_id);
        }

        public void setImageView(String url) {
            if (!url.equals("nil")) Glide.with(context).load(Uri.parse(url)).into(imageView);
        }
    }
}
