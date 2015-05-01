package instructable.server.hirarchy;

import instructable.server.ExecutionStatus;
import instructable.server.hirarchy.fieldTypes.EmailAddress;
import instructable.server.hirarchy.fieldTypes.FieldType;
import instructable.server.hirarchy.fieldTypes.PossibleFieldType;
import instructable.server.hirarchy.fieldTypes.StringField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Amos Azaria on 15-Apr-15.
 *
 * This class is like a union of FieldType and List<FieldType>.
 * In future may add a pointer to a different instance.
 */
public class FieldHolder
{

    FieldHolder(FieldDescription fieldDescription, GenericInstance fieldParent)
    {
        this.fieldName = fieldDescription.fieldName;
        this.fieldType = fieldDescription.fieldType;
        this.isList = fieldDescription.isList;
        this.fieldParent = fieldParent;

        if (isList)
        {
            fieldList = new LinkedList<FieldType>();
        }
        else
        {
            field = createNewField(fieldType);
        }

    }

    private static FieldType createNewField(PossibleFieldType fieldType)
    {
        FieldType fieldVal = null;
        switch (fieldType)
        {
            case emailAddress:
                fieldVal = new EmailAddress();
                break;
            case multiLineString:
                fieldVal = new StringField(true);
                break;
            case singleLineString:
                fieldVal = new StringField(false);
                break;
        }
        return fieldVal;
    }

    String fieldName;
    PossibleFieldType fieldType;
    boolean isList;
    FieldType field;
    List<FieldType> fieldList;
    private GenericInstance fieldParent;

    public String getParentInstanceName()
    {
        return fieldParent.getName();
    }


    static final String fieldTypeForJson = "fieldType";
    static final String isListForJson = "isList";
    static final String fieldForJson = "field";
    static final String fieldListForJson = "fieldList";

    public void set(ExecutionStatus executionStatus, String toSet)
    {
        FieldType toSetField = createNewField(fieldType);
        toSetField.setFromString(executionStatus, toSet);
        if (executionStatus.isError())
            return;
        if (isList)
        {
            fieldList.clear();
            fieldList.add(toSetField);
        }
        else
        {
            field = toSetField;
        }
        return;
    }


    public void appendTo(ExecutionStatus executionStatus, String toAdd, boolean toEnd)
    {
        if (toAdd == null)
        {
            executionStatus.add(ExecutionStatus.RetStatus.error, "the toAdd field cannot be null");
            return;
        }

        if (isList)
        {
            FieldType toBeAdded = createNewField(fieldType);
            toBeAdded.setFromString(executionStatus, toAdd);
            if (executionStatus.isError())
                return;

            if (toEnd)
                fieldList.add(toBeAdded);
            else
                fieldList.add(0,toBeAdded);
            return;
        }
        else
        {
            field.appendTo(executionStatus, toAdd,toEnd);
            return;
        }
    }

    public boolean isEmpty()
    {
        if (isList)
        {
            return fieldList.isEmpty();
        }
        else
        {
            return field.isEmpty();
        }
    }

    /*
        should be used only for presenting a value to the user.
     */
    static public String fieldFromJSonForUser(JSONObject jsonObject)
    {
        if (jsonObject.isEmpty() || (!jsonObject.containsKey(fieldListForJson) && !jsonObject.containsKey(fieldForJson)))
            return "";
        boolean isFromList = false;
        if (jsonObject.containsKey(isListForJson) && (boolean)jsonObject.get(isListForJson) ||
                !jsonObject.containsKey(isListForJson) && jsonObject.containsKey(fieldListForJson))
            isFromList = true;
        String retVal;
        if (isFromList)
            retVal = (((JSONArray)jsonObject.get(fieldListForJson)).get(0)).toString();
        else
            retVal = jsonObject.get(fieldForJson).toString();
        return retVal;
    }

    public JSONObject getFieldVal()
    {
        JSONObject obj = new JSONObject();


        obj.put(fieldTypeForJson, fieldType.toString());
        obj.put(isListForJson, isList);
        if (isList)
        {
            JSONArray jArray = fieldList.stream().map(FieldType::asString).collect(Collectors.toCollection(() -> new JSONArray()));
            obj.put(fieldListForJson, jArray);
        }
        else
        {
            obj.put(fieldForJson, field.asString());
        }
        return obj;
    }

    public void setFromJSon(ExecutionStatus executionStatus, JSONObject jsonObject, boolean addToExisting, boolean appendToEnd)
    {
        boolean mustSetFromList = false;
        if (jsonObject.containsKey(isListForJson) && (boolean)jsonObject.get(isListForJson) ||
                !jsonObject.containsKey(isListForJson) && jsonObject.containsKey(fieldListForJson))
            mustSetFromList = true;
        if (mustSetFromList)
        {
            String[] fieldListAsString = (String[]) jsonObject.get(fieldListForJson);
            if (isList)
            {
                if (!addToExisting)
                {
                    fieldList.clear();
                }
                //if need to append to beginning, need first to reverse the array.
                if (!appendToEnd)
                    Collections.reverse(Arrays.asList(fieldListAsString));
                for (String singleField : fieldListAsString)
                {
                    appendTo(executionStatus, singleField, appendToEnd);
                }
            } else
            {
                //if input is a list (array), and this isn't a list,
                if (fieldListAsString.length >= 1)
                {
                    if (fieldListAsString.length > 1)
                    {
                        executionStatus.add(ExecutionStatus.RetStatus.warning, "taking only first item out of" + fieldListAsString.length);
                    }
                    //if it has only one item, take it,
                    if (addToExisting)
                    {
                        appendTo(executionStatus, fieldListAsString[0], appendToEnd);
                    }
                    else
                    {
                        set(executionStatus, fieldListAsString[0]);
                    }
                }
                else
                {
                    executionStatus.add(ExecutionStatus.RetStatus.warning, "list is empty");
                }

            }
        }
        else
        {
            if (addToExisting)
            {
                appendTo(executionStatus, (String) jsonObject.get(fieldForJson), appendToEnd);
            }
            else
            {
                set(executionStatus, (String) jsonObject.get(fieldForJson));
            }
        }

        return;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    @Override
    public String toString()
    {
        return  fieldName;
    }
}
