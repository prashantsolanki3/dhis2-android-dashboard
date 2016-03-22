package org.hisp.dhis.android.dashboard.ui.adapters;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.ui.models.Field;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by arazabishov on 7/27/15.
 */
public class AccountFieldAdapter extends AbsAdapter<Field, AccountFieldAdapter.FieldViewHolder> {

    public AccountFieldAdapter(Context context, LayoutInflater inflater) {
        super(context, inflater);
    }

    @Override
    public FieldViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FieldViewHolder(getLayoutInflater()
                .inflate(R.layout.recycler_view_field_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FieldViewHolder holder, int position) {
        Field field = getData().get(position);
        //holder.labelTextView.setText(field.getLabel());
        if(holder.inputLayout.getEditText()!=null)
        holder.inputLayout.getEditText().setText(field.getValue());

        holder.inputLayout.setHint(field.getLabel());
    }

    public static class FieldViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.field_value_text_input_layout)
        public TextInputLayout inputLayout;

        public FieldViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            inputLayout.setEnabled(false);
            inputLayout.setHintTextAppearance(R.style.UserProfileLabelHintStyle);
        }
    }
}
