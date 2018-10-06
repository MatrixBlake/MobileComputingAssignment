package comp5216.sydney.edu.au.unichat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountsSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();
    }

    private void InitializeFields() {
        UpdateAccountsSettings=(Button)findViewById(R.id.update_settings_button);
        userName=(EditText)findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);

    }
}
