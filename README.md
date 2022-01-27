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
