# EZone_Navigation_Android
The android front end for the EZone navigation project

**SYSTEM REQUIREMENTS DOCUMENT AND PROTOTYPE**


**DESIGN CONSTRAINTS**

The important requirement for EZone Indoor Navigation Project is to track indoor positioning and navigating within various buildings of the University of Western Australia Campus. There are currently various mapping technologies to do so. Some of which we took into considerations are IndoorAtlas, Google Indoor Maps, Any Place and Mazemap.

Indoor Atlas uses geomagnetic fields, inertial sensor data, barometric pressure and radio signals to detect and track an indoor position. Indoor Atlas has higher accuracy than Wi-Fi technology and Beacons (BLE) devices. It doesn’t cost for hardware and it gives real-time location.

Google Indoor Maps is being used at limited places at the moment and is still under development for public use. Google Indoor Maps uses Wi-Fi, GPS, mobile accelerometer and Gyroscope to detect person’s indoor location. On the other hand, Any Place mapping technology is open source community which uses similar tracking features like google indoor maps and indoor atlas but doesn’t currently have iOS support.

Thus comparing various technology, currently indoor atlas has support for the kind of indoor navigation we want to implement for the project. Also it’s available for iOS and Android. Indoor Atlas uses Google maps as the base mapping technology and floor plans of the buildings are added using their own interface on top of google maps. We will be integrating indoor atlas in our application with keeping modularity in mind. Indoor Atlas provides good amount of accuracy in compared to other mapping technologies available in the market. Also our application will be flexible and future proof, allowing for the replacement of indoor atlas with more widespread and advanced indoor mapping technology as the future advancement, once the technology is matured. It will be fairly simple to add new features like adding new buildings.

