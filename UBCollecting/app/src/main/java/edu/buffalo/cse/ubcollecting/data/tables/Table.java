package edu.buffalo.cse.ubcollecting.data.tables;

/**
 * Created by aamel786 on 3/24/18.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import edu.buffalo.cse.ubcollecting.data.DatabaseManager;
import edu.buffalo.cse.ubcollecting.data.models.Model;

public abstract class Table<E extends Model> {

//    STILL HAVE TO TO DO TABLE COLUMNS AND GETTERS/SETTERS SORTING TO BE SAFE

    protected static final String MODEL_PATH = "edu.buffalo.cse.ubcollecting.data.models.";

    public final String TAG = this.getClass().getSimpleName();

    public ArrayList<String> tableColumns;

    public Table() {
        tableColumns = this.getAllColumnNames();
    }

    public abstract String createTable();

    public abstract String getTableName();

    public long insert(Model model) {

        long rowId;

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();

        ArrayList<Method> getters = model.getGetters();

        for (int i = 0; i < tableColumns.size(); i++) {

            try {
                String key = tableColumns.get(i);
                Object value = getters.get(i).invoke(model, null);
                insertContent(values, key, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        rowId = db.insert(this.getTableName(), null, values);

        DatabaseManager.getInstance().closeDatabase();

        return rowId;
    }

    protected void insertContent(ContentValues values, String key, Object value) {

        if (value instanceof Integer) {
            values.put(key, (Integer) value);
        } else if (value instanceof String) {
            values.put(key, (String) value);
        }

    }

    public ArrayList<E> getAll() {

        ArrayList<E> tuples = new ArrayList<>();

        try {

            Class theClass = Class.forName(MODEL_PATH + this.getTableName());

            String selectQuery = "SELECT  * FROM " + this.getTableName();
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);
            ArrayList<Method> setters = ((E) theClass.newInstance()).getSetters();

            if (cursor.moveToFirst()) {
                do {
                    E model = (E) theClass.newInstance();

                    for (int i = 0; i < tableColumns.size(); i++) {
                        String key = tableColumns.get(i);
                        Method method = setters.get(i);
                        insertIntoObject(cursor, model, key, method);
                    }
                    tuples.add(model);
                } while (cursor.moveToNext());
            }

            cursor.close();

            DatabaseManager.getInstance().closeDatabase();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return tuples;

    }

    protected void insertIntoObject(Cursor cursor, E model, String key, Method method) {

        try {
            Class<?> ptype = method.getParameterTypes()[0];

            if (Integer.TYPE.equals(ptype)) {
                int value = cursor.getInt(cursor.getColumnIndex(key));
                method.invoke(model, value);
            } else {
                String value = cursor.getString(cursor.getColumnIndex(key));
                method.invoke(model, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<String> getAllColumnNames() {

        Field[] allFields = this.getClass().getDeclaredFields();
        ArrayList<String> allColumnNames = new ArrayList<>();

        for (int i = 0; i < allFields.length; i++) {

            if (allFields[i].getName().startsWith("KEY")) {
                try {
                    Field field = allFields[i];
                    allColumnNames.add((String) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }

        return allColumnNames;
    }
}