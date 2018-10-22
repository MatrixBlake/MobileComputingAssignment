package comp5216.sydney.edu.au.unichat;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.io.File;
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
    private Toolbar toolbar;
    private Button addCommentBtn;




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
        toolbar=findViewById(R.id.comment_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Comment");

        addCommentBtn=findViewById(R.id.comment_post_comment_btn);


        DisplayOtherInfo();


        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list_of_comments);
        commentsListView.setAdapter(arrayAdapter);
        RetrieveAndDisplayComments();



        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CommentPostActivity.this,R.style.AlertDialog);
                builder.setTitle("Enter Comments: ");

                final EditText groupNameField = new EditText(CommentPostActivity.this);
                //groupNameField.setHint("");
                builder.setView(groupNameField);



                builder.setPositiveButton("Comment", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String newComment = groupNameField.getText().toString();
                        if(TextUtils.isEmpty(newComment)){
                            Toast.makeText(CommentPostActivity.this, "Please write Group Name", Toast.LENGTH_SHORT).show();
                        }else{
                            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Date date = new Date();
                                    postRef.child("comments").child(Long.toString(date.getTime())).child("content").setValue(dataSnapshot.getValue().toString()+" : "+newComment);
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
                    final String imageURL = dataSnapshot.getValue().toString();
                    postRef.child("imageID").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            File imgFile = new File(android.os.Environment.getExternalStorageDirectory().getPath()+"/Unichat/images/"+dataSnapshot.getValue().toString()+".jpg");
                            if(!imgFile.exists()){
                                Picasso.get().load(imageURL).placeholder(R.drawable.profile_image).into(postImage);
                            }else {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                postImage.setImageBitmap(myBitmap);
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

    private void InitializeFields() {
        postUserName=findViewById(R.id.comment_post_user_name);
        postContents=findViewById(R.id.comment_post_descriptions);
        postDateTime=findViewById(R.id.comment_post_date);
        commentsListView=findViewById(R.id.comment_post_comments);
        postImage=findViewById(R.id.comment_post_display_image);
        mToolbar=findViewById(R.id.comment_post_toolbar);
    }

    private void RetrieveAndDisplayComments() {
        postRef.child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                list_of_comments.clear();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    String v = data.getValue().toString();
                    list_of_comments.add(v.substring(9,v.length()-1));
                }

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }


}
