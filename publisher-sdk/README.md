# Code Style
This project uses the Criteo Java code style. Please follow the instructions contained on this [page](https://confluence.criteois.com/pages/viewpage.action?pageId=320439753)

# Testing
## Tests organisation

The tests in this project are organised according to the following convention:
- Unit tests are located within the [test](src/test/) directory.
- Integration tests, which are written as instrumentation tests, are located within the [androidTest directory](src/androidTest)
- The subset of integration tests which represent one of the functional tests defined [here](https://confluence.criteois.com/display/EE/Functional+Tests)
 are post-fixed with `FunctionTests`. The rest are post-fixed with `IntegrationTests`.

## Testing against a local CDB

When working in debug environment, the SDK hits the preprod of CDB. To test integration with CDB,
you can make the SDK hit a local instance of CDB instead. You need to:

- Checkout the CDB project:

```shell
cd ~ && \
mkdir -p publisher/direct-bidder && \
cd publisher/direct-bidder && \
gradle initWorkspace && \
./gradlew checkout --project=publisher/direct-bidder
```

- Follow instructions in `README.md` to start the server (either in debug or not)
- Update the `cdbUrl` in `environments.debug` closure in the `publisher-sdk/config.groovy` file
(just use the commented line if you kept default values)

Please note that from the Android device, your local computer is not `127.0.0.1` but `10.0.2.2`.

# Release
## Release steps
Releasing a new version of the PubSDK involves a few steps:

1. Publishing the new version of the AAR on Azure
2. Publishing new versions of _android-google-mediation_ and _android-mopub-mediation_ that depend
on the newly released version of PubSDK. Please refer to the README of each of those projects for
additional release information.

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


# Misc
## Dependency Graph
The project embeds a plugin that generates the dependency graph as a PNG file. This can be handy for documentation purposes and for visualization.

```
brew install graphviz
./gradlew generateDependencyGraph
```

