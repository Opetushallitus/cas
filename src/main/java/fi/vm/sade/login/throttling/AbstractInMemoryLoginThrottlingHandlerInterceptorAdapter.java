package fi.vm.sade.login.throttling;

import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInMemoryLoginThrottlingHandlerInterceptorAdapter extends AbstractLoginThrottlingHandlerInterceptorAdapter implements InitializingBean {

    private ConcurrentMap<String, List<Long>> failedLogins = new ConcurrentHashMap<String, List<Long>>();

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInMemoryLoginThrottlingHandlerInterceptorAdapter.class);

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

        if( getLimitForLoginFailures() <= failedLoginTimes.size() ) {
            LOGGER.error("Too many {} login attempts for user {}!", failedLoginTimes.size(), key);
            return false;
        }

        long loginDelayEndTime = calculateLoginDelayEndTime(failedLogins.get(key));
        LOGGER.error("Allowing new login attempt after {} ms", loginDelayEndTime - System.currentTimeMillis());
        return loginDelayEndTime <= System.currentTimeMillis();
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
        Object[] keys = failedLogins.keySet().toArray();
        for(int i = 0; i < keys.length; i++) {
            String key = (String)keys[i];
            List<Long> loginAttemptTimes = failedLogins.get(key);
            if(loginAttemptTimes.get(0) + getTimeLimitForLoginFailuresInMillis() <= System.currentTimeMillis()) {
                failedLogins.remove(key);
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

    public void setInitialLoginDelayInMillis(long initialLoginDelayInMillis) {
        this.initialLoginDelayInMillis = initialLoginDelayInMillis;
    }

    public void setTimeLimitForLoginFailuresInMillis(long timeLimitForLoginFailuresInMillis) {
        this.timeLimitForLoginFailuresInMillis = timeLimitForLoginFailuresInMillis;
    }

    public void setLimitForLoginFailures(long limitForLoginFailures) {
        this.limitForLoginFailures = limitForLoginFailures;
    }
}
