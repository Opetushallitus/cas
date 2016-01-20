package fi.vm.sade.login.throttling;

import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.SystemEnvironmentPropertySource;

public abstract class AbstractInMemoryLoginThrottlingHandlerInterceptorAdapter extends AbstractLoginThrottlingHandlerInterceptorAdapter implements InitializingBean {

    private Map<String, List<Long>> failedLogins = new HashMap<String, List<Long>>();

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
    public int getSecondsToAllowLogin(HttpServletRequest request) {
        String key = createKey(request);

        if(!failedLogins.containsKey(key)) {
            return 0;
        }

        List<Long> failedLoginTimes = failedLogins.get(key);

        if( getLimitForLoginFailures() <= failedLoginTimes.size() ) {
            return -1;
        }

        int currentLoginDelay = calculateCurrentLoginDelay(failedLoginTimes);

        return 0 < currentLoginDelay ? currentLoginDelay : 0;
    }

    @Override
    public void notifySuccessfullLogin(HttpServletRequest request) {
        LOGGER.error("Succesfull login {}", createKey(request));
        failedLogins.remove(createKey(request));
    }

    @Override
    public void notifyFailedLoginAttempt(HttpServletRequest request) {
        String key = createKey(request);
        LOGGER.error("Notifying failed login attempt for {}", key);
        LOGGER.error("Contains key {}", failedLogins.containsKey(key));

        if( !failedLogins.containsKey(key) ) {
            LOGGER.error("First failed login {}", key);
            List<Long> failedLoginTimes = new ArrayList<Long>();
            failedLoginTimes.add(System.currentTimeMillis());
            failedLogins.put(key, failedLoginTimes);
        } else {
            LOGGER.error("Increasing failed login attempts.");
            List<Long> failedLoginTimes = failedLogins.get(key);
            LOGGER.error("GOT {}", failedLoginTimes);
            failedLoginTimes.add(System.currentTimeMillis());
            LOGGER.error("One more failed login {}", key);
            failedLogins.put(key, failedLoginTimes);
        }
        LOGGER.error("User {} has {} failed login attempts", key, failedLogins.get(key).size());
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

    private int calculateCurrentLoginDelay(List<Long> failedLoginTimes) {

        long loginDelay = calculateLoginDelay(failedLoginTimes);
        long lastLoginTime = failedLoginTimes.get(failedLoginTimes.size() -1 );

        return (int)(((lastLoginTime + loginDelay) - System.currentTimeMillis()) / 1000 );
    }

    private long calculateLoginDelay(List<Long> failedLoginTimes) {
        long delay = getInitialLoginDelayInMillis();
        for(int i = 1; i < failedLoginTimes.size(); i++) {
            delay = delay * 2;
        }
        LOGGER.error("Delay is {} from latest failed login {}", delay, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(failedLoginTimes.size() - 1)));
        return delay;
    }

    /*private long calculateLoginDelayEndTime(List<Long> failedLoginTimes) {
        long delay = getInitialLoginDelayInMillis();
        for(int i = 1; i < failedLoginTimes.size(); i++) {
            delay = delay * 2;
        }
        LOGGER.error("Delay is {} from latest failed login {}", delay, failedLoginTimes.get(failedLoginTimes.size() - 1));
        return failedLoginTimes.get(failedLoginTimes.size() - 1) + delay;
    }*/

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
