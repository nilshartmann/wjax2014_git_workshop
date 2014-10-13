package de.e2.mergetool;


public class MergeToolMain {

    public static void main(String[] args) {
        if(args.length!=4) {
            System.err.println("Wrong Parameters! Usage: workDir repoUrl srcBranch destBranch");
            System.exit(-1);
        }

        String workDir=args[0];
        String repoUrl=args[1];
        String branchFrom=args[2];
        String branchTo=args[3];

        try(MergeTool mergeTool=new MergeTool(workDir,repoUrl,branchFrom,branchTo)) {
            mergeTool.run();
        } catch (Exception e) {
            System.err.println("Exception during run: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}
