# dkjs-survey-process-engine - handling surveys in time

## Deployment

On Ubuntu server:

```shell
apt install -y openjdk-17-jre-headless
```

TODO proper systemd restart solution

## Configuration



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


## References

Some context and learning materials useful in development of this application:

 * https://zetcode.com/springboot/undertow/
 * https://getbootstrap.com/docs/5.1/forms/validation/
 * https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html
 * https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html#creating-a-form
 * https://attacomsian.com/blog/spring-boot-thymeleaf-file-upload
 * https://www.baeldung.com/javax-validation
 * https://stackoverflow.com/questions/41753361/java-bean-validation-and-regex-format-and-length-in-two-different-error-messag
 * https://www.dariawan.com/tutorials/java/java-datetimeformatter-tutorial-examples/
