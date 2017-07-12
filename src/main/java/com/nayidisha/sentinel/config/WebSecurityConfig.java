package com.nayidisha.sentinel.config;

import com.nayidisha.sentinel.support.SentinelUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.sql.DataSource;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements ApplicationContextAware {

    @Autowired
    private Environment env;

    @Value("${query.usersByUsername:null}")
    private String usersByUsernameQuery;

    @Value("${query.authoritiesByUsername:null}")
    private String authoritiesByUsername;

    @Value("${userDetailServiceImplementationProvided: false}")
    private boolean userDetailServiceImplementationProvided;

    @Autowired
    private SentinelUserDetailService sentinelUserDetailsService;

    @Autowired
    public void globalUserDetails(final AuthenticationManagerBuilder auth) throws Exception {

        if (!userDetailServiceImplementationProvided) {
            auth.jdbcAuthentication().dataSource(customDataSource())
                    .usersByUsernameQuery(usersByUsernameQuery)
                    .authoritiesByUsernameQuery(authoritiesByUsername);
        } else {
            //UserDetailService provided
            auth.userDetailsService(sentinelUserDetailsService);
        }
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(customDataSource());
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/login").permitAll()
		.anyRequest().authenticated()
		.and().formLogin().permitAll()
		.and().csrf().disable();
    }

    private DataSource customDataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("userdb.jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("userdb.jdbc.url"));
        dataSource.setUsername(env.getProperty("userdb.jdbc.user"));
        dataSource.setPassword(env.getProperty("userdb.jdbc.pass"));
        return dataSource;
    }

}
