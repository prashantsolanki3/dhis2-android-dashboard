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

package org.hisp.dhis.android.dashboard.ui.fragments.interpretation;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel.Action;
import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.dashboard.DhisService;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.api.job.NetworkJob;
import org.hisp.dhis.android.dashboard.api.models.Interpretation;
import org.hisp.dhis.android.dashboard.api.models.Interpretation$Table;
import org.hisp.dhis.android.dashboard.api.models.InterpretationComment;
import org.hisp.dhis.android.dashboard.api.models.InterpretationComment$Table;
import org.hisp.dhis.android.dashboard.api.models.InterpretationElement;
import org.hisp.dhis.android.dashboard.api.models.InterpretationElement$Table;
import org.hisp.dhis.android.dashboard.api.models.meta.State;
import org.hisp.dhis.android.dashboard.api.network.SessionManager;
import org.hisp.dhis.android.dashboard.api.persistence.loaders.DbLoader;
import org.hisp.dhis.android.dashboard.api.persistence.loaders.DbLoader.TrackedTable;
import org.hisp.dhis.android.dashboard.api.persistence.loaders.Query;
import org.hisp.dhis.android.dashboard.api.persistence.preferences.ResourceType;
import org.hisp.dhis.android.dashboard.ui.activities.DashboardElementDetailActivity;
import org.hisp.dhis.android.dashboard.ui.activities.InterpretationCommentsActivity;
import org.hisp.dhis.android.dashboard.ui.adapters.InterpretationAdapter;
import org.hisp.dhis.android.dashboard.ui.events.UiEvent;
import org.hisp.dhis.android.dashboard.ui.fragments.BaseFragment;
import org.hisp.dhis.android.dashboard.ui.views.GridDividerDecoration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * @author Araz Abishov <araz.abishov.gsoc@gmail.com>.
 */
