package com.example.onestopshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

/**
 * EditItemActivity allows the user to edit details of an existing item in the inventory.
 */
public class EditItemActivity extends AppCompatActivity {
    private InventoryController inventoryController;
    private PhotosController photosController;
    private TagsController tagsController;
    private EditText itemNameText;
    private EditText descriptionText;
    private EditText purchaseDateText;
    private EditText makeText;
    private EditText modelText;
    private EditText serialNumberText;
    private EditText estimatedValueText;
    private ChipGroup tagsGroup;
    private List<String> selectedTags;
    private TextView addTagBtn;
    private EditText commentsText;
    private Button btnCancel;
    private Button confirm;
    private ImageButton addPhoto;
    private ImageView itemPhoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        confirm = findViewById(R.id.btnConfirm);
        // Initialize Views
        itemNameText = findViewById(R.id.itemName);
        descriptionText = findViewById(R.id.description);
        purchaseDateText = findViewById(R.id.purchaseDate);
        makeText = findViewById(R.id.make);
        modelText = findViewById(R.id.model);
        serialNumberText = findViewById(R.id.serialnumber);
        estimatedValueText = findViewById(R.id.estimatedValue);
        tagsGroup = findViewById(R.id.tagsGroup);
        addTagBtn = findViewById(R.id.add_tag_button);
        commentsText = findViewById(R.id.comments);
        btnCancel = findViewById(R.id.btnCancel);
        addPhoto = findViewById(R.id.add_image_button);
        itemPhoto = findViewById(R.id.productImage);
        Intent intent = getIntent();
        String itemId = intent.getStringExtra("itemId");

        inventoryController = new InventoryController();
        photosController = new PhotosController(itemId);
        tagsController = new TagsController();
        inventoryController.getItemById(itemId, new InventoryController.OnItemFetchListener() {
            @Override
            public void onItemFetched(Item item) {
                displayItemDetails(item);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String itemName = itemNameText.getText().toString();
                        String description = descriptionText.getText().toString();
                        String purchaseDate = purchaseDateText.getText().toString();
                        String make = makeText.getText().toString();
                        String model = modelText.getText().toString();
                        String serialNumber = serialNumberText.getText().toString();
                        Double estimatedValue = Double.parseDouble(estimatedValueText.getText().toString());
                        String comments = commentsText.getText().toString();
                        boolean valid = validItem(new Item(itemName, description, purchaseDate, make,
                                model, estimatedValue, comments, serialNumber, selectedTags));
                        if(valid){
                            inventoryController = new InventoryController();
                            inventoryController.updateItem(itemId, new Item(itemName, description, purchaseDate, make,
                                    model, estimatedValue, comments, serialNumber, selectedTags));
                            finish();
                        }
                    }
                });
            }

            @Override
            public void onItemFetchFailed() {

            }
        });
        photosController.getDownloadUrl(new PhotosController.DownloadUrlCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                if (downloadUrl != null && !downloadUrl.isEmpty()) {
                    // Use the download URL here, for example, load it into an ImageView
                    Picasso.get().load(downloadUrl).into(itemPhoto);
                    itemPhoto.setBackgroundColor(0);
                } else {
                    // If there's no download URL leave the default image

                }
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure, for example

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(EditItemActivity.this, EditGalleryActivity.class);
                galleryIntent.putExtra("itemId", itemId);
                startActivity(galleryIntent);
            }
        });
        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagsController.fetchExistingTags(new TagsController.OnTagsFetchListener() {
                    @Override
                    public void onSuccess(List<String> existingTags) {
                        // send existing tags to dialog
                        TagDialog tagDialog = new TagDialog(EditItemActivity.this,selectedTags, existingTags, tagsGroup);
                        tagDialog.show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure
                        e.printStackTrace();
                    }
                });
            }
        });


    }
    /**
     * Displays the details of the given item in the corresponding EditText fields.
     *
     * @param item The item whose details should be displayed.
     */
    private void displayItemDetails(Item item) {
        // Update your views with the retrieved item data
        itemNameText.setText(item.getItemName());
        descriptionText.setText(item.getDescription());
        purchaseDateText.setText(item.getPurchaseDate());
        makeText.setText(item.getMake());
        modelText.setText(item.getModel());
        String serialNumberStr = "";
        if(!(item.getSerialNumber() == null || item.getSerialNumber().isEmpty())){
            serialNumberStr = item.getSerialNumber().toString();
        }
        serialNumberText.setText(serialNumberStr);
        estimatedValueText.setText(String.format("%.2f", item.getEstimatedValue()));
        String commentsStr = "";
        if(!(item.getComments() == null || item.getComments().isEmpty())){
            commentsStr = item.getComments();
        }
        commentsText.setText(commentsStr);
        //remove brackets
        selectedTags = item.getTags();
        displayTags(tagsGroup, selectedTags);
    }
    /**
     * Validates the input fields of the item.
     *
     * @return True if the item is valid; false otherwise.
     */
    public boolean validItem(Item item) {
        boolean valid = true;
        if(item.getItemName() == null || item.getItemName().isEmpty()) {
            Toast.makeText(this, "Invalid Product Name", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else if(item.getDescription() == null || item.getDescription().isEmpty()) {
            Toast.makeText(this, "Invalid Description", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else if(item.getMake() == null || item.getMake().isEmpty()) {
            Toast.makeText(this, "Invalid Make", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else if(item.getModel() == null || item.getModel().isEmpty()) {
            Toast.makeText(this, "Invalid Model", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else if(item.getEstimatedValue() < 0) {
            Toast.makeText(this, "Value must be greater than 0", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }
    private void displayTags(ChipGroup chipGroup, List<String> tags) {
        for (String tag : tags) {
            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action);
            chip.setText(tag);
            chip.setClickable(false);
            chip.setCloseIconVisible(true);
            chipGroup.addView(chip);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chipGroup.removeView(chip);
                    selectedTags.remove(tag);
                }
            });
        }
    }
}