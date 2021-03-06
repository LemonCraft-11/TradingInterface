package ua.leonidius.trdinterface.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A model for custom item name translations
 */
@DatabaseTable(tableName = "translations")
public class Translation {

    public Translation() {
    }

    public Translation(String itemId, String translation) {
        this.itemId = itemId;
        this.translation = translation;
    }

    @DatabaseField(generatedId = true, columnName = "record_id")
    private int recordId;

    @DatabaseField(canBeNull = false, columnName = "item_id")
    private String itemId;

    @DatabaseField(canBeNull = false, columnName = "translation")
    private String translation;

    public String getItemId() {
        return itemId;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

}
