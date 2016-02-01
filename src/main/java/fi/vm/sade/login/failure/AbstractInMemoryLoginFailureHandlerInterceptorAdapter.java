package fi.vm.sade.login.failure;

import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class AbstractInMemoryLoginFailureHandlerInterceptorAdapter extends AbstractLoginFailureHandlerInterceptorAdapter implements InitializingBean {

    private SynchronizedFailedLogins failedLogins = new SynchronizedFailedLogins();

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInMemoryLoginFailureHandlerInterceptorAdapter.class);

    private final int DEFAULT_INITIAL_LOGIN_DELAY_IN_MINUTES = 5;
    private final int DEFAULT_CLEAN_LOGIN_FAILURES_OLDER_THAN_IN_MINUTES = 24 * 60;
    private final int DEFAULT_DENY_LOGIN_AFTER_FAILED_LOGINS_COUNT = 10;
    private final int DEFAULT_DELAY_LOGIN_AFTER_FAILED_LOGINS_COUNT = 5;

    @Min(1)
    private int initialLoginDelayInMinutes = DEFAULT_INITIAL_LOGIN_DELAY_IN_MINUTES;
    @Min(1)
    private int cleanLoginFailuresOlderThanInMinutes = DEFAULT_CLEAN_LOGIN_FAILURES_OLDER_THAN_IN_MINUTES;
    @Min(1)
    private int denyLoginAfterFailedLoginsCount = DEFAULT_DENY_LOGIN_AFTER_FAILED_LOGINS_COUNT;
    @Min(1)
    private int delayLoginAfterFailedLoginsCount = DEFAULT_DELAY_LOGIN_AFTER_FAILED_LOGINS_COUNT;

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("Setting initial login delay in minutes to {}", getInitialLoginDelayInMinutes());
        LOGGER.info("Setting clean login failures older than in minutes to {}", getCleanLoginFailuresOlderThanInMinutes());
        LOGGER.info("Setting deny login after failed logins count to {}", getDenyLoginAfterFailedLoginsCount());
        LOGGER.info("Setting delay login after failed logins count to {}", getDelayLoginAfterFailedLoginsCount());
    }

    @Override
    public int getMinutesToAllowLogin(HttpServletRequest request) {
        String key = createKey(request);

        int numberOfFailedLogins = failedLogins.size(key);

        if(getDelayLoginAfterFailedLoginsCount() > numberOfFailedLogins) {
            return 0;
        }

        if( getDenyLoginAfterFailedLoginsCount() <= numberOfFailedLogins) {
            LOGGER.warn("Maximum limit {} of failed login attempts reached for user {}", getDenyLoginAfterFailedLoginsCount(), key);
            return -1;
        }

        int currentLoginDelay = calculateCurrentLoginDelay(key);

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
        LOGGER.info("Cleaning failed logins older than {} minutes.", getCleanLoginFailuresOlderThanInMinutes());
        logRemovedLogins(failedLogins.clean(getCleanLoginFailuresOlderThanInMinutes()));
    }

    private void logRemovedLogins(Map<String, Integer> removed) {
        if(0 == removed.size()) {
            LOGGER.info("No failed logins to clean.");
        } else {
            List<String> messages = new ArrayList<String>();
            for(String key : removed.keySet()) {
                messages.add(key + "=" + removed.get(key));
            }
            LOGGER.info("Cleaned failed logins {}", StringUtils.collectionToCommaDelimitedString(messages));
        }
    }

    private int calculateCurrentLoginDelay(String key) {
        Long[] failedLoginTimes = failedLogins.get(key);

        long loginDelay = calculateLoginDelay(failedLoginTimes.length);
        long lastLoginTime = failedLoginTimes[failedLoginTimes.length-1];

        long nextAllowedLoginTimeMillis = lastLoginTime + TimeUnit.MINUTES.toMillis(loginDelay);
        long delayToNextLoginMillis = nextAllowedLoginTimeMillis - System.currentTimeMillis();

        int currentLoginDelay = 0 >= delayToNextLoginMillis ? 0 : (int)TimeUnit.MILLISECONDS.toMinutes(delayToNextLoginMillis);

        if(0 < currentLoginDelay) {
            LOGGER.warn("User {} has {} failed login attempts. Current login delay is {} minutes.",
              new Object[] {key, failedLoginTimes.length, currentLoginDelay});
        }

        return currentLoginDelay;
    }

    private long calculateLoginDelay(int numberOfFailedLogins) {

        if(getDelayLoginAfterFailedLoginsCount() > numberOfFailedLogins) {
            return 0;
        }

        long delay = getInitialLoginDelayInMinutes();
        for(int i = getDelayLoginAfterFailedLoginsCount(); i < numberOfFailedLogins; i++) {
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

    public int getCleanLoginFailuresOlderThanInMinutes() {
        return cleanLoginFailuresOlderThanInMinutes;
    }

    public void setCleanLoginFailuresOlderThanInMinutes(int cleanLoginFailuresOlderThanInMinutes) {
        this.cleanLoginFailuresOlderThanInMinutes = cleanLoginFailuresOlderThanInMinutes;
    }

    public int getDenyLoginAfterFailedLoginsCount() {
        return denyLoginAfterFailedLoginsCount;
    }

    public void setDenyLoginAfterFailedLoginsCount(int denyLoginAfterFailedLoginsCount) {
        this.denyLoginAfterFailedLoginsCount = denyLoginAfterFailedLoginsCount;
    }

    public int getDelayLoginAfterFailedLoginsCount() {
        return delayLoginAfterFailedLoginsCount;
    }

    public void setDelayLoginAfterFailedLoginsCount(int delayLoginAfterFailedLoginsCount) {
        this.delayLoginAfterFailedLoginsCount = delayLoginAfterFailedLoginsCount;
    }
}