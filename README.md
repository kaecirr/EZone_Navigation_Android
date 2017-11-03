# **SYSTEMS REQUIREMENTS DOCUMENT AND OVERVIEW** 

Kaelan Sinclair (21292916) 
Keyur Modi (21845191)
Lei Wang (21676963)
Yuntian He (22017827)
Brian Lee (21492167)

## **SUMMARY**

The design team has been engaged by the Faculty of Engineering, Computing and Mathematics to develop an EZone application which can perform indoor navigation in UWA buildings. This application will enable UWA students or teachers to find a shortest path from the starting position to the destination in a building. For visitors, we will also provide a path finding function, but not all the EZone application functions will be available to them. Currently, we only provide the path navigation in the Computer Science building, but we will expand our target navigation buildings in the near future. This document outlines the functional and non-functional requirements developed during the project discovery phase and will serve as a guide for client acceptance following delivery of the product. 

## **GOAL OF EZONE PROJECT** 
The EZone project aims to provide indoor navigation to users at the University of Western Australia. Users of the application may have difficulties in finding their way around inside buildings since they are not familiar with the interior layout. Thus, our requirements for this project is to help users to navigate within the buildings. The goal of the EZone project is to allow users to get the shortest path from the starting location to the destination. The user will open the EZone application and the floor plan of the building based on the current location of the user will display on the mobile phone. Users could then choose which room or destination he/she would like to head to. After choosing the destination, the shortest path will show on the floor map. Our aim is to provide the indoor navigation service for users in UWA buildings. We will consider expanding our service to other campus buildings and consider using a more proper and efficient way to store our data in the database to provide real time service with minimal lag. A future goal to consider is the implementation of new technolgies into the EZone project. These include camera capture navigation, augmented reality maps or voice identification of a destination.

## **MINIMUM FUNCTIONALITY**

The following is the minimum deliverables that we expect the appplication to have:

*	Navigation to destination on a single floor
*	Navigation to destination on different floor
*	Real-time tracking of user’s position
*	Live updating of path (of the user)
*	Search of basic destinations
*	User can place marker on destination
*	User can manually put a starting location 
*	Determines viable path

Note that the User Stories section contains potential future functionality of the application. See the application presentation for demonstrations of the supported functionality.

### **USER ROLES**

*Users*: The users could be students, university staff members or visitors that can use this application.

*Developers*: The developers are those who design and develop the indoor navigation application.

### **USER STORIES**

***Single floor navigation***: As a user, I want to select a location (or input a room number) as a destination on a single floor plan so that it can show the path and guide me to that destination.

***Multiple floor navigation***: As a user, I want to choose a different floor and select a location (or input a room number) on that floor so that it can show the path and guide me to the destination. 

***Tracking user’s position***: As a user, I want the application to show my real-time position when I am moving in the building so that I can know my current location.

***Path updating***: As a user, I want the path to the destination to be updated automatically when I move so that I can have the correct path to the destination at all times.

***Searching destination***: As a user, I want to find a specific room by entering the room number so that it will tell me the location of that room.

***Placing a marker on destination***: As a user, I want to select a location as the destination by pressing that location on the floor plan so that it will place a marker on that location.

***Putting a starting point***: As a user, I want to select a location as a starting point so that I can see a viable path from that starting point to a destination.

***Determining viable path***: As a user, I want to have a viable path between my starting location and destination so that I can reach my destination directly and quickly.

***More information for a room***: As a user, I want to get more information about a specific room when I select that room so that I can find more details about it.

***Buttons for switch floors***: As a user, I want to have buttons which allow me to switch floor plans in a building so that I can select locations on different floors.

***A-star pathfinding***: 

As a user, I want the path to be updated within a fast response time so that the application feels responsive.

As a developer, I want to utilise A* as the pathfinding algorithm so that the time for calculating a viable path is reduced.

***Current location button***: As a user, I want to have a positioning button for me to press so that I can know my current location.

***Start tracking button***: As a user, I want to have a start tracking button for me to press so that it can guide me to the destination step by step. 

***Points-of-interest***: As a user, I want to have some points-of-interest marked on the floor plan so that I can see what's on the floor.

***Favourites and recent location list***: As a user, I want to save my favourite locations or recent paths so that it will be convenient for my later use.

***Functional login system***: As a user, I want to be able to login so that I can have access to my user data.

***Syncing with the server***: As a user, I want to sync my favourite or recent locations to the backend server so that I can access it from multiple devices.

***Determining user’s closest node***: As a developer, I want to to add the user's current location to the nearest reference point so that the graph can be constructed and the shortest path can be computed.

***Storing node information and meta-data for rooms in database***: As a developer, I want to store the information so that I can use them to present data to the users.

### **SOME USE CASES**

<p align="center">
  <img  width="750"
  src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/LoginNavigation.png">
</p>

<p align="center">
  <img width="750"
  src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/SearchRoom.png">
</p>

<p align="center">
  <img width="750"
  src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/GetRoomInformation.png">
</p>

<p align="center">
  <img width="750"
  src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/StartTracking.png">
</p>

<p align="center">
  <img width="750"
  src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/NavigateSingleFloor.png">
</p>

<p align="center">
  <img width="750"
  src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/NavigateMultipleFloor.png">
</p>

## **DESIGN CONSTRAINTS**

The important requirement for the EZone Indoor Navigation Project, is to perform indoor positioning and navigating within various buildings of the University of Western Australia. There are currently various mapping technologies to do so. Some of which we took into consideration are IndoorAtlas, Google Indoor Maps, AnyPlace and Mazemap.

IndoorAtlas uses geomagnetic fields, inertial sensor data, barometric pressure and radio signals to detect and track the user's indoor position. IndoorAtlas has higher accuracy than Wi-Fi technology and Beacon (BLE) devices. There is no additional hardware cost and it gives a real-time location.

