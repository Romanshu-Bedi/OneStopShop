package com.example.onestopshop;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryController manages interactions with the Firebase Firestore database for inventory-related data.
 */
public class InventoryController {

    private CollectionReference itemsRef;

    private OnInventoryUpdateListener listener;
    /**
     * Constructs a new InventoryController and sets up a listener for inventory data changes.
     */
    public InventoryController() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        itemsRef = db.collection("users").document(userId).collection("items");

        itemsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                // Handle errors
                return;
            }
            if (queryDocumentSnapshots != null) {
                ArrayList<Item> updatedData = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String itemId = doc.getString("itemId");
                    String itemName = doc.getString("itemName");
                    String purchaseDate = doc.getString("purchaseDate");
                    double estimatedValue = doc.getDouble("estimatedValue");
                    List<String> tags = (List<String>) doc.get("tags");

                    Item item = new Item(itemId, itemName, purchaseDate, estimatedValue, tags);
                    updatedData.add(item);
                }
                if (listener != null) {
                    listener.onInventoryDataChanged(updatedData);
                }
            }
        });
    }
    /**
     * Adds a new item to the inventory in Firestore.
     *
     * @param newItem The item to be added.
     */
    public void addItem(Item newItem) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("itemName", newItem.getItemName());
        itemData.put("purchaseDate", newItem.getPurchaseDate());
        itemData.put("estimatedValue", newItem.getEstimatedValue());
        itemData.put("tags", newItem.getTags());
        itemData.put("serialNumber", newItem.getSerialNumber());
        itemData.put("make", newItem.getMake());
        itemData.put("model", newItem.getModel());
        itemData.put("comments", newItem.getComments());
        itemData.put("description", newItem.getDescription());

        // Add the new item to Firestore
        itemsRef.add(itemData)
                .addOnSuccessListener(documentReference -> {
                    // Retrieve the auto-generated document ID and set it in the Item object
                    String itemId = documentReference.getId();
                    //Add field with autogenerated ID for making deletions easier
                    itemsRef.document(itemId).update("itemId", itemId);
                })
                .addOnFailureListener(e -> {
                    // Handle failure to add the item to Firestore
                });
    }
    /**
     * Adds a new item to the inventory in Firestore but has a callback for the itemId .
     *
     * @param newItem The item to be added.
     */
    public void addItem(Item newItem, ItemAddedCallback callback) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("itemName", newItem.getItemName());
        itemData.put("purchaseDate", newItem.getPurchaseDate());
        itemData.put("estimatedValue", newItem.getEstimatedValue());
        itemData.put("tags", newItem.getTags());
        itemData.put("serialNumber", newItem.getSerialNumber());
        itemData.put("make", newItem.getMake());
        itemData.put("model", newItem.getModel());
        itemData.put("comments", newItem.getComments());
        itemData.put("description", newItem.getDescription());

        // Add the new item to Firestore
        itemsRef.add(itemData)
                .addOnSuccessListener(documentReference -> {
                    // Retrieve the auto-generated document ID and set it in the Item object
                    String itemId = documentReference.getId();
                    //Add field with autogenerated ID for making deletions easier
                    itemsRef.document(itemId).update("itemId", itemId);
                    // Call the callback with the added itemId
                    callback.onItemAdded(itemId);
                })
                .addOnFailureListener(e -> {
                    // Handle failure to add the item to Firestore
                });
    }


    /**
     * Updates an existing item in the inventory in Firestore.
     *
     * @param itemId  The ID of the item to be updated.
     * @param newItem The updated item data.
     */
    public void updateItem(String itemId, Item newItem) {
        DocumentReference itemRef = itemsRef.document(itemId);
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("itemName", newItem.getItemName());
        itemData.put("purchaseDate", newItem.getPurchaseDate());
        itemData.put("estimatedValue", newItem.getEstimatedValue());
        itemData.put("tags", newItem.getTags());
        itemData.put("serialNumber", newItem.getSerialNumber());
        itemData.put("make", newItem.getMake());
        itemData.put("model", newItem.getModel());
        itemData.put("comments", newItem.getComments());
        itemData.put("description", newItem.getDescription());

        // Add the new item to Firestore
        itemRef.update(itemData)
                .addOnSuccessListener(documentReference -> {

                })
                .addOnFailureListener(e -> {
                    // Handle failure to add the item to Firestore
                });
    }

    /**
     * Deletes an item from the inventory in Firestore.
     *
     * @param itemId The ID of the item to be deleted.
     */
    public void deleteItem(String itemId) {
        itemsRef.document(itemId).delete();
    }
    public void deleteMultipleItems(ArrayList<String> itemIds) {
        for(String itemId: itemIds) {
            itemsRef.document(itemId).delete();
        }
    }
    /**
     * Fetches details of an item from the inventory by its ID.
     *
     * @param itemId   The ID of the item to be fetched.
     * @param listener Callback for handling the fetched item or failure.
     */
    public void getItemById(String itemId, final OnItemFetchListener listener) {
        itemsRef.document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Item item = documentSnapshot.toObject(Item.class);
                        listener.onItemFetched(item);
                    } else {
                        listener.onItemFetchFailed();
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onItemFetchFailed();
                });
    }
    public String getUserEmail(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();

            if (email != null) {
                return email;
            }
        } else {
            // User is not signed in
        }
        return "";
    }
    public String getUserName(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            // Use the display name and email as needed
            if (displayName != null) {
                return displayName;
            }

        } else {
            // User is not signed in

        }
        return "";
    }



    public void setListener(OnInventoryUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Interface for receiving inventory data update events.
     */
    public interface OnInventoryUpdateListener {
        void onInventoryDataChanged(ArrayList<Item> updatedData);
    }
    /**
     * Interface for fetching details of a specific item.
     */
    public interface OnItemFetchListener {
        void onItemFetched(Item item);
        void onItemFetchFailed();
    }
    public interface ItemAddedCallback {
        void onItemAdded(String addedItemId);
    }
}
