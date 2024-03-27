package cn.henu.typechatv2.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.henu.typechatv2.databinding.ItemContainerAddedMemberBinding;
import cn.henu.typechatv2.models.User;

public class AddedMembersAdapter extends RecyclerView.Adapter<AddedMembersAdapter.MemberViewHolder> {
    private final List<User> members;

    public AddedMembersAdapter(List<User> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerAddedMemberBinding binding = ItemContainerAddedMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerAddedMemberBinding binding;

        MemberViewHolder(ItemContainerAddedMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.textName.setText(user.name);
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}