public final class InterpretationFragment extends BaseFragment
        implements LoaderCallbacks<List<Interpretation>>, InterpretationAdapter.OnItemClickListener {
    public static final String TAG = InterpretationFragment.class.getSimpleName();
    private static final int LOADER_ID = 23452435;
    private static final String IS_LOADING = "state:isLoading";

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    InterpretationAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interpretations, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        mAdapter = new InterpretationAdapter(getActivity(),
                getLayoutInflater(savedInstanceState), this);

        final int spanCount = getResources().getInteger(R.integer.column_nums);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
        mRecyclerView.addItemDecoration(new GridDividerDecoration(getActivity()
                .getApplicationContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_menu);
        mToolbar.setTitle(R.string.interpretations);
        mToolbar.inflateMenu(R.menu.menu_interpretations_fragment);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.refresh) {
                    syncInterpretations();
                    return true;
                }

                return false;
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNavigationDrawer();
            }
        });

        if (isDhisServiceBound() &&
                !getDhisService().isJobRunning(DhisService.SYNC_INTERPRETATIONS) &&
                !SessionManager.getInstance().isResourceTypeSynced(ResourceType.INTERPRETATIONS)) {
            syncInterpretations();
        }
        if(Build.VERSION.SDK_INT>=23)
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.navy_blue,getContext().getTheme()),
                    getResources().getColor(R.color.orange, getContext().getTheme()),
                    getResources().getColor(R.color.grey,getContext().getTheme()));
        else
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.navy_blue),
                    getResources().getColor(R.color.orange),
                    getResources().getColor(R.color.grey));
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, getArguments(), this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_LOADING, mSwipeRefreshLayout.isRefreshing());
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<List<Interpretation>> onCreateLoader(int id, Bundle args) {
        List<DbLoader.TrackedTable> trackedTables = Arrays.asList(
                new TrackedTable(Interpretation.class, Action.UPDATE),
                new TrackedTable(InterpretationComment.class, Action.INSERT));
        return new DbLoader<>(getActivity().getApplicationContext(),
                trackedTables, new InterpretationsQuery());
    }

    @Override
    public void onLoadFinished(Loader<List<Interpretation>> loader, List<Interpretation> data) {
        if (loader != null && loader.getId() == LOADER_ID) {
            if (data != null) {
                mAdapter.swapData(data);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Interpretation>> loader) {
        if (loader != null && loader.getId() == LOADER_ID) {
            mAdapter.swapData(null);
        }
    }

    @Override
    public void onInterpretationContentClick(Interpretation interpretation) {
        InterpretationElement element = null;
        switch (interpretation.getType()) {
            case Interpretation.TYPE_CHART: {
                element = interpretation.getChart();
                break;
            }
            case Interpretation.TYPE_MAP: {
                element = interpretation.getMap();
                break;
            }
            case Interpretation.TYPE_REPORT_TABLE: {
                element = interpretation.getReportTable();
                break;
            }
            default: {
                String message = getString(R.string.unsupported_interpretation_type);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }

        if (element != null) {
            Intent intent = DashboardElementDetailActivity
                    .newIntentForInterpretationElement(getActivity(), element.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onInterpretationTextClick(Interpretation interpretation) {
        InterpretationTextFragment
                .newInstance(interpretation.getId())
                .show(getChildFragmentManager());
    }

    @Override
    public void onInterpretationDeleteClick(Interpretation interpretation) {
        int position = mAdapter.getData().indexOf(interpretation);
        if (!(position < 0)) {
            mAdapter.getData().remove(position);
            mAdapter.notifyItemRemoved(position);
            interpretation.deleteInterpretation();

            if (isDhisServiceBound()) {
                getDhisService().syncInterpretations();

                boolean isLoading = isDhisServiceBound() &&
                        getDhisService().isJobRunning(DhisService.SYNC_INTERPRETATIONS);
                if (isLoading) {
                    doSwipeRefresh(true);
                } else {
                    doSwipeRefresh(false);
                }
            }
        }
    }

    @Override
    public void onInterpretationEditClick(Interpretation interpretation) {
        InterpretationTextEditFragment
                .newInstance(interpretation.getId())
                .show(getChildFragmentManager());
    }

    @Override
    public void onInterpretationCommentsClick(Interpretation interpretation) {
        Intent intent = InterpretationCommentsActivity
                .newIntent(getActivity(), interpretation.getId());
        startActivity(intent);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onResponseReceived(NetworkJob.NetworkJobResult<?> result) {
        if (result.getResourceType() == ResourceType.INTERPRETATIONS) {
            doSwipeRefresh(false);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onUiEventReceived(UiEvent uiEvent) {
        if (uiEvent.getEventType() == UiEvent.UiEventType.SYNC_INTERPRETATIONS) {
            boolean isLoading = isDhisServiceBound() &&
                    getDhisService().isJobRunning(DhisService.SYNC_INTERPRETATIONS);
            if (isLoading) {
                doSwipeRefresh(true);
            } else {
                doSwipeRefresh(false);
            }
        }
    }

    private void syncInterpretations() {
        if (isDhisServiceBound()) {
            getDhisService().syncInterpretations();
            doSwipeRefresh(true);
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

    private static class InterpretationsQuery implements Query<List<Interpretation>> {

        @Override
        public List<Interpretation> query(Context context) {
            List<Interpretation> interpretations
                    = new Select()
                    .from(Interpretation.class)
                    .where(Condition.column(Interpretation$Table
                            .STATE).isNot(State.TO_DELETE.toString()))
                    .queryList();
            for (Interpretation interpretation : interpretations) {
                List<InterpretationElement> elements = new Select()
                        .from(InterpretationElement.class)
                        .where(Condition.column(InterpretationElement$Table
                                .INTERPRETATION_INTERPRETATION).is(interpretation.getId()))
                        .queryList();
                List<InterpretationComment> comments = new Select()
                        .from(InterpretationComment.class)
                        .where(Condition.column(InterpretationComment$Table
                                .INTERPRETATION_INTERPRETATION).is(interpretation.getId()))
                        .and(Condition.column(InterpretationComment$Table
                                .STATE).isNot(State.TO_DELETE.toString()))
                        .queryList();
                interpretation.setInterpretationElements(elements);
                interpretation.setComments(comments);
            }

            // sort interpretations by created field in reverse order.
            Collections.sort(interpretations,
                    Collections.reverseOrder(Interpretation.CREATED_COMPARATOR));
            return interpretations;
        }
    }
}
