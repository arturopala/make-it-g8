#!/usr/bin/env bash

if [ -f ./build.sbt ] && [ -d ./src/main/g8 ]; then

   mkdir -p target
   cd target
   if [ -d ./make-it-g8 ] && [ -d ./make-it-g8/.git ] ; then
        cd make-it-g8
        git pull origin master
   else
        rm -r make-it-g8
        git clone https://github.com/arturopala/make-it-g8.git
        cd make-it-g8
   fi

   $makeItG8CommandLine$

else

    echo "WARNING: run the script ./update-g8.sh in the template root folder"
    exit -1

fi
