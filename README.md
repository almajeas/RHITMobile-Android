# RHIT Mobile (Android Edition)

## License - Source Code

Rose-Hulman Mobile is owned by Rose-Hulman Institute of Technology and released
under the
[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0):

    Copyright 2012 Rose-Hulman Institute of Technology

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this softare except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## License - Media

All original media (as well as media source files, if included) are released
under the
[Creative Commons Attribution 3.0 Unported License (CC BY 3.0)](https://creativecommons.org/licenses/by/3.0/).
Attribution to [Rose-Hulman Institute of Technology](http://www.rose-hulman.edu)
(with a link) is required in any derivative or redistributed works. Any present
or past students, faculty, or staff of Rose-Hulman Institute of Technology are
not required to include attribution, though it is still appreciated.

![Creative Commons Attribution 3.0 Unported License Logo](http://i.creativecommons.org/l/by/3.0/88x31.png)

In broad terms, this license means that you are free:

* **to Share** — to copy, distribute and transmit the work
* **to Remix** — to adapt the work
* to make commercial use of the work

For complete details, see the
[human-readable deed](https://creativecommons.org/licenses/by/3.0/),
as well as the
[full license](https://creativecommons.org/licenses/by/3.0/legalcode).

## Setting Up a Build Environment

Android projects require the [Android SDK](http://developer.android.com/sdk/index.html).  As instructions for setting up the SDK may change over time, refer to the official documentation for instructions on setting up both the Android SDK and the Eclipse ADT plugin.

## Getting the Sources

The source for the android project is split across multiple repositories.  In order to build the project, we need to first retrieve the code from these `submodules`.  From the root directory of the repository, run the following commands to initialize the required submodules:

    $ git submodule init
    $ git submodule update

## Building the Project

There are 4 relevant project folders that will need to be built (all contained within `src`):

* android-mapviewballoons/android-mapviewballoons
* ActionBarSherlock/library
* RHITMobile
* BetaManager (optional)

The BetaManager is an optional project and is only required to add beta functionality to the application (such as unit tests).

### Signing

Android projects need to be signed in order to run on any device, and during development, applications are typically signed with a debug key and is specific to each installation of the SDK.  For official releases of the application, a generated signing key is used (which for security reasons, is not included in this repository).

This signing key is needed to generate the maps API key, which is used to allow the device to download and display Google Maps.  Instructions for obtaining a Maps API Key can be found [here](http://code.google.com/android/add-ons/google-apis/mapkey.html).  The Maps API Key can be configured by modifying the `maps_api_key` string in `res/values/values.xml`.

### Build Number

Each released build of RHITMobile should have a build number higher than any previously used build number.  This number can be set in the `AndroidManifest.xml` file in the two relevant project folders (`RHITMobile` and `BetaManager`).  The value that needs to be changed is the `android:versionCode` attribute of the `manifest` element.  Setting the value to 0 will permanently disable all BetaManager functionality.

### From the command line

Building from the command requires the Android SDK to be installed, as described above.  Apache's [Ant](http://ant.apache.org/) is also required to build the project.

From each project's directory, run the following command to autogenerate a `local.properties` file:

    $ android update project --path .

To automatically sign the project, add the following entries to the `local.properties` file for both the `BetaManager` and `RHITMobile` projects:

    key.store=path/to/mobile\_directory.keystore
    key.alias=mykey
    key.store.password=password
    key.alias.password=password

where `password` is set to the password of the private keystore and the proper path to the keystore is used.

To build the RHITMobile project, run the following command from within that project's directory:

    $ ant release

To immediately install the built application to a device, the following command can be used instead:

    $ ant release install

The same commands can be used to build and install the `BetaManager` project.

### From Eclipse

Building from within Eclipse requires both the Android SDK and the Eclipse ADT Plugin, as described above.  To build the project, create a new workspace at `src` and import the 4 relevant projects contained in that directory, as shown above.  After importing the projects, use Eclipse to build and run the projects.

To create the ActionBarSherlock project:

1.  Create a new Android project.
    -  Name the project ActionBarSherlock
    -  Select "Create project from existing source"
    -  Set the location to be "[repo path]/src/ActionBarSherlock/library"
2.  Eclipse will likely create the project incorrectly.  Delete the project (but not the contents on disk), and move `.project` and `.classpath` from "src/ActionBarSherlock" to "src/ActionBarSherlock/library"
3.  Import the project contained in "src/ActionBarSherlock/library"

Possible project import issues:

*  Projects may fail to build because project specific compilation settings are erroneously automatically configured.  For each project's properties page under "Java Compiler", deselect "Enable project specific settings"

## Building the Documentation

Documentation specific to developers is included inline in the source code in [Doxygen](http://www.stack.nl/~dimitri/doxygen/) format. To build a copy of this documentation, first [install Doxygen](http://www.stack.nl/~dimitri/doxygen/download.html#latestsrc), then run the following command from the root of the repository (the directory above this one):

    $ doxygen src/Doxyfile

This will generate a set of HTML documentation under the `doc` directory within the following path: `src/RHITMobile`. Open the `index.html` file in this new directory with your favorite web browser to get started with the developer documentation. 
