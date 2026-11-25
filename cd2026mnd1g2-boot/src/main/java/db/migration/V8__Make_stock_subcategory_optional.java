package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Permite crear productos sin asociación con una subcategoría de solicitudes.
 */
public class V8__Make_stock_subcategory_optional extends BaseJavaMigration {

    /** Actualiza la nulabilidad usando la sintaxis específica del motor configurado. */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String databaseProduct = connection.getMetaData().getDatabaseProductName();
        String sql = databaseProduct.contains("HSQL")
                ? "ALTER TABLE stock ALTER COLUMN subcategory_id SET NULL"
                : "ALTER TABLE stock ALTER COLUMN subcategory_id DROP NOT NULL";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
