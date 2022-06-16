/*
 * dkjs-survey-process-engine - https://www.dkjs.de/
 * Copyright (C) 2022 Kazimierz Pogoda / https://xemantic.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.dkjs.survey.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
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
@EnableWebSecurity
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
    // important to allow iframe
    http
      .headers()
      .frameOptions()
      .sameOrigin()
    return http.build()
  }

}
