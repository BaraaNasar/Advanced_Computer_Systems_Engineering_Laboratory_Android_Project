package com.personal.finance.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.personal.finance.R;
import com.personal.finance.data.model.User;
import com.personal.finance.ui.viewmodel.FinanceViewModel;
import com.personal.finance.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private SessionManager sessionManager;
    private EditText etFirst, etLast, etPass;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        String email = sessionManager.getUserEmail();

        financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

        etFirst = view.findViewById(R.id.etProfileFirstName);
        etLast = view.findViewById(R.id.etProfileLastName);
        etPass = view.findViewById(R.id.etProfilePassword);

        // Note: we should fetch current user data to populate fields.
        // For now, we allow updating blindly or assuming user knows what they put.
        // In a real app, we would observe getUser(email).

        view.findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            String first = etFirst.getText().toString();
            String last = etLast.getText().toString();
            String pass = etPass.getText().toString();

            if (!first.isEmpty() && !last.isEmpty() && !pass.isEmpty()) {
                User user = new User(email, first, last, pass);
                financeViewModel.updateUser(user);
                Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
