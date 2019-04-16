package fi.vm.sade.login.failure;

import java.util.Map;

public interface LoginFailureStore {

    int size(String key);

    boolean remove(String key);

    Long[] get(String key);

    void add(String key, Long loginTime);

    Map<String, Long> clean(int timeLimitInMinutes);

}