Google Indoor Maps is being used in limited places at the moment and is still under development for public use. Google Indoor Maps uses Wi-Fi, GPS, mobile accelerometer and the gyroscope to detect a user’s indoor location. On the other hand, AnyPlace mapping technology is open source which uses similar tracking features like Google Indoor Maps and IndoorAtlas but doesn’t currently have iOS support.

At the moment, the indoor mapping technology is in its infancy but growing rapidly and evolving with various technologies to support indoor mapping. Google is currently working on its Indoor Maps and this is the case with other technologies as well. Thus comparing various technologies, IndoorAtlas has support for the kind of indoor navigation we want to implement for the project. Also, it’s available for iOS and Android. IndoorAtlas uses Google Maps as the base mapping technology and floor plans of the buildings are added using their own interface on top of Google Maps. We will be integrating IndoorAtlas in our application while keeping modularity in mind. IndoorAtlas provides a good amount of accuracy compared with other mapping technologies available in the market. Also, our application will be flexible and future-proof, allowing for the replacement of IndoorAtlas with a more widespread and advanced indoor mapping technology. It will then be fairly simple to add new features like adding new buildings.

IndoorAtlas is being used for the project as it provides the use cases we are looking for our project. On the other hand, it does come with a high cost to implement it within the real-world project. However, we are developing the application which has various layers and once the new technology comes to match the requirements of the project, we will easily be able to replace IndoorAtlas. As mentioned before, Indoor Atlas is useful for our use case but not the optimal solution for the project.

## **UI MOCK-UPS**

This section will discuss the user interface mock-ups. The shape and colour of the screens are not reflective of the final product.
These mock-ups are designed to represent the general placement of buttons and show the basic functionalities of what we aim to deliver

For more details (gifs, source code, etc) see the github page.

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

Tapping ‘Navigate To Here’ draws a viable path from the user’s closest node to the chosen end destination. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/navigate_on_same_floor.gif">
</p>

If the destination is on a different floor, a path is drawn to the stairwell that will provide a viable path to the destination. When the user changes to the correct floor, a path will be drawn to the destination. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/navigation_different_floors.gif">
</p>

If the user taps on a room on a floor, a destination marker will be placed in that location. The user can then press the button with the marker symbol to make the app start tracking the user's location. 

<p align="center">
  <img width="268" height="400" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/gifs/Placing_marker_and_starting_tracking.gif">
</p>

## **FUTURE SCOPE**

There is a multitude of features that could be added to enhance the functionality of the EZone application.  The following section will discuss some of these features and goals as part of the potential future scope.

In the future, the application could:
* Implement some of the above user stories and functionality that has yet to be implemented
    - Not all of the functionality has been implemented yet as the application is a proof of concept and work in progess.
*	Allow for more complex searching of destinations
    - The present scope of the project only covers very basic searching of destinations. Adding a more complex searching algorithm would make the searching more fine-tuned and help filter out unrelated results.

*	Have on-device calculations 
    - Path-finding could be improved by providing some on-device calculations used for intermediate movement. Determining the best start position for the navigation would also help in this respect. This could be achieved by considering several different nodes around the user’s position (instead of the closest node described in the minimum functionality) and selecting the node that would produce the most viable path. 

*	Provide navigation for different buildings and navigation between buildings
    - With regards to navigation, mapping of other buildings could be completed so that users could use the application in different buildings. Sometimes users of the application would also like to navigate outside a building as well. Thus, navigation between buildings is a future feature to consider.

*	Deal with inaccessible areas when navigating
    - Navigation in the current scope does not deal with areas being inaccessible. Most doors are accessible during the day, but at night and in the early hours of the morning, there are certain doors that lock.  Adding a feature that deals with this problem would help to strengthen the functionality of the application.

*	Provide further details about floors and points-of-interest
    - Providing extra detail about floors and points-of-interest could also be beneficial. Details, such as if a water fountain or a cafe is on the current floor, provides useful information for users. The locations of toilets would also be something that users may want to know when they load a floor. 

*	Allow for the sharing of the user’s location
    - Adding functionality to share the user’s location would help market the application and make it more interactive and appealing to users.

*	Allow for a website to maintain the backend
    - A website that could edit the database would be useful if lecturers change offices or if a room changes its name. Adding authorization and requests for user access could be made to ensure the security of the database.

*	Have an emergency alert notification
    - The app could be made to respond to emergency alarms. If there is an emergency in the building, such as a fire, the application could immediately alert the user and direct them to the nearest fire exit. It could block most of the functionality of the application until the emergency is resolved.

*	Allow for the usage of elevators 
    - Sometimes, users may have disabilities or would just rather use the elevator than the stairs. So, something that could potentially be considered as part of the future scope, is the usage of the application with the elevator.

It should be emphasised that these features and goals are not part of our current project scope or in our minimum requirements, but may be added if time permits. These future scope goals are provided to show how the application could be extended past the minimum requirements we hope to deliver.

## **PROTOTYPE**

In our prototype, we managed to perform navigation to a room on a single floor.

The following shows a user selecting an room and then navigating to it:

<p align="center">
  <img width="268" height="476" src="https://github.com/kaecirr/EZone_Navigation_Android/blob/master/prototypeGif/prototype.gif">
</p>

For more details (gifs, source code, etc) see the github page.

## **Conclusion**

This requirements document states the minimum requirements that the team will deliver. The team asks that the client read through the document and discuss any changes that need to be made. Once an agreement has been made, the team will commence the official development of the EZone application.

## **References**

[IndoorAtlas](http://www.indooratlas.com)

[Google Maps API](https://developers.google.com/maps/)
