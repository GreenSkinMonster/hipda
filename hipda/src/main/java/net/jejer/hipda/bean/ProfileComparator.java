package net.jejer.hipda.bean;

import java.util.Comparator;
import java.util.Map;

public class ProfileComparator implements Comparator<String> {
    Map<String, Profile> base;

    public ProfileComparator(Map<String, Profile> base) {
        this.base = base;
    }

    public int compare(String a, String b) {
        if (base.get(a).getLastLogin() >= base.get(b).getLastLogin()) {
            return -1;
        } else {
            return 1;
        }
    }
}