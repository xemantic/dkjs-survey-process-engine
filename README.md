# dkjs-survey-process-engine - handling surveys in time

See

* https://zetcode.com/springboot/undertow/
* https://getbootstrap.com/docs/5.1/forms/validation/
* https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html
* https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html#creating-a-form
* https://attacomsian.com/blog/spring-boot-thymeleaf-file-upload
* https://www.baeldung.com/javax-validation
* https://stackoverflow.com/questions/41753361/java-bean-validation-and-regex-format-and-length-in-two-different-error-messag
* https://www.dariawan.com/tutorials/java/java-datetimeformatter-tutorial-examples/
* https://medium.com/@jayphelps/backpressure-explained-the-flow-of-data-through-software-2350b3e77ce7
* https://developer.typeform.com/get-started/applications/
* https://ktor.io/docs/auth.html
* https://github.com/ktorio/ktor-documentation/tree/main/codeSnippets/snippets/client-auth-oauth-google
* https://www.wimdeblauwe.com/blog/2021/09/14/thymeleaf-iteration-and-fragments/
 
## Using Kotlin with spring-boot

https://spring.io/guides/tutorials/spring-boot-kotlin/

Most of the changes described there are already applied to this project. If lazy entity fetching
is needed, the following needs to be added to the `build.gradle.kts`

```kotlin
plugins {
  //...
  kotlin("plugin.allopen") version "1.4.32"
}

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.Embeddable")
  annotation("javax.persistence.MappedSuperclass")
}
```

## Deployment

### One-time provisioning
This section is useful if this application needs to be deployed to a new machine.

In order to enable the `djks-survey` application to run on the development server,
the following steps were executed once:
1. User `survey` was created and added to the group `www`(?)
2. systemd service file was created in `/etc/systemd/system/dkjs-survey.service` with the following content:
    ```
    [Unit]
    Description=DKJS survey process engine
    After=network.target
    
    [Service]
    User=survey
    ExecStart=/var/dkjs/dkjs-survey.jar
    
    [Install]
    WantedBy=multi-user.target
   ```
3. The service was enabled and started
   ```
   systemctl enable dkjs-survey.service
   systemctl start dkjs-survey.service
   ```

### On-demand deployment
This section is useful if a newer version of the application needs to be deployed to a machine that was previously provisioned.

1. Build an uberjar containing the application and its dependencies:
    ```
   ./gradlew build
   ```
2. Copy the resulting JAR file to the development server:
    ```
   scp dkjs-survey.jar survey@DEV_SERVER:/home/survey/dkjs-survey.jar
   ```
   where `DEV_SERVER` is the hostname/IP address of the destination server
3. Log in to the destination server as user `survey`
   ```
   ssh survey@DEV_SERVER
   ```
4. Swap the uber file in a location expected by systemd
   ```
   cp --remove-destination /home/survey/dkjs-survey.jar /var/dkjs/dkjs-survey.jar
   ```
5. Finally, restart the service
   ```
   systemctl restart dkjs-survey.service
   ```
