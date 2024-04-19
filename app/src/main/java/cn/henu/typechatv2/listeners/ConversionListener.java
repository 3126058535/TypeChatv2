package cn.henu.typechatv2.listeners;

import cn.henu.typechatv2.models.Group;
import cn.henu.typechatv2.models.User;

public interface ConversionListener {
    void onUserConversionClicked(User user);
    void onGroupConversionClicked(Group group);
}
