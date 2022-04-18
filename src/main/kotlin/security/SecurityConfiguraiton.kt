/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@Profile("security")
@Validated
@ConfigurationProperties("credentials")
@ConstructorBinding
data class CredentialsConfig (

  @get:NotEmpty
  val username: String,

  @get:NotEmpty
  val password: String

)

@Profile("security")
@Configuration
class SecurityConfiguration {

  @Bean
  fun userDetailsService(config: CredentialsConfig): UserDetailsService =
    InMemoryUserDetailsManager(
      User.builder()
        .username(config.username)
        .password(config.password)
        .roles("USER")
        .build()
    )

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .authorizeRequests()
      .anyRequest().authenticated()
      .and()
      .formLogin()
      .permitAll()
      .and()
      .logout()
      .permitAll()
    return http.build()
  }

}
