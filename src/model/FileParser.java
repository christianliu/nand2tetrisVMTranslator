package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static model.VMCodeType.*;

public class FileParser {

    public FileParser() {}

    public List<VMCode> parseFiletoVMCode(File file) {
        return makeLoVMCode(readFileNoWhitespace(file));
    }


    // ASSUMES: in-line comments marked by "//" string
    // EFFECTS: returns list of lines with comments, whitespace, quotation chars removed
    private List<String> readFileNoWhitespace(File file) {
        List<String> fileNoWhitespace = new ArrayList<>();
        String line;

        try {
            Scanner input = new Scanner(file);
            while (input.hasNextLine()) {
                line = input.nextLine();
                line = processLine(line);
                if (!line.isEmpty()) { fileNoWhitespace.add(line); }
            }
            input.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        return fileNoWhitespace;
    }

    private String processLine(String s) {
        s = s.split("//")[0]; // remove any line comments
        s = s.replaceAll("\"|\'", ""); // remove quotations
        s = s.trim(); // remove any whitespace before and after command
        return s;
    }


    // EFFECTS: converts list of strings into list of VMcode objects
    private List<VMCode> makeLoVMCode(List<String> los) {
        List<VMCode> code = new ArrayList<>();
        for (String line : los) { code.add(makeVMCode(line)); }
        return code;
    }

    // ASSUMES: VM code written as "command stringArg intArg"
    // MODIFIES: this
    // EFFECTS: separates received codeString into command and two args
    private VMCode makeVMCode(String line) {
        String[] parts = line.split(" ");
        VMCodeType type = matchType(parts[0]);
        if (type == C_ARITHMETIC) { // put primitive function in 1st arg
            return new VMCode(line, type, parts[0], 0);
        } else if (type == C_LABEL || type == C_GOTO || type == C_IF) {
            return new VMCode(line, type, parts[1], 0);
        } else if (type == C_RETURN) {
            return new VMCode(line, type, "", 0);
        } else {
            return new VMCode(line, type, parts[1], Integer.parseInt(parts[2]));
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
            case "label":
                type = C_LABEL;
                break;
            case "goto":
                type = C_GOTO;
                break;
            case "if-goto":
                type = C_IF;
                break;
            case "function":
                type = C_FUNCTION;
                break;
            case "call":
                type = C_CALL;
                break;
            case "return":
                type = C_RETURN;
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
