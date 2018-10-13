package comp5216.sydney.edu.au.unichat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder> {

    private List<GroupMessages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public GroupMessageAdapter(List<GroupMessages> userMessageList){
        this.userMessageList = userMessageList;
    }


    public class GroupMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView otherNameText, otherMessageText, myMessageText;


        public GroupMessageViewHolder(View itemView) {
            super(itemView);

            otherNameText = (TextView) itemView.findViewById(R.id.group_chat_other_name);
            otherMessageText = (TextView) itemView.findViewById(R.id.group_receiver_message_text);
            myMessageText = (TextView) itemView.findViewById(R.id.group_sender_message_text);
        }
    }


    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_messages_layout,parent,false);

        mAuth = FirebaseAuth.getInstance();

        return new GroupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupMessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        GroupMessages messages = userMessageList.get(position);

        String fromUserID =  messages.getFromID();
        String fromUserName = messages.getFromName();
        String messageText = messages.getMessage();




        holder.otherNameText.setVisibility(View.INVISIBLE);
        holder.otherMessageText.setVisibility(View.INVISIBLE);
        holder.myMessageText.setVisibility(View.INVISIBLE);


        if(fromUserID.equals(messageSenderId)){
            holder.myMessageText.setVisibility(View.VISIBLE);
            holder.myMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
            holder.myMessageText.setTextColor(Color.BLACK);
            holder.myMessageText.setText(messageText);
        }else{
            holder.otherNameText.setVisibility(View.VISIBLE);
            holder.otherMessageText.setVisibility(View.VISIBLE);

            holder.otherNameText.setText(fromUserName);
            holder.otherMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
            holder.otherMessageText.setTextColor(Color.BLACK);
            holder.otherMessageText.setText(messageText);

        }


    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

}
