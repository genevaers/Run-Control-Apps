#!/usr/bin/env bash
# Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
mvn dependency:get -Dartifact=com.ibm:jzos:2.4.8 > /dev/null 2>&1
if [ $? != 0 ]; 
then
    echo "installing jars in repository for Run Control Apps"
    mvn install:install-file -Dfile=$GERS_JARS/db2jcc4.jar -DgroupId=com.ibm -DartifactId=db2jcc4 -Dversion=4 -Dpackaging=jar
    mvn install:install-file -Dfile=$GERS_JARS/db2jcc_license_cu.jar -DgroupId=com.ibm -DartifactId=db2jcc_license_cu -Dversion=4 -Dpackaging=jar
    mvn install:install-file -Dfile=$GERS_JARS/db2jcc_license_cisuz.jar -DgroupId=com.ibm -DartifactId=db2jcc_license_cisuz -Dversion=4 -Dpackaging=jar
    mvn install:install-file -Dfile=$GERS_JARS/isfjcall.jar -DgroupId=com.ibm.zos -DartifactId=sdsf -Dversion=2.4 -Dpackaging=jar
    mvn install:install-file -Dfile=$GERS_JARS/ibmjzos.jar -DgroupId=com.ibm -DartifactId=jzos -Dversion=2.4.8 -Dpackaging=jar
else
    echo "Jars in maven repository for Run Control Apps"
fi
