package com.seeder;

public enum FileType {
    JSON,
    CSV,
    SQL;
    public static FileType findByName(String name) {
        for ( FileType fileType : values() )
            if ( fileType.name().equalsIgnoreCase(name) )
                return fileType;
        return FileType.JSON;
    }
}
