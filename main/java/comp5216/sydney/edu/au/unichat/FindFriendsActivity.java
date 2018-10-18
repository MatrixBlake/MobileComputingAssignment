package comp5216.sydney.edu.au.unichat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef;
    private Query UsersRef1;
    private Button searchButton;
    private EditText userNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        userNameInput = (EditText)findViewById(R.id.find_friends_edit_text);
        searchButton = (Button)findViewById(R.id.search_button);


        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendsRecyclerList = (RecyclerView)findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolBar=(Toolbar)findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userNameInput.getText().toString();
                if(!username.equals("")){
                    display(username);
                }

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

    protected void display(String s) {
        if (!s.equals("")) {
            UsersRef1 = UsersRef.orderByChild("name").equalTo(s);
        }else{
            UsersRef1 = UsersRef;
        }

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UsersRef1,Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());

                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};
                        final String[] imageID = {""};



                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    if(dataSnapshot.hasChild("image")) {
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        imageID[0] = dataSnapshot.child("imageID").getValue().toString();


                                        File imgFile = new File(android.os.Environment.getExternalStorageDirectory().getPath() + "/Unichat/images/" + imageID[0] + ".jpg");
                                        if (!imgFile.exists()) {
                                            Picasso.get().load(retImage[0]).into(holder.profileImage, new com.squareup.picasso.Callback() {

                                                @Override
                                                public void onSuccess() {
                                                    Picasso.get()
                                                            .load(retImage[0])
                                                            .into(picassoImageTarget(imageID[0]));
                                                }

                                                @Override
                                                public void onError(Exception e) {

                                                }
                                            });

                                        } else {
                                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                            holder.profileImage.setImageBitmap(myBitmap);
                                        }

                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id = getRef(position).getKey();

                                Intent profileIntent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id",visit_user_id);
                                startActivity(profileIntent);

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };

        FindFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }
    @Override
    protected void onStart() {
        super.onStart();
        //display("");
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView){
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);

        }
    }

    private Target picassoImageTarget(final String imageName) {
        final File directory = new File(android.os.Environment.getExternalStorageDirectory().getPath()+"/Unichat/images/");
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File myImageFile = new File(directory, imageName+".jpg"); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("image", "image saved to >>>" + myImageFile.getAbsolutePath());
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {


            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {}
            }
        };
    }
}
