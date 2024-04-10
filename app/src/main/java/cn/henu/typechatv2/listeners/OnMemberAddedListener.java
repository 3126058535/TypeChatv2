package cn.henu.typechatv2.listeners;

import java.util.List;

import cn.henu.typechatv2.models.User;

public interface OnMemberAddedListener {
    void onMemberAdded(List<User> users);
}
