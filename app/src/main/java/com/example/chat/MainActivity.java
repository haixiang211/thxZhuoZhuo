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
    private int expectedMsgId = 0; // 消息ID通常从0开始
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
        android.util.Log.d("ChatApp", "Received msgId: " + message.getMsgId() + " type: " + message.getMsgType());
        messageBuffer.offer(message);
        
        // 容错：如果收到的消息缓冲最小ID是1，且一直在等待0，我们修正 expectedMsgId 为 1
        if (expectedMsgId == 0 && messageList.isEmpty() && !messageBuffer.isEmpty() && messageBuffer.peek().getMsgId() == 1 && messageBuffer.size() >= 3) {
            expectedMsgId = 1;
        }

        int initialSize = messageList.size();
        while (!messageBuffer.isEmpty() && messageBuffer.peek().getMsgId() == expectedMsgId) {
            Message msg = messageBuffer.poll();
            messageList.add(msg);
            expectedMsgId++;
        }
        
        int insertCount = messageList.size() - initialSize;
        if (insertCount > 0) {
            adapter.notifyItemRangeInserted(initialSize, insertCount);
            recyclerView.scrollToPosition(messageList.size() - 1);
        }
    }
}
