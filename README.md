# BLEscan
Android application able to discover and connect Bluetooth Low Energy (BLE) devices. BLEscan browse all the services published by a connected device, list, read/write each service’s characteristic depending on its properties, and shows notifications when a
characteristic with notify property enabled changes.
## Features
  - Detect and present to the user if the mobile device supports BLE or not.
  - Detect and present if the Bluetooth adapter is turned on/off.
  - The app request the user to turn on/off the Bluetooth adapter and require all the permissions needed to work.
  - List all detected BLE devices and update as new devices are detected.
  - When long-click on an item of the detected devices list, the app try to connect the device.
  - After successfully connecting a BLE device, the app discover and list all the services published by the BLE device.
  - When long-click on an item of the services list, the app shows all the characteristics that belongs to the selected service; the app must shows the UUIDs, properties (R/W/N) and descriptors of each service’s characteristic.
  - The app subscribe itself to be notified to all characteristics of the service which has the notify property enabled.
  - The app is able to read and write the characteristic of a service and must shows the read and write statuses.
  - The app show notifications when characteristic with notify property enabled changes.
  - The app have a events log that shows all the BLE connection, transactions and errors events.
