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
	 *  map itself is controlled by \ref MainActivity, which uses the extension
	 *  point defined in the \ref maps package.
	 *
	 *  - Details for any location on campus.  \ref LocationActivity provides
	 *  additional details and functionality related to a specific location on
	 *  campus.  This activity will be accessible from various locations,
	 *  including the map on the \ref MainActivity.  From the location activity,
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
	 * \subsection sec_layer_model View
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



	
	/*!
	 * \package edu.rosehulman.android.directory.model
	 * 
	 * TODO Documentation for model
	 */
	
	/*!
	 * \package edu.rosehulman.android.directory.db
	 * 
	 * TODO Documentation for db
	 */
	
	/*!
	 * \package edu.rosehulman.android.directory
	 * 
	 * TODO Documentation for main package
	 */
	
	/*!
	 * \package edu.rosehulman.android.directory.maps
	 * 
	 * TODO Documentation for maps
	 */
	
	/*!
	 * \package edu.rosehulman.android.directory.service
	 * 
	 * TODO Documentation for service
	 */
	
	/*!
	 * \package edu.rosehulman.android.directory.util
	 * 
	 * TODO Documentation for util
	 */
	
	
	/** Tag used for Logging.
	 *
	 * <pre>
	 * Log.d(C.TAG, &quot;Debug Message&quot;);
	 * </pre>
	 */
	public static String TAG = "MobileDirectory";
}
