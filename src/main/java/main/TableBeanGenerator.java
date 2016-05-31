package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by huangzhiqiang on 16/5/31.
 */
public class TableBeanGenerator {

    public static void main(String[] args) throws SQLException, IOException {
        String tableName = "product";

        Connection connection = DBUtil.getConnection(DBUtil.url, DBUtil.user, DBUtil.password);
        ResultSet rs = DBUtil.checkTable(connection, tableName);

        String javaName = camelFirst(underscoreToCamelCase(tableName));
        String fileName = "src/main/java/" + javaName + ".java";
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        FileWriter fw = new FileWriter(file);
        fw.write("\n");

        _import(rs, fw);

        fw.write("\n");
        _insert(tableName, rs, fw);

        fw.write("\n");
        fw.write("public class " + javaName + " {\n");
        fw.write("\n");

        _field(rs, fw);

        fw.write("\n");
        _method(rs, fw);

        fw.write("}");
        fw.flush();
        fw.close();
    }

    private static void _insert(String tableName, ResultSet rs, FileWriter fw) throws IOException, SQLException {
        fw.write("//insert into " + tableName);
        fw.write("(");
        rs.beforeFirst();
        boolean first = true;
        while (rs.next()) {
            if (first) {
                first = false;
            } else {
                fw.write(", ");
            }
            fw.write(rs.getString(DBConstants.COLUMN_NAME));
        }
        fw.write(")");
        fw.write("\n");

        rs.beforeFirst();
        first = true;
        fw.write("//values(");
        while (rs.next()) {
            if (first) {
                first = false;
            } else {
                fw.write(", ");
            }
            fw.write("#{" + underscoreToCamelCase(rs.getString(DBConstants.COLUMN_NAME)) + "}");
        }
        fw.write(")");
    }

    private static void _method(ResultSet rs, FileWriter fw) throws SQLException, IOException {
        rs.beforeFirst();
        while (rs.next()) {
            String fieldName = underscoreToCamelCase(rs.getString(DBConstants.COLUMN_NAME));
            String typeName = typeToName(rs.getInt(DBConstants.DATA_TYPE), fieldName);
            String getOrIs = typeName.equals("Boolean") ? "is" : "get";
            fw.write("\tpublic " + typeName + " " + getOrIs + camelFirst(fieldName) + "(" + "){\n");
            fw.write("\t\treturn " + fieldName + ";\n");
            fw.write("\t}\n");

            fw.write("\tpublic void set" + camelFirst(fieldName) + "(" + typeName + " " + fieldName + "){\n");
            fw.write("\t\tthis." + fieldName + " = " + fieldName + ";\n");
            fw.write("\t}\n");

            fw.write("\n");
        }
    }

    private static void _field(ResultSet rs, FileWriter fw) throws SQLException, IOException {
        rs.beforeFirst();
        while (rs.next()) {
            int type = rs.getInt(DBConstants.DATA_TYPE);
            String fieldName = underscoreToCamelCase(rs.getString(DBConstants.COLUMN_NAME));
            fw.write("\t");
            fw.write("private ");
            fw.write(typeToName(type, fieldName));
            fw.write(" " + fieldName);
            fw.write(";\n");
        }
    }

    private static void _import(ResultSet rs, FileWriter fw) throws SQLException, IOException {
        boolean containDate = false;
        rs.beforeFirst();
        while (rs.next()) {
            String fieldName = underscoreToCamelCase(rs.getString(DBConstants.COLUMN_NAME));
            if (typeToName(rs.getInt(DBConstants.DATA_TYPE), fieldName).equals("Date")) {
                containDate = true;
            }
        }
        if (containDate) {
            fw.write("import java.util.Date;\n");
        }
    }

    public static String typeToName(int type, String fieldName) {
        switch (type) {
            case Types.BIT:
                return "Boolean";
            case Types.TINYINT:
                return "Byte";
            case Types.SMALLINT:
                return "Short";
            case Types.INTEGER:
                return "Integer";
            case Types.BIGINT:
                return "Long";

            case Types.FLOAT:
                return "Float";
            case Types.DOUBLE:
                return "Double";

            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
                return "String";

            case Types.DECIMAL:
                return "BigDecimal";

            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIME:
                return "Date";
        }
        throw new RuntimeException("Unknown:" + fieldName + "(" + type + ")");
    }

    public static String underscoreToCamelCase(String name) {
        name = name.toLowerCase();
        if (name.contains("_")) {
            StringBuilder sb = new StringBuilder();
            byte[] bytes = name.getBytes();
            boolean camel = false;
            for (byte b : bytes) {
                if (b == '_') {
                    camel = true;
                    continue;
                }
                if (camel && b >= 'a' && b <= 'z') {
                    b = (byte) ('A' + b - 'a');
                    camel = false;
                }
                sb.append((char) b);
            }
            return sb.toString();
        }
        return name;
    }

    public static String camelFirst(String str) {
        if (str.charAt(0) >= 'a' && str.charAt(0) <= 'z') {
            return (char) (str.charAt(0) - 'a' + 'A') + str.substring(1);
        }
        return str;
    }
}

