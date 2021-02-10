#!/usr/bin/env bash

if [[ -d ./src/main/g8 ]]; then

    if ! command -v g8 &> /dev/null
    then
        echo "[ERROR] g8 command cannot be found, please install g8 following http://www.foundweekends.org/giter8/setup.html"
        exit -1
    fi

    export TEMPLATE=`pwd | xargs basename`
    echo "Processing ${TEMPLATE} ..."
    mkdir -p $testTargetFolder$
    cd $testTargetFolder$
    sudo rm -r $testTemplateName$
    g8 file://../../../${TEMPLATE} $g8CommandLineArgs$ "$@"
    cd $testTemplateName$
    $beforeTest$
    $testCommand$

    echo "Done."
    exit 0

else

    echo "[ERROR] run the script ./test.sh in the template's root folder"
    exit -1

fi