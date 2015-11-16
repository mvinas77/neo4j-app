grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test   : [maxMemory: 4096, minMemory: 2048, debug: false, maxPerm: 512, daemon: true],
    // configure settings for the run-app JVM
    run    : [maxMemory: 4096, minMemory: 2048, debug: false, maxPerm: 512, forkReserve: false],
    // configure settings for the run-war JVM
    war    : [maxMemory: 4096, minMemory: 2048, debug: false, maxPerm: 512, forkReserve: false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 4096, minMemory: 2048, debug: false, maxPerm: 512]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsCentral()
        mavenCentral()

        mavenRepo "http://repo.grails.org/grails/core"
    }

    def neo4jVersion = "2.3.0"
    def solrConfig = "3.6.1"
    dependencies {
        // Neo4j-shell
        // Neo4j
        compile "org.neo4j:neo4j:$neo4jVersion",
                "org.neo4j:neo4j-graphviz:$neo4jVersion",
                "org.neo4j.app:neo4j-server:$neo4jVersion", {

                    excludes "neo4j-udc", "logback-classic", "logback-access",
                             "javax.servlet-api", "org.eclipse.jetty.orbit"
                }

        runtime "org.neo4j:neo4j-shell:$neo4jVersion"

        runtime(group: "org.neo4j", name: "neo4j-kernel", version: neo4jVersion, classifier: "tests")

        runtime(group: "org.neo4j.app", name: "neo4j-server", version: neo4jVersion, classifier: "static-web") {
            excludes "logback-classic", "logback-access", "javax.servlet-api", "org.eclipse.jetty.orbit"
        }

        compile "org.apache.solr:solr-solrj:jar:$solrConfig",
                "org.apache.solr:solr-core:jar:$solrConfig"
    }

    plugins {
        // plugins for the build system only
        build ":tomcat:7.0.55.3" // or ":tomcat:8.0.22"

        // plugins for the compile step
        compile ":scaffolding:2.1.2"
        compile ':cache:1.1.8'
        compile ":asset-pipeline:2.5.7"

        compile ":neo4j:5.0.0.BUILD-SNAPSHOT"

        // plugins needed at runtime but not for compilation
        runtime ":hibernate4:4.3.10" // or ":hibernate:3.6.10.18"
    }
}
