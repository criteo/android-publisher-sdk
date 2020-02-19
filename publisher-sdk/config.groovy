environments {
    debug {
        cdbUrl = 'http://directbidder-test-app.par.preprod.crto.in'
        remoteConfigUrl = 'https://pub-sdk-cfg.par.preprod.crto.in'
        eventUrl = 'https://gum.par.preprod.crto.in'
    }

    staging {
        cdbUrl = 'https://bidder.criteo.com'
        remoteConfigUrl = 'https://pub-sdk-cfg.criteo.com'
        eventUrl = 'https://gum.criteo.com'
    }

    release {
        cdbUrl = 'https://bidder.criteo.com'
        remoteConfigUrl = 'https://pub-sdk-cfg.criteo.com'
        eventUrl = 'https://gum.criteo.com'
    }
}