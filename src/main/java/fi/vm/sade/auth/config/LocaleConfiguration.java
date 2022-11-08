package fi.vm.sade.auth.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import static fi.vm.sade.CasOphConstants.SUPPORTED_LANGUAGES;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LocaleConfiguration {

    private final CasConfigurationProperties casProperties;

    public LocaleConfiguration(CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Bean
    public LocaleContextResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolverWithSupportedLanguages(SUPPORTED_LANGUAGES);
        String defaultValue = casProperties.getLocale().getDefaultValue();
        if (defaultValue != null && !defaultValue.isEmpty()) {
            localeResolver.setDefaultLocale(new Locale(defaultValue));
        }
        return localeResolver;
    }

    private static class CookieLocaleResolverWithSupportedLanguages extends CookieLocaleResolver {

        private final Set<String> supportedLanguages;

        public CookieLocaleResolverWithSupportedLanguages(Set<String> supportedLanguages) {
            this.supportedLanguages = supportedLanguages;
        }

        @Override
        public Locale resolveLocale(HttpServletRequest request) {
            Locale locale = super.resolveLocale(request);
            if (supportedLanguages.stream().map(Locale::new).noneMatch(locale::equals)) {
                locale = getDefaultLocale();
            }
            return locale;
        }

        @Override
        public LocaleContext resolveLocaleContext(HttpServletRequest request) {
            final LocaleContext context = super.resolveLocaleContext(request);
            if (supportedLanguages.stream().map(Locale::new).noneMatch(locale -> locale.equals(context.getLocale()))) {
                return new SimpleTimeZoneAwareLocaleContext(getDefaultLocale(),
                        (TimeZone) request.getAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME));
            }
            return context;
        }

    }

}
