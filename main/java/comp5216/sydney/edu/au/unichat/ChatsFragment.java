package comp5216.sydney.edu.au.unichat;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.xml.sax.helpers.LocatorImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatsList;
    private DatabaseReference UsersRef, MessageRef;
    private Query ChatsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID).orderByChild("LastTime");

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        MessageRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);

        chatsList=(RecyclerView)PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return PrivateChatsView;

    }



    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatsRef,Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};
                        final String[] imageID = {""};



                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    if(dataSnapshot.hasChild("image")){
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        imageID[0] = dataSnapshot.child("imageID").getValue().toString();


                                        File imgFile = new File(android.os.Environment.getExternalStorageDirectory().getPath()+"/Unichat/images/"+imageID[0]+".jpg");
                                        if(!imgFile.exists()){
                                            Picasso.get().load(retImage[0]).into(holder.profileImage, new com.squareup.picasso.Callback(){

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

                                        }else {
                                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                            holder.profileImage.setImageBitmap(myBitmap);
                                        }
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();

                                    holder.userName.setText(retName);

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",usersIDs);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            chatIntent.putExtra("visit_image_id",imageID[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        MessageRef.child(usersIDs).orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot child: dataSnapshot.getChildren()){
                                    String retStatus = child.child("message").getValue().toString();
                                    holder.userStatus.setText("Last message: "+retStatus);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        return new ChatsViewHolder(view);
                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView userStatus, userName;


        public ChatsViewHolder(View itemView) {
            super(itemView);

            profileImage= itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);

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