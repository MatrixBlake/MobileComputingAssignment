package comp5216.sydney.edu.au.unichat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreateGroupActivity extends AppCompatActivity {


    private ListView createGroupListview, peopleInGroupListview;
    private TextView peopleTextView;
    private ArrayAdapter<String> itemsAdapter,itemsAdapter2;
    ArrayList<String> items=new ArrayList<>();
    ArrayList<String> peoples=new ArrayList<>();
    ArrayList<String> userKeys=new ArrayList<>();
    DatabaseReference RootRef;
    FirebaseAuth mAuth;
    private String currentID,groupName,groupKey;
    private Toolbar GroupToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        createGroupListview=findViewById(R.id.lstView_create_group);
        peopleInGroupListview=findViewById(R.id.create_group_users_listview);

        groupName = getIntent().getStringExtra("GroupName");
        groupKey = getIntent().getStringExtra("GroupKey");

        RootRef=FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        currentID=mAuth.getUid();



        GroupToolBar =(Toolbar) findViewById(R.id.create_group_toolbar);
        setSupportActionBar(GroupToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Group: "+groupName);

        getItems();
        getPeople();



        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        createGroupListview.setAdapter(itemsAdapter);

        itemsAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peoples);
        peopleInGroupListview.setAdapter(itemsAdapter2);





    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }



    private void getItems() {

        RootRef.child("Contacts").child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userKeys.clear();

                for(DataSnapshot data : dataSnapshot.getChildren()){
                    String userKey = data.getKey();
                    userKeys.add(userKey);
                    RootRef.child("Users").child(userKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String userName = dataSnapshot.getValue().toString();
                                items.add(userName);

                                itemsAdapter = new ArrayAdapter<String>(CreateGroupActivity.this, android.R.layout.simple_list_item_1, items);
                                createGroupListview.setAdapter(itemsAdapter);

                                createGroupListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroupActivity.this, R.style.AlertDialog);
                                        builder.setTitle("Add members");
                                        final String nowUserName = items.get(position);
                                        builder.setMessage("Are you sure to add " + nowUserName + " to group " + groupName + "?");

                                        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String userKey = userKeys.get(position);
                                                RootRef.child("Users").child(userKey).child("groups").child(groupKey).child(groupName).setValue("normal");
                                                RootRef.child("Groups").child(groupKey).child("people").child(nowUserName).setValue("in");

                                                Toast.makeText(CreateGroupActivity.this, items.get(position) + " has been added to Group " + groupName, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        builder.show();
                                    }

                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getPeople() {

        RootRef.child("Groups").child(groupKey).child("people").orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                peoples.clear();

                for(DataSnapshot data : dataSnapshot.getChildren()){
                    peoples.add(data.getKey());
                }
                itemsAdapter2.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void backClick(View v){
        finish();
    }

}
