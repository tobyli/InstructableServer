package instructable.server.hirarchy;

import instructable.server.hirarchy.fieldTypes.PossibleFieldType;

/**
 * Created by Amos Azaria on 21-Apr-15.
 */
public class FieldDescription
{
    public String fieldName;
    public PossibleFieldType fieldType;
    public boolean isList;
    public boolean mutable;

    public FieldDescription(String fieldName, PossibleFieldType fieldType, boolean isList, boolean mutable)
    {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.isList = isList;
        this.mutable = mutable;
    }
}
