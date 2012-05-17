package edu.rosehulman.android.directory;

/** Constants used throughout the application */
public class C {
	
	/*! \mainpage
	 * 
	 * \section sec_intro Introduction
	 *
	 * Welcome to the Android developer documetation for the RHIT Mobile
	 * project.  The purpose of this documentation is to provide an easily
	 * digestable overview of how the project is structured, specifically for
	 * future teams who may be assigned maintenance of this work, project
	 * advisors, or even curious developers.
	 *
	 *
	 * \section sec_overview Project Overview
	 *
	 * RHIT Mobile Android Edition is the Android-powered branch of the RHIT
	 * Mobile project, a senior project commissioned during the 2011-2012 school
	 * year and maintained by various other Rose-Hulman bodies afterwards. The
	 * app provides 3 main classes of functionality.
	 *
	 * \subsection sec_func_map Map and Location Services
	 *
	 * One of the core services RHIT Mobile aims to provide is a comprehensive
	 * and cohesive map and location service.  This functionality serves as the
	 * basis of navigation around the app and is what the user is initially
	 * exposed to when the application starts up
	 *
	 *  - An interactive map of the campus.  Android provides a set of Google
	 *  APIs that include a MapView with various extension points.  Many of our
	 *  project's modifications to the MapView class can be found in the classes
	 *  contained within \ref edu.rosehulman.android.directory.maps.  The campus
	 *  map itself is controlled by \ref CampusMapActivity, which uses the extension
	 *  point defined in the \ref maps package.
	 *
	 *  - Details for any location on campus.  \ref LocationActivity provides
	 *  additional details and functionality related to a specific location on
	 *  campus.  This activity will be accessible from various locations,
	 *  including the map on the \ref CampusMapActivity.  From the location activity,
	 *  users can view it on the map, see which locations are within that
	 *  location, get directions to that location, any navigate to any useful
	 *  links associated with that location.
	 *
	 *  - Search for locations.  Users will not always want to manipulate a map
	 *  to find locations on campus.  Instead, we have a search feature that
	 *  will allow users the enter the name of a location or any other
	 *  searchable text associated with a location and we can then bring the
	 *  user to the location activity associated with their search results.
	 *
	 * \subsection sec_func_directory Directory Services
	 *
	 * Access to the Rose-Hulman student, staff, and faculty directory is one of
	 * the more useful features to be included in RHIT Mobile. In order to
	 * protect confidentiality of personal information, users will be required
	 * to first authenticate using their Kerberos credentials before they will
	 * be presented with any campus member's personal information. All of the
	 * directory services, once a user is authenticated, will consist of the
	 * following:
	 *
	 *  - Search for students, staff, and faculty members.  Similar to searching
	 *  for a location, the user needs to be able to find information on other
	 *  members of Rose-Hulman.  This information will be accessed by a simple
	 *  search interface.
	 *
	 *  - View detailed information for a student, staff member, or faculty
	 *  member.  After searching for a particular person, the application will
	 *  display relevant information to the user.  This information will include
	 *  information such as the persons contact information, location,
	 *  schedules, and other relevant information (the person may have different
	 *  information associated with them based on their type).
	 *
	 * \subsection sec_func_info Information Services
	 *
	 * In addition to the sections listed above, there is some information that
	 * doesn't necessarily belong attached to a specific location or person, or
	 * that needs to be easily accessible without having to browse to its parent
	 * location or person. This information includes announcements by Career
	 * Services, Aramark information, Public Safety information, Hatfield
	 * programs, and more. Because this information needs to be easily
	 * accessible, this functionality will be located in a separate area of the
	 * application.
	 *
	 *
	 * \section sec_layers Application Structure
	 * 
	 * Android follows a Model View Controller architectural pattern, and the
	 * various packages of this project can be categorized into one of these
	 * layers.
	 *
	 * \subsection sec_layer_model Model
	 *
	 *  - \ref edu.rosehulman.android.directory.model
	 *  - \ref edu.rosehulman.android.directory.db
	 *
	 * \subsection sec_layer_view View
	 *
	 * The view layer for Android actually contains no code.  Instead, the views
	 * are entirely contained in layout XML files found in the project's
	 * res/layout directory.  These views are then inflated by each of the
	 * view's controllers at runtime.
	 * 
	 * \subsection sec_layer_controller Controller
	 *
	 * - \ref edu.rosehulman.android.directory
	 * - \ref edu.rosehulman.android.directory.maps
	 * - \ref edu.rosehulman.android.directory.service
	 * - \ref edu.rosehulman.android.directory.util
	 * - \ref edu.rosehulman.android.directory.fragments
	 * - \ref edu.rosehulman.android.directory.loaders
	 * - \ref edu.rosehulman.android.directory.tasks
	 * - \ref edu.rosehulman.android.directory.providers
	 *
	 * \subsection sec_layer_misc Misc
	 *
	 * - \ref edu.rosehulman.android.directory.auth
	 * - \ref edu.rosehulman.android.directory.compat
	 *
	 *
	 * \section doc_maintenance_sec Maintaining This Documentation
	 *
	 * For developers working on this project after its initial creation, please
	 * document any code you add or change inline, using the existing files as
	 * an example. For administrative-level documentation, including this index
	 * page, make changes to C.java.
	 *
	 *
	 */



	
	/*! \package edu.rosehulman.android.directory.model
	 * 
	 * The model classes do little other than hold data and provide simple
	 * operations on the data, but they are used in many of the application's
	 * components.  The structure of these data models is based on the structure
	 * of the data returned by the \ref service "server" API calls.
	 *
	 * \ref Location and \ref MapAreaData are the primary classes describing a
	 * location on the map and are used by any feature dealing with specific
	 * locations.  
	 */
	
