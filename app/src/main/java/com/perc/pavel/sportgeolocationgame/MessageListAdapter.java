package com.perc.pavel.sportgeolocationgame;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    
    private Context mContext;
    private List<UserMessage> mMessageList;
    private String currentLogin;
    
    public MessageListAdapter(Context context, List<UserMessage> messageList, String currentLogin) {
        mContext = context;
        mMessageList = messageList;
        this.currentLogin = currentLogin;
    }
    
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
    
    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        UserMessage message = mMessageList.get(position);
        
        if (message.getSenderLogin().equals(currentLogin)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }
    
    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("myTag", "Called onCreateViewHolder.");
    
        View view;
        
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
        
        return null;
    }
    
    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d("myTag", "Called onCreateViewHolder.");
    
        UserMessage message = mMessageList.get(position);
        
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }
    
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        
        SentMessageHolder(View itemView) {
            super(itemView);
            
            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
        }
        
        void bind(UserMessage message) {
            messageText.setText(message.getMessage());
        }
    }
    
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, nameText;
        
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            
            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
        }
        
        void bind(UserMessage message) {
            messageText.setText(message.getMessage());
            
            nameText.setText(message.getSenderLogin() + ":");
        }
    }
}