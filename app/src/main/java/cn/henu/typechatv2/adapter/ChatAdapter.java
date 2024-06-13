package cn.henu.typechatv2.adapter;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.henu.typechatv2.databinding.ItemContainerRecieverMessageBinding;
import cn.henu.typechatv2.databinding.ItemContainerSendMessageBinding;
import cn.henu.typechatv2.models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImageBitmap;
    private final String senderId;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImageBitmap, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImageBitmap = receiverProfileImageBitmap;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageHolder(
                    ItemContainerSendMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceivedMessageHolder(
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
            ((SentMessageHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageHolder) holder).setData(chatMessages.get(position), receiverProfileImageBitmap);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            //Log.d("ChatAdapter", "getItemViewType: " + VIEW_TYPE_SENT +" " + chatMessages.get(position).senderId+" "+ senderId);
            return VIEW_TYPE_SENT;
        } else {
            //Log.d("ChatAdapter", "getItemViewType: " + VIEW_TYPE_RECEIVED +" "+ chatMessages.get(position).senderId+" "+ senderId);
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSendMessageBinding binding;
        private Bitmap receiverProfileImageBitmap;
        SentMessageHolder(ItemContainerSendMessageBinding itemContainerSendMessageBinding) {
            super(itemContainerSendMessageBinding.getRoot());
            this.binding = itemContainerSendMessageBinding;
        }
        void setData(ChatMessage chatMessage) {
            binding.textmessage.setText(chatMessage.message);
            binding.texttime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private final ItemContainerRecieverMessageBinding binding;

        ReceivedMessageHolder(ItemContainerRecieverMessageBinding itemContainerRecieverMessageBinding) {
            super(itemContainerRecieverMessageBinding.getRoot());
            this.binding = itemContainerRecieverMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImageBitmap) {
            binding.textmessage.setText(chatMessage.message);
            binding.texttime.setText(chatMessage.dateTime);
            binding.imageProfile.setImageBitmap(receiverProfileImageBitmap);
        }
    }
}
