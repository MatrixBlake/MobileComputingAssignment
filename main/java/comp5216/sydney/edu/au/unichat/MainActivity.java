package comp5216.sydney.edu.au.unichat;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private String currentUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        RootRef=FirebaseDatabase.getInstance().getReference();

        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("UniChat");

        myViewPager=(ViewPager)findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter= new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    protected void onStart(){
        super.onStart();
        if(currentUser==null){
            SendUserToLoginActivity();
        }else{
//            if(!currentUser.isEmailVerified()){
//                SendUserToLoginActivity();
//            }else{
                VerifyUserExistence();
 //           }
        }
    }

    private void VerifyUserExistence() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                }else {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option){
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_settings_option){
            SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_find_friends_option){
            SendUserToFindFriendsActivity();
        }
        if(item.getItemId()==R.id.main_posts_option){
            SendUserToPostActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option){
            RequestNewGroup();
        }
        return true;
    }



    private void RequestNewGroup() {


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g mobile assignment group");
        builder.setView(groupNameField);



        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group Name", Toast.LENGTH_SHORT).show();
                }else{
                    RootRef.child("Users").child(mAuth.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                currentUserName=dataSnapshot.getValue().toString();
                                DatabaseReference groupKeyRef = RootRef.child("Groups").push();
                                groupKeyRef.child("groupName").setValue(groupName);
                                RootRef.child("Groups").child(groupKeyRef.getKey()).child("people").child(currentUserName).setValue("in");
                                RootRef.child("Users").child(currentUser.getUid()).child("groups").child(groupKeyRef.getKey()).child(groupName).setValue("normal");
                                SendUserToCreateGroupActivity(groupKeyRef.getKey(),groupName);

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void SendUserToCreateGroupActivity(String groupKey, String groupName) {
        Intent createGroupIntent = new Intent(MainActivity.this,CreateGroupActivity.class);
        createGroupIntent.putExtra("GroupKey",groupKey);
        createGroupIntent.putExtra("GroupName",groupName);
        startActivity(createGroupIntent);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent =new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToPostActivity() {
        Intent postActivity = new Intent(MainActivity.this,PostsActivity.class);
        startActivity(postActivity);
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent =new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent =new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }
}
