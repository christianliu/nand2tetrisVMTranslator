package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Codewriter {
    List<String> assembly;

    public Codewriter() {
    }

    // ASSUMES: arg2 in bounds of arg1 range (ex: temp only allows between 0..7)
    public List<String> makePush(String arg1, int arg2, String className) {
        assembly = new ArrayList<>();

        // get base address of memory stack if relevant
        String base = "";
        switch (arg1) {
            case "local":
                base = "@LCL";
                break;
            case "argument":
                base = "@ARG";
                break;
            case "this":
                base = "@THIS";
                break;
            case "that":
                base = "@THAT";
                break;
            case "temp":
                base = "@5";
                break;
        }
        // get value
        switch (arg1) {
            case "local":
            case "argument":
            case "this":
            case "that":
                assembly.addAll(Arrays.asList("@" + arg2, "D=A", base, "A=D+M"));
                break;
            case "temp":
                assembly.addAll(Arrays.asList("@" + arg2, "D=A", base, "A=D+A"));
                break;
            case "constant":
                assembly.add("@" + arg2);
                break;
            case "static":
                assembly.add("@" + className + "." + arg2);
                break;
            case "pointer":
                assembly.add((arg2 == 0) ? "@THIS" : "@THAT");
                break;
        }

        // put value in D
        if (arg1.equals("constant")) {
            assembly.add("D=A");
        } else {
            assembly.add("D=M");
        }
        // put D into address SP has stored
        assembly.addAll(Arrays.asList("@SP", "A=M", "M=D"));
        // increment SP
        assembly.addAll(Arrays.asList("@SP", "M=M+1"));

        return assembly;
    }

    // ASSUMES: arg1 is not "constant", @addrX symbol not used
    public List<String> makePop(String arg1, int arg2, String className, int i) {
        assembly = new ArrayList<>();

        // get base address of memory stack if relevant
        String base = "";
        switch (arg1) {
            case "local":
                base = "@LCL";
                break;
            case "argument":
                base = "@ARG";
                break;
            case "this":
                base = "@THIS";
                break;
            case "that":
                base = "@THAT";
                break;
            case "temp":
                base = "@5";
                break;
        }
        // get address to pop to, and store in temp variable @addr
        switch (arg1) {
            case "local":
            case "argument":
            case "this":
            case "that":
                assembly.addAll(Arrays.asList("@" + arg2, "D=A", base, "D=D+M"));
                assembly.addAll(Arrays.asList("@addr" + i, "M=D"));
                break;
            case "temp":
                assembly.addAll(Arrays.asList("@" + arg2, "D=A", base, "D=D+A"));
                assembly.addAll(Arrays.asList("@addr" + i, "M=D"));
                break;
        }

        // set D to first value of stack
        assembly.addAll(Arrays.asList("@SP", "A=M-1", "D=M"));
        // get address to store value in
        switch (arg1) {
            case "static":
                assembly.add("@" + className + "." + arg2);
                break;
            case "pointer":
                assembly.add((arg2 == 0) ? "@THIS" : "@THAT");
                break;
            default:
                assembly.addAll(Arrays.asList("@addr" + i, "A=M"));
        }
        // put D into selected address
        assembly.add("M=D");
        // decrement SP
        assembly.addAll(Arrays.asList("@SP", "M=M-1"));

        return assembly;
    }

    // ASSUMES: @TRUEX, @ENDX symbols not used
    public List<String> makeArithmetic(String arg1, int i) {
        assembly = new ArrayList<>();

        // get last value on stack
        assembly.addAll(Arrays.asList("@SP", "A=M-1"));
        // one arg:
        // compute in place, don't need to move pointer
        if (arg1.equals("neg")) {
            assembly.add("M=-M");
        } else if (arg1.equals("not")) {
            assembly.add("M=!M");
        }
        // two args:
        else {
            // get second to last value
            assembly.addAll(Arrays.asList("D=M", "A=A-1"));
            // compute in place
            switch(arg1) {
                case "and":
                    assembly.add("M=D&M");
                    break;
                case "or":
                    assembly.add("M=D|M");
                    break;
                case "add":
                    assembly.add("M=D+M");
                    break;
                case "sub":
                    assembly.add("M=M-D");
                    break;
                case "gt":
                case "lt":
                case "eq":
                    assembly.add("MD=M-D");
                    // jump if a comparison to set to TRUE or FALSE
                    assembly.addAll(Arrays.asList("@TRUE" + i, "D;J" + arg1.toUpperCase(),
                            "@SP", "A=M-1", "A=A-1", "M=0", "@END" + i, "0;JMP",
                            "(TRUE" + i + ")", "@SP", "A=M-1", "A=A-1", "M=-1", "(END" + i + ")"));
                    break;
            }
            // decrement SP
            assembly.addAll(Arrays.asList("@SP", "M=M-1"));
        }

        return assembly;
    }
}
