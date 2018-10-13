package comp5216.sydney.edu.au.unichat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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


    private ListView createGroupListview;
    private TextView peopleTextView;
    private ArrayAdapter<String> itemsAdapter;
    ArrayList<String> items=new ArrayList<>();
    ArrayList<String> userKeys=new ArrayList<>();
    DatabaseReference RootRef;
    FirebaseAuth mAuth;
    private String currentID;
    private String groupName;
    private Toolbar GroupToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        createGroupListview=findViewById(R.id.lstView_create_group);
        peopleTextView=findViewById(R.id.create_group_people);

        groupName = getIntent().getStringExtra("GroupName");

        RootRef=FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        currentID=mAuth.getUid();



        GroupToolBar =(Toolbar) findViewById(R.id.create_group_toolbar);
        setSupportActionBar(GroupToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Add friends to "+groupName);




        getItems();



        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        // Connect the listView and the adapter
        createGroupListview.setAdapter(itemsAdapter);

  //      setupListViewListener();






    }

    private void getItems() {

        RootRef.child("Contacts").child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    String userKey = ((DataSnapshot) iterator.next()).getKey();
                    userKeys.add(userKey);
                    RootRef.child("Users").child(userKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                final String userName = dataSnapshot.getValue().toString();
                                items.add(userName);

                                itemsAdapter = new ArrayAdapter<String>(CreateGroupActivity.this, android.R.layout.simple_list_item_1, items);
                                createGroupListview.setAdapter(itemsAdapter);

                                createGroupListview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int
                                            position, long rowId) {
                                        String userKey = userKeys.get(position);
                                        RootRef.child("Users").child(userKey).child("groups").child(groupName).child("in").setValue("0");
                                        peopleTextView.setText(items.get(position)+" has been removed from Group "+groupName);
                                        return true;
                                    }

                                });
                                createGroupListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        String userKey = userKeys.get(position);
                                        RootRef.child("Users").child(userKey).child("groups").child(groupName).child("in").setValue("1");
                                        peopleTextView.setText( items.get(position)+" has been added to Group "+groupName);

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

}
