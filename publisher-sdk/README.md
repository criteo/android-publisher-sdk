# Code Style
This project uses the Criteo Java code style. Please follow the instructions contained on this [page](https://confluence.criteois.com/pages/viewpage.action?pageId=320439753)

# Testing
## Tests organisation

The tests in this project are organised according to the following convention:
- Unit tests are located within the [test](src/test/) directory.
- Integration tests, which are written as instrumentation tests, are located within the [androidTest directory](src/androidTest)
- The subset of integration tests which represent one of the functional tests defined [here](https://confluence.criteois.com/display/EE/Functional+Tests)
 are post-fixed with `FunctionTests`. The rest are post-fixed with `IntegrationTests`.

# Release
## Internal releases on Nexus
Each new version is published on the internal Nexus repository on post-submit. No manual action is needed
at this point.

### Use internal releases on Nexus

To use an internal release of the SDK as a dependency, you need to install the Nexus as a Maven
repository, and then declare the dependency.

In the root `build.gradle`:
```Groovy
allprojects {
    repositories {
        maven { url "http://nexus.criteo.prod/content/groups/android/" }
    }
}
```

In your module `build.gradle`:
```Groovy
dependencies {
    // Adapt the version accordingly to your needs
    implementation "com.criteo.publisher:criteo-publisher-sdk:3.4.0-20200317.1720"
}
```

## Customer facing releases on Azure
Publishers consume PubSDK artifacts from the Azure maven repository. To publish a new version of the SDK, run the following command:
```shell script
./scripts/do-release.sh $version
```

To release version 3.5.0 the following command would be run:
```shell script
./scripts/do-release.sh 3.5.0
```

### Use public releases on Azure

To use a public release of the SDK as a dependency, you need to install the Azure as a Maven
repository, and then declare the dependency.

In the root `build.gradle`:
```Groovy
allprojects {
    repositories {
        maven { url "https://pubsdk-bin.criteo.com/publishersdk/android" }
    }
}
```

In your module `build.gradle`:
```Groovy
dependencies {
    // Adapt the version accordingly to your needs
    implementation "com.criteo.publisher:criteo-publisher-sdk:3.4.0"
}
```