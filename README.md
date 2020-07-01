# Code Style
This project uses the Criteo Java code style. Please follow the instructions contained on this [page](https://go.crto.in/publisher-sdk-java-code-style)

# Testing
## Tests organisation

The tests in this project are organised according to the following convention:
- Unit tests are located within the [test](src/test/) directory.
- Integration tests, which are written as instrumentation tests, are located within the [androidTest directory](src/androidTest)
- The subset of integration tests which represent one of the functional tests defined [here](https://go.crto.in/publisher-sdk-functional-tests)
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

1. Bump the version in `buildSrc/src/main/java/SdkVersion.kt`
2. Bump the version of _android-google-mediation_ (see its README)
3. Bump the version of _android-mopub-mediation_ (see its README)
4. Select a version declared in #pub-sdk-release-candidates as a RC.
5. Promote it (follows instructions)
6. Execute a bugfest on the RC (with mediation adapters), if there is a blocker, fix it and restart from 4.
7. Release the new SDK version on Azure (see Slack message or section below)
8. Release the new _android-google-mediation_ version on Azure (see its README)
9. Release the new _android-mopub-mediation_ version on Azure (see its README)

## Development releases
Each new version is published on the Bintray repository on post-submit. No manual action is needed
at this point.

### Development releases

To use a development release of the SDK as a dependency, you need to declare the dependency.

In your module `build.gradle`:
```Groovy
dependencies {
    // Adapt the version accordingly to your needs
    implementation "com.criteo.publisher:criteo-publisher-sdk-development:3.4.0-20200317.1720"
}
```

## Customer facing releases on Azure

Publishers consume PubSDK artifacts from the Azure maven repository.
To select a new version of the SDK:
- Select a commit representing the new version (the one validated by bugfest)
- Go on [Jenkins deploy job](https://go.crto.in/publisher-sdk-prod-deployment)
- Insert the commit SHA1 and validate

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

# License

       Copyright 2020 Criteo

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.