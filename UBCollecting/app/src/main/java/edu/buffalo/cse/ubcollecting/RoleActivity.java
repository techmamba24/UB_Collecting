package edu.buffalo.cse.ubcollecting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.InputStream;
import java.util.UUID;

import edu.buffalo.cse.ubcollecting.data.models.Role;
import edu.buffalo.cse.ubcollecting.data.tables.PersonTable;
import edu.buffalo.cse.ubcollecting.data.tables.RoleTable;

public class RoleActivity extends AppCompatActivity {

    private static final String TAG = RoleActivity.class.getSimpleName().toString();

    private TextView idField;
    private EditText nameField;
    private EditText introRequiredField;
    private EditText photoRequiredField;
    private EditText onClientField;
    private Button submitButton;

    private RoleTable roleTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        idField = this.findViewById(R.id.role_id_field);
        idField.setText(UUID.randomUUID().toString());
        nameField = this.findViewById(R.id.role_name_field);
        introRequiredField = this.findViewById(R.id.role_intro_required_field);
        photoRequiredField = this.findViewById(R.id.role_photo_required_field);
        onClientField = this.findViewById(R.id.role_on_client_field);

        roleTable = new RoleTable();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Role role = new Role();
                role.setId(idField.getText().toString());
                role.setName(nameField.getText().toString());
                role.setIntroRequired(Integer.valueOf(introRequiredField.getText().toString()));
                role.setPhotoRequired(Integer.valueOf(photoRequiredField.getText().toString()));
                role.setOnClient(Integer.valueOf(onClientField.getText().toString()));

                roleTable.addRole(role);
            }
        });
    }
}