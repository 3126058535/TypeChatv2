package cn.henu.typechatv2.models;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    public String groupname, image, id, description, createdBy;
    public List<String> members;
}
