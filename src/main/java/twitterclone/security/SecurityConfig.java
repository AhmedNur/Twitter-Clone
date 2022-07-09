package twitterclone.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import twitterclone.services.UserDetailsServiceImpl;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    protected void configure(final AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception{
        http
                .cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()).and()
                .csrf().disable()
                .requiresChannel()
                .anyRequest()
                .requiresSecure()
                .and()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/api/me").permitAll()
                .antMatchers("/api/register").anonymous()
                .antMatchers("/api/*").authenticated()
                .antMatchers("/*").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .failureHandler(authFailureHandler())
                .loginProcessingUrl("/api/authenticate")
                .defaultSuccessUrl("/", true)
                .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                .deleteCookies("JSESSIONID")
                .and()
                .rememberMe().key("CasePepperJellyGlass").alwaysRemember(true).tokenValiditySeconds(31536000);
    }

    @Bean
    public AuthenticationFailureHandler authFailureHandler(){
        return new SimpleUrlAuthenticationFailureHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
