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
1. `git clone --recursive https://github.com/SpongePowered/Sponge.git`
2. `cd Sponge`
3. `cp scripts/pre-commit .git/hooks`

## Setup
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__For [Eclipse]__  
  1. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  2. Run `gradle eclipse`
  3. Import Sponge as an existing project (File > Import > General)
  4. Select the root folder for Sponge and make sure `Search for nested projects` is enabled
  5. Check Sponge when it finishes building and click **Finish**

__For [IntelliJ]__  
  1. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  2. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  3. Click File > New > Project from Existing Sources > Gradle and select the root folder for Sponge.

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
