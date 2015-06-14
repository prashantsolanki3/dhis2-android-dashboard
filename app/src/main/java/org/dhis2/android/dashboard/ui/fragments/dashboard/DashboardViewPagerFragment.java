/*
 * Copyright (c) 2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.android.dashboard.ui.fragments.dashboard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

import org.dhis2.android.dashboard.R;
import org.dhis2.android.dashboard.api.models.Dashboard;
import org.dhis2.android.dashboard.api.persistence.loaders.DbLoader;
import org.dhis2.android.dashboard.api.persistence.loaders.Query;
import org.dhis2.android.dashboard.ui.activities.INavigationCallback;
import org.dhis2.android.dashboard.ui.adapters.DashboardAdapter;
import org.dhis2.android.dashboard.ui.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DashboardViewPagerFragment extends BaseFragment
        implements LoaderCallbacks<List<Dashboard>>, View.OnClickListener, Toolbar.OnMenuItemClickListener {
    private static final int LOADER_ID = 1233432;

    private DashboardAdapter mDashboardAdapter;

    @InjectView(R.id.dashboard_tabs) TabLayout mTabs;
    @InjectView(R.id.dashboard_view_pager) ViewPager mViewPager;
    @InjectView(R.id.toolbar) Toolbar mToolbar;

    INavigationCallback mNavCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof INavigationCallback) {
            mNavCallback = (INavigationCallback) activity;
        } else {
            throw new UnsupportedOperationException("Parent activity must " +
                    "implement INavigationCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mNavCallback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboards, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.inject(this, view);

        mToolbar.setNavigationIcon(R.mipmap.ic_menu);
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setTitle(R.string.dashboard);
        mToolbar.inflateMenu(R.menu.menu_dashboard_fragment);
        mToolbar.setOnMenuItemClickListener(this);

        mDashboardAdapter = new DashboardAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mDashboardAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, savedInstanceState, this);
    }

    @Override
    public Loader<List<Dashboard>> onCreateLoader(int id, Bundle state) {
        if (id == LOADER_ID && isAdded()) {
            List<Class<? extends Model>> tablesToTrack = new ArrayList<>();
            tablesToTrack.add(Dashboard.class);
            return new DbLoader<>(getActivity().getApplicationContext(),
                    tablesToTrack, new DashboardQuery());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Dashboard>> loader, List<Dashboard> data) {
        if (loader.getId() == LOADER_ID && data != null) {
            setDashboards(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Dashboard>> loader) {
        if (loader.getId() == LOADER_ID) {
            setDashboards(null);
        }
    }

    @Override
    public void onClick(View view) {
        mNavCallback.toggleNavigationDrawer();
    }

    private void setDashboards(List<Dashboard> dashboards) {
        mDashboardAdapter.swapData(dashboards);
        mTabs.removeAllTabs();
        mTabs.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh: {
                getService().syncDashboards();
                return true;
            }
        }
        return false;
    }

    private static class DashboardQuery implements Query<List<Dashboard>> {

        @Override public List<Dashboard> query(Context context) {
            List<Dashboard> dashboards = new Select()
                    .from(Dashboard.class).queryList();
            Collections.sort(dashboards, Dashboard.DISPLAY_NAME_MODEL_COMPARATOR);
            return dashboards;
        }
    }
}