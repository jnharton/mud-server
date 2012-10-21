mud-server
==========

A basic MUD server written in Java.

Some of the following directories need to exist before the game starts. '\' is the top directory
for all the files. I am pretty sure that the current code will note create them itself, so for
now they'll need to be added manually. Only the * directories are absolutely essential, although
the others will probably be important as well in the future.

Obviously you need the program itself, so this is just excepting source and compiled .class files

* \data          IMPORTANT! the folder inside of which all game data is stored, server will likely crash without data
  \data\accounts for account files, not really used or important
  \data\backup   a folder for database backups, no system for backups besides main text db file
* \data\config   IMPORTANT! config files
* \data\help     server/game command help files, if not there game may crash when trying to find them
  \data\maps     not really important at all, except for map command which was just a tangential test
* \data\motd     IMPORTANT! message  of the day, ets shown on connect before login, non-existence may cause crash?
  \data\spells   spell data, totally unused
* \data\theme    IMPORTANT! sets mud name as well as day/month/year for time, month names etc

This code is very much in an unfinished state, there may be radical shifts in the inner workings
in the future. Try not to make too many assumptions about stability and backup the database if you
play with this as future code may destroy them or use a alternate approach for data storage.

## Usage
```
Usage: java MUDServer.jar

    --port  specifiy port for the server to listen on
    --debug enable debugging messages
```

## Updates
None of the code is up yet for this because I want/need to decide on a license to use and then deal
with the fact that my code depends on some LGPL'd network (server/client) code.