package cn.henu.typechatv2.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.henu.typechatv2.databinding.ItemContainerAddMemberBinding;
import cn.henu.typechatv2.listeners.OnMemberAddedListener;
import cn.henu.typechatv2.models.User;

public class AddGroupMembersAdapter extends RecyclerView.Adapter<AddGroupMembersAdapter.AddMemberViewHolder> implements OnMemberAddedListener {
    private final List<User> users;
    private final OnMemberAddedListener onMemberAddedListener;

    public AddGroupMembersAdapter(List<User> users, OnMemberAddedListener onMemberAddedListener) {
        this.users = users;
        this.onMemberAddedListener = onMemberAddedListener;
    }

    @NonNull
    @Override
    public AddMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerAddMemberBinding binding = ItemContainerAddMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AddMemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddMemberViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public void onMemberAdded(User user) {
        onMemberAddedListener.onMemberAdded(user);
    }

    class AddMemberViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerAddMemberBinding binding;

        AddMemberViewHolder(ItemContainerAddMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.textName.setText(user.name);
            binding.checkBox.setChecked(false);
            binding.getRoot().setOnClickListener(v -> {
                if (binding.checkBox.isChecked()) {
                    binding.checkBox.setChecked(false);
                } else {
                    binding.checkBox.setChecked(true);
                    onMemberAddedListener.onMemberAdded(user);
                }
            });
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

}