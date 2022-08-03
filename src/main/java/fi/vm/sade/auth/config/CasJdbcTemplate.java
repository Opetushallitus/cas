package fi.vm.sade.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class CasJdbcTemplate {


    @Bean
    @Qualifier("cas")
    JdbcTemplate  jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
