package de.metanome.algorithms.dcfinder.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RelationalInput {

    private BufferedReader reader;
    public int numberOfColumns;
    public String relationName;
    public String[] columnNames;
    public List<String> next = new ArrayList<>();
    public String filePath;

    public RelationalInput(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
        columnNames = reader.readLine().split(",");
        numberOfColumns = columnNames.length;
        relationName = file.getName();
        filePath = file.getPath();
    }

    public RelationalInput(String fp) {
        File file = new File(fp);
        try {
            reader = new BufferedReader(new FileReader(file));
            columnNames = reader.readLine().split(",");
        } catch (Exception e) {
            System.out.println(e);
        }
        numberOfColumns = columnNames.length;
        relationName = file.getName();
        filePath = file.getPath();
    }

    public boolean hasNext() {
        String[] a = new String[numberOfColumns];
        String line;
        next.clear();
        try {
            if ((line = reader.readLine()) == null) return false;
            a = line.split(",", -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.addAll(next, a);
        return next.get(0) != null;
    }

    public List<String> next() {
        // TODO Auto-generated method stub
        return next;
    }

    public int numberOfColumns() {
        // TODO Auto-generated method stub
        return numberOfColumns;
    }

    public String relationName() {
        // TODO Auto-generated method stub
        return relationName;
    }

    public String[] columnNames() {
        // TODO Auto-generated method stub
        return columnNames;
    }

}