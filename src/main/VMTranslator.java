package main;

import model.CodeWriter;
import model.FileParser;
import model.VMCode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VMTranslator {

    public static void main(String[] args) throws IOException {
        FileParser p = new FileParser();
        CodeWriter cw = new CodeWriter();

        String folder = "/Users/christianliu/Dropbox/OSSU Files/nand2tetris/projects/07/";
        List<String> fileNames = Arrays.asList(
                "StackArithmetic/SimpleAdd/SimpleAdd",
                "StackArithmetic/StackTest/StackTest",
                "MemoryAccess/BasicTest/BasicTest",
                "MemoryAccess/PointerTest/PointerTest",
                "MemoryAccess/StaticTest/StaticTest");

        File f;
        List<VMCode> loc;
        List<String> assembly;

        for (String name : fileNames) {
            f = new File(folder + name + ".vm");
            loc = p.parseFiletoVMCode(f);
            assembly = cw.makeLoAssembly(loc, name.split("/")[2]);
            cw.addEndLoop(assembly);
            p.writeLoStoFile(assembly, "../" + name + ".asm");
        }
    }

}
