#!/usr/bin/env bash                                                                               
# Copyright Contributors to the GenevaERS Project.                                                
#        SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation                        
#        2008                                                                                     
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
                                                                                                  
convE2A() {                                                                                       
    mv $1 $1.old                                                                                  
    iconv -f"IBM-1047" -t"ISO8859-1" $1.old > $1                                                  
    chtag -c"ISO8859-1" -t $1                                                                     
    rm $1.old                                                                                     
}                                                                                                 
                                                                                                  
cp ~/PEJAVA/PECode.java PerformanceEngine/target/generated-sources/org/genevaers/engine/extractor/
# cd PerformanceEngine/target/generated-sources/org/genevaers/engine/extractor/                   
convE2A PerformanceEngine/target/generated-sources/org/genevaers/engine/extractor/PECode.java     
mvn clean                                                                                         
mvn install -Ppe -DskipTests                                                                      
if [[ ! -z "$GERS_RCA_JAR_DIR" ]]; then                                                           
    export rev=`grep "<revision>" pom.xml | awk -F '<revision>||</revision>' '{print $2}'`;       
    echo RCA release number $rev;                                                                 
    cp PerformanceEngine/target/*-jar-with-dependencies.jar $GERS_RCA_JAR_DIR/pe-$rev.jar;        
    cd $GERS_RCA_JAR_DIR;                                                                         
    touch pe-latest.jar;                                                                          
    rm pe-latest.jar;                                                                             
    ln -s pe-$rev.jar pe-latest.jar;                                                              
fi                                                                                                