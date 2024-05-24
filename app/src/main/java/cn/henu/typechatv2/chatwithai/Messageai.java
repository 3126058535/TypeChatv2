package cn.henu.typechatv2.chatwithai;

public class Messageai {
    private final String time;
    private String content;
    private boolean isUserMessage;

    public Messageai(String content, boolean isUserMessage,String time) {
        this.content = content;
        this.isUserMessage = isUserMessage;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public String getTime() {
        return time;
    }
}
