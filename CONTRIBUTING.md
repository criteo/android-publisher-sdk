# Contributing to Criteo Publisher SDK

First of all, thanks for contributing! We love your input! We want to make contributing to this
project as easy and transparent as possible, whether it's:

* Reporting a bug
* Discussing the current state of the code
* Submitting a fix
* Proposing new features

## Pull Requests

Have you fixed a bug or written a new check and want to share it? Many thanks!

We use [Github Flow](https://guides.github.com/introduction/flow/index.html), so all changes happen
through pull requests.

* Fork the repo and create your branch from master.
* If you've added code, add tests.
* If you've changed APIs, update the documentation.
* If you've changed public facing behavior, update the changelog.
* Ensure the test suite passes.
* Make sure your code lints.
* Have a proper commit history (we advise you to rebase and amend rather than fixup).
* Issue that pull request against the `main` branch!

### Commit messages

Please, take a moment to write meaningful commit messages. Here is a well-known citation on how
to write great commit message:

> Well-crafted Git commit message is the best way to communicate context about a change to fellow
> developers (and indeed to their future selves). A diff will tell you what changed, but only the
> commit message can properly tell you why.
>
>---
>
>> Keep in mind:
>[This](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html)
>[has](https://www.git-scm.com/book/en/v2/Distributed-Git-Contributing-to-a-Project#_commit_guidelines)
>[all](https://github.com/torvalds/subsurface-for-dirk/blob/master/README.md#contributing)
>[been](http://who-t.blogspot.com/2009/12/on-commit-messages.html)
>[said](https://github.com/erlang/otp/wiki/writing-good-commit-messages)
>[before](https://github.com/spring-projects/spring-framework/blob/30bce7/CONTRIBUTING.md#format-commit-messages).
>
> * Separate subject from body with a blank line
> * Limit the subject line to 50 characters
> * Capitalize the subject line
> * Do not end the subject line with a period
> * Use the imperative mood in the subject line
> * Wrap the body at 72 characters
> * Use the body to explain what and why vs. how

Chris Beams - [How to Write a Git Commit Message](https://chris.beams.io/posts/git-commit/)

## Development

### Setup

* Download [Android Studio](https://developer.android.com/studio) or
[IntelliJ](https://www.jetbrains.com/fr-fr/idea/download/) with Android tools
* Use Java 8 or below to run Gradle commands
* Building project: `./gradlew build`
* Running Java tests: `./gradlew check`
* Running Android tests: `./gradlew :publisher-sdk-tests:connectedCheck`

### List of modules

* `app`: test application to try the SDK
* `publisher-sdk`: the Android SDK and its Java tests
* `publisher-sdk-tests`: the Android tests of the SDK
* `test-utils`: helping module to assist both Java and Android tests

### Coding style

New codes are expected to be written in Kotlin rather than in Java.

If using IntelliJ or Android Studio, please use the coding style stored in this repository:
`.idea/codeStyles`.

For Java code, the coding style roughly follows the
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

For Kotlin code, the coding style follows the
[Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)

## License

By contributing, you agree that your contributions will be licensed under its
[MIT License](LICENSE).
