package com.example.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import io.zhuozhuo.remotetestlib.DataCenter;
import io.zhuozhuo.remotetestlib.Message;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private final List<Message> messageList = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int expectedMsgId = 1;
    private final PriorityQueue<Message> messageBuffer = new PriorityQueue<>(22, (m1, m2) -> Integer.compare(m1.getMsgId(), m2.getMsgId()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataCenter.init(this);
        ImageCache.getInstance().init(this);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(this, messageList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.setScrolling(false);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.setScrolling(true);
                }
            }
        });

        DataCenter.register(new DataCenter.OnMessageChangeListener() {
            @Override
            public void onMessageChange(Message message) {
                mainHandler.post(() -> handleNewMessage(message));
            }
        });
    }

    private void handleNewMessage(Message message) {
        messageBuffer.offer(message);
        boolean added = false;
        while (!messageBuffer.isEmpty() && messageBuffer.peek().getMsgId() == expectedMsgId) {
            Message msg = messageBuffer.poll();
            messageList.add(msg);
            expectedMsgId++;
            added = true;
        }
        if (added) {
            adapter.notifyItemRangeInserted(messageList.size() - 1, 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        }
    }
}
