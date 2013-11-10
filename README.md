mud-server
==========

A MUD server written in Java. Elements of the D&D/d20 system are used to implement the 'game' aspect of the
server code with regards to things like player and item stats, etc.

## Notes

Some of the following directories need to exist before the game starts. '\' is the top directory
for all the files. The code makes an attempt to find these folders and create them while it is setting
up the game, and generates some basic data like an empty database and configuration files. That means
that if you don't download the data from here (effectively test data at some level), then you will need
to get find suitable pre-existing data or you'll need to add some manually.

**Only the IMPORTANT! directories are absolutely essential, although the others may be important in the future.**

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
play with this as future code may corrupt the data and/or utilize an alternate approach for data storage.

This code requires at least Java 7 (1.7)

## Usage

```
Usage: java -jar MUDServer.jar

    --port  <port number> specifiy port for the server to listen on
    --debug               enable debugging messages
```

## Help

For help using the code and miscellaneous documentation, see the [Wiki](https://github.com/jnharton/mud-server/wiki) for this project.

## Updates

see the [Wiki](https://github.com/jnharton/mud-server/wiki)

## Copyright
Copyright (c) 2012 Jeremy Harton. See LICENSE.txt for further details.

The license given basically applies to all files in the source (MUDServer/src) directory.
