package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeWriter {
    String fnName;

    public CodeWriter() { fnName = ""; }

    // ASSUMES: className syntax follows syntax required of naming variables in Hack assembly
    // MODIFIES: adds assembly translation to each VMCode object
    // EFFECTS: returns combined assembly code generated from list of VMCodes
    public List<String> makeLoAssembly(List<VMCode> loc, String className) {
        List<String> assembly = new ArrayList<>();
        int i = 0;
        for (VMCode code : loc) {
            makeAssembly(code, className, i);
            assembly.addAll(code.getAssembly());
            i++;
        }
        return assembly;
    }

    // MODIFIES: this
    // EFFECTS: gets assembly code based on code type and args
    private void makeAssembly(VMCode c, String className, int i) {
        String codeString = c.getCodeString();
        VMCodeType type = c.getType();
        String arg1 = c.getArg1();
        int arg2 = c.getArg2();

        List<String> assembly = new ArrayList<>();
        assembly.add("// " + codeString); // add comment of what VMCode is translated
        switch (type) {
            case C_PUSH:
                assembly.addAll(makePush(arg1, arg2, className));
                break;
            case C_POP:
                assembly.addAll(makePop(arg1, arg2, className, i));
                break;
            case C_ARITHMETIC:
                assembly.addAll(makeArithmetic(arg1, className, i));
                break;
            case C_LABEL:
                assembly.addAll(makeLabel(arg1, className, fnName));
                break;
            case C_GOTO:
                assembly.addAll(makeGoto(arg1, fnName));
                break;
            case C_IF:
                assembly.addAll(makeIf(arg1, fnName));
                break;
            case C_FUNCTION:
                fnName = arg1;
                assembly.addAll(makeFunction(arg1, arg2, className, i));
                break;
            case C_CALL:
                assembly.addAll(makeCall(arg1, arg2, className, i));
                break;
            case C_RETURN:
//                if (!lastFnName.isEmpty()) { lastFnName.remove(lastFnName.size() - 1); }
                assembly.addAll(makeReturn(className, i));
                break;
        }
        c.setAssembly(assembly);
    }


    // ASSUMES: arg2 in bounds of arg1 range (ex: temp only allows between 0..7)
    // EFFECTS: generates assembly code for "push arg1 arg2"
    private List<String> makePush(String arg1, int arg2, String className) {
        List<String> assembly = new ArrayList<>();

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
    private List<String> makePop(String arg1, int arg2, String className, int i) {
        List<String> assembly = new ArrayList<>();

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
                assembly.addAll(Arrays.asList("@addr" + className + i, "M=D"));
                break;
            case "temp":
                assembly.addAll(Arrays.asList("@" + arg2, "D=A", base, "D=D+A"));
                assembly.addAll(Arrays.asList("@addr" + className + i, "M=D"));
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
                assembly.addAll(Arrays.asList("@addr" + className + i, "A=M"));
        }
        // put D into selected address
        assembly.add("M=D");
        // decrement SP
        assembly.addAll(Arrays.asList("@SP", "M=M-1"));

        return assembly;
    }

    // ASSUMES: @TRUEX, @ENDX symbols not used
    private List<String> makeArithmetic(String arg1, String className, int i) {
        List<String> assembly = new ArrayList<>();

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
                    assembly.addAll(Arrays.asList("@TRUE" + className + i, "D;J" + arg1.toUpperCase(),
                            "@SP", "A=M-1", "A=A-1", "M=0", "@END" + className + i, "0;JMP", "(TRUE" + className + i + ")",
                            "@SP", "A=M-1", "A=A-1", "M=-1", "(END" + className + i + ")"));
                    break;
            }
            // decrement SP
            assembly.addAll(Arrays.asList("@SP", "M=M-1"));
        }

        return assembly;
    }

    // ASSUMES: label is unique in class and function, becomes Class.fn$label
    private List<String> makeLabel(String arg1, String className, String fnName) {
        List<String> assembly = new ArrayList<>();

        assembly.add("(" +  fnName + "$" + arg1 + ")"); // create label with given name

        return assembly;
    }

    // ASSUMES: label is unique in class and function, is of form Class.fn$label
    private List<String> makeGoto(String arg1, String fnName) {
        List<String> assembly = new ArrayList<>();

        assembly.addAll(Arrays.asList("@" + fnName + "$" + arg1, "0;JMP")); // jump to given label

        return assembly;
    }

    // ASSUMES: label is unique in class and function, is of form Class.fn$label
    private List<String> makeIf(String arg1, String fnName) {
        List<String> assembly = new ArrayList<>();

        assembly.addAll(Arrays.asList("@SP", "A=M-1", "D=M",  // get last value of stack
                "@SP", "M=M-1",                               // decrement SP
                "@" + fnName + "$" + arg1, "D;JNE"));         // jump if anything other than 0

        return assembly;
    }

    // ASSUMES: nVars is nonnegative, fnName is unique in class, becomes Class.fnName
    //          @INITLCLLOOPX and @initLclCountX and @INITLCLENDX are unique
    private List<String> makeFunction(String arg1, int arg2, String className, int i) {
        List<String> assembly = new ArrayList<>();

        assembly.addAll(Arrays.asList("(" + arg1 + ")",                                         // make function label to jump to
                "@initLclCount" + className + i, "M=0", "(INITLCLLOOP" + className + i + ")",   // make var storing number of local vars created, loop label to jump to
                "@initLclCount" + className + i, "D=M", "@" + arg2, "D=A-D",
                "@INITLCLEND" + className + i, "D;JEQ",                                         // if done, jump to end of loop
                "@initLclCount" + className + i, "D=M", "M=M+1", "@LCL", "A=M", "A=A+D", "M=0", // increase count of local vars, get address of next local var and set to 0
                "@SP", "M=M+1",                                                                 // increment SP
                "@INITLCLLOOP" + className + i, "0;JMP",                                        // return to start of loop
                "(INITLCLEND" + className + i + ")"));                                          // create end label to jump to end loop

        return assembly;
    }

    // ASSUMES: @RETADDRX is unique
    private List<String> makeCall(String arg1, int arg2, String className, int i) {
        List<String> assembly = new ArrayList<>();

        assembly.addAll(Arrays.asList("@RETADDR" + className + i, "D=A", "@SP", "A=M", "M=D", "@SP", "M=M+1",  // get return address and push to stack
                "@LCL", "D=M", "@SP", "A=M", "M=D", "@SP", "M=M+1",
                "@ARG", "D=M", "@SP", "A=M", "M=D", "@SP", "M=M+1",
                "@THIS", "D=M", "@SP", "A=M", "M=D", "@SP", "M=M+1",
                "@THAT", "D=M", "@SP", "A=M", "M=D", "@SP", "M=M+1",                             // push local, args, this, that
                "@SP", "D=M", "@" + arg2, "D=D-A", "@5", "D=D-A", "@ARG", "M=D",                 // reposition ARG to SP-5-nargs
                "@SP", "D=M", "@LCL", "M=D",                                                     // set LCL = SP
                "@" + arg1, "0;JMP", "(RETADDR" + className + i + ")"));                         // jump to function and make return label to jump back to

        return assembly;
    }

    // ASSUMES: @endFrameX and @retAddrVarX unique
    private List<String> makeReturn(String className, int i) {
        List<String> assembly = new ArrayList<>();

        assembly.addAll(Arrays.asList("@LCL", "D=M", "@endFrame" + className + i, "M=D",  // store address of the end of the frame ?? why do we need this?
                "@5", "A=D-A", "D=M", "@retAddrVar" + className + i, "M=D",               // store return address in temp b/c could be overridden by function return if were no args
                "@SP", "A=M-1", "D=M", "@ARG", "A=M", "M=D",                              // get final value to return, put where first arg was
                "D=A+1", "@SP", "M=D",                                                    // set SP to one after where first arg was
                "@endFrame" + className + i, "AM=M-1", "D=M", "@THAT", "M=D",             // set THAT to value of one before endframe
                "@endFrame" + className + i, "AM=M-1", "D=M", "@THIS", "M=D",
                "@endFrame" + className + i, "AM=M-1", "D=M", "@ARG", "M=D",
                "@endFrame" + className + i, "AM=M-1", "D=M", "@LCL", "M=D",              // set THIS, ARG, LCL to one before THAT, etc.
                "@retAddrVar" + className + i, "A=M", "0;JMP"));                          // go to return address

        return assembly;
    }

    public List<String> makeInit() {
        List<String> assembly = new ArrayList<>();
        assembly.addAll(Arrays.asList("@256", "D=A", "@SP", "M=D")); // set SP = 256
        assembly.addAll(makeCall("Sys.init", 0, "", 0));
        return assembly;
    }

    // ASSUMES: @endloop symbol not used
    // MODIFIES: adds loop to end of assembly code to prevent NOP slide
    public void addEndLoop(List<String> assembly) {
        assembly.add("// end loop");
        assembly.addAll(Arrays.asList("(ENDLOOP)", "@ENDLOOP", "0;JMP"));
    }
}
