# netpaint
Android application for drawing together over Bluetooth or TCP/IP

The project consists of a client and a server part, and this repository contains the client part only.

Features:

* Drawing surface with color picker, effects, eraser, zoom, undo and redo
* Possibility to save and load images
* Single-user mode
* Multi-user mode using Bluetooth
* Multi-user mode using sockets and TCP/IP

Single-user: The user can draw on either a blank drawing surface or on a loaded image using different colors and effects. The image can be saved to the phone.

Multi-user: Users can draw together, and one of two protocols can be selected for communication - Bluetooth or TCP/IP. If Bluetooth is selected, a master-slave relationship between two clients is established, and the clients exchange messages with each other regarding the drawing movements and properties to keep both drawing surfaces updated and identical. 

If TCP/IP is selected, the client will communicate with other clients through a server, connecting n number of clients together in a "room". All clients in the same "room" should have identical drawing surfaces. Messages are sent and received through sockets in real-time. Whenever the server receives a message from one of the clients in a "room", it pushes the information to all the other clients of that "room". The clients receive the message and can immediately update their surfaces.


