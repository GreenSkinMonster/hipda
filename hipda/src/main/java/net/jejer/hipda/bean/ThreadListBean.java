package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class ThreadListBean {

	public int count;
	public List<ThreadBean> threads = new ArrayList<ThreadBean>();
	private boolean mAddStickThreads = false;

	public ThreadListBean(Context ctx) {
		count = 0;
		mAddStickThreads = HiSettingsHelper.getInstance().isShowStickThreads();
	}

	public void add(ThreadBean thread) {
		if (!mAddStickThreads && thread.getIsStick()) {
			return;
		} else {
			threads.add(thread);
			count++;
		}
	}

}
