#!/bin/bash
echo "Starting!"
if [ -e /unodisk ] ; then
            echo "Run with a volume with the uno downloads:"
            echo "    sudo docker run -d --rm -P -v /unodisk/fluo-uno/:/unoShare  feduno /start.sh"
            echo "flags: -d demaon, --rm remove when done, -P expose ports, -v volume mount"
            exit 1
fi

mkdir --parents /usr/java/latest/bin
ln -s /usr/bin/java /usr/java/latest/bin/java

### This is needed by host's: ln -s /var/install  /unodisk/fluo-uno/install
mkdir /var/install
touch /var/install/exists.txt
if [ ! -e /unoShare/install/exists.txt ] ; then
            echo "Must have a symbolic link set to container's /var :"
            echo "ln -s /var/install  /unodisk/fluo-uno/install"
            exit 1
fi

### defaults to "localhost"
export UNO_HOST=$(hostname)
### Uno setup requires ssh to localhost.
/etc/init.d/ssh start

trap "echo TRAPed signal" HUP INT QUIT TERM

### Clear existing data if this was run before, then start Accumulo.
/unoShare/bin/uno setup accumulo

### use this to start with old data.
#/unoShare/bin/uno start accumulo


#echo "[hit enter key to exit] or run 'docker stop <container>'"
#read
echo "To stop, run 'docker stop <container>'"
sleep 5
tail -f /unoShare/install/logs/accumulo/*

# stop service and clean up here
echo "stopping accumulo"

/unoShare/bin/uno stop accumulo

echo "exited $0"
