package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.List;

public class ThreadListBean {

    public int count;
    public boolean parsed = false;
    private String uid;
    public List<ThreadBean> threads = new ArrayList<ThreadBean>();

    public ThreadListBean() {
        count = 0;
    }

    public void add(ThreadBean thread) {
        threads.add(thread);
        count++;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
