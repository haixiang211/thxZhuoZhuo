package com.example.chat;

import android.content.Context;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.zhuozhuo.remotetestlib.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final Context context;
    private final List<Message> messageList;
    private boolean isScrolling = false;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageItemView itemView = new MessageItemView(context);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemView.setLayoutParams(layoutParams);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message, isScrolling);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final MessageItemView itemView;

        public MessageViewHolder(@NonNull MessageItemView itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bind(Message message, boolean isScrolling) {
            itemView.setMessage(message, isScrolling);
        }
    }
}
