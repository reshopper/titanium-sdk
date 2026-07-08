## [13.6.0](https://github.com/jonasfunk/titanium-sdk/compare/13.5.0...13.6.0) (2026-04-23)

## About this release

Titanium SDK 13.6.0 is a minor release of the SDK, adding Intl.DisplayNames and Intl.RelativeTimeFormat support.

## New Features

### JavaScript

* **Intl**: add Intl.DisplayNames and Intl.RelativeTimeFormat support ([9153c4a](https://github.com/jonasfunk/titanium-sdk/commit/9153c4a33365b4f563f6e79e22cedf0ef718fe05))

### iOS

* **TiViewController**: add `releaseProxy` method and update build process for arm64 compatibility ([e671646](https://github.com/jonasfunk/titanium-sdk/commit/e671646a8a2dd2d2528b20cf69cd4858635091e3))

## Bug Fixes

### iOS platform

* **Proxy cleanup**: prevent potential crashes by niling out `_proxy` in `dealloc` ([77b3c22](https://github.com/jonasfunk/titanium-sdk/commit/77b3c22ebcfdbf05593a044d00fc6ee28073d9e7))

## Community Credits

* jonasfunk
  * add Intl.DisplayNames and Intl.RelativeTimeFormat support ([9153c4a](https://github.com/jonasfunk/titanium-sdk/commit/9153c4a33365b4f563f6e79e22cedf0ef718fe05))
  * prevent potential crashes by niling out `_proxy` in `dealloc` ([77b3c22](https://github.com/jonasfunk/titanium-sdk/commit/77b3c22ebcfdbf05593a044d00fc6ee28073d9e7))
  * add `releaseProxy` method and update build process for arm64 compatibility ([e671646](https://github.com/jonasfunk/titanium-sdk/commit/e671646a8a2dd2d2528b20cf69cd4858635091e3))

## [13.5.0](https://github.com/jonasfunk/titanium-sdk/compare/13.4.0...13.5.0) (2026-04-09)

## About this release

Titanium SDK 13.5.0 updates this fork by merging upstream `13.1.1.GA` changes.
# [13.2.0](https://github.com/tidev/titanium_mobile/compare/13_1_X...13.2.0) (2026-04-08)

## About this release

Titanium SDK 13.2.0 is a minor release of the SDK, focussing on quality-of-life improvements, e.g. iOS core changes, Node.js 24 support and several Android bug fixes!

## Community Credits

* César Estrada
  * fix Ti.UI.Button styling and events on Mac Catalyst ([1897d79](https://github.com/tidev/titanium_mobile/commit/1897d7961436ac0f36b0aacb495b2097a62dd949))
  * support module builds with mac=true on Catalyst ([dd2899b](https://github.com/tidev/titanium_mobile/commit/dd2899b9de0e37acbd5f1ce56fc3110a51b82f2c))

* Michael Gangolf
  * null pointer check in cpp files ([76aff13](https://github.com/tidev/titanium_mobile/commit/76aff13943084d6eef936be0686937498c6d76f6))
  * fix endless loop in setLanguage pre API 33 ([c48cf78](https://github.com/tidev/titanium_mobile/commit/c48cf780bf3ce7835d4c58fc10efaff2e6aa489f))
  * keepHardwareMode property in Ti.UI.View ([3d03db0](https://github.com/tidev/titanium_mobile/commit/3d03db0baa533328deead4c698314a12c84e9fc8))
  * cache ListView searchText ([2e7fa1f](https://github.com/tidev/titanium_mobile/commit/2e7fa1fc9a64fc81714598bd96973b16da41aeef))
  * move ScrollableView to ViewPager2 ([8f2359b](https://github.com/tidev/titanium_mobile/commit/8f2359b9b093c32ed96befc9c6dc967a02ce131b))
  * update internal Android libraries ([2f7ed0c](https://github.com/tidev/titanium_mobile/commit/2f7ed0cc1ea8e5cf11ff2ed383baf2181772f0b0))
  * support for AttributedString objects ([a753fc2](https://github.com/tidev/titanium_mobile/commit/a753fc2d362a44658dfcec2f57f3250b078b0a5e))
  * use ConcurrentHashMap in TiRhelper and TiUIHelper ([5198d50](https://github.com/tidev/titanium_mobile/commit/5198d501da074b10a4cd5acd55b70b666d7d9fa5))
  * pre-compile regex patterns ([2653a62](https://github.com/tidev/titanium_mobile/commit/2653a62e8e1e396b20635e826994276547052384))
  * use i18n app name in AndroidManifest ([ce0dc80](https://github.com/tidev/titanium_mobile/commit/ce0dc8017994d5b35d09231e0204463e73a54839))
  * upgrade core-js to remove baseline-browser-mapping warning ([de4eb55](https://github.com/tidev/titanium_mobile/commit/de4eb55af06c6c86bf47e4844de2758b1845d0fc))
  * doc github worklow ([5dd1d65](https://github.com/tidev/titanium_mobile/commit/5dd1d6511847b2b625a3bebd20239cbf636e02f9))
  * add Alloy app to create menu ([3016623](https://github.com/tidev/titanium_mobile/commit/30166236297d6002dcffa42514ac54a92726b859))

* Hans Knöchel
  * modernize kroll core with GCD ([6c24372](https://github.com/tidev/titanium_mobile/commit/6c24372fb056cc33f2dffc04674e3725ee6e4374))
  * fix image blob memory issues ([cef7d46](https://github.com/tidev/titanium_mobile/commit/cef7d465cf1c3cd6bfaa0ab960e88eabc911db66))
  * add AGENTS.md and CLAUDE.md ([e7d7edf](https://github.com/tidev/titanium_mobile/commit/e7d7edf1742f35eb06d34e595f59d4ec88bf8454))
  * add web view API to hide input accessory views ([b48b6d1](https://github.com/tidev/titanium_mobile/commit/b48b6d14212661f8bd359423f8dd7a6193c223cf))
  * bump master to 13.2.0 ([75ebb28](https://github.com/tidev/titanium_mobile/commit/75ebb2806bfee677fd4dd626f0ba1610f5f61ebc))

* Prashant Saini
  * allow image to drag horizontally inside a ScrollableView ([3929e28](https://github.com/tidev/titanium_mobile/commit/3929e28ead6d436c9f51755633bbe2692ab287f8))

* Chris Barber
  * Switch to oxlint (#14394) ([b8e2b3c](https://github.com/tidev/titanium_mobile/commit/b8e2b3c078148d41b93976ee9f40d745ea19c378))


## Bug Fixes

### Android platform

* allow image to drag horizontally inside a ScrollableView ([3929e28](https://github.com/tidev/titanium_mobile/commit/3929e28ead6d436c9f51755633bbe2692ab287f8))
* fix endless loop in setLanguage pre API 33 ([c48cf78](https://github.com/tidev/titanium_mobile/commit/c48cf780bf3ce7835d4c58fc10efaff2e6aa489f))
* fix image blob memory issues ([cef7d46](https://github.com/tidev/titanium_mobile/commit/cef7d465cf1c3cd6bfaa0ab960e88eabc911db66))
* null pointer check in cpp files ([76aff13](https://github.com/tidev/titanium_mobile/commit/76aff13943084d6eef936be0686937498c6d76f6))
* use i18n app name in AndroidManifest ([ce0dc80](https://github.com/tidev/titanium_mobile/commit/ce0dc8017994d5b35d09231e0204463e73a54839))

### iOS platform

* fix Ti.UI.Button styling and events on Mac Catalyst ([1897d79](https://github.com/tidev/titanium_mobile/commit/1897d7961436ac0f36b0aacb495b2097a62dd949))
* support module builds with mac=true on Catalyst ([dd2899b](https://github.com/tidev/titanium_mobile/commit/dd2899b9de0e37acbd5f1ce56fc3110a51b82f2c))

### Multiple platforms

* upgrade core-js to remove baseline-browser-mapping warning ([de4eb55](https://github.com/tidev/titanium_mobile/commit/de4eb55af06c6c86bf47e4844de2758b1845d0fc))

## Features

### Multiple platforms

* add web view API to hide input accessory views ([b48b6d1](https://github.com/tidev/titanium_mobile/commit/b48b6d14212661f8bd359423f8dd7a6193c223cf))
* add Alloy app to create menu ([3016623](https://github.com/tidev/titanium_mobile/commit/30166236297d6002dcffa42514ac54a92726b859))
* support for AttributedString objects ([a753fc2](https://github.com/tidev/titanium_mobile/commit/a753fc2d362a44658dfcec2f57f3250b078b0a5e))

### Android platform

* cache ListView searchText ([2e7fa1f](https://github.com/tidev/titanium_mobile/commit/2e7fa1fc9a64fc81714598bd96973b16da41aeef))
* keepHardwareMode property in Ti.UI.View ([3d03db0](https://github.com/tidev/titanium_mobile/commit/3d03db0baa533328deead4c698314a12c84e9fc8))
* move ScrollableView to ViewPager2 ([8f2359b](https://github.com/tidev/titanium_mobile/commit/8f2359b9b093c32ed96befc9c6dc967a02ce131b))
* pre-compile regex patterns ([2653a62](https://github.com/tidev/titanium_mobile/commit/2653a62e8e1e396b20635e826994276547052384))
* use ConcurrentHashMap in TiRhelper and TiUIHelper ([5198d50](https://github.com/tidev/titanium_mobile/commit/5198d501da074b10a4cd5acd55b70b666d7d9fa5))

## SDK Module Versions

| Module      | Android version | iOS Version |
| ----------- | --------------- | ----------- |
| facebook | 14.0.0 | 15.0.0 |
| ti.map | 5.7.0 | 7.3.1 |
| ti.webdialog | 2.5.0 | 3.0.2 |
| ti.playservices | 18.6.0 | n/a |
| ti.identity | 3.2.0 | 5.0.0 |
| urlSession | n/a | 4.0.1 |
| ti.coremotion | n/a | 4.0.1 |
| ti.applesignin | n/a | 3.1.2 |
| hyperloop | 7.1.0 | 7.1.0 |

## [13.1.1](https://github.com/tidev/titanium_mobile/compare/13_1_0_GA...13.1.1) (2026-01-29)

## About this release

Titanium SDK 13.1.1 is a patch release of the SDK, addressing high-priority issues from previous releases.

As of this GA release, the previous Titanium SDK patch release (13.1.0) is no longer supported.

## Community Credits

* Hans Knöchel
  * update xcode in runner to 26.2 ([951b2ab](https://github.com/tidev/titanium_mobile/commit/951b2ab943edca700693d1b8e05797aa8b855f6f))

* Hendrik Bugdoll
  * reverted tabBarItem reuse & added missing initial values ([1a1079b](https://github.com/tidev/titanium_mobile/commit/1a1079baa659173f50339478379eb8feadf8467e))
  * completed Xcode task names for pretty build log ([b93f3da](https://github.com/tidev/titanium_mobile/commit/b93f3da5b900dd76de84789caa9ca1b087aad059))
  * fixed Android environment detection in CLI info ([ebf8de8](https://github.com/tidev/titanium_mobile/commit/ebf8de8f10dc49ca751f5fc542bc38069b71dbc3))

* Prashant Saini
  * apply null checks on `localOverlayProxy` ([229bb6a](https://github.com/tidev/titanium_mobile/commit/229bb6a52ddd1d8c45ac7f966865d2eb64983f6c))

* Michael Gangolf
  * upgrade gradle plugin ([8ce48bc](https://github.com/tidev/titanium_mobile/commit/8ce48bcead6c6633d24760678809655c4f5ca16d))
  * fix gradle warning in SDK build ([bb903e3](https://github.com/tidev/titanium_mobile/commit/bb903e3a3e917af14d4a95f89f7d0f96266614e3))
  * ioslib update ([56f968d](https://github.com/tidev/titanium_mobile/commit/56f968d3db1efc13db8c3fc83fb4ede0ccf04c6e))

* César Estrada
  * Resolve Mac Catalyst build failures and App Store distribution issues ([3f6de97](https://github.com/tidev/titanium_mobile/commit/3f6de975374e45e53855411ff9d5f30d441de21e))

## Bug Fixes

### Android platform

* apply null checks on `localOverlayProxy` ([229bb6a](https://github.com/tidev/titanium_mobile/commit/229bb6a52ddd1d8c45ac7f966865d2eb64983f6c))
* fixed Android environment detection in CLI info ([ebf8de8](https://github.com/tidev/titanium_mobile/commit/ebf8de8f10dc49ca751f5fc542bc38069b71dbc3))

### iOS platform

* Resolve Mac Catalyst build failures and App Store distribution issues ([3f6de97](https://github.com/tidev/titanium_mobile/commit/3f6de975374e45e53855411ff9d5f30d441de21e))
* reverted tabBarItem reuse & added missing initial values ([1a1079b](https://github.com/tidev/titanium_mobile/commit/1a1079baa659173f50339478379eb8feadf8467e))

## SDK Module Versions

| Module      | Android version | iOS Version |
| ----------- | --------------- | ----------- |
| facebook | 14.0.0 | 15.0.0 |
| ti.map | 5.7.0 | 7.3.1 |
| ti.webdialog | 2.5.0 | 3.0.2 |
| ti.playservices | 18.6.0 | n/a |
| ti.identity | 3.2.0 | 5.0.0 |
| urlSession | n/a | 4.0.1 |
| ti.coremotion | n/a | 4.0.1 |
| ti.applesignin | n/a | 3.1.2 |
| hyperloop | 7.1.0 | 7.1.0 |

# [13.1.0](https://github.com/tidev/titanium_mobile/compare/13_0_X...13.1.0) (2026-01-13)

## About this release

Titanium SDK 13.1.0 is a minor release of the SDK, addressing high-priority issues from previous releases.


## Community Credits

* Hans Knöchel
  * improve changelog generation ([558cf58](https://github.com/tidev/titanium_mobile/commit/558cf58fcf775f8e2ed3dbbadab55444383070a8))
  * propery handle incremental builds in Xcode ([8ae9374](https://github.com/tidev/titanium_mobile/commit/8ae9374b965b1551afaa9cc7fcd8312f537d2a4e))
  * support swift package manager (SPM) module dependencies ([a6dee74](https://github.com/tidev/titanium_mobile/commit/a6dee746d1c2020353749ae3981bd574510554e8))
  * add support for iOS 26+ UIScrollEdgeEffect API ([6babec0](https://github.com/tidev/titanium_mobile/commit/6babec02c2efb79c8f089d78751847033e822d22))
  * add parity for “backgroundSelectedColor” API ([e47e4b2](https://github.com/tidev/titanium_mobile/commit/e47e4b2e7c069a803cf8c473015a12b18171fc2a))
  * fix local Xcode build ([b1474dd](https://github.com/tidev/titanium_mobile/commit/b1474ddc994981404dde3d5d8a30c9990925e26f))
  * properly set Titanium SDK version for runtime usage ([cf9dfa8](https://github.com/tidev/titanium_mobile/commit/cf9dfa8dba49b37b8014077b89fb4f20e6b7420f))
  * be able to pass button configuration as object, support “loading" state ([03339a1](https://github.com/tidev/titanium_mobile/commit/03339a16dbd8153e5f50617b825945145c5c1214))
  * update hyperloop.next to 7.1.0 to support 16 KB page size ([219f6bc](https://github.com/tidev/titanium_mobile/commit/219f6bcd0872e82e813d54a9ee56a531a8581c45))
  * bump master to 13.1.0 ([ee00961](https://github.com/tidev/titanium_mobile/commit/ee00961b5b96f3187eb3aabe95da612ea5248e63))

* Michael Gangolf
  * update gradle to 8.14.3 ([3b23367](https://github.com/tidev/titanium_mobile/commit/3b23367a2fb6827190e0b71c13f0d7752a00ecc5))
  * kotlin version update ([8170e05](https://github.com/tidev/titanium_mobile/commit/8170e054fb8fecf4aaeb7d30f0c54f3b21d89548))
  * update minSdk to API Level 23 ([089cabc](https://github.com/tidev/titanium_mobile/commit/089cabcf76089c2ba74aba8c4ffc42cac67e8970))
  * include LaunchLogos again ([93b8e61](https://github.com/tidev/titanium_mobile/commit/93b8e61413fbe34859c179e3fd84408ac3b5962a))
  * update workflow ([8200206](https://github.com/tidev/titanium_mobile/commit/82002069c3134258eee9edb5ca87033a7d5331e8))
  * fix BottomNavigation selection when using activeTab ([471e61d](https://github.com/tidev/titanium_mobile/commit/471e61de42c58c43644ef221be88abca8fb6cedd))
  * clearUserCache method ([405c51a](https://github.com/tidev/titanium_mobile/commit/405c51a03add47fd6c312d8349dce07f9a3cf7af))
  * new WebView property 'multipleWindows' ([a8c7278](https://github.com/tidev/titanium_mobile/commit/a8c7278150b6ed0191727e408b47af7c721c542e))
  * navBarColor for Window ([b2b4ac8](https://github.com/tidev/titanium_mobile/commit/b2b4ac8c94d89ce8cceda52dad6461f475f0f5d3))
  * android library updates ([c026d19](https://github.com/tidev/titanium_mobile/commit/c026d194d9c434591ed8088c376f01b3070468dd))
  * fix screenCaptured crash on Android 13 and lower ([fe01676](https://github.com/tidev/titanium_mobile/commit/fe01676870d771d288adbaccacec078feefc3277))
  * fix gradle build ([16c3122](https://github.com/tidev/titanium_mobile/commit/16c312253fd9bcc09ea672b90d2de381f88f04a6))
  * update clang and lint iphone files ([123e61a](https://github.com/tidev/titanium_mobile/commit/123e61a143bf79fa474e52c932c6089a8c6adfac))
  * parity for `screenshotcaptured`  event ([ad0b76f](https://github.com/tidev/titanium_mobile/commit/ad0b76f940b914244aef2cf1ab83b4d3d450ac8a))
  * another commander place ([14fedc6](https://github.com/tidev/titanium_mobile/commit/14fedc68909870ac22bf3dc77428378159fb709b))
  * update commander ([c8c4a40](https://github.com/tidev/titanium_mobile/commit/c8c4a40a12a4c2aa4164465ecd953f93f1860d80))
  * lower clang ([c5c2329](https://github.com/tidev/titanium_mobile/commit/c5c2329255d640567f1da73048255872dff8e715))
  * updates ([c48b66f](https://github.com/tidev/titanium_mobile/commit/c48b66f1080f248cd518c3143bc11454142062e8))
  * remove more modules ([09624eb](https://github.com/tidev/titanium_mobile/commit/09624eb13ce7068a0f1244c8969752528d6326b7))
  * clean up unused src folders ([6aa9089](https://github.com/tidev/titanium_mobile/commit/6aa90894bd2b56ed6eb89f22ce28fd2927c4cac0))
  * downgrade fs-extras and ssri ([6abced1](https://github.com/tidev/titanium_mobile/commit/6abced144e4f286e0d24c44804c3aeac42e0683f))
  * fix lint ([ee65097](https://github.com/tidev/titanium_mobile/commit/ee6509739c2a97b1ab90bb0aeccb667c68bd05f9))
  * npm package updates ([26f2a73](https://github.com/tidev/titanium_mobile/commit/26f2a739d217c373f5d60e79fdfda963d47f89b9))
  * rename APS classes and remove unused analytics class ([40a61fa](https://github.com/tidev/titanium_mobile/commit/40a61fa76765bdb2ae46d04207265cdee050a7cd))
  * remove aps-analytics.jar and titanium-verify.jar ([d4eb9fc](https://github.com/tidev/titanium_mobile/commit/d4eb9fce08a6343113b3dbd2efd4a28dff094734))
  * remove -u parameter from CLI ([5b468e1](https://github.com/tidev/titanium_mobile/commit/5b468e122b5da15c19137a391b2a2249f37961e3))
  * remove URL question in 'ti create' ([68dd395](https://github.com/tidev/titanium_mobile/commit/68dd3958537c033bc72f7e6bb9f1857577667f3e))
  * node package updates ([579f33c](https://github.com/tidev/titanium_mobile/commit/579f33c16652fe8ab03b18ed1412de097758a6e3))

* Hendrik Bugdoll
  * added dSYM for Swift framework-based modules ([eba5c63](https://github.com/tidev/titanium_mobile/commit/eba5c63e103d9e735f0a14ed8ddf145ffeb16108))
  * added dSYM for TitaniumKit.framework ([4dd433e](https://github.com/tidev/titanium_mobile/commit/4dd433e3696cff5d543e26710645c4b5e2b5d1d9))
  * removed old Studio project file ([87b049b](https://github.com/tidev/titanium_mobile/commit/87b049b52f0783cdece04196290d03f2d1c3b628))
  * fixed Swift compilation conditions and flags ([23c26d3](https://github.com/tidev/titanium_mobile/commit/23c26d313daff361763395b397e427190f649683))
  * fixed TabBarItem update for iOS 18+ ([ef3ae90](https://github.com/tidev/titanium_mobile/commit/ef3ae90b3743705cc9510af78f15bc878f608631))
  * fixed multiple matching destinations warning for SDK build and clean app build ([4f42aeb](https://github.com/tidev/titanium_mobile/commit/4f42aebead4cc4cf22c67bf0d10c85e7f6c504db))
  * removed iOS 8 & 9 guards and fallbacks ([25bb40c](https://github.com/tidev/titanium_mobile/commit/25bb40cbb4df528303d265e94502df15178458d0))
  * removed unused imports and const in install hook ([37a63a2](https://github.com/tidev/titanium_mobile/commit/37a63a2443fd8dd44394490979551438584600a8))
  * improved readme and fixed further wording ([40c0cc0](https://github.com/tidev/titanium_mobile/commit/40c0cc01a80df1c45d355cb84ad960a6b1727371))
  * allowed multiple arguments for further scons commands ([d7919be](https://github.com/tidev/titanium_mobile/commit/d7919bee53b3f68d17fa879e04a4766808714751))
  * removed deprecated buffer init in buffer test ([c83bcf0](https://github.com/tidev/titanium_mobile/commit/c83bcf0e79f9bb42da09696bbe48d7f837ec8f75))
  * removed APSAnalytics framework and analytics stuff ([471ca27](https://github.com/tidev/titanium_mobile/commit/471ca27ed53a5b7b4e04de8dda8aa84480247ab0))
  * fixed further typos and wording ([b55d18a](https://github.com/tidev/titanium_mobile/commit/b55d18a4e3f7fea92f58ed4bfe1a4831d551fd5c))
  * fixed some typos in sources ([d2a91c5](https://github.com/tidev/titanium_mobile/commit/d2a91c5b90cb1c31d352eced71a630c06e41c5a8))
  * opt-out new tab bar under iPadOS 18+ ([3a902e5](https://github.com/tidev/titanium_mobile/commit/3a902e594ea8a417bfb3f3c2951d1b9b621cb39e))
  * fixed test:iphone:trace ([8c034a6](https://github.com/tidev/titanium_mobile/commit/8c034a6160a408399363c0ea951de9c4874d3751))
  * removed old iTunes references in apidoc ([2663745](https://github.com/tidev/titanium_mobile/commit/2663745adf46ce79933e6608c99f1831db75ed50))
  * dropped iTunes Sync ([34e7ea8](https://github.com/tidev/titanium_mobile/commit/34e7ea8b28deee0684cea3b65d46a3dea1ac817a))
  * added trace log option for tests ([4a6f0a2](https://github.com/tidev/titanium_mobile/commit/4a6f0a29a30af8d86a5f1e96b740874a3dd600c2))
  * dropped iTunes artwork generation ([6b683f2](https://github.com/tidev/titanium_mobile/commit/6b683f214a06236b2b65002a525d2eefc6def5a4))
  * memory display parity with CLI ([0c9cf2b](https://github.com/tidev/titanium_mobile/commit/0c9cf2bb7cf50eae47b31614ffe1052f0e295d98))
  * simulator background modes warnings ([3424f05](https://github.com/tidev/titanium_mobile/commit/3424f05db4c19157b42f9775dd474e04ba95adab))

* Prashant Saini
  * add script message handler support to Android WebView ([be49840](https://github.com/tidev/titanium_mobile/commit/be4984007eea273e216f16ade64302b265a2a2a7))

* Chris Barber
  * overwrite modules on local SDK build install ([9ca14af](https://github.com/tidev/titanium_mobile/commit/9ca14afedbf1b5356189e5a74219a9b0176a9ba0))
  * rename master to main in files ([f53ddeb](https://github.com/tidev/titanium_mobile/commit/f53ddebe916dc9f509d52cbc0e203f4b0a000eda))
  * show error if build command's config fails ([71d14f6](https://github.com/tidev/titanium_mobile/commit/71d14f6756375edf38d88efcc0a52203b7c3cbae))
  * Refactor Node.js code to ESM (#14040) ([b058a7f](https://github.com/tidev/titanium_mobile/commit/b058a7feb6505d05413e24eb684b726441c66ae2))


## Bug Fixes

### Android platform

* clean up unused src folders ([6aa9089](https://github.com/tidev/titanium_mobile/commit/6aa90894bd2b56ed6eb89f22ce28fd2927c4cac0))
* fix BottomNavigation selection when using activeTab ([471e61d](https://github.com/tidev/titanium_mobile/commit/471e61de42c58c43644ef221be88abca8fb6cedd))
* fix screenCaptured crash on Android 13 and lower ([fe01676](https://github.com/tidev/titanium_mobile/commit/fe01676870d771d288adbaccacec078feefc3277))

### Multiple platforms

* improve changelog generation ([558cf58](https://github.com/tidev/titanium_mobile/commit/558cf58fcf775f8e2ed3dbbadab55444383070a8))
* overwrite modules on local SDK build install ([9ca14af](https://github.com/tidev/titanium_mobile/commit/9ca14afedbf1b5356189e5a74219a9b0176a9ba0))
* properly set Titanium SDK version for runtime usage ([cf9dfa8](https://github.com/tidev/titanium_mobile/commit/cf9dfa8dba49b37b8014077b89fb4f20e6b7420f))
* removed deprecated buffer init in buffer test ([c83bcf0](https://github.com/tidev/titanium_mobile/commit/c83bcf0e79f9bb42da09696bbe48d7f837ec8f75))
* show error if build command's config fails ([71d14f6](https://github.com/tidev/titanium_mobile/commit/71d14f6756375edf38d88efcc0a52203b7c3cbae))

### iOS platform

* added dSYM for Swift framework-based modules ([eba5c63](https://github.com/tidev/titanium_mobile/commit/eba5c63e103d9e735f0a14ed8ddf145ffeb16108))
* added dSYM for TitaniumKit.framework ([4dd433e](https://github.com/tidev/titanium_mobile/commit/4dd433e3696cff5d543e26710645c4b5e2b5d1d9))
* fix local Xcode build ([b1474dd](https://github.com/tidev/titanium_mobile/commit/b1474ddc994981404dde3d5d8a30c9990925e26f))
* fixed multiple matching destinations warning for SDK build and clean app build ([4f42aeb](https://github.com/tidev/titanium_mobile/commit/4f42aebead4cc4cf22c67bf0d10c85e7f6c504db))
* fixed Swift compilation conditions and flags ([23c26d3](https://github.com/tidev/titanium_mobile/commit/23c26d313daff361763395b397e427190f649683))
* fixed TabBarItem update for iOS 18+ ([ef3ae90](https://github.com/tidev/titanium_mobile/commit/ef3ae90b3743705cc9510af78f15bc878f608631))
* fixed test:iphone:trace ([8c034a6](https://github.com/tidev/titanium_mobile/commit/8c034a6160a408399363c0ea951de9c4874d3751))
* include LaunchLogos again ([93b8e61](https://github.com/tidev/titanium_mobile/commit/93b8e61413fbe34859c179e3fd84408ac3b5962a))
* propery handle incremental builds in Xcode ([8ae9374](https://github.com/tidev/titanium_mobile/commit/8ae9374b965b1551afaa9cc7fcd8312f537d2a4e))
* simulator background modes warnings ([3424f05](https://github.com/tidev/titanium_mobile/commit/3424f05db4c19157b42f9775dd474e04ba95adab))

## Features

### Multiple platforms

* add script message handler support to Android WebView ([be49840](https://github.com/tidev/titanium_mobile/commit/be4984007eea273e216f16ade64302b265a2a2a7))
* added trace log option for tests ([4a6f0a2](https://github.com/tidev/titanium_mobile/commit/4a6f0a29a30af8d86a5f1e96b740874a3dd600c2))

### Android platform

* clearUserCache method ([405c51a](https://github.com/tidev/titanium_mobile/commit/405c51a03add47fd6c312d8349dce07f9a3cf7af))
* navBarColor for Window ([b2b4ac8](https://github.com/tidev/titanium_mobile/commit/b2b4ac8c94d89ce8cceda52dad6461f475f0f5d3))
* new WebView property 'multipleWindows' ([a8c7278](https://github.com/tidev/titanium_mobile/commit/a8c7278150b6ed0191727e408b47af7c721c542e))
* parity for `screenshotcaptured`  event ([ad0b76f](https://github.com/tidev/titanium_mobile/commit/ad0b76f940b914244aef2cf1ab83b4d3d450ac8a))
* update gradle to 8.14.3 ([3b23367](https://github.com/tidev/titanium_mobile/commit/3b23367a2fb6827190e0b71c13f0d7752a00ecc5))

### iOS platform

* add parity for “backgroundSelectedColor” API ([e47e4b2](https://github.com/tidev/titanium_mobile/commit/e47e4b2e7c069a803cf8c473015a12b18171fc2a))
* add support for iOS 26+ UIScrollEdgeEffect API ([6babec0](https://github.com/tidev/titanium_mobile/commit/6babec02c2efb79c8f089d78751847033e822d22))
* be able to pass button configuration as object, support “loading" state ([03339a1](https://github.com/tidev/titanium_mobile/commit/03339a16dbd8153e5f50617b825945145c5c1214))
* opt-out new tab bar under iPadOS 18+ ([3a902e5](https://github.com/tidev/titanium_mobile/commit/3a902e594ea8a417bfb3f3c2951d1b9b621cb39e))
* support swift package manager (SPM) module dependencies ([a6dee74](https://github.com/tidev/titanium_mobile/commit/a6dee746d1c2020353749ae3981bd574510554e8))

## SDK Module Versions

| Module      | Android version | iOS Version |
| ----------- | --------------- | ----------- |
| facebook | 14.0.0 | 15.0.0 |
| ti.map | 5.7.0 | 7.3.1 |
| ti.webdialog | 2.5.0 | 3.0.2 |
| ti.playservices | 18.6.0 | n/a |
| ti.identity | 3.2.0 | 5.0.0 |
| urlSession | n/a | 4.0.1 |
| ti.coremotion | n/a | 4.0.1 |
| ti.applesignin | n/a | 3.1.2 |
| hyperloop | 7.1.0 | 7.1.0 |

## [13.0.1](https://github.com/tidev/titanium_mobile/compare/13_0_0_GA...13.0.1) (2025-10-31)

## About this release

Titanium SDK 13.0.1 is a patch release of the SDK, addressing high-priority issues from previous releases.

As of this GA release, the previous Titanium SDK patch release (13.0.0) is no longer supported.


## Community Credits

* Michael Gangolf
  * new WebView property 'multipleWindows' ([4d78dcc](https://github.com/tidev/titanium_mobile/commit/4d78dcc661aaa1b4db9a4883d9be702e0fc99a65))
  * update iOS workflow ([4f99762](https://github.com/tidev/titanium_mobile/commit/4f99762ec2f286cb0f17e6c9ad7f228d35546c29))

* Prashant Saini
  * locale fixes and improvements ([58dea74](https://github.com/tidev/titanium_mobile/commit/58dea743851ad3e82a4b9e66cb3560c7b4e28842))
  * fix ANR caused by the `getNumberOfCameras` method ([4f85145](https://github.com/tidev/titanium_mobile/commit/4f85145594c584ca5dcb7436fce50e3d00feb487))

* Hans Knöchel
  * properly set Titanium SDK version for runtime usage ([3c56aa6](https://github.com/tidev/titanium_mobile/commit/3c56aa64288fd1b6f628a774ffff169bd193e59c))
  * fix ButtonConfiguration API from throwing an error on device ([7ddf0f5](https://github.com/tidev/titanium_mobile/commit/7ddf0f53ffb793342497d96cd972f1ee108aa8ff))

* Hendrik Bugdoll
  * TabBar appearance issue on iPhone landscape & iPad ([2e3f04e](https://github.com/tidev/titanium_mobile/commit/2e3f04edccdb60dadccef82eafe0b2988ddb2aac))
  * not conform to protocol warnings ([7100588](https://github.com/tidev/titanium_mobile/commit/7100588f1aeb5f0507de45b1a4e87f024b7243d2))

* narbs
  * ensure eventStoreChanged notification is not over-registered ([83704bd](https://github.com/tidev/titanium_mobile/commit/83704bd7ce8f1f36417916957a087027feca4104))
  * patch ActivityKit so it works with catalyst (#14280) ([be918dd](https://github.com/tidev/titanium_mobile/commit/be918ddb39c5a610d475cb23fb41544eabe6a5c7))

## Bug Fixes

### Android platform

* fix ANR caused by the `getNumberOfCameras` method ([4f85145](https://github.com/tidev/titanium_mobile/commit/4f85145594c584ca5dcb7436fce50e3d00feb487))

### Multiple platforms

* ensure eventStoreChanged notification is not over-registered ([83704bd](https://github.com/tidev/titanium_mobile/commit/83704bd7ce8f1f36417916957a087027feca4104))
* patch ActivityKit so it works with catalyst (#14280) ([be918dd](https://github.com/tidev/titanium_mobile/commit/be918ddb39c5a610d475cb23fb41544eabe6a5c7))
* properly set Titanium SDK version for runtime usage ([3c56aa6](https://github.com/tidev/titanium_mobile/commit/3c56aa64288fd1b6f628a774ffff169bd193e59c))

### iOS platform

* fix ButtonConfiguration API from throwing an error on device ([7ddf0f5](https://github.com/tidev/titanium_mobile/commit/7ddf0f53ffb793342497d96cd972f1ee108aa8ff))
* not conform to protocol warnings ([7100588](https://github.com/tidev/titanium_mobile/commit/7100588f1aeb5f0507de45b1a4e87f024b7243d2))
* TabBar appearance issue on iPhone landscape & iPad ([2e3f04e](https://github.com/tidev/titanium_mobile/commit/2e3f04edccdb60dadccef82eafe0b2988ddb2aac))

## Features

### Android platform

* locale fixes and improvements ([58dea74](https://github.com/tidev/titanium_mobile/commit/58dea743851ad3e82a4b9e66cb3560c7b4e28842))
* new WebView property 'multipleWindows' ([4d78dcc](https://github.com/tidev/titanium_mobile/commit/4d78dcc661aaa1b4db9a4883d9be702e0fc99a65))

## BREAKING CHANGES


## SDK Module Versions

| Module      | Android version | iOS Version |
| ----------- | --------------- | ----------- |
| facebook | 14.0.0 | 15.0.0 |
| ti.map | 5.7.0 | 7.3.1 |
| ti.webdialog | 2.5.0 | 3.0.2 |
| ti.playservices | 18.6.0 | n/a |
| ti.identity | 3.2.0 | 5.0.0 |
| urlSession | n/a | 4.0.1 |
| ti.coremotion | n/a | 4.0.1 |
| ti.applesignin | n/a | 3.1.2 |
| hyperloop | 7.0.9 | 7.0.9 |

# [13.0.0](https://github.com/tidev/titanium_mobile/compare/12_8_X...13.0.0) (2025-09-15)

## About this release

Titanium SDK 13.0.0 is a major release of the SDK, providing full support for iOS 26 and Xcode 26.

## Community Credits

* Jan Vennemann
  * use proxy viewCount in ScrollableView to prevent empty display ([bc0b7d3](https://github.com/tidev/titanium_mobile/commit/bc0b7d3c4fd2b12cddce37e00f542d9b6fa48524))
  * correct safe area for standalone windows ([c471606](https://github.com/tidev/titanium_mobile/commit/c471606fc7c4421be26851861f696bd9f1c95c21))
  * improve safe area layout lifecycle ([4a13546](https://github.com/tidev/titanium_mobile/commit/4a1354683817584259b4e3bd493068f50f153869))
  * include top safe area inset in navigation window ([e859350](https://github.com/tidev/titanium_mobile/commit/e85935055104070831690397fb88d66e3d4815b8))
  * improve safe area detection and trigger relayout ([3c86467](https://github.com/tidev/titanium_mobile/commit/3c864672ed2e91d25cd6b12a589b2cbd7d0c656f))

* Hans Knöchel
  * Revert "feat(ios): support iOS 26+ source views for non-iPad devices (#14258)" (#14276) ([6ae5a29](https://github.com/tidev/titanium_mobile/commit/6ae5a290588e8252ace96f3bbd5758699f3897cd))
  * update xcode/ios/watchos compatibility versions ([f1922f7](https://github.com/tidev/titanium_mobile/commit/f1922f786090cbd797e25c7651f20bc841b123d4))
  * remove interactive dismiss mode ([97dcc32](https://github.com/tidev/titanium_mobile/commit/97dcc32f3b171dca8c028503ec3dcaeb86a5ae38))
  * update changelog ([9034457](https://github.com/tidev/titanium_mobile/commit/903445773268ea7fb2bc635c632f7fe05ce01839))
  * update ioslib to 5.1.0 to support Xcode 26 ([c36edc4](https://github.com/tidev/titanium_mobile/commit/c36edc443dd44b9b0e5e3a9e9f51c74538f05504))
  * update changelog ([519b817](https://github.com/tidev/titanium_mobile/commit/519b817d36abc0380e37cb4f0b733e73322d9fd5))
  * Revert "fix: properly handle containment of tab group (#14261)" (#14266) ([6023d7e](https://github.com/tidev/titanium_mobile/commit/6023d7ee0db716fa0b051bbeebe866595f894146))
  * properly handle containment of tab group ([2894f9f](https://github.com/tidev/titanium_mobile/commit/2894f9f68bf16af39f7fbd32b232de21de4dbfdd))
  * properly clean build folder ([5347cc5](https://github.com/tidev/titanium_mobile/commit/5347cc5be569ff1365e6c0eb3b8ce47de6e37f5c))
  * add 13.0.0 changelog ([f678a93](https://github.com/tidev/titanium_mobile/commit/f678a93c184ba46269814ea9d7d6bf0ec40ca203))
  * build with Xcode 26 ([4d9d041](https://github.com/tidev/titanium_mobile/commit/4d9d0419e00b3c9add07af3487c1d74d664afc92))
  * bump master to 13.0.0 ([8e92123](https://github.com/tidev/titanium_mobile/commit/8e9212326c736c3187a4a86a6a56d61afde6b076))
  * add support for modern button configuration ([a7ab5bd](https://github.com/tidev/titanium_mobile/commit/a7ab5bd26af2100e83b00644dbe882087233dda7))
  * add support for iOS 26+ glass effects ([26a1a86](https://github.com/tidev/titanium_mobile/commit/26a1a8631fdebb3863108588f2cbc5875ec934d9))
  * support for iOS 26+ bottomAccessoryView API ([99461f5](https://github.com/tidev/titanium_mobile/commit/99461f571cf0d3e5844366d7ba80b1af8b26db2a))
  * support iOS 26+ source views for non-iPad devices ([ce3752b](https://github.com/tidev/titanium_mobile/commit/ce3752bcfddc5e1139a73ff2344cf4910c5b3a29))
  * make sure the scrollable view child view is a Ti view ([6549100](https://github.com/tidev/titanium_mobile/commit/65491002402faa05f1cc6a605edcf41c3dd15755))
  * fix possible build issues related to dsym generation ([98acc26](https://github.com/tidev/titanium_mobile/commit/98acc262d34eb06ff9a46e7f7be38ebfb0d5459c))
  * disable debug dylib for more stable dev builds ([ac53981](https://github.com/tidev/titanium_mobile/commit/ac539813c5ff4d48b9a586e20aafb95cdf4aa279))
  * trim trailing new line on html text areas ([8c39dc7](https://github.com/tidev/titanium_mobile/commit/8c39dc72d1b65a2b20a260ee043f02709908d3bc))
  * trim trailing new line on html labels ([64e0632](https://github.com/tidev/titanium_mobile/commit/64e06321d96d5dc24bdc972db6f0d9ff187270f7))
  * Revert "fix: fix all docs warnings (#14253)" (#14254) ([ad12a80](https://github.com/tidev/titanium_mobile/commit/ad12a80444c79b121569f5bb7e5be93065e8b637))
  * fix all docs warnings ([0d0f2d0](https://github.com/tidev/titanium_mobile/commit/0d0f2d06f43c56520ae7cb59e56393887dfabdab))
  * use Xcode 16.4 for building the SDK ([85b7048](https://github.com/tidev/titanium_mobile/commit/85b7048a67db448c6de1a1cbd949d401faafb7d8))

* Michael Gangolf
  * update hyperloop.next to 7.1.0 to support 16 KB page size ([219f6bc](https://github.com/tidev/titanium_mobile/commit/219f6bcd0872e82e813d54a9ee56a531a8581c45))
  * update module versions ([ffb3607](https://github.com/tidev/titanium_mobile/commit/ffb36078a25f11a02dc454bbef55acf12f27fd1f))
  * keep TableView search results on enter ([0ed2b8d](https://github.com/tidev/titanium_mobile/commit/0ed2b8dce0637c007952b53789b3a973332c073a))
  * fix transparent TextField backgroundColor ([8906cd5](https://github.com/tidev/titanium_mobile/commit/8906cd51997fe3e181e033aa54739ce2d5faac8f))
  * fix hidding actionbar in drawerLayout ([d42adf2](https://github.com/tidev/titanium_mobile/commit/d42adf26b86d749b6d037109c9932038a1ca8e2f))
  * code refactor and cleanup ([6c87189](https://github.com/tidev/titanium_mobile/commit/6c87189a2e01f14ec68ed3920eea2907be1259be))
  * update ndk, camerax for 16kb page sizes support ([22720c4](https://github.com/tidev/titanium_mobile/commit/22720c465a19a8b3b94a3d1c8506ee14cfd12f6f))

## Bug Fixes

### Android platform

* fix hidding actionbar in drawerLayout ([d42adf2](https://github.com/tidev/titanium_mobile/commit/d42adf26b86d749b6d037109c9932038a1ca8e2f))
* fix transparent TextField backgroundColor ([8906cd5](https://github.com/tidev/titanium_mobile/commit/8906cd51997fe3e181e033aa54739ce2d5faac8f))

### Multiple platforms

* fix all docs warnings ([0d0f2d0](https://github.com/tidev/titanium_mobile/commit/0d0f2d06f43c56520ae7cb59e56393887dfabdab))
* properly clean build folder ([5347cc5](https://github.com/tidev/titanium_mobile/commit/5347cc5be569ff1365e6c0eb3b8ce47de6e37f5c))
* properly handle containment of tab group ([2894f9f](https://github.com/tidev/titanium_mobile/commit/2894f9f68bf16af39f7fbd32b232de21de4dbfdd))

### iOS platform

* correct safe area for standalone windows ([c471606](https://github.com/tidev/titanium_mobile/commit/c471606fc7c4421be26851861f696bd9f1c95c21))
* fix possible build issues related to dsym generation ([98acc26](https://github.com/tidev/titanium_mobile/commit/98acc262d34eb06ff9a46e7f7be38ebfb0d5459c))
* improve safe area detection and trigger relayout ([3c86467](https://github.com/tidev/titanium_mobile/commit/3c864672ed2e91d25cd6b12a589b2cbd7d0c656f))
* improve safe area layout lifecycle ([4a13546](https://github.com/tidev/titanium_mobile/commit/4a1354683817584259b4e3bd493068f50f153869))
* include top safe area inset in navigation window ([e859350](https://github.com/tidev/titanium_mobile/commit/e85935055104070831690397fb88d66e3d4815b8))
* keep TableView search results on enter ([0ed2b8d](https://github.com/tidev/titanium_mobile/commit/0ed2b8dce0637c007952b53789b3a973332c073a))
* make sure the scrollable view child view is a Ti view ([6549100](https://github.com/tidev/titanium_mobile/commit/65491002402faa05f1cc6a605edcf41c3dd15755))
* trim trailing new line on html labels ([64e0632](https://github.com/tidev/titanium_mobile/commit/64e06321d96d5dc24bdc972db6f0d9ff187270f7))
* trim trailing new line on html text areas ([8c39dc7](https://github.com/tidev/titanium_mobile/commit/8c39dc72d1b65a2b20a260ee043f02709908d3bc))
* use proxy viewCount in ScrollableView to prevent empty display ([bc0b7d3](https://github.com/tidev/titanium_mobile/commit/bc0b7d3c4fd2b12cddce37e00f542d9b6fa48524))

## Features

### Android platform

* update ndk, camerax for 16kb page sizes support ([22720c4](https://github.com/tidev/titanium_mobile/commit/22720c465a19a8b3b94a3d1c8506ee14cfd12f6f))

### iOS platform

* add support for iOS 26+ glass effects ([26a1a86](https://github.com/tidev/titanium_mobile/commit/26a1a8631fdebb3863108588f2cbc5875ec934d9))
* add support for modern button configuration ([a7ab5bd](https://github.com/tidev/titanium_mobile/commit/a7ab5bd26af2100e83b00644dbe882087233dda7))
* support for iOS 26+ bottomAccessoryView API ([99461f5](https://github.com/tidev/titanium_mobile/commit/99461f571cf0d3e5844366d7ba80b1af8b26db2a))
* support iOS 26+ source views for non-iPad devices ([ce3752b](https://github.com/tidev/titanium_mobile/commit/ce3752bcfddc5e1139a73ff2344cf4910c5b3a29))

## BREAKING CHANGES

*  iOS: The safe area padding is now always set to the native safe area padding and not manually to "`0`" anymore if navBarHidden or tabBarHidden are set

## SDK Module Versions

| Module      | Android version | iOS Version |
| ----------- | --------------- | ----------- |
| facebook | 14.0.0 | 15.0.0 |
| ti.map | 5.7.0 | 7.3.1 |
| ti.webdialog | 2.5.0 | 3.0.2 |
| ti.playservices | 18.6.0 | n/a |
| ti.identity | 3.2.0 | 5.0.0 |
| urlSession | n/a | 4.0.1 |
| ti.coremotion | n/a | 4.0.1 |
| ti.applesignin | n/a | 3.1.2 |
| hyperloop | 7.1.0 | 7.1.0 |

# [12.8.0](https://github.com/tidev/titanium_mobile/compare/12_7_X...12.8.0) (2025-07-17)

## About this release

Titanium SDK 13.4.0 is a minor release of the SDK, focused on NavigationWindow improvements, new UI features, and cross-platform event handling enhancements.

## New Features

### Android

* **NavigationWindow**: Enhanced close/open lifecycle — NavigationWindow no longer creates a spurious Activity, `close()` works correctly, and the `windows` array stays in sync when child windows are closed via back button or directly ([836d8f4](https://github.com/jonasfunk/titanium-sdk/commit/836d8f44d2))
* **NavigationWindow**: Added `windows` array property to access the current window stack ([9d3b063](https://github.com/jonasfunk/titanium-sdk/commit/9d3b06394b))
* **Window**: Added `willClose` event, fired before a window begins closing ([b29f9f1](https://github.com/jonasfunk/titanium-sdk/commit/b29f9f1218))
* **Label**: Added `padding` property for controlling internal text padding ([0d4cf01](https://github.com/jonasfunk/titanium-sdk/commit/0d4cf010f6))

### iOS

* **NavigationWindow**: Added `insertWindow` method to insert a window at a specific index in the navigation stack ([2aee62d](https://github.com/jonasfunk/titanium-sdk/commit/2aee62dfb5), [e534718](https://github.com/jonasfunk/titanium-sdk/commit/e53471869a))
* **NavigationWindow**: Support creation without `window` property — the first `openWindow` call now sets the root window dynamically ([836d8f4](https://github.com/jonasfunk/titanium-sdk/commit/836d8f44d2))
* **Window**: Added `willClose` event, fired before a window begins closing ([b29f9f1](https://github.com/jonasfunk/titanium-sdk/commit/b29f9f1218))
* **Label**: Added `padding` property for controlling internal text padding ([0d4cf01](https://github.com/jonasfunk/titanium-sdk/commit/0d4cf010f6))

## Bug Fixes

### Android & iOS

* **View**: Fixed event handling for children added directly in the creation dictionary ([51d8eb5](https://github.com/jonasfunk/titanium-sdk/commit/51d8eb541a))

### iOS

* **NavigationWindow**: Fixed `insertWindow` transition handling and stack management ([eab6b7c](https://github.com/jonasfunk/titanium-sdk/commit/eab6b7c521))

## Community Credits

* jonasfunk
  * enhance NavigationWindowProxy close/open lifecycle and windows array sync ([836d8f4](https://github.com/jonasfunk/titanium-sdk/commit/836d8f44d2))
  * added willClose event to windows ([b29f9f1](https://github.com/jonasfunk/titanium-sdk/commit/b29f9f1218))
  * added a windows array property ([9d3b063](https://github.com/jonasfunk/titanium-sdk/commit/9d3b06394b))
  * proper eventhandling for children added directly in creation ([51d8eb5](https://github.com/jonasfunk/titanium-sdk/commit/51d8eb541a))
  * added insert window method ([e534718](https://github.com/jonasfunk/titanium-sdk/commit/e53471869a))
  * added padding to Ti.UI.Label ([0d4cf01](https://github.com/jonasfunk/titanium-sdk/commit/0d4cf010f6))
  * updated insertWindow ([eab6b7c](https://github.com/jonasfunk/titanium-sdk/commit/eab6b7c521))
  * add insertWindow method to NavigationWindow ([2aee62d](https://github.com/jonasfunk/titanium-sdk/commit/2aee62dfb5))

# [13.3.0](https://github.com/tidev/titanium_mobile/compare/13_2_X...13.3.0) (2025-01-07)

## About this release

Titanium SDK 13.3.0 is a minor release of the SDK.

## New Features

### JavaScript

* **Array.at()**: Added polyfill for ES2022 `Array.prototype.at()` method, enabling negative indexing support for arrays (e.g., `arr.at(-1)` returns the last element).

### Android

* **Media**: Added `targetImageSize` option for image scaling in MediaModule, allowing precise control over output image dimensions.
* **TiImageHelper**: Added method to copy EXIF metadata from source to destination image, preserving photo metadata during image processing.

## Bug Fixes

### iOS

* **Slider**: Fixed issue where the `change` event was fired continuously during sliding instead of only when the value changes by a step increment.

## Community Credits

* jonasfunk
  * add Array polyfill for ES2022 .at() method support ([46cd3aa](https://github.com/tidev/titanium_mobile/commit/46cd3aa20b1ec729432234e145dc924d1fb634fc))
  * add method to copy EXIF metadata from source to destination image ([a118994](https://github.com/tidev/titanium_mobile/commit/a118994159854715f6d9433cea1845d366ed83d9))
  * add targetImageSize option for image scaling in MediaModule ([0d6bf60](https://github.com/tidev/titanium_mobile/commit/0d6bf60cc9f506e95b24f92f166127dd858564a5))
  * make sure slider step change event is only fired on step ([294723e](https://github.com/tidev/titanium_mobile/commit/294723ef2b02a3e8ad497b42b5e19b1945351cfe))
