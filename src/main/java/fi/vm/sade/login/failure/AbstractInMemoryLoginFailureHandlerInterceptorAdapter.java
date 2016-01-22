package fi.vm.sade.login.failure;

import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInMemoryLoginFailureHandlerInterceptorAdapter extends AbstractLoginFailureHandlerInterceptorAdapter implements InitializingBean {

    private SynchronizedFailedLogins failedLogins = new SynchronizedFailedLogins();

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInMemoryLoginFailureHandlerInterceptorAdapter.class);

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
        LOGGER.info("Setting initial login delay in minutes to {}", getInitialLoginDelayInMinutes());
        LOGGER.info("Setting time limit for login failures in minutes to {}", getTimeLimitForLoginFailuresInMinutes());
        LOGGER.info("Setting max limit for login failures in minutes to {}", getMaxLimitForLoginFailures());
        LOGGER.info("Setting min limit for login failures in minutes to {}", getMinLimitForLoginFailures());
    }

    @Override
    public int getMinutesToAllowLogin(HttpServletRequest request) {
        String key = createKey(request);

        int numberOfFailedLogins = failedLogins.size(key);

        if(getMinLimitForLoginFailures() > numberOfFailedLogins) {
            return 0;
        }

        if( getMaxLimitForLoginFailures() <= numberOfFailedLogins) {
            LOGGER.warn("Maximum limit {} of failed login attempts reached for user {}", getMaxLimitForLoginFailures(), key);
            return -1;
        }

        int currentLoginDelay = calculateCurrentLoginDelay(key);

        LOGGER.info("Current login delay for user {} is {} minutes.", key, currentLoginDelay);

        return 0 < currentLoginDelay ? currentLoginDelay : 0;
    }

    @Override
    public void notifySuccessfullLogin(HttpServletRequest request) {
        LOGGER.debug("Succesfull login for {}. Cleaning failed logins.", createKey(request));
        failedLogins.remove(createKey(request));
    }

    @Override
    public void notifyFailedLoginAttempt(HttpServletRequest request) {

        String key = createKey(request);
        failedLogins.add(key, System.currentTimeMillis());

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("User {} has {} failed login attempts.", key, failedLogins.size(key));
        }
    }

    public void clean() {
        LOGGER.info("Resetting failed logins older than {} minutes.", getTimeLimitForLoginFailuresInMinutes());
        failedLogins.clean(getTimeLimitForLoginFailuresInMinutes());
    }

    private int calculateCurrentLoginDelay(String key) {
        Long[] failedLoginTimes = failedLogins.get(key);

        long loginDelay = calculateLoginDelay(failedLoginTimes.length);
        long lastLoginTime = failedLoginTimes[failedLoginTimes.length-1];

        long nextAllowedLoginTimeMillis = lastLoginTime + TimeUnit.MINUTES.toMillis(loginDelay);
        long delayToNextLoginMillis = nextAllowedLoginTimeMillis - System.currentTimeMillis();

        return 0 >= delayToNextLoginMillis ? 0 : (int)TimeUnit.MILLISECONDS.toMinutes(delayToNextLoginMillis);
    }

    private long calculateLoginDelay(int numberOfFailedLogins) {

        if(getMinLimitForLoginFailures() > numberOfFailedLogins) {
            return 0;
        }

        long delay = getInitialLoginDelayInMinutes();
        for(int i = getMinLimitForLoginFailures(); i < numberOfFailedLogins; i++) {
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
