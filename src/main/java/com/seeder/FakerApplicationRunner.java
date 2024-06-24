package com.seeder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seeder.FakerApplicationService.BLACK_LIST;

public class FakerApplicationRunner {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Scanner scannerInt = new Scanner(System.in);
    private static final Pattern validFieldNamePattern = Pattern.compile("^[a-zA-Z_]+\\d*");
    private static final Pattern validFileNamePattern = Pattern.compile("^([a-zA-Z_]+[0-9]*)$");
    private static final List<FieldType> fields = Collections.synchronizedList(new ArrayList<>());
    private static final int fieldsCount;

    static {
        fields.add(null);
        fields.addAll(Arrays.asList(FieldType.values()));
        fieldsCount = fields.size();
    }


    public static void main(String[] args) {
        var fakerDataGeneratorService = new FakerApplicationService();
        var builder = FakerApplicationGenerateRequest.builder();
        builder.fileName(getValidatedFileName());
        builder.fileType(getValidFileType());
        builder.count(getValidRowCount());
        Set<Field> dataFields = new HashSet<>();
        var choice = "";
        while ( !choice.startsWith("s") ) {
            String fieldName = getValidatedFieldName();
            showFieldTypes();
            FieldType fieldType = getValidatedFieldType();
            int min = 0;
            int max = 0;
            if ( BLACK_LIST.contains(fieldType) ) {
                System.out.print(fieldName + " min value = ");
                min = scannerInt.nextInt();
                System.out.print(fieldName + " max value = ");
                max = scannerInt.nextInt();
            }
            var field = new Field(fieldName, fieldType, min, max);
            dataFields.add(field);
            System.out.println(String.join("", dataFields.stream().map(Field :: toString).toList()));
            System.out.println("Add Field -> y(es)");
            System.out.println("Stop Adding Fields -> s(top)");
            choice = scanner.nextLine();
        }
        builder.fields(dataFields);
        var response = fakerDataGeneratorService.processRequest(builder.build());
        System.out.println(response);
    }

    private static int getValidRowCount() {
        System.out.print("Enter Rows Count : ");
        try {
            int rowCount = Integer.parseInt(scanner.nextLine());
            if ( rowCount < 0 || rowCount > 1_000_000 )
                throw new InputMismatchException();
            return rowCount;
        } catch (InputMismatchException e) {
            System.out.println("Row Count is Invalid");
            return getValidRowCount();
        }
    }

    private static FileType getValidFileType() {
        System.out.print("Enter File Type (JSON, CSV, SQL) : ");
        return FileType.findByName(scanner.nextLine());
    }

    private static String getValidatedFileName() {
        System.out.print("Enter File Name : ");
        String nextLine = scanner.nextLine();
        Matcher matcher = validFileNamePattern.matcher(nextLine);
        if ( !matcher.matches() ) {
            System.out.printf("File Name Is Not Valid : '%s'%n", nextLine);
            return getValidatedFileName();
        }
        return nextLine;
    }

    private static FieldType getValidatedFieldType() {
        System.out.printf("\nEnter Field Type ID(1-%d) : ", fieldsCount);
        String nextLine = scanner.nextLine();
        try {
            var fieldTypeIndex = Integer.parseInt(nextLine);
            if ( fieldTypeIndex < 1 || fieldTypeIndex > fieldsCount )
                throw new InputMismatchException();
            return fields.get(fieldTypeIndex);
        } catch (InputMismatchException e) {
            System.out.printf("Input must be a number and must be between[1-%d%n]", fieldsCount);
            return getValidatedFieldType();
        }
    }

    private static String getValidatedFieldName() {
        System.out.print("Enter Field Name : ");
        var fieldName = scanner.nextLine();
        Matcher matcher = validFieldNamePattern.matcher(fieldName);
        if ( !matcher.matches() ) {
            System.out.println("Invalid Field Name");
            return getValidatedFieldName();
        }
        return fieldName;
    }

    private static void showFieldTypes() {
        int i = 1;
        for ( FieldType fieldType : FieldType.values() ) {
            System.out.printf("%2d.%-20s", i, fieldType);
            if ( i % 2 == 0 )
                System.out.println();
            i++;
        }
    }
}