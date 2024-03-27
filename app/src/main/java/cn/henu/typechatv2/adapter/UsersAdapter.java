package cn.henu.typechatv2.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.henu.typechatv2.databinding.ItemContainerUserBinding;
import cn.henu.typechatv2.listeners.UserListener;
import cn.henu.typechatv2.models.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewUserHolder> {
    private final List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public ViewUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding binding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewUserHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewUserHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewUserHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding Binding;

        public ViewUserHolder(ItemContainerUserBinding binding) {
            super(binding.getRoot());
            Binding = binding;

        }

        public void setUserData(User user) {
            Binding.imageProfile.setImageBitmap(getUserImage(user.image));
            Binding.textName.setText(user.name);
            Binding.textEmail.setText(user.email);
            Binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }
    private Bitmap getUserImage(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
