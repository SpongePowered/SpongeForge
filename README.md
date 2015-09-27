Sponge [![Build Status](https://travis-ci.org/SpongePowered/Sponge.svg?branch=master)](https://travis-ci.org/SpongePowered/Sponge)
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
* [Java] 6+

## Cloning
The following steps will ensure your project is cloned properly.

1. `git clone --recursive https://github.com/SpongePowered/Sponge.git`
2. `cd Sponge`
3. `cp scripts/pre-commit .git/hooks`

## Setup
__Note:__ If you do not have [Gradle] installed then use `./gradlew` for Unix systems or Git Bash and `gradlew.bat` for Windows systems in place of any `gradle` command.

Before you are able to build Sponge, you must first prepare the environment:

  - Run `gradle setupDecompWorkspace --refresh-dependencies`

**Note**: You may substitute `setupDecompWorkspace` for `setupCIWorkspace` when building on a CI such as [Jenkins].

### IDE Setup
__For [Eclipse]__
  1. Run `gradle eclipse`
  2. Import Sponge as an existing project (File > Import > General)
  3. Select the root folder for Sponge and make sure `Search for nested projects` is enabled
  4. Check Sponge when it finishes building and click **Finish**

__For [IntelliJ]__
  1. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  2. Click File > New > Project from Existing Sources > Gradle and select the root folder for Sponge.

## Running
__Note:__ The following is aimed to help you setup run configurations for Eclipse and IntelliJ, if you do not want to be able to run Sponge directly from your IDE then you can skip this.

__For [Eclipse]__ 
  1. Go to **Run > Run Configurations**.
  2. Right-click **Java Application** and select **New**.
  3. Set the current project.
  4. Set the name as `Sponge (forge/client)` and apply the information for Client below.
  5. Repeat step 1 through 4, then set the name as `Sponge (forge/server)` and apply the information for Server below.
  6. When launching the server for the first time, it will shutdown by itself. You will need to modify eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).


__For [IntelliJ]__
  1. Go to **Run > Edit Configurations**.
  2. Click the green + button and select **Application**.
  3. Set the name as `Sponge (forge/client)` and apply the information for Client below.
  4. Repeat step 2 and set the name as `Sponge (forge/server)` and apply the information for Server below.
  5. When launching the server for the first time, it will shutdown by itself. You will need to modify eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).

__Client__

|     Property      | Value                                     |
|:-----------------:|:------------------------------------------|
|    Main class     | GradleStart                               |
|    VM options     | -Dfml.coreMods.load=org.spongepowered.mod.SpongeCoremod |
| Working directory | ./run (Included in project)               |
| Module classpath  | Sponge (IntelliJ Only)                    |

__Server__

|     Property      | Value                              |
|:-----------------:|:-----------------------------------|
|    Main class     | GradleStartServer                  |
|    VM Options     | -Dfml.coreMods.load=org.spongepowered.mod.SpongeCoremod |
| Working directory | ./run                              |
| Module classpath  | Sponge (IntelliJ Only)             |


## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__Note:__ You must [Setup the environment](#setup) before you can build Sponge.

In order to build Sponge you simply need to run the `gradle` command. You can find the compiled JAR files in `./build/libs` but in most cases
you'll only need 'sponge-x.x.x-x-x.x-x.jar'.

## Updating your Clone
The following steps will update your clone with the official repo.

1. `git remote add upstream git@github.com:SpongePowered/Sponge.git`
2. `git pull --rebase upstream master`
3. `git submodule update --recursive`

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
