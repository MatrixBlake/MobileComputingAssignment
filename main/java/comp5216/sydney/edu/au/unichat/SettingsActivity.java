package comp5216.sydney.edu.au.unichat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountsSettings, AddNewCourses;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID, imageID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int GalleryPick=1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;
    private StorageReference filePath;
    private String downloadUrl;
    private String retrieveProfileImage;
    private Toolbar SettingsToolBar;
    private File compressedImage;
    ListView coursesListView;
    private ArrayList<String> courseNames = new ArrayList<>();
    private ArrayAdapter<String> itemsAdapter;
    private String newImageID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef=FirebaseStorage.getInstance().getReference().child("Profile Images");

        //RootRef.child("Users").child(currentUserID).child("uid").setValue(currentUserID);


        InitializeFields();

//        userName.setVisibility(View.INVISIBLE);

        UpdateAccountsSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        AddNewCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddCourses();
                UpdateSettingsFake();
            }
        });

        RetrieveUserInfo();

        coursesListView = findViewById(R.id.setting_courses_added);
        getCourses();
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, courseNames);
        coursesListView.setAdapter(itemsAdapter);

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
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




    private void InitializeFields() {
        UpdateAccountsSettings=(Button)findViewById(R.id.update_settings_button);
        AddNewCourses=(Button)findViewById(R.id.add_course_button);
        userName=(EditText)findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);


        SettingsToolBar =(Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();
                try{
                    compressedImage = new Compressor(this).compressToFile(FileUtil.from(this, resultUri));
                }catch (Exception e){}
                Uri resultUri2= Uri.fromFile(new File(compressedImage.getAbsolutePath()));

                Date date = new Date();
                newImageID=Long.toString(date.getTime());
                filePath = UserProfileImagesRef.child(newImageID+".jpg");

                saveImage(resultUri2, newImageID);

                filePath.putFile(resultUri2).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image uploaded successfully..", Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = uri.toString();
                                    RootRef.child("Users").child(currentUserID).child("image")
                                            .setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        RootRef.child("Users").child(currentUserID).child("imageID").setValue(newImageID).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(SettingsActivity.this, "Image save in Database successfully...", Toast.LENGTH_SHORT).show();
                                                                    loadingBar.dismiss();
                                                                }
                                                            }
                                                        });

                                                    } else {
                                                        String message = task.getException().toString();
                                                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error:"+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }

    }

    private void UpdateSettingsFake() {
        String setUserName=userName.getText().toString();
        String setUserStatus=userStatus.getText().toString();
        if(downloadUrl==null){
            downloadUrl=retrieveProfileImage;
        }

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your user name first...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(this, "Please write your user status...", Toast.LENGTH_SHORT).show();
        }else if(setUserName.contains(".") || setUserName.contains("#") || setUserName.contains("$") || setUserName.contains("[") || setUserName.contains("]")){
            Toast.makeText(this, "Name can't has '.','#','$','[',']'", Toast.LENGTH_SHORT).show();
        }else{
            RootRef.child("Users").child(currentUserID).child("name").setValue(setUserName);
            RootRef.child("Users").child(currentUserID).child("status").setValue(setUserStatus);
            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl);
        }
    }

    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setUserStatus=userStatus.getText().toString();
        if(downloadUrl==null){
            downloadUrl=retrieveProfileImage;
        }

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your user name first...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please write your user status...", Toast.LENGTH_SHORT).show();
        }else if(setUserName.contains(".") || setUserName.contains("#") || setUserName.contains("$") || setUserName.contains("[") || setUserName.contains("]")){
            Toast.makeText(this, "Name can't has '.', '#', '$', '[', ']'", Toast.LENGTH_SHORT).show();
        }else{
            RootRef.child("Users").child(currentUserID).child("name").setValue(setUserName);
            RootRef.child("Users").child(currentUserID).child("status").setValue(setUserStatus);
            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl);
            SendUserToMainActivity();
        }
    }

    private void AddCourses() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Course Number: ");

        final EditText groupNameField = new EditText(SettingsActivity.this);
        groupNameField.setHint("e.g COMP5216");
        builder.setView(groupNameField);



        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String groupName = groupNameField.getText().toString().toUpperCase();
                String coursePattern = "^([A-Za-z]){4}(\\d){4}$";
                if(!groupName.matches(coursePattern)){
                    Toast.makeText(SettingsActivity.this, "Please write the right course number", Toast.LENGTH_SHORT).show();
                }else{
                    final DatabaseReference groupKeyRef = RootRef.child("CourseGroups").push();
                    RootRef.child("CourseGroups").orderByChild("groupName").equalTo(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                groupKeyRef.child("groupName").setValue(groupName);
                                RootRef.child("Users").child(currentUserID).child("groups").child(groupKeyRef.getKey()).child(groupNameField.getText().toString()).setValue("course");
                            }else{
                                for(DataSnapshot data: dataSnapshot.getChildren()){
                                    RootRef.child("Users").child(currentUserID).child("groups").child(data.getKey()).child(groupNameField.getText().toString()).setValue("course");
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }


            }
        });

        builder.show();
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")){
                            try{
                                String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                                String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                                retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                                imageID = dataSnapshot.child("imageID").getValue().toString();
                                userName.setText(retrieveUserName);
                                userStatus.setText(retrieveStatus);
                            }catch (Exception e){}



                            File imgFile = new  File(android.os.Environment.getExternalStorageDirectory().getPath()+"/Unichat/images/"+imageID+".jpg");
                            if(imgFile.exists()){
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                userProfileImage.setImageBitmap(myBitmap);
                            }else{
                                Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                            }



                        }else if(dataSnapshot.exists() && dataSnapshot.hasChild("name")){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        }else if(dataSnapshot.exists() && dataSnapshot.hasChild("image")){
                            String retrieveID = dataSnapshot.child("uid").getValue().toString();
                            retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                            String retrieveImageID = dataSnapshot.child("image").getValue().toString();

                            File imgFile = new  File(android.os.Environment.getExternalStorageDirectory().getPath()+"/Unichat/images/"+retrieveImageID+".jpg");
                            if(imgFile.exists()){
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                userProfileImage.setImageBitmap(myBitmap);
                            }else{
                                Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                            }

                        }else{
//                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void saveImage(Uri resultUri2, String imageID) {

        File folder = new File(Environment.getExternalStorageDirectory() + "/Unichat/images/");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            String sourceFilename= resultUri2.getPath();
            String destinationFilename = android.os.Environment.getExternalStorageDirectory().getPath()+"/Unichat/images/"+imageID+".jpg";

            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try {
                bis = new BufferedInputStream(new FileInputStream(sourceFilename));
                bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
                byte[] buf = new byte[1024];
                bis.read(buf);
                do {
                    bos.write(buf);
                } while(bis.read(buf) != -1);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null) bis.close();
                    if (bos != null) bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getCourses() {
        RootRef.child("Users").child(currentUserID).child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                courseNames.clear();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    String groupNameLong = data.getValue().toString();
                    String groupNameinGetCourses = groupNameLong.substring(1,groupNameLong.length()-8);
                    String groupTypeinGetCourses = groupNameLong.substring(groupNameLong.length()-7,groupNameLong.length()-1);
                    //Toast.makeText(SettingsActivity.this, groupTypeinGetCourses, Toast.LENGTH_SHORT).show();
                    if(groupTypeinGetCourses.equals("course")){
                        courseNames.add(groupNameinGetCourses.toUpperCase());
                    }

                }
                itemsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void SendUserToMainActivity() {
        Intent mainIntent =new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}