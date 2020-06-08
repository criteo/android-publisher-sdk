# Inspired by https://github.com/react-native-community/docker-android/blob/master/Dockerfile
FROM openjdk:8

LABEL Description="This image provides a base Android development environment, and may be used to run tests."

# Configure apt-get.
ARG DEBIAN_FRONTEND=noninteractive

# Set default build arguments.
ARG SDK_VERSION=sdk-tools-linux-4333796.zip
ARG ANDROID_BUILD_VERSION=29
ARG ANDROID_TOOLS_VERSION=29.0.3

# Set default user (overriden in the command line with Jenkins' actual user).
ARG UNAME=jenkins
ARG UID=1000
ARG GID=1000

# Set default environment variables.
ENV ANDROID_HOME=/opt/android
ENV ANDROID_SDK_HOME=${ANDROID_HOME}
ENV ANDROID_SDK_ROOT=${ANDROID_HOME}

ENV PATH=${ANDROID_HOME}/emulator:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:${PATH}

# Install system dependencies.
RUN apt-get -qq update > /dev/null && apt-get -qq -y --no-install-recommends install \
        apt-transport-https \
        curl \
        build-essential \
        file \
        git \
        gnupg2 \
        unzip \
        > /dev/null \
    && rm -rf /var/lib/apt/lists/*;

# Install Android SDK and accept licences.
RUN curl -sS "https://dl.google.com/android/repository/${SDK_VERSION}" -o /tmp/sdk.zip \
    && mkdir "${ANDROID_HOME}" \
    && unzip -q -d "${ANDROID_HOME}" /tmp/sdk.zip \
    && rm /tmp/sdk.zip \
    && mkdir -p "${ANDROID_HOME}/.android" \
    && touch "${ANDROID_HOME}/.android/repositories.cfg" \
    && yes | sdkmanager --licenses > dev/null \
    && yes | sdkmanager "platform-tools" \
        "emulator" \
        "platforms;android-$ANDROID_BUILD_VERSION" \
        "build-tools;$ANDROID_TOOLS_VERSION" \
        "add-ons;addon-google_apis-google-23" \
        "system-images;android-19;google_apis;armeabi-v7a" \
        "extras;android;m2repository" > /dev/null \
    && rm -rf "${ANDROID_HOME}/.android"

# Switch to a non-root user.
RUN groupadd -g "$GID" -o "$UNAME"
RUN useradd -m -u "$UID" -g "$GID" -o -s /bin/bash -d "/home/$UNAME" "$UNAME"
USER $UNAME