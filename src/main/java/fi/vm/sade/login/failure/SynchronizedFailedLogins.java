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

    public void remove(String key) {
        synchronized (loginMap) {
            loginMap.remove(key);
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

    public void clean(int timeLimitInMinutes) {

        long timeLimitInMillis = TimeUnit.MINUTES.toMillis(timeLimitInMinutes);
        long currentTime = System.currentTimeMillis();

        synchronized (loginMap) {
            Object[] keys = loginMap.keySet().toArray();
            for(int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                long firstLogin = loginMap.get(key).get(0);
                if(firstLogin + timeLimitInMillis <= currentTime) {
                    loginMap.remove(key);
                }
            }
        }
    }
}
