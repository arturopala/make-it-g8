#!/usr/bin/env bash

if [[ -f ./build.sbt ]] && [[ -d ./src/main/g8 ]]; then

   mkdir -p target
   cd target
   if [[ -d .makeitg8 ]] && [[ -d .makeitg8/.git ]] ; then
        cd .makeitg8
        git pull origin master
   else
        rm -r .makeitg8
        git clone https://github.com/arturopala/make-it-g8.git .makeitg8
        cd .makeitg8
   fi

   $makeItG8CommandLine$

else

    echo "WARNING: run the script ./update-g8.sh in the template root folder"
    exit -1

fi
