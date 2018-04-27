package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 实验室人员
 */
public class MemberDB extends DataSupport {

    private int id;// 唯一主键
    private int serverId;// 服务端生成的Id
    private int number;// 工号或者学号（工号4位、学号7位）
    private String password;// 账号密码
    private String name;// 实验室人员名称
    private String phone;// 手机号
    private int type;// 身份类型：1主任、2老师、3学生
    private int memberId;// 上级id
    private String MAC;// 蓝牙MAC地址
    private String faceId;// 人脸标识
    private long lastEditTime;// 最后一次编辑时间
    private boolean isDelete;// 是否删除

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(long lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }
}
