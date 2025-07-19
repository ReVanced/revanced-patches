plugins {
    java
    antlr
}

dependencies {
    antlr(libs.antlr4)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    generateGrammarSource {
        arguments = listOf("-visitor")
    }
}
