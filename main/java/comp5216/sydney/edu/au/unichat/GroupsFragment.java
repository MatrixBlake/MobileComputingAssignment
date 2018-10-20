package comp5216.sydney.edu.au.unichat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View groupFragmentView;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();
    private ArrayList<String> list_of_group_types = new ArrayList<>();
    private ArrayList<String> list_of_group_keys = new ArrayList<>();

    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;

    private String currentID;
    ArrayList<String> groupKeys = new ArrayList<>();


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView= inflater.inflate(R.layout.fragment_groups, container, false);

        RootRef=FirebaseDatabase.getInstance().getReference();
        mAuth =FirebaseAuth.getInstance();
        currentID=mAuth.getUid();

        IntializeFields();

        RetrieveAndDisplayGroups();

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = list_of_groups.get(position);
                Intent groupChatIntent = new Intent(getContext(),GroupChatActivity.class);
                String currentGroupKey = groupKeys.get(position);
                String currentGroupType = list_of_group_types.get(position);
                groupChatIntent.putExtra("groupName",currentGroupName);
                groupChatIntent.putExtra("groupKey",currentGroupKey);
                groupChatIntent.putExtra("groupType",currentGroupType);
                startActivity(groupChatIntent);
            }
        });

        return groupFragmentView;
    }


    private void IntializeFields() {
        list_view=(ListView)groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }


    private void RetrieveAndDisplayGroups() {
        RootRef.child("Users").child(currentID).child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                list_of_groups.clear();
                list_of_group_types.clear();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    groupKeys.add(data.getKey());
                    String thisGroupNameLong = data.getValue().toString();
                    String thisGroupName = thisGroupNameLong.substring(1,thisGroupNameLong.length()-8);
                    String thisGroupType = thisGroupNameLong.substring(thisGroupNameLong.length()-7,thisGroupNameLong.length()-1);
                    if(thisGroupType.equals("course")){
                        list_of_groups.add("Course: "+thisGroupName.toUpperCase());
                    }else{
                        list_of_groups.add(thisGroupName);
                    }

                    list_of_group_types.add(thisGroupType);
                }


                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
