/*
 * Copyright (c) 2015, dhis2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.ui.fragments.dashboard;

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
 * @author Araz Abishov <araz.abishov.gsoc@gmail.com>.
 *         <p/>
 *         This fragment is shown in case there
 *         is no any dashboards in local database.
 */
public class DashboardEmptyFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = DashboardEmptyFragment.class.getSimpleName();
    private static final String IS_LOADING = "state:isLoading";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboards_empty, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mToolbar.setNavigationIcon(R.mipmap.ic_menu);
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setTitle(R.string.dashboard);
        mToolbar.inflateMenu(R.menu.menu_dashboard_empty_fragment);
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
                !getDhisService().isJobRunning(DhisService.SYNC_DASHBOARDS) &&
                !SessionManager.getInstance().isResourceTypeSynced(ResourceType.DASHBOARDS)) {
            syncDashboards();
        }

        boolean isLoading = isDhisServiceBound() &&
                getDhisService().isJobRunning(DhisService.SYNC_DASHBOARDS);
        if ((savedInstanceState != null &&
                savedInstanceState.getBoolean(IS_LOADING)) || isLoading) {
            doSwipeRefresh(true);
        } else {
            doSwipeRefresh(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_LOADING, mSwipeRefreshLayout.isRefreshing());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        toggleNavigationDrawer();
    }

    public boolean onMenuItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh: {
                syncDashboards();
                return true;
            }
            case R.id.add_dashboard: {
                new DashboardAddFragment()
                        .show(getChildFragmentManager());
                return true;
            }
        }
        return false;
    }

    private void syncDashboards() {
        if (isDhisServiceBound()) {
            getDhisService().syncDashboardsAndContent();
            doSwipeRefresh(true);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onResponseReceived(NetworkJob.NetworkJobResult<?> result) {
        if (result.getResourceType() == ResourceType.DASHBOARDS) {
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
