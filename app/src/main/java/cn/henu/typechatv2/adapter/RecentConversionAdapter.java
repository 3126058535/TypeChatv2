package cn.henu.typechatv2.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.henu.typechatv2.databinding.ItemContainerRecentConversionBinding;
import cn.henu.typechatv2.listeners.ConversionListener;
import cn.henu.typechatv2.models.ChatMessage;
import cn.henu.typechatv2.models.Group;
import cn.henu.typechatv2.models.User;

public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder>{

    private final List<ChatMessage> chatMessages;

    private final ConversionListener conversionListener;

    public RecentConversionAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setDta(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;
       ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }
        void setDta(ChatMessage chatMessage) {
           if (chatMessage.conversionImage == null) {

            } else {
                binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            }
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                if (chatMessage.isGroup) {
                    Group group = new Group();
                    group.id = chatMessage.conversionId;
                    group.groupname = chatMessage.conversionName;
                    group.image = chatMessage.conversionImage;
                    conversionListener.onGroupConversionClicked(group);
                } else {
                    User user = new User();
                    user.id = chatMessage.conversionId;
                    user.name = chatMessage.conversionName;
                    user.image = chatMessage.conversionImage;
                    conversionListener.onUserConversionClicked(user);
                }
            });
        }

    }
    private Bitmap getConversionImage(String conversionImage) {
        byte[] bytes = Base64.decode(conversionImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

