package org.genevaers.runcontrolgenerator.singlepassoptimiser;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.flogger.FluentLogger;

import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.ViewSource;

public class LogicGroup implements Comparable<LogicGroup> {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    int lfID;
    LogicalFile lf;

    Set<Integer> pfIds;
    Set<ViewSource> viewSources = new HashSet<>();
    List<ViewSourceWrapper> sortableViewSources = new ArrayList<>();

    public void setLf(LogicalFile lf) {
        this.lf = lf;
    }

    public void setLfID(int lfID) {
        this.lfID = lfID;
    }

    public void setPfIds(Set<Integer> pfIds) {
        this.pfIds = pfIds;
    }

    public void setViewSources(Set<ViewSource> viewSources) {
        this.viewSources = viewSources;
    }

    public void addViewSources(Set<ViewSource> vss) {
        viewSources.addAll(vss);
    }

    public Set<Integer> getPfIds() {
        return pfIds;
    }

    public int getLfID() {
        return lfID;
    }

    public void updateViewSourcesToUseNewLF() {
        viewSources.stream().forEach(vs -> vs.setSourceLFID(lfID));
    }

    @Override
    public int compareTo(LogicGroup lfin) {
        if(lf.isToken() && lfin.lf.isNotToken() ) {
            return -1;
        } else if(lf.isToken() && lfin.lf.isToken()) {
                return idCompare(lfin);
        } else if(lf.isNotToken() && lfin.lf.isToken()) {
            return 1;
        } else {
            return idCompare(lfin);
        }
    }

    private int idCompare(LogicGroup lfin) {
        if(lf.getID() == lfin.getLfID()) {
            return 0;
        } else if(lf.getID() > lfin.getLfID()) {
            return 1;
        } else {
            return -1;
        }
    }

    public void logData() {
        StringBuilder sb =  new StringBuilder();
        sb.append("----------------------------------");
        sb.append("\nLF " + lfID + "\nHas PFs ");
        lf.getPFs().stream().forEach(pf -> sb.append(pf.getComponentId() + " "));
        lf.getPFs().stream().forEach(pf -> pf.setRequired(true));
        lf.setRequired(true);
        // sb.append("\nPFs cross check ");
        // pfIds.stream().forEach(pf -> sb.append(pf + " "));
        sb.append("\nRead by View Sources\n");

        sortViewSources();
        //summary form
        Iterator<ViewSourceWrapper> vswiS = sortableViewSources.iterator();
        while(vswiS.hasNext()) {
            ViewSourceWrapper vsw = vswiS.next();
            sb.append(vsw.getViewSourceString() + " ");
        }

        boolean showViewSourceDetails = false;
        if(showViewSourceDetails) {
            sb.append("\n\nView Source Details\n");
            Iterator<ViewSourceWrapper> vswi = sortableViewSources.iterator();
            while(vswi.hasNext()) {
                ViewSourceWrapper vsw = vswi.next();
                sb.append(vsw.getLogString());
            }
        }
        logger.atFine().log("%s", sb.toString());
    }

    private void sortViewSources() {
        Iterator<ViewSource> vsi = viewSources.iterator();
        while(vsi.hasNext()) {
            ViewSource vs = vsi.next();
            sortableViewSources.add(new ViewSourceWrapper(vs));
        }
        Collections.sort(sortableViewSources);
     }

    public Object getViewSourcesByNumber() {
        return null;
    }

    public List<ViewSourceWrapper> getSortedViewSources() {
        if(sortableViewSources.size() == 0)
            sortViewSources();
        return sortableViewSources;
    }

    public void addPFIDSet(Set<Integer> pfIDset) {
        pfIds.addAll(pfIDset);
    }

    public LogicalFile getLogicalFile() {
        return lf;
    }


}
