package com.example.buysellapp.ui.home;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.buysellapp.R;
import com.example.buysellapp.models.Item;
import com.example.buysellapp.ui.multipleuse.ShowAdFragment;
import com.example.buysellapp.ui.my_ads.MyAdsContainerFragment;
import com.example.buysellapp.ui.my_ads.MyAdsFragment;
import com.example.buysellapp.ui.my_ads.SellFragment;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class HomeFragment extends Fragment {
    private ProgressBar progressBar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    FirestoreRecyclerAdapter adapter;
    private RecyclerView recyclerView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = root.findViewById(R.id.home_list);
        progressBar = root.findViewById(R.id.home_progress_bar);
//        final Button category_button = root.findViewById(R.id.category_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

//        category_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
//                builder.setTitle("Choose Category");
//
//                final String[] choices = getResources().getStringArray(R.array.categories_button);
//                builder.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        category_button.setText(choices[which]);
//                        Log.d("sgfjsd",choices[which]);
//                        if(choices[which].equals("All")){
//                            Query query = db.collection("Item")
//                                    .orderBy("title", Query.Direction.DESCENDING);
//
//                            addFirestoreAdapter(query);
//                        }
//                        else{
//                            Query query = db.collection("Item").whereEqualTo("category",choices[which])
//                                    .orderBy("title", Query.Direction.DESCENDING);
//
//                            addFirestoreAdapter(query);
//
//                        }
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog alert = builder.create();
//                alert.show();
//            }
//        });

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://quickr-app.appspot.com");

        Query query = db.collection("Item")
                .orderBy("title", Query.Direction.DESCENDING);

        addFirestoreAdapter(query);
        recyclerView.setAdapter(adapter);



        return root;
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        TextView title,category,price;
        ImageView imageView;

        ItemHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.list_item_title);
            category = itemView.findViewById(R.id.list_item_category);
            price = itemView.findViewById(R.id.list_item_price);
            imageView = itemView.findViewById(R.id.list_item_image);
        }
    }

    private void addFirestoreAdapter(Query query){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String email = currentUser.getEmail();
        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Item, HomeFragment.ItemHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull final HomeFragment.ItemHolder holder, int position, @NonNull final Item model) {
                progressBar.setVisibility(View.GONE);

                if (model.getOwner_email().equals(email)) {
                    RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                    param.height = 0;
                    param.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    holder.itemView.setVisibility(View.VISIBLE);
                } else {

                    holder.title.setText(model.getTitle());
                    holder.category.setText(model.getCategory());
                    holder.price.setText("Price: Rs." + model.getPrice());
                    String storagePath = "images/";
                    StorageReference storageReferenceChild = storageReference.child(storagePath + model.getItem_id());
                    storageReferenceChild.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageURL = uri.toString();
                            Glide.with(requireActivity()).load(imageURL).into(holder.imageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                            String id= snapshot.getId();
                            Log.d("yo",id);
                            Fragment mF = getParentFragment();

                            if (mF instanceof HomeContainerFragment) {
                                Log.d("hgjhg","inside mf");
                                assert getFragmentManager() != null;
                                getFragmentManager().beginTransaction()
                                        .replace((getParentFragment()).requireView().findViewById(R.id.home_fragment_container).getId()
                                                , new ShowAdFragment(model,id))
                                        .commit();
                            }
                        }
                    });
                }
            }

            @NonNull
            @Override
            public HomeFragment.ItemHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.list_item, group, false);
                return new HomeFragment.ItemHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("error", Objects.requireNonNull(e.getMessage()));
            }
        };

        adapter.notifyDataSetChanged();
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


}
