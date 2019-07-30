package com.snowdragon.whatsnext.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

/**
 * Represents a fragment that is not scrollable.
 *
 * This class represents fragments which are static in nature
 * and is thus not able to exhibit any scrolling behaviour.
 * All subclasses of this class must invoke
 * {@code super.onCreateView} in their overriden onCreateView
 * method. Otherwise, the behaviour of the app bar is not
 * guranteed by the application.
 *
 * Also, the hosting activity of subclasses of this fragment
 * must have an appbar that has ID of {@code R.id.appbar_layout},
 * otherwise a {@code NullPointerException} will be thrown
 * resulting in crashes.
 *
 * The purpose of this class is to ensure that when transitioning
 * from a scrollable view to a static view, the app bar that could
 * be hidden by the scrollable view(when scrolling down) is shown
 * at the top again when switching to a static view.
 */
public abstract class AbstractStaticFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppBarLayout layout = Objects.requireNonNull(getActivity()).findViewById(R.id.appbar_layout);
        FloatingActionButton floatingActionButton = getActivity().findViewById(R.id.floating_add_button);
        floatingActionButton.hide();
        layout.setExpanded(true, true);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
