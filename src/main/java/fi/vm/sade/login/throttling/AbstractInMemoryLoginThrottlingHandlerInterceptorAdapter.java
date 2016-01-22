package fi.vm.sade.login.throttling;

import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInMemoryLoginThrottlingHandlerInterceptorAdapter extends AbstractLoginThrottlingHandlerInterceptorAdapter implements InitializingBean {

    private SynchronizedFailedLogins failedLogins = new SynchronizedFailedLogins();

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInMemoryLoginThrottlingHandlerInterceptorAdapter.class);

    private final int DEFAULT_INITIAL_LOGIN_DELAY_IN_MINUTES = 5;
    private final int DEFAULT_TIME_LIMIT_FOR_LOGIN_FAILURES_IN_MINUTES = 24 * 60;
    private final int DEFAULT_MAX_LIMIT_FOR_LOGIN_FAILURES = 10;
    private final int DEFAULT_MIN_LIMIT_FOR_LOGIN_FAILURES = 5;

    @Min(1)
    private int initialLoginDelayInMinutes = DEFAULT_INITIAL_LOGIN_DELAY_IN_MINUTES;
    @Min(1)
    private int timeLimitForLoginFailuresInMinutes = DEFAULT_TIME_LIMIT_FOR_LOGIN_FAILURES_IN_MINUTES;
    @Min(1)
    private int maxLimitForLoginFailures = DEFAULT_MAX_LIMIT_FOR_LOGIN_FAILURES;
    @Min(1)
    private int minLimitForLoginFailures = DEFAULT_MIN_LIMIT_FOR_LOGIN_FAILURES;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public int getMinutesToAllowLogin(HttpServletRequest request) {
        String key = createKey(request);

        int numberOfFailedLogins = failedLogins.size(key);

        if(getMinLimitForLoginFailures() > numberOfFailedLogins) {
            return 0;
        }

        if( getMaxLimitForLoginFailures() <= numberOfFailedLogins) {
            return -1;
        }

        int currentLoginDelay = calculateCurrentLoginDelay(key);

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
        failedLogins.add(key, System.currentTimeMillis());
        LOGGER.error("User {} has {} failed login attempts", key, failedLogins.size(key));
    }

    public void clean() {
        failedLogins.clean();
    }

    private int calculateCurrentLoginDelay(String key) {
        Long[] failedLoginTimes = failedLogins.get(key);

        long loginDelay = calculateLoginDelay(failedLoginTimes.length);
        long lastLoginTime = failedLoginTimes[failedLoginTimes.length];

        LOGGER.error("Delay is {} m from latest failed login {}", loginDelay, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastLoginTime)));

        long nextAllowedLoginTime = lastLoginTime + (loginDelay * 60 * 1000);

        int currentDelayInMinutes = (int)((( nextAllowedLoginTime - System.currentTimeMillis() ) / 1000) / 60);

        return currentDelayInMinutes;
    }

    private long calculateLoginDelay(int numberOfFailedLogins) {

        if(getMinLimitForLoginFailures() > numberOfFailedLogins) {
            return 0;
        }

        long delay = getInitialLoginDelayInMinutes();
        for(int i = getMinLimitForLoginFailures() - 1; i < numberOfFailedLogins; i++) {
            delay = delay * 2;
        }

        return delay;
    }

    public abstract String createKey(HttpServletRequest request);

    public int getInitialLoginDelayInMinutes() {
        return initialLoginDelayInMinutes;
    }

    public void setInitialLoginDelayInMinutes(int initialLoginDelayInMinutes) {
        this.initialLoginDelayInMinutes = initialLoginDelayInMinutes;
    }

    public int getTimeLimitForLoginFailuresInMinutes() {
        return timeLimitForLoginFailuresInMinutes;
    }

    public void setTimeLimitForLoginFailuresInMinutes(int timeLimitForLoginFailuresInMinutes) {
        this.timeLimitForLoginFailuresInMinutes = timeLimitForLoginFailuresInMinutes;
    }

    public int getMaxLimitForLoginFailures() {
        return maxLimitForLoginFailures;
    }

    public void setMaxLimitForLoginFailures(int maxLimitForLoginFailures) {
        this.maxLimitForLoginFailures = maxLimitForLoginFailures;
    }

    public int getMinLimitForLoginFailures() {
        return minLimitForLoginFailures;
    }

    public void setMinLimitForLoginFailures(int minLimitForLoginFailures) {
        this.minLimitForLoginFailures = minLimitForLoginFailures;
    }
}
