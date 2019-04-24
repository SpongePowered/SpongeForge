SpongeForge [![Build Status](https://travis-ci.org/SpongePowered/SpongeForge.svg?branch=master)](https://travis-ci.org/SpongePowered/SpongeForge)
=============

**Currently not stable and under heavy development!**

A Forge implementation of the Sponge API.

* [Homepage]
* [Source]
* [Issues]
* [Documentation]
* [Community Chat]: #sponge on irc.esper.net
* [Development Chat]: #spongedev on irc.esper.net

## Prerequisites
* [Java] 8

## Cloning
The following steps will ensure your project is cloned properly.

1. `git clone --recursive https://github.com/SpongePowered/SpongeForge.git`
2. `cd SpongeForge`
3. `cp scripts/pre-commit .git/hooks`

## Setup
**Note**: SpongeForge uses [Gradle] as its build system. The repo includes the Gradle wrapper that will automatically download the correct Gradle 
version. Local installations of Gradle may work but are untested. To execute the Gradle wrapper, run the `./gradlew` script on Unix systems or only
`gradlew` on Windows systems.

Before you are able to build SpongeForge, you must first prepare the environment:

  - Run `./gradlew setupDecompWorkspace --refresh-dependencies`

### IDE Setup
__For [Eclipse]__
  1. Run `./gradlew eclipse`
  2. Import Sponge as an existing project (File > Import > General)
  3. Select the root folder for SpongeForge and make sure `Search for nested projects` is enabled
  4. Check Sponge when it finishes building and click **Finish**

__For [IntelliJ]__
  1. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  2. Click File > New > Project from Existing Sources > Gradle and select the root folder for SpongeForge.
  3. Make sure _Use default gradle wrapper_ is selected. Older/newer Gradle versions may work but we only test using the wrapper.
  4. Make sure _Create separate module per source set_ is selected. This ensures that Intellij properly handles SpongeForge's Kotlin and Java dependencies.

## Running
__Note:__ The following is aimed to help you setup run configurations for Eclipse and IntelliJ, if you do not want to be able to run SpongeForge directly from your IDE then you can skip this.

__For [Eclipse]__ 
  1. Running `./gradlew eclipse` should have generated the run configurations automatically.
  2. When launching the server for the first time, it will shutdown by itself. You will need to modify eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).

__For [IntelliJ]__
  1. Run `./gradlew genIntelliJRuns`
  2. Restart IntelliJ IDEA or reload the project, the run configuration should now be generated.
  3. When launching the server for the first time, it will shutdown by itself. You will need to modify eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).

## Building
__Note:__ You must [Setup the environment](#setup) before you can build SpongeForge.

In order to build SpongeForge you simply need to run the `gradlew` command. On Windows systems you should run `gradlew` instead of `./gradlew` to
invoke the Gradle wrapper. You can find the compiled JAR files in `./build/libs` but in most cases you'll only need 'spongeforge-x.x.x-x-x.x-x.jar'.

## Updating your Clone
The following steps will update your clone with the official repo.

1. `git pull`
2. `git submodule update --recursive`
3. `./gradlew setupDecompWorkspace --refresh-dependencies`

## FAQ
__A dependency was added, but my IDE is missing it! How do I add it?__
>If a new dependency was added, you can just restart your IDE and the Gradle plugin for that IDE should pull in the new dependencies.

## Contributing
Are you a talented programmer looking to contribute some code? We'd love the help!
* Open a pull request with your changes, following our [guidelines](CONTRIBUTING.md).
* Please follow the above guidelines for your pull request(s) to be accepted.

__Help! Things are not working!__
>Some issues can be resolved by deleting the '.gradle' folder in your user directory and running through the setup steps again, or even running `gradle cleanCache` and running through the setup again. Otherwise if you are having trouble with something that the README does not cover, feel free to join our IRC channel and ask for assistance.

[Eclipse]: https://eclipse.org/
[Gradle]: https://gradle.org/
[Homepage]: https://spongepowered.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Issues]: https://github.com/SpongePowered/Sponge/issues
[Documentation]: https://docs.spongepowered.org/
[Java]: http://java.oracle.com/
[Source]: https://github.com/SpongePowered/Sponge/
[MIT License]: http://www.tldrlegal.com/license/mit-license
[Community Chat]: https://webchat.esper.net/?channels=sponge
[Development Chat]: https://webchat.esper.net/?channels=spongedev
[Jenkins]: https://jenkins-ci.org/
