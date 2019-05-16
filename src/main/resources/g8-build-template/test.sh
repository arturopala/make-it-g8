#!/usr/bin/env bash

if [[ -f ./build.sbt ]] && [[ -d ./src/main/g8 ]]; then

    export TEMPLATE=`pwd | xargs basename`
    echo ${TEMPLATE}
    mkdir -p $testTargetFolder$
    cd $testTargetFolder$
    sudo rm -r $testTemplateName$
    g8 file://../../../${TEMPLATE} $g8CommandLineArgs$ -o $testTemplateName$ "$@"
    cd $testTemplateName$
    $beforeTest$
    $testCommand$

else

    echo "WARNING: run the script ./test.sh in the template root folder"
    exit -1

fi