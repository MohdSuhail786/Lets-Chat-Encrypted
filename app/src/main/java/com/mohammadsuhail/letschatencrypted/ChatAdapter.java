package com.mohammadsuhail.letschatencrypted;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadsuhail.letschatencrypted.MainActivity.unreadChat;

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

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onBindViewHolder(@NonNull final ChatHolder holder, int position) {

        final Chat chat = chatlist.get(position);
        holder.setChatName(chat.getName());
        holder.setChatNumber(chat.getNumber());
        if (holder.isUnreadChat(chat)) holder.dot.setVisibility(View.VISIBLE);
        holder.setImageView("");
        FirebaseDatabase.getInstance().getReference().child("Users").child(chat.getNumber()).child("image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Toast.makeText(context, ""+snapshot.getValue(), Toast.LENGTH_SHORT).show();
                if (snapshot.getValue()!=null) {
                    chat.setImageurl(snapshot.getValue().toString());
                    holder.setImageView(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ChatboxActivity.class);
                intent.putExtra("name",chat.getName());
                intent.putExtra("number",chat.getNumber());
                intent.putExtra("profileurl",chat.getImageurl());
                holder.dot.setVisibility(View.INVISIBLE);
                unreadChat.remove(chat.getNumber());
                view.getContext().startActivity(intent);
                ((Activity) view.getContext()).finish();
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
        private CircleImageView imageView;
        private ImageView dot;
        public ChatHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileID);
            txtName = itemView.findViewById(R.id.txt_name);
            txtNumber = itemView.findViewById(R.id.txt_number);
            dot = itemView.findViewById(R.id.dot_id);
        }
        public void setImageView(String url) { if (!url.equals("")) Picasso.get().load(url).into(imageView); else imageView.setImageResource(R.drawable.ic_baseline_account_circle_24);
        }
        public void setChatName(String name) {
            txtName.setText(name);
        }
        public void setChatNumber(String number) {
            txtNumber.setText(number);
        }
        public boolean isUnreadChat(Chat chat) { if (unreadChat.get(chat.getNumber()) !=null) return true; return false;}
    }
}
