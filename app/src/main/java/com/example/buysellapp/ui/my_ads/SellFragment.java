package com.example.buysellapp.ui.my_ads;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buysellapp.R;
import com.example.buysellapp.models.Item;
import com.example.buysellapp.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;


//Work Left: 1. Compressing image before uploading. 2.Submit button 3.bottom nav view over keyboard

public class SellFragment extends Fragment{
    private ImageView item_image;
    private EditText title,description,price;
    private TextView category;
    private int image_set=0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private Uri FilePathUri;
    private StorageReference storageReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_sell, container, false);

        item_image = view.findViewById(R.id.item_image);
        title = view.findViewById(R.id.title);
        description = view.findViewById(R.id.description);
        category = view.findViewById(R.id.category);
        price = view.findViewById(R.id.price);


        Button submit_button = view.findViewById(R.id.submit_button);
        Button back_button = view.findViewById(R.id.sell_back_button);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment();
            }
        });

        mAuth=FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://quickr-app.appspot.com");

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidation()){
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    assert currentUser != null;
                    final String email = currentUser.getEmail();
                    final int item_price = Integer.parseInt(price.getText().toString());
                    final String image_id= UUID.randomUUID().toString();
                    UploadImageFileToFirebaseStorage(image_id);

                    assert email != null;
                    db.collection("User").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user=documentSnapshot.toObject(User.class);
                            assert user != null;
                            String name=user.getUsername();
                            Item item= new Item(email,name,title.getText().toString(),
                                    description.getText().toString(),category.getText().toString(),image_id,item_price);
                            db.collection("Item")
                                    .add(item)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            changeFragment();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                        }
                    });






                }
            }
        });

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("Choose Category");

                final String[] choices = getResources().getStringArray(R.array.categories);
                builder.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        category.setText(choices[which]);
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        item_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = { "Camera", "Gallery","Cancel" };

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Load Image from:");

                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (options[item].equals("Camera")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);

                        } else if (options[item].equals("Gallery")) {
                            String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                            if (!EasyPermissions.hasPermissions(requireContext(), galleryPermissions)) {
                                EasyPermissions.requestPermissions(requireActivity(), "Access for storage", 101, galleryPermissions);
                            }
                            if(EasyPermissions.hasPermissions(getContext(), galleryPermissions)) {
                                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto, 1);
                            }
                            else{
                                Toast.makeText(getContext(),"Please give permission!!",Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }


                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });




        return view;
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {

                        Bitmap selectedImage = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                        assert selectedImage != null;
                        FilePathUri=getImageUri(requireContext(),selectedImage);
                        item_image.setImageBitmap(selectedImage);
                        image_set=1;
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        FilePathUri=selectedImage;
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = requireActivity().getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                item_image.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                image_set=1;
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }

    private boolean checkValidation(){
        if(title.getText().toString().equals("")){
            Toast.makeText(getContext(),"Failed!! Enter title",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(description.getText().toString().equals("")){
            Toast.makeText(getContext(),"Failed!! Enter description",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(category.getText().toString().equals(getString(R.string.category))){
            Toast.makeText(getContext(),"Failed!! Enter Category",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(price.getText().toString().equals("")){
            Toast.makeText(getContext(),"Failed!! Enter Price",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(image_set==0){
            Toast.makeText(getContext(),"Failed!! Set Image",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void UploadImageFileToFirebaseStorage(String id) {
        if (FilePathUri != null) {
            String storagePath = "images/";
            StorageReference storageReferenceChild = storageReference.child(storagePath + id);
            Log.d("djg",storageReference.getPath()+storageReferenceChild.getPath());
            storageReferenceChild.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                           Log.d(TAG, "Image Uploaded Successfully ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG, Objects.requireNonNull(exception.getMessage()));
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        }
        else {

            Log.d(TAG, "Please Select Image or Add Image Name");

        }
    }

    private void changeFragment(){
        Fragment mF = getParentFragment();
        if (mF instanceof MyAdsContainerFragment) {
            Log.d("hgjhg","inside mf");
            assert getFragmentManager() != null;
            getFragmentManager().beginTransaction()
                    .replace((getParentFragment()).requireView().findViewById(R.id.my_ads_fragment_container).getId()
                            , new MyAdsFragment())
                    .commit();
        }
    }
}