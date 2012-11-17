mud-server
==========

A basic MUD server written in Java.

Some of the following directories need to exist before the game starts. '\' is the top directory
for all the files. I am pretty sure that the current code will not create them itself, so for
now they'll need to be added manually. Only the IMPORTANT! directories are absolutely essential,
although the others will probably be important as well in the future.

Obviously you need the program itself, so this is just excepting source and compiled .class files
```
\data          IMPORTANT! the folder inside of which all game data is stored, server will likely crash without data
\data\accounts for account files, not really used or important
\data\backup   database backups: currently no system for backups besides copying main text db file
\data\config   IMPORTANT! config files
\data\help     IMPORTANT! server/game command help files: if absent, the game may crash when trying to find them
\data\maps     not really important at all, except for map command which was just a tangential test
\data\motd     IMPORTANT! message  of the day, ets shown on connect before login, non-existence may cause crash?
\data\spells   spell data, totally unused
\data\theme    IMPORTANT! sets mud name as well as day/month/year for time, month names etc
```
This code is very much in an unfinished state, there may be radical shifts in the inner workings
in the future. Try not to make too many assumptions about stability and backup the database if you
play with this as future code may destroy them or use a alternate approach for data storage.

This code may require at least Java 6 (1.6)

## Usage
```
Usage: java -jar MUDServer.jar

    --port  <port number> specifiy port for the server to listen on
    --debug               enable debugging messages
```

## Updates
None of the code is up yet for this because I want/need to decide on a license to use and then deal
with the fact that my code depends on some LGPL'd network (server/client) code.

## Copyright
Copyright (c) 2012 Jeremy Harton. See LICENSE.txt for further details. (license and code not up yet)