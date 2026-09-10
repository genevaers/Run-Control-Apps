package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.WriteASTNode;
import org.genevaers.compilers.extract.astnodes.WriteExitNode;
import org.genevaers.compilers.extract.astnodes.WriteExtractNode;
import org.genevaers.compilers.extract.astnodes.WriteSourceNode;

/**
 * Generates the Java write statement from a WRITE AST node.
 *
 * Grammar: WRITE( [SOURCE=DATA|INPUT|VIEW] [, DEST=EXTRACT=N|FILE=…|DEFAULT] [, PROC=exit] )
 *
 * The WRITE node has optional child nodes:
 *   WRITESOURCE   — SOURCE= arg  (DATA → write data record,
 *                                 INPUT → pass-through input,
 *                                 VIEW  → write to extract file)
 *   WRITEEXTRACT  — DEST=EXTRACT=N  (extract file number)
 *   WRITEDEST     — DEST=DEFAULT
 *   WRITEFILE     — DEST=FILE=…
 *   WRITEEXIT     — PROC=exit / USEREXIT=exit
 *
 * Default (no children / no SOURCE): write the data record to the default output.
 *
 * For the Java emitter the write is always:
 *   outWriter.getRecordToFill().bytes.position(<outputLength>);
 *   outWriter.writeAndClearTheRecord();
 *
 * User-exit (PROC/USEREXIT) is not yet supported in the Java runtime — a comment
 * is emitted to mark the location.
 */
public class WriteStatementGenerator extends ExtractRecordGenerator {

    private final WriteASTNode writeNode;

    public WriteStatementGenerator(WriteASTNode node) {
        this.writeNode = node;
    }

    @Override
    public void generateCode() {
        columnRecs.add(buildWriteStatement(writeNode));
    }

    @Override
    public String getCode(ExtractBaseAST node) {
        return buildWriteStatement((WriteASTNode) node);
    }

    // -------------------------------------------------------------------------

    private String buildWriteStatement(WriteASTNode node) {
        StringBuilder sb = new StringBuilder();

        WriteSourceNode srcNode    = (WriteSourceNode)    node.getFirstNodeOfType(ASTFactory.Type.WRITESOURCE);
        WriteExtractNode extractNode = (WriteExtractNode) node.getFirstNodeOfType(ASTFactory.Type.WRITEEXTRACT);
        WriteExitNode exitNode     = (WriteExitNode)      node.getFirstNodeOfType(ASTFactory.Type.WRITEEXIT);

        // --- User-exit write (PROC= / USEREXIT=) ---
        if (exitNode != null) {
            sb.append("        // WRITE PROC/USEREXIT not supported in Java emitter — exit id: ")
              .append(exitNode.getExitID());
            String params = exitNode.getParams();
            if (params != null && !params.isEmpty()) {
                sb.append(" params: ").append(params);
            }
            sb.append("\n");
            // Still emit the default write so the record is not silently dropped
        }

        // --- Source type comment ---
        if (srcNode != null) {
            sb.append("        // WRITE SOURCE=").append(srcNode.getFunctionCode()).append("\n");
        }

        // --- Extract-file numbered write  DEST=EXTRACT=N ---
        if (extractNode != null) {
            sb.append("        // WRITE DEST=EXTRACT=").append(extractNode.getFileNumber()).append("\n");
        }

        // --- The actual write ---
        sb.append(String.format(
            "            outWriter.getRecordToFill().bytes.position(%d);\n" +
            "            outWriter.writeAndClearTheRecord();",
            outputLength));

        return sb.toString();
    }
}
