package cn.henu.typechatv2.models;

import java.util.Date;

public class GroupChat {
    public String senderId;
    public String groupId; // 群聊ID
    public String message;
    public String dateTime;
    public Date dateObject;

    // 构造函数
    public GroupChat(String senderId, String groupId, String message, String dateTime, Date dateObject) {
        this.senderId = senderId;
        this.groupId = groupId;
        this.message = message;
        this.dateTime = dateTime;
        this.dateObject = dateObject;
    }
}
