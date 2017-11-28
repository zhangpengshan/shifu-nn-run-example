#!/bin/bash
# 
# Copyright (c) 2016 PayPal Corporation. All rights reserved.

# --------------------------------------------------------------------------------------------------------------
#
#    Run shifu nn model
#
#    Author: pengzhang@paypal.com
#
# --------------------------------------------------------------------------------------------------------------
# --------------------------------------------------------------------------------------------------------------
#
# Variables
#
# --------------------------------------------------------------------------------------------------------------
export BIN_DIR="$( cd "$( dirname $0)" && pwd )"
# --------------------------------------------------------------------------------------------------------------
#
# Shell flow
#
# --------------------------------------------------------------------------------------------------------------
cd ${BIN_DIR}

echo $BIN_DIR

# check java
if [ -f "/x/opt/java7/bin/java" ]; then
    JAVA=/x/opt/java7/bin/java
else
    if [ "${JAVA_HOME}X" != "X" ]; then
        JAVA=${JAVA_HOME}/bin/java
    else
        java -version
        _status=$?
        if [ "${_status}X" != "0X" ]; then
            echo "ERROR: JAVA_HOME is not set, cannot run iras service with Jetty."
            exit 1
        else
            JAVA="java"
        fi
    fi
fi

CLASSPATH=`find ${BIN_DIR}/../lib -name "*.jar" | grep -v javadoc | xargs | sed 's/ /:/g'`

echo "NN model prediction start ..."
$JAVA -server -Xms512M -Xmx512M \
    -classpath ${CLASSPATH}\
    ml.shifu.nn.IndeMain $BIN_DIR/../model/binary.nn \
    $BIN_DIR/../data/nnbinary.csv

echo "INFO: DONE."