#!/usr/bin/env bash

if [[ -f ./build.sbt ]] && [[ -d ./src/main/g8 ]]; then

   if ! command -v git &> /dev/null
   then
     echo "[ERROR] git command cannot be found, please install git first"
     exit -1
   fi

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
   
   echo "Done."
   exit 0

else

    echo "[ERROR] run the script ./update-g8.sh in the template root folder"
    exit -1

fi
