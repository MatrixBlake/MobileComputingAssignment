package comp5216.sydney.edu.au.unichat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CommentPostActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference postRef, userRef;
    private String currentID, postID;
    private TextView postUserName, postContents, postDateTime;
    private ListView commentsListView;
    private EditText newComment;
    private ImageButton sendComment;
    private ImageView postImage;
    private Toolbar mToolbar;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_comments=new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_post);

        mAuth=FirebaseAuth.getInstance();
        currentID=mAuth.getUid();

        postID=getIntent().getExtras().getString("PostID");


        postRef=FirebaseDatabase.getInstance().getReference().child("Posts").child(postID);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users").child(currentID);

        InitializeFields();


        DisplayOtherInfo();


        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list_of_comments);
        commentsListView.setAdapter(arrayAdapter);
        RetrieveAndDisplayComments();



        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newComment.getText().toString()==null){
                    Toast.makeText(CommentPostActivity.this, "Please write something", Toast.LENGTH_SHORT).show();
                }else{
                    userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Date date = new Date();
                            postRef.child("comments").child(dataSnapshot.getValue().toString()+" : "+newComment.getText().toString()).setValue("");
                            postRef.child("comments").child(dataSnapshot.getValue().toString()+" : "+newComment.getText().toString()).child("time").setValue(date.getTime());
                            newComment.setText("");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });





    }

    private void DisplayOtherInfo() {

        postRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postUserName.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postRef.child("description").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postContents.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postRef.child("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String date = dataSnapshot.getValue().toString();
                postRef.child("time").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        postDateTime.setText(date+" "+dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postRef.child("image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Picasso.get().load(dataSnapshot.getValue().toString()).into(postImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        postUserName=findViewById(R.id.comment_post_user_name);
        postContents=findViewById(R.id.comment_post_descriptions);
        postDateTime=findViewById(R.id.comment_post_date);
        commentsListView=findViewById(R.id.comment_post_comments);
        newComment=findViewById(R.id.comment_post_input_message);
        sendComment=findViewById(R.id.comment_post_send_comment);
        postImage=findViewById(R.id.comment_post_display_image);
        mToolbar=findViewById(R.id.comment_post_toolbar);
    }

    private void RetrieveAndDisplayComments() {
        postRef.child("comments").orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                list_of_comments.clear();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    list_of_comments.add(data.getKey());
                }

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
