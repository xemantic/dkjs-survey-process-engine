package de.dkjs.survey.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import javax.inject.Inject


@ConstructorBinding
@ConfigurationProperties("credentials")
data class CredentialsFromProperties (
    val username: String,
    val password: String
)

@Configuration
@EnableWebSecurity
class WebSecurityConfig @Inject constructor(
    private val credentials: CredentialsFromProperties,
) : WebSecurityConfigurerAdapter() {

    @Bean
    override fun userDetailsService(): UserDetailsService? {
        val user: UserDetails = User.builder()
            .username(credentials.username)
            .password(credentials.password)
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .permitAll()
                .and()
            .logout()
                .permitAll()
    }

}
