package de.metanome.algorithms.dcfinder.input;

import java.nio.charset.StandardCharsets;
import java.util.*;

import com.csvreader.CsvReader;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

public class Input {

    private final String name;
    private final int colCount;
    private final int rowCount;

    private final List<ParsedColumn<?>> parsedColumns;

    private final int[][] intInput;   // int expression of the dataset

    private final IndexProvider<String> providerS;
    private final IndexProvider<Long> providerL;
    private final IndexProvider<Double> providerD;


    public Input(RelationalInput relationalInput) {
        this(relationalInput, -1);
    }

    public Input(RelationalInput relationalInput, int rowLimit) {
        name = relationalInput.relationName();
        providerS = new IndexProvider<>();
        providerL = new IndexProvider<>();
        providerD = new IndexProvider<>();

        Column[] columns = readRelationalInputToColumns(relationalInput, rowLimit);
        colCount = columns.length;
        rowCount = colCount > 0 ? columns[0].getLineCount() : 0;

        parsedColumns = buildParsedColumns(columns);
        intInput = buildIntInput(parsedColumns);
        System.out.println(" [Input] # of Tuples: " + rowCount);
        System.out.println(" [Input] # of Attributes: " + colCount);
    }

    private Column[] readRelationalInputToColumns(RelationalInput relationalInput, int rowLimit) {
        final int columnCount = relationalInput.numberOfColumns();
        Column[] columns = new Column[columnCount];
        for (int i = 0; i < columnCount; ++i)
            columns[i] = new Column(relationalInput.relationName(), relationalInput.columnNames[i]);

        int nLine = 0;
        try {
            CsvReader csvReader = new CsvReader(relationalInput.filePath, ',', StandardCharsets.UTF_8);
            csvReader.readHeaders();    // skip the header
            while (csvReader.readRecord()) {
                String[] line = csvReader.getValues();
                for (int i = 0; i < columnCount; ++i)
                    columns[i].addLine(line[i]);

                ++nLine;
                if (rowLimit > 0 && nLine >= rowLimit)
                    break;
            }
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return columns;
    }

    private List<ParsedColumn<?>> buildParsedColumns(Column[] columns) {
        List<ParsedColumn<?>> pColumns = new ArrayList<>(colCount);

        for (int i = 0; i < colCount; i++) {
            Column c = columns[i];
            if (c.getType() == Column.Type.LONG) {
                ParsedColumn<Long> pColumn = new ParsedColumn<>(c.getName(), Long.class, i, providerL);
                pColumns.add(pColumn);
                for (int l = 0; l < c.getLineCount(); ++l)
                    pColumn.addLine(c.getLong(l));
            } else if (c.getType() == Column.Type.NUMERIC) {
                ParsedColumn<Double> pColumn = new ParsedColumn<>(c.getName(), Double.class, i, providerD);
                pColumns.add(pColumn);
                for (int l = 0; l < c.getLineCount(); ++l)
                    pColumn.addLine(c.getDouble(l));
            } else if (c.getType() == Column.Type.STRING) {
                ParsedColumn<String> pColumn = new ParsedColumn<>(c.getName(), String.class, i, providerS);
                pColumns.add(pColumn);
                for (int l = 0; l < c.getLineCount(); ++l)
                    pColumn.addLine(c.getString(l));
            }
        }

        return pColumns;
    }

    private int[][] buildIntInput(List<ParsedColumn<?>> pColumns) {
        IndexProvider.sort(providerL);
        IndexProvider.sort(providerD);

        int[][] currIntInput = new int[colCount][rowCount];

        for (int col = 0; col < colCount; col++) {
            ParsedColumn<?> pColumn = pColumns.get(col);
            for (int row = 0; row < rowCount; ++row)
                currIntInput[col][row] = pColumn.getIndexAt(row);
        }

        return currIntInput;
    }


    public int getRowCount() {
        return rowCount;
    }

    public int[][] getIntInput() {
        return intInput;
    }

    public ParsedColumn<?>[] getColumns() {
        return parsedColumns.toArray(new ParsedColumn[0]);
    }

    public List<ParsedColumn<?>> getParsedColumns() {
        return parsedColumns;
    }

    public String getName() {
        return name;
    }

}
