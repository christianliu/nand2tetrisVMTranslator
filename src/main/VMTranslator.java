package main;

import model.CodeWriter;
import model.FileParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

    // EFFECTS: writes los to new file of given filename
    public static void writeLoStoFile(List<String> los, String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (String s : los) {
            writer.write(s);
            writer.newLine();
        }
        writer.close();
    }

    private static void translateFile(String filePath, boolean addInit, boolean addEndLoop) throws IOException {
        FileParser p = new FileParser();
        CodeWriter cw = new CodeWriter();

        File f = new File(filePath);
        String className = f.getName().split("\\.")[0];
        List<String> assembly = (addInit ? cw.makeInit() : new ArrayList<>());
        assembly.addAll(cw.makeLoAssembly(p.parseFiletoVMCode(f), className));
        if (addEndLoop) { cw.addEndLoop(assembly); }

        writeLoStoFile(assembly, filePath.split("\\.")[0] + ".asm");
    }

    private static void translateDir(String dirPath, List<String> filePaths, boolean addInit, boolean addEndLoop) throws IOException {
        FileParser p = new FileParser();
        CodeWriter cw = new CodeWriter();
        File f;
        String className;
        List<String> assembly = (addInit ? cw.makeInit() : new ArrayList<>());

        for (String filePath : filePaths) {
            f = new File(filePath);
            className = f.getName().split("\\.")[0];
            assembly.addAll(cw.makeLoAssembly(p.parseFiletoVMCode(f), className));
        }
        if (addEndLoop) { cw.addEndLoop(assembly); }

        writeLoStoFile(assembly,dirPath + "/" + new File(dirPath).getName() + ".asm");
    }

    public static void translateVM(String path, boolean addInit, boolean addEndLoop) throws IOException {
        File f = new File(path);
        if (f.isDirectory()) {
            List<String> todo = new ArrayList<>();
            List<String> results = new ArrayList<>();
            todo.add(path);
            listVMFilePathsFromDir(todo, results);
            translateDir(path, results, addInit, addEndLoop);
        } else {
            translateFile(path, addInit, addEndLoop);
        }
    }

    public static void main(String[] args) throws IOException {

        String folder = "/Users/christianliu/Dropbox/OSSU Files/nand2tetris/projects/";
        List<String> projects = Arrays.asList(
                "08/ProgramFlow/BasicLoop",
                "08/ProgramFlow/FibonacciSeries",
                "08/FunctionCalls/SimpleFunction",
                "08/FunctionCalls/NestedCall",
                "08/FunctionCalls/FibonacciElement",
                "08/FunctionCalls/StaticsTest",
                "07/StackArithmetic/SimpleAdd",
                "07/StackArithmetic/StackTest/StackTest.vm",
                "07/MemoryAccess/BasicTest",
                "07/MemoryAccess/PointerTest",
                "07/MemoryAccess/StaticTest");

        List<Boolean> addInits = Arrays.asList(false, false, false, true, true, true, false, false, false, false, false);
        List<Boolean> addEndLoops = Arrays.asList(false, false, false, false, false, false, true, true, true, true, true);

        int i = 0;
        for (String project : projects) {
            translateVM(folder + project, addInits.get(i), addEndLoops.get(i));
            i++;
        }
    }

}
