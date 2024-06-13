package cn.henu.typechatv2.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.henu.typechatv2.databinding.ItemContainerGroupBinding;
import cn.henu.typechatv2.listeners.GroupListener;
import cn.henu.typechatv2.models.Group;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewGroupHolder>{
    private final List<Group> groups;
    private final GroupListener groupListener;
    public GroupAdapter(List<Group> groups, GroupListener userListener) {
        this.groups = groups;
        this.groupListener = userListener;
    }
    @NonNull
    @Override
    public ViewGroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerGroupBinding Binding = ItemContainerGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewGroupHolder(Binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewGroupHolder holder, int position) {
        holder.setGroupData(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }
    class ViewGroupHolder extends RecyclerView.ViewHolder {
        ItemContainerGroupBinding Binding;
        public ViewGroupHolder(ItemContainerGroupBinding binding) {
            super(binding.getRoot());
            Binding = binding;
        }
        public void setGroupData(Group group) {
            Binding.groupImage.setImageBitmap(getUserImage(group.image));
            Binding.groupName.setText(group.groupname);
            Binding.getRoot().setOnClickListener(v -> groupListener.onGroupClicked(group));
        }
    }
    private Bitmap getUserImage(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
