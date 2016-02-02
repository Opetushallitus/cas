package fi.vm.sade.login.failure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SynchronizedFailedLogins {
    private Map<String, List<Long>> loginMap = new HashMap<String, List<Long>>();

    public int size(String key) {
        synchronized (loginMap) {
            return loginMap.containsKey(key) ? loginMap.get(key).size() : 0;
        }
    }

    public boolean remove(String key) {
        synchronized (loginMap) {
            return null != loginMap.remove(key);
        }
    }

    public Long[] get(String key) {
        synchronized (loginMap) {
            return loginMap.containsKey(key) ? loginMap.get(key).toArray(new Long[0]) : new Long[0];
        }
    }

    public void add(String key, Long loginTime) {
        synchronized (loginMap) {
            if( !loginMap.containsKey(key) ) {
                loginMap.put(key, new ArrayList<Long>());
            }
            loginMap.get(key).add(loginTime);
        }
    }

    public Map<String, Integer> clean(int timeLimitInMinutes) {

        long timeLimitInMillis = TimeUnit.MINUTES.toMillis(timeLimitInMinutes);
        long currentTime = System.currentTimeMillis();

        Map<String, Integer> removed = new HashMap<String, Integer>();

        synchronized (loginMap) {
            Object[] keys = loginMap.keySet().toArray();
            for(int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                long firstLogin = loginMap.get(key).get(0);
                if(firstLogin + timeLimitInMillis <= currentTime) {
                    removed.put(key, loginMap.get(key).size());
                    loginMap.remove(key);
                }
            }
        }

        return removed;
    }
}
