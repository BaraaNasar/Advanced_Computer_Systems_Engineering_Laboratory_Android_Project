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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line,
                periods);

        autoCompletePeriod.setAdapter(adapter);

        // Set current selection
        String currentDefault = sessionManager.getDefaultPeriod();
        autoCompletePeriod.setText(currentDefault, false); // false to prevent filtering

        // Force show all items when clicked, clearing any filter
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
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_manage_categories, null);
        builder.setView(view);

        com.google.android.material.tabs.TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        android.widget.ListView listView = view.findViewById(R.id.listViewCategories);
        com.google.android.material.textfield.TextInputEditText etNewCategory = view.findViewById(R.id.etNewCategory);
        android.widget.Button btnAdd = view.findViewById(R.id.btnAddCategory);

        final String[] currentType = { "INCOME" }; // Mutable wrapper
        final android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1, new java.util.ArrayList<>());
        listView.setAdapter(adapter);

        String email = sessionManager.getUserEmail();

        // Function to refresh list
        Runnable refreshList = () -> {
            financeViewModel.getCategoriesByType(email, currentType[0]).observe(getViewLifecycleOwner(), categories -> {
                adapter.clear();
                for (com.personal.finance.data.model.Category c : categories) {
                    adapter.add(c.name);
                }
                adapter.notifyDataSetChanged();

                // Handle deletion on click
                listView.setOnItemClickListener((parent, v, position, id) -> {
                    com.personal.finance.data.model.Category toDelete = categories.get(position);
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Delete Category")
                            .setMessage("Delete " + toDelete.name + "?")
                            .setPositiveButton("Yes", (d, w) -> financeViewModel.deleteCategory(toDelete))
                            .setNegativeButton("No", null)
                            .show();
                });
            });
        };

        // Initial Load
        refreshList.run();

        // Tab Change Listener
        tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                currentType[0] = tab.getPosition() == 0 ? "INCOME" : "EXPENSE";
                refreshList.run();
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }
        });

        // Add Button Listener
        btnAdd.setOnClickListener(v -> {
            String name = etNewCategory.getText().toString().trim();
            if (!name.isEmpty()) {
                com.personal.finance.data.model.Category newCat = new com.personal.finance.data.model.Category(name,
                        currentType[0], email);
                financeViewModel.insertCategory(newCat);
                etNewCategory.setText("");
            }
        });

        builder.setPositiveButton("Close", null);
        builder.show();
    }
}
