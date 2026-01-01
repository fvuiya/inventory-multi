package com.bsoft.inventorymanager.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.utils.Debouncer;

public class SearchAndFilterView extends FrameLayout {

    private ImageView searchTrigger;
    private View searchBarContainer;
    private EditText searchInput;
    private ImageView filterButton; // Inside text field

    private OnSearchListener onSearchListener;
    private OnClickListener onFilterClickListener;

    // Default mode: expanded or collapsed? User said "clicking search icon will
    // reveal".
    // So distinct state needed.

    public interface OnSearchListener {
        void onQueryTextChange(String newText);

        void onQueryTextSubmit(String query);
    }

    public SearchAndFilterView(Context context) {
        super(context);
        init(context);
    }

    public SearchAndFilterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchAndFilterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_search_and_filter, this, true);

        searchTrigger = findViewById(R.id.iv_search_trigger);
        searchBarContainer = findViewById(R.id.cl_search_bar_container);
        searchInput = findViewById(R.id.et_search_input);
        filterButton = findViewById(R.id.iv_filter_icon);

        setupListeners();
    }

    private void setupListeners() {
        // 1. Expand Logic
        if (searchTrigger != null) {
            searchTrigger.setOnClickListener(v -> expand());
        }

        // 2. Filter
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                if (onFilterClickListener != null) {
                    onFilterClickListener.onClick(v);
                }
            });
        }

        // 4. Text Input
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (onSearchListener != null) {
                        // Local filtering can still happen automatically if desired,
                        // but request implies "automatically it does not work" for the search action.
                        // We will allow onQueryTextChange for local filtering.
                        onSearchListener.onQueryTextChange(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            searchInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    performSearch();
                    return true;
                }
                return false;
            });
        }
    }

    private void expand() {
        // searchTrigger stays VISIBLE
        if (searchBarContainer != null) {
            searchBarContainer.setVisibility(VISIBLE);
            if (searchInput != null)
                searchInput.requestFocus();
            // Show keyboard?
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && searchInput != null) {
                imm.showSoftInput(searchInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    public void collapse() {
        if (searchBarContainer != null)
            searchBarContainer.setVisibility(GONE);
        if (searchTrigger != null)
            searchTrigger.setVisibility(VISIBLE);
        // Clear focus/text?
        if (searchInput != null) {
            searchInput.setText("");
            searchInput.clearFocus();
        }
        // Hide keyboard
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && searchInput != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    private void performSearch() {
        if (onSearchListener != null && searchInput != null) {
            onSearchListener.onQueryTextSubmit(searchInput.getText().toString());
            // Optional: collapse or keep open? Usually keep open to show results.
            searchInput.clearFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
            }
        }
    }

    public void setHint(String hint) {
        if (searchInput != null) {
            searchInput.setHint(hint);
        }
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
    }

    public void setOnFilterClickListener(OnClickListener listener) {
        this.onFilterClickListener = listener;
    }
}
