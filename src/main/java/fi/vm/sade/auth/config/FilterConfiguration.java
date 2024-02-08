package fi.vm.sade.auth.config;

import fi.vm.sade.javautils.http.RemoteAddressFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfiguration implements BeanPostProcessor {

    private static final int REMOTE_ADDRESS_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE;

    @Bean
    public FilterRegistrationBean<RemoteAddressFilter> remoteAddressFilter() {
        FilterRegistrationBean<RemoteAddressFilter> bean = new FilterRegistrationBean<>(new RemoteAddressFilter());
        bean.setOrder(REMOTE_ADDRESS_FILTER_ORDER);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if ("casClientInfoLoggingFilter".equals(beanName)) {
            ((FilterRegistrationBean) bean).setOrder(REMOTE_ADDRESS_FILTER_ORDER + 1);
        }
        return bean;
    }

}
