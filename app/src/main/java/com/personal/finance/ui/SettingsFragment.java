package com.personal.finance.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.personal.finance.R;
import com.personal.finance.utils.SessionManager;

public class SettingsFragment extends Fragment {

    private SessionManager sessionManager;
    private SwitchMaterial switchTheme;
    private AutoCompleteTextView autoCompletePeriod;
    private MaterialCardView cardTheme, cardPeriod, cardCategories;
    private com.personal.finance.ui.viewmodel.FinanceViewModel financeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        // Bind Views
        switchTheme = view.findViewById(R.id.switchTheme);
        autoCompletePeriod = view.findViewById(R.id.autoCompletePeriod);
        cardTheme = view.findViewById(R.id.cardTheme);
        cardPeriod = view.findViewById(R.id.cardPeriod);

        // Apply Animations
        Animation slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_slide_up);
        cardTheme.startAnimation(slideUp);
        // Add a slight delay for the second card for a cascading effect
        Animation slideUpDelayed = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_slide_up);
        slideUpDelayed.setStartOffset(100);
        cardPeriod.startAnimation(slideUpDelayed);

        // Theme Logic
        boolean isDark = "DARK".equals(sessionManager.getTheme());
        switchTheme.setChecked(isDark);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                sessionManager.setTheme("DARK");
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                sessionManager.setTheme("LIGHT");
            }
        });

        // Allow clicking the card to toggle the switch
        cardTheme.setOnClickListener(v -> switchTheme.toggle());

        // Default Period Logic (Exposed Dropdown Menu)
        String[] periods = getResources().getStringArray(R.array.periods_array);
        // Use a standard ArrayAdapter but ensure we don't filter out items based on
        // selection
        // Use a standard ArrayAdapter but ensure we don't filter out items based on
        // selection
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                periods) {
            @NonNull
            @Override
            public android.widget.Filter getFilter() {
                return new android.widget.Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = periods;
                        results.count = periods.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };

        autoCompletePeriod.setAdapter(adapter);

        // Set current selection
        String currentDefault = sessionManager.getDefaultPeriod();
        autoCompletePeriod.setText(currentDefault, false); // false to prevent filtering

        // Force show all items when clicked
        autoCompletePeriod.setOnClickListener(v -> {
            autoCompletePeriod.showDropDown();
        });

        autoCompletePeriod.setOnItemClickListener((parent, view1, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            sessionManager.setDefaultPeriod(selected);
        });
        // Manage Categories Logic
        cardCategories = view.findViewById(R.id.cardCategories);
        financeViewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.personal.finance.ui.viewmodel.FinanceViewModel.class);

        Animation slideUpStep2 = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_slide_up);
        slideUpStep2.setStartOffset(200);
        cardCategories.startAnimation(slideUpStep2);

        cardCategories.setOnClickListener(v -> showManageCategoriesDialog());
    }

    private void showManageCategoriesDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(
                requireContext(), R.style.CustomAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_manage_categories, null);
        builder.setView(view);

        com.google.android.material.tabs.TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        android.widget.ListView listView = view.findViewById(R.id.listViewCategories);
        com.google.android.material.textfield.TextInputEditText etNewCategory = view.findViewById(R.id.etNewCategory);
        android.widget.Button btnAdd = view.findViewById(R.id.btnAddCategory);

        final String[] currentType = { "INCOME" };
        final java.util.List<com.personal.finance.data.model.Category> currentList = new java.util.ArrayList<>();
        final String email = sessionManager.getUserEmail();

        final android.widget.ArrayAdapter<com.personal.finance.data.model.Category> adapter = new android.widget.ArrayAdapter<com.personal.finance.data.model.Category>(
                requireContext(), R.layout.item_category, currentList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_category, parent, false);
                }
                com.personal.finance.data.model.Category item = getItem(position);
                android.widget.TextView tvName = convertView.findViewById(R.id.tvCategoryName);
                android.widget.ImageButton btnEdit = convertView.findViewById(R.id.btnEditCategory);
                android.widget.ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteCategory);

                if (item != null) {
                    tvName.setText(item.getName());
                    btnEdit.setOnClickListener(
                            v -> showEditCategoryDialog(item,
                                    () -> refreshCategoryList(email, currentType[0], currentList, this), currentList));
                    btnDelete.setOnClickListener(v -> {
                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(),
                                R.style.CustomAlertDialog)
                                .setTitle("Delete Category?")
                                .setMessage("Are you sure you want to delete '" + item.getName() + "'?")
                                .setPositiveButton("Delete", (d, w) -> {
                                    financeViewModel.deleteCategory(item);
                                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                                            () -> refreshCategoryList(email, currentType[0], currentList, this), 200);
                                    android.widget.Toast.makeText(requireContext(), "Category deleted",
                                            android.widget.Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });
                }
                return convertView;
            }
        };

        listView.setAdapter(adapter);

        refreshCategoryList(email, currentType[0], currentList, adapter);

        tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                currentType[0] = tab.getPosition() == 0 ? "INCOME" : "EXPENSE";
                refreshCategoryList(email, currentType[0], currentList, adapter);
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }
        });

        btnAdd.setOnClickListener(v -> {
            String name = etNewCategory.getText().toString().trim();
            if (name.isEmpty()) {
                android.widget.Toast
                        .makeText(requireContext(), "Please enter a category name", android.widget.Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            for (com.personal.finance.data.model.Category c : currentList) {
                if (c.getName().equalsIgnoreCase(name)) {
                    android.widget.Toast.makeText(requireContext(), "This category already exists!",
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            com.personal.finance.data.model.Category newCat = new com.personal.finance.data.model.Category(name,
                    currentType[0], email);
            financeViewModel.insertCategory(newCat);
            etNewCategory.setText("");
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                    () -> refreshCategoryList(email, currentType[0], currentList, adapter), 200);
            android.widget.Toast
                    .makeText(requireContext(), "Category added successfully", android.widget.Toast.LENGTH_SHORT)
                    .show();
        });

        builder.setPositiveButton("Close", null);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
    }

    private void showEditCategoryDialog(com.personal.finance.data.model.Category category, Runnable onComplete,
            java.util.List<com.personal.finance.data.model.Category> categories) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(
                requireContext(), R.style.CustomAlertDialog);
        builder.setTitle("Edit Category");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_category, null);
        com.google.android.material.textfield.TextInputEditText input = dialogView
                .findViewById(R.id.etEditCategoryName);
        input.setText(category.getName());
        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                android.widget.Toast
                        .makeText(requireContext(), "Category name cannot be empty", android.widget.Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if (newName.equalsIgnoreCase(category.getName()))
                return;

            for (com.personal.finance.data.model.Category c : categories) {
                if (c.getName().equalsIgnoreCase(newName)) {
                    android.widget.Toast.makeText(requireContext(), "This category name already exists!",
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            financeViewModel.updateCategory(category, newName);
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(onComplete, 200);
            android.widget.Toast.makeText(requireContext(), "Category updated", android.widget.Toast.LENGTH_SHORT)
                    .show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void refreshCategoryList(String email, String type,
            java.util.List<com.personal.finance.data.model.Category> currentList,
            android.widget.ArrayAdapter<?> adapter) {
        new Thread(() -> {
            java.util.List<com.personal.finance.data.model.Category> categories = financeViewModel
                    .getCategoriesByType(email, type);

            requireActivity().runOnUiThread(() -> {
                currentList.clear();
                currentList.addAll(categories);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
