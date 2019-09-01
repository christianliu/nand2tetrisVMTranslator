package main;

import model.FileParser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VMTranslator {

    public static void main(String[] args) throws IOException {
        String folder = "/Users/christianliu/Dropbox/OSSU Files/nand2tetris/projects/07/";
        List<String> fileNames = Arrays.asList("StackArithmetic/SimpleAdd/SimpleAdd",
                "StackArithmetic/StackTest/StackTest", "MemoryAccess/BasicTest/BasicTest",
                "MemoryAccess/PointerTest/PointerTest", "MemoryAccess/StaticTest/StaticTest");

        File f;
        FileParser p;
        for (String name : fileNames) {
            f = new File(folder + name + ".vm");
            p = new FileParser(f, name.split("/")[2]);
            p.writeAssemblytoFile("../" + name + ".asm");
        }
    }
}
