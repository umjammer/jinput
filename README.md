[![Release](https://jitpack.io/v/umjammer/jinput.svg)](https://jitpack.io/#umjammer/jinput)
[![Java CI](https://github.com/umjammer/jinput/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/jinput/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/jinput/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/jinput/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# JInput

🎮 Library for access to input devices.

this is jna-ized and service-loader-ized version. [original](https://github.com/jinput/jinput)

## Install

* https://jitpack.io/#umjammer/jinput

## References

* https://github.com/bozjator/JInput-Joystick
* mac
    * https://github.com/code-orchestra/code-orchestra-core/tree/b2bbf8362be2e2173864c294c635badb2e27ecc6/core/actionScript/source/com/semaphore/jna/cf
* windows
  * https://github.com/StrikerX3/JXInput

## License

Licensed under [BSD License](https://opensource.org/licenses/BSD-3-Clause), copyright is attributed in each source file committed.

## TODO

* ~~use proper ServiceLoader instead of proprietary plugin system (backport from vavi-awt-joystick)~~
* linux spi not tested yet
* ~~windows wip, package name~~ windows spi not tested yet
* ~~deprecate polling, use event listener?~~
* native parts
  * backport cf lib to rococoa
  * separate jna parts as jna-platform-extended?
* ~~at windows, i mistake dword as 2byte~~
* component value should be generics? (currently float fixed)
