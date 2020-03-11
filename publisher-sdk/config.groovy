pubSdkSharedPreferences = 'com.criteo.publisher.sdkSharedPreferences'
debugLogging = false

environments {
    debug {
        cdbUrl = 'http://directbidder-test-app.par.preprod.crto.in'
        remoteConfigUrl = 'https://pub-sdk-cfg.par.preprod.crto.in'
        eventUrl = 'https://gum.par.preprod.crto.in'

        debugLogging = true
    }

    staging {
        cdbUrl = 'https://bidder.criteo.com'
        remoteConfigUrl = 'https://pub-sdk-cfg.criteo.com'
        eventUrl = 'https://gum.criteo.com'

        debugLogging = true
    }

    release {
        cdbUrl = 'https://bidder.criteo.com'
        remoteConfigUrl = 'https://pub-sdk-cfg.criteo.com'
        eventUrl = 'https://gum.criteo.com'
    }
}