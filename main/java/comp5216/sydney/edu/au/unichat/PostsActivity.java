package comp5216.sydney.edu.au.unichat;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class PostsActivity extends AppCompatActivity {

    private ImageButton AddNewPostButton;
    private RecyclerView postlist;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;
    private  Toolbar mToolbar;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        mToolbar=(Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();

        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");

        postlist=(RecyclerView)findViewById(R.id.all_users_post_list);
        postlist.setLayoutManager(new LinearLayoutManager(this));
        /*postlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postlist.setLayoutManager(linearLayoutManager);*/


        AddNewPostButton=(ImageButton)findViewById(R.id.post_add_new_post_button);
        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToEditPostActivity();

            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Posts>options=
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(PostsRef.orderByChild("lastTime"),Posts.class)
                        .build();
        FirebaseRecyclerAdapter<Posts,PostViewHolder> adapter=
                new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Posts model) {

                        holder.name.setText(model.getName());
                        holder.date.setText(model.getDate()+" "+model.getTime());
                        holder.description.setText(model.getDescription());
                        if(model.getImage()!=null){
                            Picasso.get().load(model.getImage()).into(holder.image);
                        }

                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(PostsActivity.this,R.style.AlertDialog);
                                builder.setTitle("Report");
                                builder.setMessage("Do you want to report this post?");
                                builder.setNegativeButton("Report", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(PostsActivity.this, "Reported!", Toast.LENGTH_SHORT).show();
                                        PostsRef.child(model.getPostid()).child("reported").setValue("1");
                                    }
                                });

                                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();

                                return true;
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SendUserToCommentPostActivity(model.getPostid());
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_post,parent,false);
                        // View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_post,parent,false);
                        PostViewHolder viewHolder = new PostViewHolder(view);
                        return viewHolder;
                    }};
        postlist.setAdapter(adapter);
        adapter.startListening();

    }

    private void SendUserToCommentPostActivity(String id) {
        Intent commentPostIntent = new Intent(PostsActivity.this,CommentPostActivity.class);
        commentPostIntent.putExtra("PostID",id);
        startActivity(commentPostIntent);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{


        TextView name, date, time, description;
        ImageView image;


        public PostViewHolder(View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.post_user_name);
            date=itemView.findViewById(R.id.post_date);
            // time=itemView.findViewById(R.id.post_time);
            description=itemView.findViewById(R.id.post_descriptions);
            image=itemView.findViewById(R.id.post_display_image);


            name.setText("IIIII");
        }
    }

    private void SendUserToEditPostActivity() {

        Intent addNewPostIntent= new Intent(PostsActivity.this,EditPostActivity.class);
        startActivity(addNewPostIntent);
    }
}