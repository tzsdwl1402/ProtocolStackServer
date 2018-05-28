package com.yijiagou.Vo;

public class VoFriend {
    private String userName;
    private String nickName;
    private String gender;
    private int age;
    private String address;

    public VoFriend(){

    }

    public VoFriend(String userName, String nickName, String gender, int age, String address) {
        this.userName = userName;
        this.nickName = nickName;
        this.gender = gender;
        this.age = age;
        this.address = address;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
