package model;

import java.util.ArrayList;
import java.util.List;

public class VMCode {
    private String codeString;
    private VMCodeType type;
    private String arg1;
    private int arg2;
    private List<String> assembly = new ArrayList<>();

    public VMCode(String codeString, VMCodeType type, String arg1, int arg2) {
        this.codeString = codeString;
        this.type = type;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    // getters
    public String getCodeString() { return codeString; }
    public VMCodeType getType() { return type; }
    public String getArg1() { return arg1; }
    public int getArg2() { return arg2; }
    public List<String> getAssembly() { return assembly; }

    // setters
    public void setAssembly(List<String> assembly) { this.assembly = assembly; }

}