	/*! \package edu.rosehulman.android.directory.db
	 * 
	 * All data retrieved by the \ref service "server" is stored in a SQLite
	 * database on the phone itself.  This package provides a layer of
	 * abstraction from actually interfacing with the database by providing a
	 * set of adapter classes (\ref LocationAdapter, for example) that perform
	 * operations with the database in terms of \ref model objects instead of
	 * forcing all other layers use SQL directly.
	 */
	
	/*! \package edu.rosehulman.android.directory
	 * 
	 * Certain classes do not fit well in any other package and are therefore
	 * left at the main package level.  Examples include:
	 *
	 *  - \ref MyApplication "MyApplication": maintains global application state
	 *
	 *  - \ref C "C": contains global constants that can be used throughout the
	 *  application
	 *
	 * This package also includes all of the Activities (such as \ref
	 * LocationActivity)
	 */
	
	/*! \package edu.rosehulman.android.directory.maps
	 * 
	 * This application makes numerous modifications and extensions to the
	 * provided MapView class, all of which are categorized in this layer.
	 * Various overlays are drawn on the map (ex \ref BuildingOverlayLayer), and
	 * there are numerous supporting classes required to simplify interaction
	 * with the map.
	 */
	
	/*! \package edu.rosehulman.android.directory.service
	 * 
	 * RHIT Mobile has to deal with a large amount of data, none of which
	 * originates from the device itself.  Instead of storing static copies of
	 * data and doing had processing (like directions) on the device itself, we
	 * have a server component to house that data and perform complex
	 * computations.  With that additional component comes the need to be able
	 * to communicate with the server.  This package provides that
	 * functionality.
	 */
	
	/*! \package edu.rosehulman.android.directory.util
	 * 
	 * This layer contains various utility functionality that is not
	 * associated with any other package and should not be associated with
	 * the UI classes.
	 */
	
	/*! \package edu.rosehulman.android.directory.fragments
	 * 
	 * Fragments contain common functionalilty that can be used by more
	 * than one activity.  Most fragments are stored in this package.
	 */
	
	/*! \package edu.rosehulman.android.directory.loaders
	 * 
	 * Loaders are a more powerful form of background data loading when
	 * compared to AsyncTasks.  Unlike AsyncTasks, Loaders are not bound to
	 * an activity, so it makes sense to define them in their own class
	 * files.
	 */
	
	/*! \package edu.rosehulman.android.directory.tasks
	 * 
	 * This package contains any background tasks that may be used by
	 * multiple activities.
	 */
	
	/*! \package edu.rosehulman.android.directory.providers
	 * 
	 * When searching it is useful to provide search suggestions.  This
	 * feature requires a provider to do the background processing, which
	 * is implemented in this package
	 */
	
	/*! \package edu.rosehulman.android.directory.auth
	 * 
	 * RHIT Mobile stores user credentials in the android accounts list, so
	 * RHIT Mobile needs to set itself up as an account authenticator.
	 * That funcitonality is isolated to this package for easy extraction
	 * if it ever becomes necessary.
	 */
	
	/*! \package edu.rosehulman.android.directory.compat
	 * 
	 * In order to maintain compatability with android 2.2, certain classes
	 * needed to be backported from newer versions of android.  Those
	 * classes are kept in this package.
	 */
	
	
	/** Tag used for Logging.
	 *
	 * <pre>
	 * Log.d(C.TAG, &quot;Debug Message&quot;);
	 * </pre>
	 */
	public static String TAG = "RHITMobile";
}
