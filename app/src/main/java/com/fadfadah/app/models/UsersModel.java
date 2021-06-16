package com.fadfadah.app.models;

public class UsersModel {
    private String userKey = "";
    private String firstName = "";
    private String lastName = "";
    private int day;
    private int month;
    private int year;
    private String emailAddress = null;
    private String userToken = "";
    private String image = "";
    //0 male , 1 female
    private int sex;
    // 0 offline , 1 online
    private long lastOnlineTime = 0;
    // 0 private , 1 public
    private int privacy = 1;
    private String country = "", city = "";
    private String cover;

    public UsersModel() {
    }

    public String getCover() {
        if (cover == null || cover.isEmpty())
            return "https://www.jewishdeafcongress.org/wp-content/uploads/2019/06/1920x1080-light-blue-solid-color-background.jpg";
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public UsersModel(String userKey, String firstName, String lastName, int day, int month,
                      int year, String emailAddress, String userToken,
                      String image, long lastOnlineTime, int sex, int privacy, String country, String city, String cover) {
        this.userKey = userKey;
        this.firstName = firstName;
        this.lastName = lastName;
        this.day = day;
        this.cover = cover;
        this.month = month;
        this.year = year;
        this.emailAddress = emailAddress;
        this.userToken = userToken;
        this.image = image;
        this.lastOnlineTime = lastOnlineTime;
        this.privacy = privacy;
        this.sex = sex;
        this.country = country;
        this.city = city;
    }

    public String getCountry() {
        if (country == null)
            return "----";
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        if (city == null)
            return "";
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getImage() {
        if (image == null)
            return "";
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserKey() {
        if (userKey == null)
            return "";
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getFirstName() {
        if (firstName == null)
            return "";
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        if (lastName == null)
            return "";
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getEmailAddress() {
        if (emailAddress == null)
            return "";
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getUserToken() {
        if (userKey == null)
            return "";
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public long getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(long lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public int getPrivacy() {
        return privacy;
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
    }
}
