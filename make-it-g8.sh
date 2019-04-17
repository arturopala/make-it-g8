#!/usr/bin/env bash

if [ -d .makeitg8 ]; then

   cd .makeitg8
   echo "Updating make-it-g8 repo ..."
   git pull origin master

else

   mkdir .makeitg8
   echo "Cloning make-it-g8 repo ..."
   git clone https://github.com/arturopala/make-it-g8.git .makeitg8
   cd .makeitg8

fi

sbt "run $*"




