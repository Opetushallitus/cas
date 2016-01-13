package fi.vm.sade.login.throttling;

import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractInMemoryLoginThrottlingHandlerInterceptorAdapter extends AbstractLoginThrottlingHandlerInterceptorAdapter implements InitializingBean {

    private ConcurrentMap<String, List<Long>> failedLogins = new ConcurrentHashMap<String, List<Long>>();

    private final long DEFAULT_INITIAL_LOGIN_DELAY_IN_MILLIS = 2000;
    private final long DEFAULT_TIME_LIMIT_FOR_LOGIN_FAILURES_IN_MILLIS = 24 * 60 * 60 * 1000;
    private final long DEFAULT_LIMIT_FOR_LOGIN_FAILURES = 10;

    @Min(1)
    private long initialLoginDelayInMillis = DEFAULT_INITIAL_LOGIN_DELAY_IN_MILLIS;
    @Min(1)
    private long timeLimitForLoginFailuresInMillis = DEFAULT_TIME_LIMIT_FOR_LOGIN_FAILURES_IN_MILLIS;
    @Min(1)
    private long limitForLoginFailures = DEFAULT_LIMIT_FOR_LOGIN_FAILURES;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public boolean allowLoginAttempt(HttpServletRequest request) {
        String key = createKey(request);

        if(!failedLogins.containsKey(key)) {
            return true;
        }

        List<Long> failedLoginTimes = failedLogins.get(key);

        if( getLimitForLoginFailures() >= failedLoginTimes.size() ) {
            return false;
        }

        long loginDelayEndTime = calculateLoginDelayEndTime(failedLogins.get(key));
        return loginDelayEndTime < System.currentTimeMillis();
    }

    @Override
    public void notifySuccessfullLogin(HttpServletRequest request) {
        failedLogins.remove(createKey(request));
    }

    @Override
    public void notifyFailedLoginAttempt(HttpServletRequest request) {
        String key = createKey(request);

        if( !failedLogins.containsKey(key) ) {
            failedLogins.put(key, Arrays.asList(System.currentTimeMillis()));
        } else {
            failedLogins.get(key).add(System.currentTimeMillis());
        }
    }

    public void clean() {
        String[] keys = (String[])failedLogins.keySet().toArray();
        for(int i = 0; i < keys.length; i++) {
            List<Long> loginAttemptTimes = failedLogins.get(keys[i]);
            if(loginAttemptTimes.get(0) + getTimeLimitForLoginFailuresInMillis() <= System.currentTimeMillis()) {
                failedLogins.remove(keys[i]);
            }
        }
    }

    private long calculateLoginDelayEndTime(List<Long> failedLoginTimes) {
        long delay = getInitialLoginDelayInMillis();
        for(int i = 1; i < failedLoginTimes.size(); i++) {
            delay *= 2;
        }
        return failedLoginTimes.get(failedLoginTimes.size() - 1) + delay;
    }

    public abstract String createKey(HttpServletRequest request);

    public long getInitialLoginDelayInMillis() {
        return initialLoginDelayInMillis;
    }

    public long getTimeLimitForLoginFailuresInMillis() {
        return timeLimitForLoginFailuresInMillis;
    }

    public long getLimitForLoginFailures() {
        return limitForLoginFailures;
    }
}
