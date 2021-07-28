package net.jejer.hipda.bean;

/**
 * Created by GreenSkinMonster on 2021-07-27.
 */
public class Profile {

    private String username;
    private String password;
    private String uid;
    private String secQuestion;
    private String secAnswer;
    private long lastLogin;

    public Profile(String username, String password, String uid, String secQuestion, String secAnswer) {
        this.username = username;
        this.password = password;
        this.uid = uid;
        this.secQuestion = secQuestion;
        this.secAnswer = secAnswer;
        lastLogin = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUid() {
        return uid;
    }

    public String getSecQuestion() {
        return secQuestion;
    }

    public String getSecAnswer() {
        return secAnswer;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void updateLastLoginTime() {
        this.lastLogin = System.currentTimeMillis();
    }
}
