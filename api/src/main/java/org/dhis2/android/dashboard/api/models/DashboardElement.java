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

package org.dhis2.android.dashboard.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.dhis2.android.dashboard.api.persistence.DbDhis;
import org.joda.time.DateTime;

@Table(databaseName = DbDhis.NAME)
public final class DashboardElement extends BaseModel implements BaseIdentifiableModel {
    static final String DASHBOARD_ITEM_KEY = "dashboardItem";

    @JsonIgnore @Column @PrimaryKey(autoincrement = true) long localId;
    @JsonIgnore @Column @NotNull State state;

    @JsonIgnore @Column @NotNull @ForeignKey(
            references = {
                    @ForeignKeyReference(columnName = DASHBOARD_ITEM_KEY, columnType = long.class, foreignColumnName = "localId")
            }, saveForeignKeyModel = false, onDelete = ForeignKeyAction.CASCADE
    ) DashboardItem dashboardItem;

    @JsonProperty("id") @Column String id;
    @JsonProperty("name") @Column String name;
    @JsonProperty("created") @Column DateTime created;
    @JsonProperty("lastUpdated") @Column DateTime lastUpdated;
    @JsonProperty("displayName") @Column String displayName;

    public DashboardElement() {
        state = State.SYNCED;
    }

    @JsonIgnore @Override
    public long getLocalId() {
        return localId;
    }

    @JsonIgnore @Override
    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @JsonIgnore public State getState() {
        return state;
    }

    @JsonIgnore public void setState(State state) {
        this.state = state;
    }

    @JsonIgnore public DashboardItem getDashboardItem() {
        return dashboardItem;
    }

    @JsonIgnore public void setDashboardItem(DashboardItem dashboardItem) {
        this.dashboardItem = dashboardItem;
    }

    @JsonIgnore @Override public String getId() {
        return id;
    }

    @JsonIgnore @Override public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore @Override public String getName() {
        return name;
    }

    @JsonIgnore @Override public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore @Override public DateTime getCreated() {
        return created;
    }

    @JsonIgnore @Override public void setCreated(DateTime created) {
        this.created = created;
    }

    @JsonIgnore @Override public DateTime getLastUpdated() {
        return lastUpdated;
    }

    @JsonIgnore @Override public void setLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @JsonIgnore public String getDisplayName() {
        return displayName;
    }

    @JsonIgnore public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}