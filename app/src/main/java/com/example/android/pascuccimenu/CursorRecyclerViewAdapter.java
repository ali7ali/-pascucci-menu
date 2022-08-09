package com.example.android.pascuccimenu;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.pascuccimenu.data.PascucciMenuContract;

import java.util.Locale;

/**
 * Created by Trouble_Maker on 11/1/2018.
 */
public  class CursorRecyclerViewAdapter  extends   RecyclerView.Adapter<CursorRecyclerViewAdapter.MyViewHolder> {
    private ContentResolver resolver;

    private Context mContext;

    private Cursor mCursor;

    private boolean mDataValid;

    private int mRowIdColumn;

    private DataSetObserver mDataSetObserver;

    private static ClickListener clickListener;

     CursorRecyclerViewAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex(PascucciMenuContract.MenuEntry._ID) : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }

    }


    public Cursor getCursor() {
        return mCursor;
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
       }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener, View.OnLongClickListener
         {

        // each data item is just a string in this case
         TextView mTextView;
         ImageView mimageView;
         MyViewHolder(View vi) {
            super(vi);
            vi.setOnClickListener(this);
            vi.setOnLongClickListener(this);
            mTextView = (TextView) vi.findViewById(R.id.itemname);
            mimageView=(ImageView) vi.findViewById(R.id.itemimage);
        }


             @Override
             public void onClick(View v) {
                 clickListener.onItemClick(getAdapterPosition(), v);
             }

             @Override
             public boolean onLongClick(View v) {
                 clickListener.onItemLongClick(getAdapterPosition(), v);
                 return false;
             }


         }
     void setOnItemClickListener(ClickListener clickListener) {
        CursorRecyclerViewAdapter.clickListener = clickListener;
    }



    public interface ClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }
    // Create new views (invoked by the layout manager)
    @Override
    public CursorRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
      LayoutInflater inflater = LayoutInflater.from(mContext);
        View view =inflater.inflate(R.layout.recycler_items,parent,false);
        return new MyViewHolder(view);

    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (!mCursor.moveToPosition(position)) {
            return;
        }
            int nameColumnIndex = mCursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_NAME);
            int nameColumnIndexar = mCursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_NAME_AR);
            int photoColumnIndex = mCursor.getColumnIndex(PascucciMenuContract.MenuEntry.COLUMN_PHOTO);
            // Read the menu item attributes from the Cursor for the current menu item
            if (Locale.getDefault().getLanguage().equals("ar")) {

                String itemName = mCursor.getString(nameColumnIndexar);
                holder.mTextView.setText(itemName);
            } else {

                String itemName = mCursor.getString(nameColumnIndex);
                holder.mTextView.setText(itemName);
            }
        String imageUriString = mCursor.getString(photoColumnIndex);
        Uri productImageUri = Uri.parse(imageUriString);
        holder.mimageView.setImageURI(productImageUri);


      //  Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(productImageUri.getPath()),110,140);
      //  holder.mimageView.setImageBitmap(bitmap);


        // byte[] imgByte = mCursor.getBlob(photoColumnIndex);
          //  Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
          //  holder.mimageView.setImageBitmap(bitmap);
    }
     Cursor swapCursor(Cursor newCursor) {

        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }


}
