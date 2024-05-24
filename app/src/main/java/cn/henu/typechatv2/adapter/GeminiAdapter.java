package cn.henu.typechatv2.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.henu.typechatv2.databinding.ItemContainerRecieverMessageBinding;
import cn.henu.typechatv2.databinding.ItemContainerSendMessageBinding;
import cn.henu.typechatv2.models.ChatMessage;


public class GeminiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImageBitmap;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    public GeminiAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImageBitmap) {
        this.chatMessages = chatMessages;
        this.receiverProfileImageBitmap = receiverProfileImageBitmap;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new ChatAdapter.SentMessageHolder(
                    ItemContainerSendMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ChatAdapter.ReceivedMessageHolder(
                    ItemContainerRecieverMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false)
            );
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((ChatAdapter.SentMessageHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ChatAdapter.ReceivedMessageHolder) holder).setData(chatMessages.get(position), receiverProfileImageBitmap);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


}
