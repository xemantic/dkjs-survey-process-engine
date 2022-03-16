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
   
   > All the commands in this section should be run by `root`

This section is useful if this application needs to be deployed to a new machine.

In order to enable the `djks-survey` application to run on the development server,
the following steps were executed once:
1. User `survey` was created (the application will be run by this user)
   ```
   useradd -m survey
   passwd survey
   usermod -aG sudo survey
   chsh survey -s /usr/bin/bash
   ```
2. Configure SSH: add authorized keys (of developers), and enable SSH login as user `survey`
   ```
   mkdir /home/survey/.ssh
   cp /root/.ssh/authorized_keys /home/survey/.ssh/authorized_keys
   chown -R survey:survey /home/survey/.ssh
   ```
3. Target directory for the application was created, and ownership was assigned to the user `survey`
   ```
   cd /var
   mkdir dkjs
   chown survey:survey dkjs
   ```
4. systemd service file was created in `/etc/systemd/system/dkjs-survey.service` with the following content:
    ```
    [Unit]
    Description=DKJS survey process engine
    After=network.target
    StartLimitIntervalSec=0

    [Service]
    Type=simple
    Restart=always
    RestartSec=1
    User=survey
    WorkingDirectory=/var/dkjs/
    ExecStart=java -jar /var/dkjs/dkjs-survey.jar

    [Install]
    WantedBy=multi-user.target
    ```
5. The service was enabled and started
   ```
   sudo systemctl enable dkjs-survey.service
   sudo systemctl start dkjs-survey.service
   ```
6. See if the application runs
   ```
   curl localhost:8080
   ```

### On-demand deployment
This section is useful if a newer version of the application needs to be deployed to a machine that was previously provisioned.

1. Build a fat jar containing the application and its dependencies:
    ```
   ./gradlew build
   ```
   So called "fat jar" (a self-sufficient archive which contains both classes and dependencies
needed to run an application) is created automatically by springboot gradle plugin
in the following path: `build/libs/dkjs-survey-process-engine-1.0-SNAPSHOT.jar`.

2. Copy the resulting JAR file to the development server:
    ```
   scp build/libs/dkjs-survey-process-engine-1.0-SNAPSHOT.jar survey@DEV_SERVER:/home/survey/dkjs-survey.jar
   ```
   where `DEV_SERVER` is the hostname/IP address of the destination server

3. Log in to the destination server as user `survey`
   ```
   ssh survey@DEV_SERVER
   ```

4. Swap the fat jar in a location expected by systemd
   ```
   cp --remove-destination /home/survey/dkjs-survey.jar /var/dkjs/dkjs-survey.jar
   ```

5. Finally, restart the service
   ```
   systemctl restart dkjs-survey.service
   ```

## Development

### Set up configuration files
In order to run the application, you need to manually modify the following files:
- `src/main/resources/application.yml` needs `typeform.clientId` key-value pair
- `src/main/resources/application-settings.yml` needs to be created with the contents of `src/main/resources/application-settings-template.yml`

### Connect to the application running on a remote server
Before the dev server is set-up to expose the application in a secure way,
it's still possible to connect to the application that runs there - through SSH port forwarding:
```
ssh -L 8080:localhost:8080 survey@DEV_SERVER
```
This will start a regular SSH session, and also forward port 8080 to your machine.
Now it's possible to open `localhost:8080` in the browser and display the app that runs on `DEV_SERVER`.

