#!/usr/bin/env bash

if [ -f ./build.sbt ] && [ -d ./src/main/g8 ]; then

    export TEMPLATE=`pwd | xargs basename`
    echo ${TEMPLATE}
    mkdir -p $testTarget$
    cd $testTarget$
    sudo rm -r $templateName$
    g8 file://../../../${TEMPLATE} $g8CommandLineArgs$ "$@"
    cd $templateName$
    git init
    git add .
    git commit -m start
    $testCommand$

else

    echo "WARNING: run test in the template root folder"
    exit -1

fi