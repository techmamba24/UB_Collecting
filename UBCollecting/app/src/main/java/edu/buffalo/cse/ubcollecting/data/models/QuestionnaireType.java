package edu.buffalo.cse.ubcollecting.data.models;

/**
 * Created by aamel786 on 2/17/18.
 */

public class QuestionnaireType extends Model {

    private static final String TAG = QuestionnaireType.class.getSimpleName().toString();

    public String name;

    public String getIdentifier() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
