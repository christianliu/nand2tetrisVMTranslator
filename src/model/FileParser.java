package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileParser {
    private File file;
    private String className;
    private List<String> inputNoWhitespace;
    private List<VMCode> code;

    public FileParser(File file, String className) {
        this.file = file;
        this.className = className;
        makeInputNoWhitespace();
        makeCode();
        addEndLoop();
    }

    // getters
    public List<VMCode> getCode() { return code; }

    // MODIFIES: this
    // EFFECTS: converts inputNoWhitespace strings into VMcode objects
    private void makeCode() {
        code = new ArrayList<>();
        int i = 0;
        for (String line : inputNoWhitespace) {
            code.add(new VMCode(line, className, i));
            i++;
        }
    }

    // ASSUMES: @endloop symbol not used
    // MODIFIES: this
    // EFFECTS: adds loop to end of assembly code to prevent NOP slide
    private void addEndLoop() {
        code.add(new VMCode());
    }

    // ASSUMES: in-line comments marked by "//" string, no quotations used in VM commands
    // MODIFIES: this
    // EFFECTS: sets inputNoWhitespace to be input with comments and whitespace removed
    private void makeInputNoWhitespace() {
        inputNoWhitespace = new ArrayList<>();
        String line = "";

        try {
            Scanner input = new Scanner(file);
            while (input.hasNextLine()) {
                line = input.nextLine();
                line = processLine(line);
                if (!line.isEmpty()) { inputNoWhitespace.add(line); }
            }
            input.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
    }

    private String processLine(String s) {
        s = s.trim(); // remove any whitespace before and after command
        s = s.split("//")[0]; // remove any line comment
        s = s.replaceAll("\"|\'", ""); // remove quotations
        return s;
    }

    // EFFECTS: writes binary line by line to new file of given filename
    public void writeAssemblytoFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (VMCode c : code) {
            for (String s : c.getAssembly()) {
                writer.write(s);
                writer.newLine();
            }
        }
        writer.close();
    }
}
