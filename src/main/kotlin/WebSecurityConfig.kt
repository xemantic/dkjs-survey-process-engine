package de.dkjs.survey

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.factory.PasswordEncoderFactories


@Configuration
@EnableWebSecurity
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {

        val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

        auth
            .inMemoryAuthentication()
            .withUser("dkjs")
            .password("{bcrypt}\$2a\$12\$5QZi66oy2yNpjhRsGwvO5uldyP/z90nYJbQrxj2d4sNpoJVa93XOq")
            .roles("USER")
    }
}
