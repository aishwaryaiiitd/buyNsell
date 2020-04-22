package com.example.buysellapp.ui.my_ads;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.buysellapp.ui.multipleuse.ShowAdFragment;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.example.buysellapp.R;
import com.example.buysellapp.models.Item;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class MyAdsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private FirestoreRecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_myads, container, false);
        FloatingActionButton floatingActionButton = root.findViewById(R.id.floatingActionButton);
        RecyclerView recyclerView = root.findViewById(R.id.my_ads_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://quickr-app.appspot.com");

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment();
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String email = currentUser.getEmail();

        Query query = db.collection("Item").whereEqualTo("owner_email",email)
                .orderBy("title", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, new SnapshotParser<Item>() {
                    @NonNull
                    @Override
                    public Item parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Item item = snapshot.toObject(Item.class);
                        assert item != null;
                        if(item.getOwner_email().equals(email)){
                            return item;
                        }
                        return null;
                    }
                })
                .build();

        adapter = new FirestoreRecyclerAdapter<Item, ItemHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull final ItemHolder holder, int position, @NonNull final Item model) {



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

                        if (mF instanceof MyAdsContainerFragment) {
                            Log.d("hgjhg","inside mf");
                            assert getFragmentManager() != null;
                            getFragmentManager().beginTransaction()
                                    .replace((getParentFragment()).requireView().findViewById(R.id.my_ads_fragment_container).getId()
                                            , new ShowAdFragment(model,id))
                                    .commit();
                        }

                    }
                });
            }

            @NonNull
            @Override
            public ItemHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                Log.d("yo","oncreateviewholder");
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.list_item, group, false);
                return new ItemHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("error", Objects.requireNonNull(e.getMessage()));
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


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

        if (mF instanceof MyAdsContainerFragment) {
            Log.d("hgjhg","inside mf");
            assert getFragmentManager() != null;
            getFragmentManager().beginTransaction()
                    .replace((getParentFragment()).requireView().findViewById(R.id.my_ads_fragment_container).getId()
                            , new SellFragment())
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
