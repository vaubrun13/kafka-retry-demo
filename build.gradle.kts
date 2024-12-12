plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.bakdata.avro") version "1.0.0"

}

group = "com.acme"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {


    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.confluent:kafka-avro-serializer:7.7.1") {
        exclude(group = "org.apache.kafka", module = "kafka-clients")
    }



    testImplementation("org.springframework.boot:spring-boot-starter-test")

    platform("org.testcontainers:testcontainers-bom:1.20.3")
    testImplementation ("org.springframework.kafka:spring-kafka-test")
    testImplementation ("org.springframework.boot:spring-boot-testcontainers")
    testImplementation ("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


}

spotless {
    java {
        googleJavaFormat()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
