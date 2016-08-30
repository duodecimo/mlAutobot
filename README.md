# mlAutobot
Autonomous robot that rides rodes using machine learning.

This project aims to build a small tricicle autonomous robot capable of driving a road after leaning how to drive observing a human being performing the driving trough remote control.

1) The robot.
The chassis is made with mdf. A 5v eletric motor with double wheels moves the robot, while a servo turns the eletric motor to give it direction.
The robot carries an Android Mobile running IP Webcam Pro (accquired (BRL 9,00) in play store: https://play.google.com/store/apps/details?id=com.pas.webcam.pro&hl=pt-BR, August 29, 2016).

2) The server.
IP Webcam Pro is used with default configuration, and the server is launched with blank user name and password connected on intranet.

3) Java desktop.
A Java program receives the broadcasted images using vlcj (accquired from: http://capricasoftware.co.uk/#/projects/vlcj, August 29, 2016). By the way, vlcj is "an Open Source project that provides Java bindings and an application framework for the excellent VLC media player from VideoLAN. The bindings can be used to build media player client and server software using Java.".
