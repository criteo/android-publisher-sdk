# Code Style
This project uses the Criteo Java code style. Please follow the instructions contained on this [page](https://confluence.criteois.com/pages/viewpage.action?pageId=320439753)

# Testing
## Tests organisation

The tests in this project are organised according to the following convention:
- Unit tests are located within the [test](src/test/) directory.
- Integration tests, which are written as instrumentation tests, are located within the [androidTest directory](src/androidTest)
- The subset of integration tests which represent one of the functional tests defined [here](https://confluence.criteois.com/display/EE/Functional+Tests)
 are post-fixed with `FunctionTests`. The rest are post-fixed with `IntegrationTests`.
