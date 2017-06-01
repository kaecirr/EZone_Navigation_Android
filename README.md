**SYSTEMS REQUIREMENTS DOCUMENT AND PROTOTYPE** 

Kaelan Sinclair (21292916) 
Keyur Modi (21845191)
Lei Wang (21676963)
He Yuntian (22017827)
Brian Lee (21492167)

**GOAL**

**USER STORIES**

**DESIGN CONSTRAINTS**

The important requirement for EZone Indoor Navigation Project is to track indoor positioning and navigating within various buildings of the University of Western Australia Campus. There are currently various mapping technologies to do so. Some of which we took into considerations are IndoorAtlas, Google Indoor Maps, Any Place and Mazemap.

Indoor Atlas uses geomagnetic fields, inertial sensor data, barometric pressure and radio signals to detect and track an indoor position. Indoor Atlas has higher accuracy than Wi-Fi technology and Beacons (BLE) devices. It doesn’t cost for hardware and it gives real-time location.

Google Indoor Maps is being used at limited places at the moment and is still under development for public use. Google Indoor Maps uses Wi-Fi, GPS, mobile accelerometer and Gyroscope to detect person’s indoor location. On the other hand, Any Place mapping technology is open source community which uses similar tracking features like google indoor maps and indoor atlas but doesn’t currently have iOS support.

At the moment, the Indoor mapping technology is infantry but growing rapidly and evolving with various technologies to support indoor mapping. Google is currently working on it’s Indoor Maps and similar is the case with other technologies. Thus comparing various technology, indoor atlas has support for the kind of indoor navigation we want to implement for the project. Also it’s available for iOS and Android. Indoor Atlas uses Google maps as the base mapping technology and floor plans of the buildings are added using their own interface on top of google maps. We will be integrating indoor atlas in our application with keeping modularity in mind. Indoor Atlas provides good amount of accuracy in compared to other mapping technologies available in the market. Also our application will be flexible and future proof, allowing for the replacement of indoor atlas with more widespread and advanced indoor mapping technology as the future advancement, once the technology is matured. It will be fairly simple to add new features like adding new buildings.

Indoor Atlas is being used for the project as it provides with the use cases we are looking in our project. On the other hand, it does come with a high cost to implement for the real-world big project. However, we are developing the application which has various layers and in future once the new technology comes to match the requirements of the project, we will be easily able to replace it with indoor atlas.

**UI MOCK-UPS**

This section will discuss the user interface mock-ups. The shape and colour of the screens are not reflective of the final product.
These mock-ups are designed to represent the general placement of buttons and show the basic functionalities.

<p align="center">
  <img width="931" height="810" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/button%20func.png">
</p>

When the user opens the app, they will be greeted by the splash screen. After a moment, it transitions to the map screen. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/splash_screen_to_first_screen.gif">
</p>

A message should appear asking for permission to use the user’s location.
From here, the user can access the menu screen by tapping the button on the top left corner. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/first_screen_to_visitor_menu.gif">
</p>

The user can login by tapping ‘Sign In’. This pulls up the login page which allows the user to enter their credentials. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/visitor_menu_to_login_in_Screen.gif">
</p>

If the credentials are entered incorrectly, the user is notified that they have entered the ‘incorrect login details’. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/incorrect_login_resized.png">
</p>

If the credentials were correct, it just goes back to the map screen. Pressing the menu button again, we can see that the ‘Visitor’ label has now changed to the username of the user. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/login_screen_to_signed_in_menu_screen.gif">
</p>

The menu screen also has the functionality to view the favourite and recent locations and also the settings screen. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/menu_options.gif">
</p>

‘Favourite Locations’ shows the locations that the user has added to their favourites. 
‘Recent Locations’ shows the locations that the user has searched recently.
‘Settings’ brings up the syncing options. Syncing allows the user to save their favourite and recent locations on the server so that they can access it from any other device. This feature requires the user to be logged in. There are two ways of syncing: manually and automatically. The user can turn off syncing if they wish.

Back on the map screen, users can tap the search bar and enter a room that they would like to go to. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/first_screen_to_search_screen.gif">
</p>

The search will show the results that most likely correspond to what was entered. If the search does not correspond to any room, it will display ‘No search results’.

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/no_search_results_resized.png">
</p>

If the user searches for ‘CSSE Lab’ for example, it brings up all the labs in the CSSE building. Tapping on one of the results, leads to a screen that shows where the room is and some details about it. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/Search_results_to_CSSE_Lab_screen.gif">
</p>

Tapping ‘Read more’ brings the user to a screen that gives further details about the searched room.

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/getting_room_details.gif">
</p>

Tapping ‘Navigate To Here’ draws the shortest path from the user’s closest node to the chosen end destination. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/navigate_on_same_floor.gif">
</p>

If the destination is on a different floor, a path is drawn to the stairwell that will provide the shortest path to the destination. When the user changes to the correct floor, a path will be drawn to the destination. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/navigation_different_floors.gif">
</p>

If the user taps on a room on a floor, a destination marker will be placed in that location. The user can then press the button with the marker symbol to make the app start tracking the user's location. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/Placing_marker_and_starting_tracking.gif">
</p>

If there is an emergency, such as a fire, the app will give an emergency alert and point to the nearest emergency exit. This function disables the searching feature. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/emergency_alert.gif">
</p>


**MINIMUM FUNCTIONALITY**



