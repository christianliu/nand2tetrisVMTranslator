package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.VMCodeType.*;

public class VMCode {
    private String codeString;
    private String className;
    private VMCodeType type;
    private String arg1;
    private int arg2;
    private int i;
    private List<String> assembly = new ArrayList<>();

    public VMCode(String codeString, String className, int i) {
        this.codeString = codeString;
        this.className = className;
        this.i = i;
        parseCodeString();
        makeAssembly();
    }

    public VMCode() {
        assembly.add("// end loop");
        assembly.addAll(Arrays.asList("(ENDLOOP)", "@ENDLOOP", "0;JMP"));
    }

    // getters
    public String getCodeString() { return codeString; }
    public List<String> getAssembly() { return assembly; }

    // MODIFIES: this
    // EFFECTS: gets assembly code based on code type and args
    private void makeAssembly() {
        assembly.add("// " + codeString); // add comment of what VMCode is translated
        Codewriter c = new Codewriter();
        switch (type) {
            case C_PUSH:
                assembly.addAll(c.makePush(arg1, arg2, className));
                break;
            case C_POP:
                assembly.addAll(c.makePop(arg1, arg2, className, i));
                break;
            case C_ARITHMETIC:
                assembly.addAll(c.makeArithmetic(arg1, i));
                break;
        }
    }

    // ASSUMES: VM code written as "command stringArg intArg"
    // MODIFIES: this
    // EFFECTS: separates received codeString into command and two args
    private void parseCodeString() {
        String[] parts = codeString.split(" ");
        type = matchType(parts[0]);
        if (type == C_ARITHMETIC) { // put primitive function in 1st arg
            arg1 = parts[0];
        } else {
            arg1 = parts[1];
            arg2 = Integer.parseInt(parts[2]);
        }
    }

    private VMCodeType matchType(String command) {
        VMCodeType type = null;
        switch (command) {
            case "push":
                type = C_PUSH;
                break;
            case "pop":
                type = C_POP;
                break;
            case "add": // match all primitive functions
            case "sub":
            case "neg":
            case "eq":
            case "gt":
            case "lt":
            case "and":
            case "or":
            case "not":
                type = C_ARITHMETIC;
                break;
        }
        return type;
    }
}
