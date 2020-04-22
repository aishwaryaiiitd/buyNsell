package com.example.buysellapp.ui.multipleuse;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.buysellapp.R;
import com.example.buysellapp.models.Comment;
import com.example.buysellapp.models.Item;
import com.example.buysellapp.models.User;
import com.example.buysellapp.ui.home.HomeContainerFragment;
import com.example.buysellapp.ui.home.HomeFragment;
import com.example.buysellapp.ui.my_ads.MyAdsContainerFragment;
import com.example.buysellapp.ui.my_ads.MyAdsFragment;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class ShowAdFragment extends Fragment {
    private Item item;
    private RecyclerView recyclerView;
    private TextView title,posted_by,email;
    private TextView price;
    private EditText comment_edit;
    private ImageButton add_comment;
    private ImageView imageView;
    private StorageReference storageReference;

//    private ProgressBar progressBar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirestoreRecyclerAdapter adapter;
    private String item_id;

    public ShowAdFragment(Item item,String item_id){
        this.item=item;
        this.item_id=item_id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_show_ad, container, false);
        recyclerView=view.findViewById(R.id.comment_recycler_view);
        title = view.findViewById(R.id.show_ads_title);
        price = view.findViewById(R.id.show_ads_price);
        TextView description = view.findViewById(R.id.show_ads_description);
        TextView category = view.findViewById(R.id.show_ads_category);
        Button back_button = view.findViewById(R.id.back_button);
        comment_edit = view.findViewById(R.id.comment_edit_text);
        add_comment = view.findViewById(R.id.add_comment_button);
        imageView = view.findViewById(R.id.show_ads_item_image);
        posted_by = view.findViewById(R.id.show_ads_posted_by);
        email = view.findViewById(R.id.show_ads_email);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment();
            }
        });
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://quickr-app.appspot.com");


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        title.setText(item.getTitle());
        price.setText("Price: Rs."+item.getPrice());
        posted_by.setText("Posted By: "+item.getOwner_name());
        email.setText("Contact Email: "+item.getOwner_email());
        category.setText(item.getCategory());
        description.setText(item.getDescription());
        String storagePath = "images/";
        StorageReference storageReferenceChild = storageReference.child(storagePath + item.getItem_id());
        storageReferenceChild.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(requireActivity()).load(imageURL).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        final String email = currentUser.getEmail();

        add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!comment_edit.getText().toString().equals("")) {

                    assert email != null;
                    db.collection("User").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user=documentSnapshot.toObject(User.class);
                            assert user != null;
                            String name=user.getUsername();
                            final Comment comment = new Comment(email,comment_edit.getText().toString(),item_id,name );
                            db.collection("Comment")
                                    .add(comment)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            comment_edit.setText(null);
                                            comment_edit.setHint(getString(R.string.add_a_public_comment));
                                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
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
        Log.d("yup",item_id);


        Query query = db.collection("Comment").whereEqualTo("item_id",item_id)
                .orderBy("email", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Comment, ShowAdFragment.CommentHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull final ShowAdFragment.CommentHolder holder, int position, @NonNull Comment model) {
//                progressBar.setVisibility(View.GONE);
                holder.username.setText(model.getName());
                holder.comment.setText(model.getComment());
            }

            @NonNull
            @Override
            public ShowAdFragment.CommentHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.comment_list_item, group, false);
                return new ShowAdFragment.CommentHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("error", Objects.requireNonNull(e.getMessage()));
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return view;


    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        TextView username,comment;

        CommentHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.list_item_username);
            comment = itemView.findViewById(R.id.list_item_comment);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void changeFragment(){
        Fragment mF = getParentFragment();

        if (mF instanceof HomeContainerFragment) {
            Log.d("hgjhg","inside mf");
            assert getFragmentManager() != null;
            getFragmentManager().beginTransaction()
                    .replace((getParentFragment()).requireView().findViewById(R.id.home_fragment_container).getId()
                            , new HomeFragment())
                    .commit();
        }

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
