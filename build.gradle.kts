plugins {
    id("com.android.library") version "9.1.1"
    id("maven-publish")
}

group = "io.github.kitleong1981"
version = "0.1.0"

android {
    namespace = "io.github.kitleong1981.crawl4android"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("org.jsoup:jsoup:1.23.2")
    testImplementation("junit:junit:4.13.2")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "crawl4android"
            version = project.version.toString()
            afterEvaluate { from(components["release"]) }
            pom {
                name.set("Crawl4Android")
                description.set("Android-first bounded web research for local LLM applications")
                url.set("https://github.com/kitleong1981/crawl4android")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/kitleong1981/crawl4android")
                    connection.set("scm:git:https://github.com/kitleong1981/crawl4android.git")
                }
            }
        }
    }
}
