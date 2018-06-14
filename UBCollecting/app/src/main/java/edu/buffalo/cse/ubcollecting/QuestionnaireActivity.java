package edu.buffalo.cse.ubcollecting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.buffalo.cse.ubcollecting.data.models.QuestionLangVersion;
import edu.buffalo.cse.ubcollecting.data.models.Questionnaire;
import edu.buffalo.cse.ubcollecting.data.models.QuestionnaireContent;
import edu.buffalo.cse.ubcollecting.data.models.QuestionnaireType;
import edu.buffalo.cse.ubcollecting.data.tables.Table;
import edu.buffalo.cse.ubcollecting.ui.AddQuestionsActivity;
import edu.buffalo.cse.ubcollecting.ui.EntryOnItemSelectedListener;

import static edu.buffalo.cse.ubcollecting.data.DatabaseHelper.QUESTIONNAIRE_CONTENT_TABLE;
import static edu.buffalo.cse.ubcollecting.data.DatabaseHelper.QUESTIONNAIRE_TABLE;
import static edu.buffalo.cse.ubcollecting.data.DatabaseHelper.QUESTIONNAIRE_TYPE_TABLE;
import static edu.buffalo.cse.ubcollecting.data.DatabaseHelper.QUESTION_LANG_VERSION_TABLE;
import static edu.buffalo.cse.ubcollecting.ui.UIUtils.setDynamicHeight;
/**
 * Activity for creating a questionnaire
 */
public class QuestionnaireActivity extends EntryActivity<Questionnaire> {

    private static final String TAG = QuestionnaireActivity.class.getSimpleName().toString();
    public static final int RESULT_ADD_QUESTIONS = 1;
    public static final String EXTRA_QUESTIONNAIRE_CONTENT = "edu.buffalo.cse.ubcollecting.questionnaire_content";

    private EditText nameField;
    private EditText descriptionField;
    private Spinner typeSpinner;
    private ArrayAdapter<QuestionnaireType> typeAdapter;
    private DragSortListView questionnaireDragView;
    private QuestionnaireContentAdapter questionnaireContentAdapter;
    private Button addQuestionsButton;
    private Button updateButton;
    private Button submitButton;
    private ArrayList<QuestionnaireContent> questionnaireContent;


    void setUI(Questionnaire entry) {
        nameField.setText(entry.getName());
        descriptionField.setText(entry.getDescription());

        int i = 0;
        for (i = 0; i < typeAdapter.getCount(); i++) {
            QuestionnaireType type = typeAdapter.getItem(i);
            if (type.getId().equals(entry.getTypeId())) {
                break;
            }
        }
        typeSpinner.setSelection(0);
    }

    @Override
    void setEntryByUI() {
        QuestionnaireType type = (QuestionnaireType) typeSpinner.getSelectedItem();

        entry.setName(nameField.getText().toString());
        entry.setDescription(descriptionField.getText().toString());
        entry.setTypeId(type.getId());
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        questionnaireContent = new ArrayList<>();

        nameField = this.findViewById(R.id.questionnaire_name_field);
        descriptionField = this.findViewById(R.id.questionnaire_description_field);
        typeSpinner = this.findViewById(R.id.questionnaire_type_spinner);

        List<QuestionnaireType> types = QUESTIONNAIRE_TYPE_TABLE.getAll();
        typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                types);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setSelected(false);
        typeSpinner.setOnItemSelectedListener(new EntryOnItemSelectedListener<QuestionnaireType>());

