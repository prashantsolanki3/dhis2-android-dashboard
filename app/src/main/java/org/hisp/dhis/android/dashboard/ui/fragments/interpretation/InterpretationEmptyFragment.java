package org.hisp.dhis.android.dashboard.ui.fragments.interpretation;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.dashboard.DhisService;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.api.job.NetworkJob;
import org.hisp.dhis.android.dashboard.api.network.SessionManager;
import org.hisp.dhis.android.dashboard.api.persistence.preferences.ResourceType;
import org.hisp.dhis.android.dashboard.ui.fragments.BaseFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by arazabishov on 7/24/15.
 */
public class InterpretationEmptyFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = InterpretationEmptyFragment.class.getSimpleName();
    private static final String IS_LOADING = "state:isLoading";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;


    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interpretations_empty, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mToolbar.setNavigationIcon(R.mipmap.ic_menu);
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setTitle(R.string.interpretations);
        mToolbar.inflateMenu(R.menu.menu_interpretations_fragment);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onMenuItemClicked(item);
            }
        });
        if(Build.VERSION.SDK_INT>=23)
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.navy_blue,getContext().getTheme()),
                    getResources().getColor(R.color.orange, getContext().getTheme()),
                    getResources().getColor(R.color.grey,getContext().getTheme()));
        else
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.navy_blue),
                    getResources().getColor(R.color.orange),
                    getResources().getColor(R.color.grey));

        if (isDhisServiceBound() &&
                !getDhisService().isJobRunning(DhisService.SYNC_INTERPRETATIONS) &&
                !SessionManager.getInstance().isResourceTypeSynced(ResourceType.INTERPRETATIONS)) {
            syncInterpretations();
        }

        boolean isLoading = isDhisServiceBound() &&
                getDhisService().isJobRunning(DhisService.SYNC_INTERPRETATIONS);
        if ((savedInstanceState != null &&
                savedInstanceState.getBoolean(IS_LOADING)) || isLoading) {
            doSwipeRefresh(true);
        } else {
            doSwipeRefresh(false);
        }
    }

    @Override
    public void onClick(View view) {
        toggleNavigationDrawer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_LOADING, mSwipeRefreshLayout.isRefreshing());
        super.onSaveInstanceState(outState);
    }

    public boolean onMenuItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh: {
                syncInterpretations();
                return true;
            }
        }
        return false;
    }

    private void syncInterpretations() {
        if (isDhisServiceBound()) {
            getDhisService().syncInterpretations();
            doSwipeRefresh(true);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onResponseReceived(NetworkJob.NetworkJobResult<?> result) {
        if (result.getResourceType() == ResourceType.INTERPRETATIONS) {
            doSwipeRefresh(false);
        }
    }

    private void doSwipeRefresh(final boolean enable){
        //Workaround for showing swipe refresh layout initially.
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(enable);
            }
        });
    }
}
