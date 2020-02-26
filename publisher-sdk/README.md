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

## Customer facing releases on Azure
Publishers consume PubSDK artifacts from the Azure maven repository. To publish a new version of the SDK, run the following command:
```shell script
./scripts/do-release.sh $version
```

To release version 3.5.0 the following command would be run:
```shell script
./scripts/do-release.sh 3.5.0
```