        addQuestionsButton = findViewById(R.id.questionnaire_add_questions_button);
        addQuestionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = AddQuestionsActivity.newIntent(QuestionnaireActivity.this, entry);
                startActivityForResult(i, RESULT_ADD_QUESTIONS);
            }
        });

        updateButton = this.findViewById(R.id.questionnaire_update_button);
        updateButton.setOnClickListener(new UpdateButtonOnClickListener(QUESTIONNAIRE_TABLE));

        submitButton = this.findViewById(R.id.questionnaire_submit_button);
        submitButton.setOnClickListener(new QuestionnaireSubmitOnClickListener());

        questionnaireDragView = this.findViewById(R.id.questionnaire_question_list_view);
        questionnaireContentAdapter =
                new QuestionnaireContentAdapter(QuestionnaireActivity.this, questionnaireContent);
        questionnaireDragView.setAdapter(questionnaireContentAdapter);
        questionnaireDragView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from > to) {
                    int temp = from;
                    from = to;
                    to = temp;
                }
                QuestionnaireContent fromContent = questionnaireContent.get(from);
                QuestionnaireContent toContent = questionnaireContent.get(to);

                toContent.setQuestionOrder(from + 1);
                fromContent.setQuestionOrder(to + 1);
                Collections.sort(questionnaireContent);

                questionnaireContentAdapter.notifyDataSetChanged();
            }
        });

        if (getIntent().getFlags() == Table.FLAG_EDIT_ENTRY) {
            entry = getEntry(getIntent());
            setUI(entry);
            updateButton.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);
        } else {
            entry = new Questionnaire();
            updateButton.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == RESULT_ADD_QUESTIONS) {
            ArrayList<QuestionnaireContent> serializableObject =
                    (ArrayList<QuestionnaireContent>) data.getSerializableExtra(EXTRA_QUESTIONNAIRE_CONTENT);

            questionnaireContent.addAll(serializableObject);

//            questionnaireContent =  serializableObject;
//            questionnaireContentAdapter.clear();
//            for (int i = 0; i < questionnaireContent.size(); i++) {
//                questionnaireContentAdapter.insert(questionnaireContent.get(i),i);
//            }
            questionnaireContentAdapter.notifyDataSetChanged();

            // Added to increase size of the list since it doesn't seem to be scrollable.
            setDynamicHeight(questionnaireDragView);

            if (questionnaireContent.size() > 0) {
                questionnaireDragView.setVisibility(View.VISIBLE);
            } else {
                questionnaireDragView.setVisibility(View.GONE);
            }
        }
    }

    protected boolean validateEntry() {
        boolean valid = true;

        if (nameField.getText().toString().trim().isEmpty()) {
            nameField.setError("This field is required");
            valid = false;
        }
        if (typeSpinner.getSelectedItem() == null) {
            nameField.setError("This field is required");
            valid = false;
        }

        if (!valid) {
            Toast.makeText(this, "Please Fill in All Required Fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private class QuestionnaireSubmitOnClickListener extends SubmitButtonOnClickListener {
        public QuestionnaireSubmitOnClickListener() {
            super(QUESTIONNAIRE_TABLE);
        }

        @Override
        public void onClick(View view) {
            setEntryByUI();
            if (validateEntry()) {
                table.insert(entry);
                setEntryResult(entry);
                for (QuestionnaireContent content : questionnaireContent) {
                    QUESTIONNAIRE_CONTENT_TABLE.insert(content);
                }
                finish();
            }
        }
    }

    private class QuestionnaireContentAdapter extends ArrayAdapter<QuestionnaireContent> {
        public QuestionnaireContentAdapter(Context context, ArrayList<QuestionnaireContent> questionnaireContent) {
            super(context, 0, questionnaireContent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            QuestionnaireContent content = questionnaireContent.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.numbered_list_item_view, parent, false);
            }
            TextView numberView = convertView.findViewById(R.id.numbered_list_item_number_view);

//            numberView.setText(Integer.toString(content.getQuestionOrder()));
            numberView.setText(Integer.toString(position+1));
            content.setQuestionOrder(position+1);

            TextView textView = convertView.findViewById(R.id.numbered_list_item_text_view);
            QuestionLangVersion question = QUESTION_LANG_VERSION_TABLE.getQuestionTextInEnglish(content.getQuestionId());
            textView.setText(question.getIdentifier());


            return convertView;
        }
    }
}
