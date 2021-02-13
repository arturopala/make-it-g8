#!/usr/bin/env bash

if [[ -d ./src/main/g8 ]]; then

    if ! command -v g8 &> /dev/null
    then
        echo "[ERROR] g8 command cannot be found, please install g8 following http://www.foundweekends.org/giter8/setup.html"
        exit -1
    fi

    export TEMPLATE=`pwd | xargs basename`

    echo "Creating new project $testTargetFolder$/$testTemplateName$ from the ${TEMPLATE} template ..."
    
    mkdir -p $testTargetFolder$
    cd $testTargetFolder$
    find . -not -name .git -delete

    g8 file://../../../${TEMPLATE} $g8CommandLineArgs$ "$@"

    if [[ -d ./$testTemplateName$ ]]; then
        cd $testTemplateName$
        $beforeTest$
        $testCommand$
        echo "Done, created new project in $testTargetFolder$/$testTemplateName$"
        exit 0
    else
        echo "[ERROR] something went wrong, project has not been created in $testTargetFolder$/$testTemplateName$"
        exit -1
    fi

else

    echo "[ERROR] run the script ./test.sh in the template's root folder"
    exit -1

fi