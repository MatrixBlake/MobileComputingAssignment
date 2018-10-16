package comp5216.sydney.edu.au.unichat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.internal.ValidateAccountRequestCreator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class EditPostActivity extends AppCompatActivity {

    private  Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private ImageView uploadPostImageButton;
    private  Button UpdatePostButton;
    private  EditText PostDescription;

    private  String Description;

    private StorageReference filePath,UserPostImagesRef;

    private DatabaseReference UsersRef,PostRef;

    private FirebaseAuth mAuth;
    private String downloadUrl;
    private File compressedImage;


    private String saveCurrentDate, saveCurrentTime, postRandomName,current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();


        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        UserPostImagesRef=FirebaseStorage.getInstance().getReference().child("Post Images");

        UpdatePostButton =(Button) findViewById(R.id.update_post_button);
        PostDescription=(EditText)findViewById(R.id.post_description);
        uploadPostImageButton=findViewById(R.id.edit_post_image);
        loadingBar= new ProgressDialog(this);


        mToolbar =(Toolbar)findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("update Post");


        uploadPostImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(EditPostActivity.this);
            }
        });
        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }





    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                loadingBar.setTitle("Upload Post Image");
                loadingBar.setMessage("Please wait, your post image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                try{
                    compressedImage = new Compressor(this).compressToFile(FileUtil.from(this, resultUri));
                }catch (Exception e){}
                Uri resultUri2= Uri.fromFile(new File(compressedImage.getAbsolutePath()));



                Calendar calFordDate= Calendar.getInstance();
                SimpleDateFormat currentDate=new SimpleDateFormat("yyyy-MM-dd");
                saveCurrentDate=currentDate.format(calFordDate.getTime());

                Calendar calFordTime= Calendar.getInstance();
                SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
                saveCurrentTime=currentTime.format(calFordTime.getTime());


                postRandomName=saveCurrentDate+saveCurrentTime;

                filePath = UserPostImagesRef.child(current_user_id+postRandomName+".jpg");

                filePath.putFile(resultUri2).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(EditPostActivity.this, "Profile Image uploaded successfully..", Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = uri.toString();
                                    loadingBar.dismiss();
                                    Picasso.get().load(downloadUrl).into(uploadPostImageButton);
                                    uploadPostImageButton.setEnabled(false);
                                }
                            });
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(EditPostActivity.this, "Error:"+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }

    }

    private void ValidatePostInfo() {

        Description= PostDescription.getText().toString();

        if(TextUtils.isEmpty(Description)){
            Toast.makeText(this,"Please say something...",Toast.LENGTH_SHORT).show();
        }
        else {

            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please Wait");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringPostToFirebaseStorage();
        }
    }

    private void StoringPostToFirebaseStorage()
    {

        Calendar calFordDate= Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("yyyy-MM-dd");
        saveCurrentDate=currentDate.format(calFordDate.getTime());

        Calendar calFordTime= Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calFordTime.getTime());


        postRandomName=saveCurrentDate+saveCurrentTime;

        SavingPostInformationToDataBase();


    }

    private void SavingPostInformationToDataBase() {
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){


                    String userName= dataSnapshot.child("name").getValue().toString();

                    HashMap postsMap = new HashMap();
                        postsMap.put("uid",current_user_id);
                        postsMap.put("name",userName);
                        postsMap.put("date",saveCurrentDate);
                        postsMap.put("time",saveCurrentTime);
                        postsMap.put("description",Description);
                        if(downloadUrl!=null){
                            postsMap.put("image",downloadUrl);
                        }
                        Date date = new Date();
                        postsMap.put("lastTime",-date.getTime());
                        postsMap.put("postid",current_user_id+postRandomName);



                    PostRef.child(current_user_id+postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if(task.isSuccessful()){
                                        SendUserToPostActicity();
                                        Toast.makeText(EditPostActivity.this,"New Post is update successfully",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else{
                                        Toast.makeText(EditPostActivity.this,"Error",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id= item.getItemId();
        if(id== android.R.id.home){
            SendUserToPostActicity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToPostActicity() {
        this.finish();
       /* Intent postIntent= new Intent(EditPostActivity.this,PostsActivity.class);
        startActivity(postIntent);*/
    }

}
