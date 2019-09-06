package main;

import model.CodeWriter;
import model.FileParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VMTranslator {

    // ASSUMES: arg1 is a list of dirs file paths to draw files from,
    //          arg2 is an empty list for file paths to be stored in
    // EFFECTS: returns all files ending in ".vm" at any level of given directories
    public static void listVMFilePathsFromDir(List<String> todo, List<String> results) {
        String firstTodo;
        File[] files;
        String filePath;
        String[] pathSplit;

        if (!todo.isEmpty()) {
            firstTodo = todo.get(0);
            todo.remove(0);
            files = new File(firstTodo).listFiles();

            for (File file : files) {
                filePath = file.getPath();
                pathSplit = filePath.split("\\.");

                if (file.isDirectory()) {
                    todo.add(filePath);
                    listVMFilePathsFromDir(todo, results);
                } else {
                    if (pathSplit.length > 1 && pathSplit[1].equals("vm")) {
                        results.add(filePath);
                    }
                }
            }
        }
    }

    public static void translateFile(String filePath) throws IOException {
        FileParser p = new FileParser();
        CodeWriter cw = new CodeWriter();

        File f = new File(filePath);
        String className = f.getName().split("\\.")[0];
        List<String> assembly = cw.makeLoAssembly(p.parseFiletoVMCode(f), className);
        cw.addEndLoop(assembly);

        p.writeLoStoFile(assembly, filePath.split("\\.")[0] + ".asm");
    }

    public static void translateDir(String dirPath, List<String> filePaths) throws IOException {
        FileParser p = new FileParser();
        CodeWriter cw = new CodeWriter();
        File f;
        String className;
        List<String> assembly = new ArrayList<>();

        for (String filePath : filePaths) {
            f = new File(filePath);
            className = f.getName().split("\\.")[0];
            assembly.addAll(cw.makeLoAssembly(p.parseFiletoVMCode(f), className));
        }
        cw.addEndLoop(assembly);
        
        p.writeLoStoFile(assembly,dirPath + "/" + new File(dirPath).getName() + ".asm");
    }

    public static void main(String[] args) throws IOException {

        String folder = "/Users/christianliu/Dropbox/OSSU Files/nand2tetris/projects/07/";
        List<String> projects = Arrays.asList(
                "StackArithmetic/SimpleAdd",
                "StackArithmetic/StackTest/StackTest.vm",
                "MemoryAccess/BasicTest",
                "MemoryAccess/PointerTest",
                "MemoryAccess/StaticTest");

        for (String project : projects) {

            File f = new File(folder + project);
            if (f.isDirectory()) {
                List<String> todo = new ArrayList<>();
                List<String> results = new ArrayList<>();
                todo.add(folder + project);
                listVMFilePathsFromDir(todo, results);
                translateDir(folder + project, results);
            } else {
                translateFile(folder + project);
            }
        }
    }

}
