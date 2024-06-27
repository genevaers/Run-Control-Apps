package org.genevaers.genevaio.fieldnodes;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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


import org.genevaers.genevaio.vdpfile.record.VDPRecord;

public class RootTypeFactory {
    public static RecordNode getRecordNodeForType(short recType) {
        RecordNode typeNode = new RecordNode();
        switch (recType) {
            case VDPRecord.VDP_GENERATION:
                typeNode.setName("Generation");
                break;
            case VDPRecord.VDP_FORMAT_VIEWS:
                typeNode.setName("Format_Views");
                break;
            case VDPRecord.VDP_CONTROL:
                typeNode.setName("Control_Records");
                break;
            case VDPRecord.VDP_PHYSICAL_FILE:
                typeNode.setName("Physical_Files");
                break;
            case VDPRecord.VDP_EXIT:
                typeNode.setName("Exits");
                break;
            case VDPRecord.VDP_LOGICAL_RECORD:
                typeNode.setName("Logical_Records");
                break;
            case VDPRecord.VDP_LRFIELD:
                typeNode.setName("LR_Fields");
                break;
            case VDPRecord.VDP_INDEX:
                typeNode.setName("LR_Indexes");
                break;
            case VDPRecord.VDP_LOOKUP:
            case VDPRecord.VDP_LOOKUP_OLD:
                typeNode.setName("Lookup_Paths");
                break;
            case VDPRecord.VDP_LOOKUP_TARGET_SET:
                typeNode.setName("Lookup_Target_Set");
                break;
            case VDPRecord.VDP_LOOKUP_GENMAP:
                typeNode.setName("Lookup_Generation_Map");
                break;
            case VDPRecord.VDP_EXTRACT_OUTPUT_FILE:
                typeNode.setName("Extract_Output_File");
                break;
            case VDPRecord.VDP_EXTRACT_RECORD_FILE:
                typeNode.setName("Extract_Record_File");
                break;
            case VDPRecord.VDP_VIEW:
                typeNode.setName("View_Definition");
                break;
            case VDPRecord.VDP_FORMAT_OUTPUT_FILE:
                typeNode.setName("View_Output_File");
                break;
            case VDPRecord.VDP_EXTRACT_TARGET_SET:
                typeNode.setName("Extract_Target_Set");
                break;
            case VDPRecord.VDP_COLUMN:
                typeNode.setName("Columns");
                break;
            case VDPRecord.VDP_SORT_KEY:
                typeNode.setName("Sort_Keys");
                break;
            case VDPRecord.VDP_VIEW_SOURCE:
                typeNode.setName("Sources");
                break;
            case VDPRecord.VDP_EXTRACT_FILTER:
                typeNode.setName("Extract_Filter");
                break;
            case VDPRecord.VDP_COLUMN_SOURCE:
                typeNode.setName("Column_Sources");
                break;
            case VDPRecord.VDP_COLUMN_LOGIC:
                typeNode.setName("Column_Logic");
                break;
            case VDPRecord.VDP_OUTPUT_LOGIC:
                typeNode.setName("Output_Logic");
                break;
            case VDPRecord.VDP_FORMAT_FILTER_LOGIC:
                typeNode.setName("Format_Filter");
                break;
            case VDPRecord.VDP_COLUMN_CALCULATION:
                typeNode.setName("Column_Calculation");
                break;
            case VDPRecord.VDP_FORMAT_FILTER_STACK:
                typeNode.setName("Format_Filter_Stack");
                break;
            case VDPRecord.VDP_COLUMN_CALCULATION_LT:
                typeNode.setName("Column_Calculation_Stack");
                break;
            case VDPRecord.VDP_HEADER:
                typeNode.setName("Header");
                break;
            case VDPRecord.VDP_FOOTER:
                typeNode.setName("Footer");
                break;

            case VDPRecord.VDP_FORMAT_FILTER_OLD:
            case VDPRecord.VDP_COLUMN_CALCULATION_OLD:
            case VDPRecord.VDP_EXTRACT_FILTER_OLD:
            case VDPRecord.VDP_COLUMN_LOGIC_OLD:
            case VDPRecord.VDP_COLUMN_LOGIC_LT:
            default:
        }
        return typeNode;
    }
}
