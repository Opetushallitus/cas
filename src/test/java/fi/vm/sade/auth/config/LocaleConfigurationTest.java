package fi.vm.sade.auth.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class LocaleConfigurationTest {

    private CasConfigurationProperties casProperties;
    private LocaleConfiguration localeConfiguration;

    @Before
    public void setup() {
        casProperties = new CasConfigurationProperties();
        casProperties.getLocale().setDefaultValue("fi");
        localeConfiguration = new LocaleConfiguration(casProperties);
    }

    @Test
    public void localeResolverDefaultLocale() {
        LocaleResolver localeResolver = localeConfiguration.localeResolver();
        HttpServletRequest request = new MockHttpServletRequest();

        Locale locale = localeResolver.resolveLocale(request);

        assertThat(locale).returns("fi", Locale::getLanguage);
    }

    @Test
    public void localeResolverSetSupportedLocale() {
        LocaleResolver localeResolver = localeConfiguration.localeResolver();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Locale locale = new Locale("sv");

        localeResolver.setLocale(request, response, locale);
        locale = localeResolver.resolveLocale(request);

        assertThat(locale).returns("sv", Locale::getLanguage);
    }

    @Test
    public void localeResolverSetUnsupportedLocale() {
        LocaleResolver localeResolver = localeConfiguration.localeResolver();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Locale locale = new Locale("fr");

        localeResolver.setLocale(request, response, locale);
        locale = localeResolver.resolveLocale(request);

        assertThat(locale).returns("fi", Locale::getLanguage);
    }

    @Test
    public void localeResolverSetNullLocale() {
        LocaleResolver localeResolver = localeConfiguration.localeResolver();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Locale locale = null;

        localeResolver.setLocale(request, response, locale);
        locale = localeResolver.resolveLocale(request);

        assertThat(locale).returns("fi", Locale::getLanguage);
    }

    @Test
    public void localeResolverDefaultLocaleContext() {
        LocaleContextResolver localeResolver = localeConfiguration.localeResolver();
        HttpServletRequest request = new MockHttpServletRequest();

        LocaleContext context = localeResolver.resolveLocaleContext(request);

        assertThat(context).returns("fi", t -> t.getLocale().getLanguage());
    }

    @Test
    public void localeResolverSetSupportedLocaleContext() {
        LocaleContextResolver localeResolver = localeConfiguration.localeResolver();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        LocaleContext context = new SimpleLocaleContext(new Locale("sv"));

        localeResolver.setLocaleContext(request, response, context);
        context = localeResolver.resolveLocaleContext(request);

        assertThat(context).returns("sv", t -> t.getLocale().getLanguage());
    }

    @Test
    public void localeResolverSetUnsupportedLocaleContext() {
        LocaleContextResolver localeResolver = localeConfiguration.localeResolver();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        LocaleContext context = new SimpleLocaleContext(new Locale("fr"));

        localeResolver.setLocaleContext(request, response, context);
        context = localeResolver.resolveLocaleContext(request);

        assertThat(context).returns("fi", t -> t.getLocale().getLanguage());
    }

    @Test
    public void localeResolverSetNullLocaleContext() {
        LocaleContextResolver localeResolver = localeConfiguration.localeResolver();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        LocaleContext context = null;

        localeResolver.setLocaleContext(request, response, context);
        context = localeResolver.resolveLocaleContext(request);

        assertThat(context).returns("fi", t -> t.getLocale().getLanguage());
    }

}
