Sponge [![Build Status](https://travis-ci.org/SpongePowered/Sponge.svg?branch=master)](https://travis-ci.org/SpongePowered/Sponge)
=============
**Currently not stable and under heavy development!**  
A Forge implementation of the Sponge API. It is licensed under the [MIT License]. 

* [Homepage]
* [Source]
* [Issues]
* [SpongeAPI Wiki]
* [Sponge Wiki]
* [Community Chat]: #sponge on irc.esper.net
* [Development Chat]: #spongedev on irc.esper.net

## Prerequisites
* [Java] 6

## Clone
The following steps will ensure your project is cloned properly.  
1. `git clone git@github.com:SpongePowered/Sponge.git`  
2. `cd Sponge`  
3. `git submodule update --init --recursive`  
4. `cp scripts/pre-commit .git/hooks`

## Setup
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__For [Eclipse]__  
  1. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  2. Make sure you have the Gradle plugin installed (Help > Eclipse Marketplace > Gradle Integration Plugin)  
  3. Import Sponge as a Gradle project (File > Import)
  4. Select the root folder for Sponge and click **Build Model**
  5. Check Sponge when it finishes building and click **Finish**

__For [IntelliJ]__  
  1. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  2. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  3. Click File > Import Module and select the **build.gradle** file for Sponge.

## Running
__Note:__ The following is aimed to help you setup run configurations for Eclipse and IntelliJ, if you do not want to be able to run Sponge directly from your IDE then you can skip this.  

__For [Eclipse]__  
  1. Go to **Run > Run Configurations**.  
  2. Right-click **Java Application** and select **New**.  
  3. Set the current project.  
  4. Set the name as `Sponge (Client)` and apply the information for Client below.  
  5. Repeat step 1 through 4, then set the name as `Sponge (Server)` and apply the information for Server below.  
  6. When launching the server for the first time, it will shutdown by itself. You will need to modify the server.properties to set onlinemode=false and modify the eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server)

__For [IntelliJ]__  
  1. Go to **Run > Edit Configurations**.  
  2. Click the green + button and select **Application**.  
  3. Set the name as `Sponge (Client)` and apply the information for Client below.  
  4. Repeat step 2 and set the name as `Sponge (Server)` and apply the information for Server below.  
  5. When launching the server for the first time, it will shutdown by itself. You will need to modify the server.properties to set onlinemode=false and modify the eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).

__Client__

|     Property      | Value                                     |
|:-----------------:|:------------------------------------------|
|    Main class     | GradleStart                               |
|    VM options     | -Djava.library.path="../../build/natives" -Dfml.coreMods.load=org.spongepowered.mod.SpongeCoremod |
| Working directory | ./run/client (Included in project)        |
| Module classpath  | Sponge (IntelliJ Only)                    |

__Server__

|     Property      | Value                              |
|:-----------------:|:-----------------------------------|
|    Main class     | GradleStartServer                  |
|    VM Options     | -Dfml.coreMods.load=org.spongepowered.mod.SpongeCoremod |
| Working directory | ./run/server (Included in project) |
| Module classpath  | Sponge (IntelliJ Only)             |


## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

In order to build Sponge you simply need to run the `gradle` command. You can find the compiled JAR file in `./build/libs` labeled similarly to 'sponge-x.x.x-SNAPSHOT.jar'.

## Contributing
Are you a talented programmer looking to contribute some code? We'd love the help!
* Open a pull request with your changes, following our [guidelines](CONTRIBUTING.md).
* Please follow the above guidelines for your pull request(s) to be accepted.

[Eclipse]: http://www.eclipse.org/
[Gradle]: http://www.gradle.org/
[Homepage]: http://spongepowered.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Issues]: http://issues.spongepowered.org/
[SpongeAPI Wiki]: https://github.com/SpongePowered/SpongeAPI/wiki/
[Sponge Wiki]: https://github.com/SpongePowered/Sponge/wiki/
[Java]: http://java.oracle.com/
[Source]: https://github.com/SpongePowered/Sponge/
[MIT License]: http://www.tldrlegal.com/license/mit-license
[Community Chat]: https://webchat.esper.net/?channels=sponge
[Development Chat]: https://webchat.esper.net/?channels=spongedev
