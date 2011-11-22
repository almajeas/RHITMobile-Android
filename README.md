# RHIT Mobile (Android Edition)

## Setting Up a Build Environment

Android projects require the [Android SDK](http://developer.android.com/sdk/index.html).  As instructions for setting up the SDK may change over time, refer to the official documentation for instructions on setting up both the Android SDK and the Eclipse ADT plugin.

## Getting the Sources

The source for the android project is split across multiple repositories.  In order to build the project, we need to first retrieve the code from these `submodules`.  From the root directory of the repository, run the following commands to initialize the required submodules:

    $ git submodule init
    $ git submodule update

## Building the Project

There are 3 relevant project folders that may need to be built (all contained within `android/src`):

* android-mapviewballoons/android-mapviewballoons
* RHITMobile
* BetaManager (optional)

The BetaManager is an optional project and is only required to add beta functionality to the application (such as unit tests).

### Signing

Android projects need to be signed in order to run on any device, and during development, applications are typically signed with a debug key and is specific to each installation of the SDK.  For official releases of the application, a generated signing key is used (which for security reasons, is not included in this repository).

This signing key is needed to generate the maps API key, which is used to allow the device to download and display Google Maps.  Instructions for obtaining a Maps API Key can be found [here](http://code.google.com/android/add-ons/google-apis/mapkey.html).

### Build Number

Each released build of RHITMobile should have a build number higher than any previously used build number.  This number can be set in the `AndroidManifest.xml` file in the two relevant project folders (`RHITMobile` and `BetaManager`).  The value that needs to be changed is the `android:versionCode` attribute of the `manifest` element.  Leaving the value set to 0 will permanently disable all BetaManager functionality.

# From the command line

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

# From Eclipse

Building from within Eclipse requires both the Android SDK and the Eclipse ADT Plugin, as described above.  To build the project, simply create a new workspace at `android/src` and import the 3 relevant projects contained in that directory, as shown above.  After importing the projects, simply use Eclipse to build and run the projects.

## Building the Documentation

Documentation specific to developers is included inline in the source code in [Doxygen](http://www.stack.nl/~dimitri/doxygen/) format. To build a copy of this documentation, first [install Doxygen](http://www.stack.nl/~dimitri/doxygen/download.html#latestsrc), then run the following command from the root of the repository (the directory above this one):

    $ doxygen android/src/Doxyfile

This will generate a set of HTML documentation under the `doc` directory within the following path: `android/src/RHITMobile`. Open the `index.html` file in this new directory with your favorite web browser to get started with the developer documentation. 
