/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) [2017] [velli20]
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package com.velli.tachograph.filepicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.velli.tachograph.R;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;


public class FragmentFileList extends Fragment {
    public static final String KEY_BUNDLE_PATH = "path";
    public static final String KEY_BUNDLE_SORT_BY = "sort by";
    public static final String KEY_BUNDLE_FILE_EXTENSION = "file extension";
    private RecyclerView mList;
    private FileAdapter mAdapter;
    private String mPath;
    private String mFileExtension;
    private OnFileClickedListener mListener;
    private int mSortBy = FileComparator.COMPARE_BY_NAME;
    private Theme mTheme = Theme.LIGHT;


    public interface OnFileClickedListener {
        void onFileClicked(File file);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View v = inflater.inflate(R.layout.fragment_file_list, container, false);


        if(getArguments() != null) {
            mPath = getArguments().getString(KEY_BUNDLE_PATH, Environment.getExternalStorageDirectory().toString());
            mSortBy = getArguments().getInt(KEY_BUNDLE_SORT_BY, FileComparator.COMPARE_BY_NAME);
            mFileExtension = getArguments().getString(KEY_BUNDLE_FILE_EXTENSION, null);
        } else {
            mPath = Environment.getExternalStorageDirectory().toString();
        }

        mAdapter = new FileAdapter(getActivity(), new File(mPath), mTheme);
        mList = (RecyclerView) v.findViewById(R.id.fragment_file_list_recycler_view);
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setAdapter(mAdapter);

        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_file_picker_add_folder:
                showCreateFolderDialog();
                return true;
            case R.id.menu_file_picker_sort_by_date:
                mSortBy = FileComparator.COMPARE_BY_LAST_MODIFIED;
                updateDirectory(mPath);
                break;
            case R.id.menu_file_picker_sort_by_name:
                mSortBy = FileComparator.COMPARE_BY_NAME;
                updateDirectory(mPath);
                break;
            case R.id.menu_file_picker_sort_by_size:
                mSortBy = FileComparator.COMPARE_BY_SIZE;
                updateDirectory(mPath);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setOnFileClickedListener(OnFileClickedListener l) {
        mListener = l;
    }

    private void updateDirectory(String path) {
        if(mAdapter == null) {
            mAdapter = new FileAdapter(getActivity(), new File(mPath), Theme.LIGHT);
            mList.setAdapter(mAdapter);
        } else {
            mAdapter.setFileDirctory(new File(mPath));
            mAdapter.notifyDataSetChanged();
        }
    }

    private void createDirctory(String path) {
        File file;
        int count = 0;

        do {
            if(count == 0) {
                file = new File(path);
                count++;
            } else {
                file = new File(path + " (" + String.valueOf(count) + ")");
                count++;
            }

        } while(file == null || file.exists());

        file.mkdir();
        updateDirectory(mPath);
        if(mListener != null) {
            mListener.onFileClicked(file);
        }
    }

    private void showCreateFolderDialog() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.file_picker_title_create_folder)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(android.R.string.cancel)
                .theme(mTheme)
                .input(null, null, new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        createDirctory(mPath + "/" + input);
                    }
                }).show();
    }

    public String getPath() { return mPath; }

    public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final LayoutInflater mInflater;
        private File[]  mDirectory;
        private final DateFormat formOut = new SimpleDateFormat("dd.MM.yyyy ' 'HH:mm:ss", Locale.getDefault());
        private final Resources mRes;

        private Theme mTheme;
        private ColorStateList mTextColorDark;
        private ColorStateList mTextColorLight;

        public FileAdapter(Context c, File directory, Theme theme) {
            mInflater = LayoutInflater.from(c);
            setFileDirctory(directory);
            mRes = c.getResources();

            Resources mRes = getResources();
            mTheme = theme;

            mTextColorDark = mRes.getColorStateList(R.color.dialog_timepicker_dark);
            mTextColorLight = mRes.getColorStateList(R.color.dialog_timepicker_light);
        }

        public void setFileDirctory(File file) {
            mDirectory = file.listFiles();
            Arrays.sort(mDirectory, new FileComparator(mSortBy));
        }

        public File getFile(int position) {
            return mDirectory[position];
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolderFile holder = new ViewHolderFile(mInflater.inflate(R.layout.list_item_file, parent, false));
            holder.mTitle.setTextColor(mTheme == Theme.DARK ? mTextColorLight : mTextColorDark);
            holder.mSubtitle.setTextColor(mTheme == Theme.DARK ? mTextColorLight : mTextColorDark);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final File file = getFile(position);
            final String fileName = file.getName();
            String extension = "";

            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i);
            }

            ((ViewHolderFile)holder).mTitle.setText(file.getName());
            ((ViewHolderFile)holder).mSubtitle.setText(formOut.format(file.lastModified()));
            if(file.isDirectory()) {
                ((ViewHolderFile)holder).mIcon.setImageDrawable(mRes.getDrawable(R.drawable.ic_filepicker_folder));
            } else if(fileName.endsWith(".xls")) {
                ((ViewHolderFile)holder).mIcon.setImageDrawable(mRes.getDrawable(R.drawable.ic_filepicker_excel));
            } else if(fileName.endsWith(".jpg") || file.getName().endsWith(".png")) {
                ((ViewHolderFile)holder).mIcon.setImageDrawable(mRes.getDrawable(R.drawable.ic_filepicker_image));
            } else if(fileName.endsWith(".pdf")) {
                ((ViewHolderFile)holder).mIcon.setImageDrawable(mRes.getDrawable(R.drawable.ic_filepicker_pdf));
            } else {
                ((ViewHolderFile)holder).mIcon.setImageDrawable(mRes.getDrawable(R.drawable.ic_filepicker_file));
            }

            if(mFileExtension != null && !file.isDirectory() && !mFileExtension.equals(extension)) {
                holder.itemView.setAlpha(0.30f);
                holder.itemView.setEnabled(false);
            } else {
                holder.itemView.setAlpha(1f);
                holder.itemView.setEnabled(true);
            }
            holder.itemView.setOnClickListener(new ClickListener(position));

        }

        @Override
        public int getItemCount() {
            if(mDirectory == null) {
                return 0;
            }
            return mDirectory.length;
        }

        private class ClickListener implements View.OnClickListener {
            private int mPosition;

            public ClickListener(int position) {
                mPosition = position;
            }

            @Override
            public void onClick(View v) {
                if(mListener != null && v.isEnabled()) {
                    mListener.onFileClicked(getFile(mPosition));
                }
            }
        }

    }





}
