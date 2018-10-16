package comp5216.sydney.edu.au.unichat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter(List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView senderImage, receiverImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            senderImage=(ImageView)itemView.findViewById(R.id.sender_message_image);
            receiverImage =(ImageView)itemView.findViewById(R.id.receiver_message_image);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserID =  messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text")){
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setVisibility(View.INVISIBLE);
            holder.senderImage.setVisibility(View.INVISIBLE);
            holder.receiverImage.setVisibility(View.INVISIBLE);
            holder.senderImage.getLayoutParams().height = 0;
            holder.receiverImage.getLayoutParams().height = 0;

            if(fromUserID.equals(messageSenderId)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage());
            }else{
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage());
            }
        } else{
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setVisibility(View.INVISIBLE);
            holder.senderImage.setVisibility(View.INVISIBLE);
            holder.receiverImage.setVisibility(View.INVISIBLE);
            holder.senderImage.getLayoutParams().height = WRAP_CONTENT;
            holder.receiverImage.getLayoutParams().height = WRAP_CONTENT;
            holder.senderImage.setMaxHeight(750);
            holder.receiverImage.setMaxHeight(750);

            String imageID=messages.getImageID();

            if(fromUserID.equals(messageSenderId)){
                holder.senderImage.setVisibility(View.VISIBLE);

                File imgFile = new  File(android.os.Environment.getExternalStorageDirectory().getPath()+"/Unichat/images/"+imageID+".jpg");
                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    holder.senderImage.setImageBitmap(myBitmap);
                }else{
                    Picasso.get().load(messages.getImage()).into(holder.senderImage);
                }


            }else{
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverImage.setVisibility(View.VISIBLE);

                File imgFile = new  File(android.os.Environment.getExternalStorageDirectory().getPath()+"/Unichat/images/"+imageID+".jpg");
                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    holder.receiverImage.setImageBitmap(myBitmap);
                }else{
                    Picasso.get().load(messages.getImage()).into(holder.receiverImage);
                }


            }
        }


    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

}