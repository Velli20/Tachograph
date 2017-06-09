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

package com.velli20.tachograph.filepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.afollestad.materialdialogs.Theme;
import com.velli20.tachograph.R;

import java.io.File;
import java.io.IOException;

public class ActivityFilePicker extends AppCompatActivity implements AdapterView.OnItemSelectedListener, FragmentManager.OnBackStackChangedListener, FragmentFileList.OnFileClickedListener, View.OnClickListener, TextWatcher {
    public static final String INTENT_EXTRA_MODE = "mode";
    public static final String INTENT_EXTRA_FILE_EXTENSION = "file extension";
    public static final String INTENT_EXTRA_FILENAME = "filename";
    public static final String INTENT_EXTRA_FILEPATH = "filepath";

    public static final int MODE_CHOOSE_FOLDER = 0;
    public static final int MODE_PICK_FILE = 1;
    public static final int MODE_CREATE_FILE = 2;

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_DIRECTORY = 0;

    private Toolbar mToolbar;
    private FileNavigationSpinnerAdapter mAdapter;
    private Spinner mNavigationSpinner;

    private RelativeLayout mEditTextContainer;
    private AppCompatEditText mEditText;
    private AppCompatButton mButtonSave;
    private ImageView mFileExtIcon;

    private int mMode = MODE_CREATE_FILE;
    private String mFileExtension = "";
    private String mFilename = "";
    private boolean mAdded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        if(getIntent().getExtras() != null) {
            mMode = getIntent().getExtras().getInt(INTENT_EXTRA_MODE, MODE_CREATE_FILE);
            mFileExtension = getIntent().getExtras().getString(INTENT_EXTRA_FILE_EXTENSION, "");
            mFilename = getIntent().getExtras().getString(INTENT_EXTRA_FILENAME, "");
        }

        mToolbar = (Toolbar) findViewById(R.id.activity_file_picker_toolbar);
        mEditText = (AppCompatEditText) findViewById(R.id.activity_file_picker_edittext);
        mEditText.setText(mFilename);


        mFileExtIcon = (ImageView) findViewById(R.id.activity_file_picker_file_ext_icon);

        mButtonSave = (AppCompatButton) findViewById(R.id.activity_file_picker_save);
        mButtonSave.setOnClickListener(this);

        mEditTextContainer = (RelativeLayout) findViewById(R.id.activity_file_picker_edittext_container);
        mEditTextContainer.setVisibility(mMode == MODE_CREATE_FILE ? View.VISIBLE : View.GONE);

        mAdapter = new FileNavigationSpinnerAdapter(this, Theme.DARK);

        mNavigationSpinner = (Spinner) findViewById(R.id.activity_file_picker_navigation_spinner);
        mNavigationSpinner.setAdapter(mAdapter);
        mNavigationSpinner.post(new Runnable() {
            @Override
            public void run() {
                mNavigationSpinner.setOnItemSelectedListener(ActivityFilePicker.this);
            }
        });



        if(mMode == MODE_CREATE_FILE) {
            mFileExtIcon.setImageDrawable(getResources().getDrawable(FilePickerUtils.getDrawableResourceForFileExt(mFileExtension)));
            mEditText.addTextChangedListener(this);
        }

        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowHomeEnabled(false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            findViewById(R.id.activity_file_picker_toolbar_shadow).setVisibility(View.GONE);
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if(result != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_DIRECTORY);

                return;

            }
        }
        addRootFragment();

    }

    public FragmentFileList getNewFileFragment(String path) {
        final Bundle b = new Bundle();
        b.putString(FragmentFileList.KEY_BUNDLE_PATH, path);
        b.putString(FragmentFileList.KEY_BUNDLE_FILE_EXTENSION, mFileExtension);

        final FragmentFileList frag = new FragmentFileList();
        frag.setOnFileClickedListener(this);
        frag.setArguments(b);

        return frag;
    }

    private void addRootFragment() {
        FragmentManager m = getSupportFragmentManager();
        m.addOnBackStackChangedListener(this);

        final String path = Environment.getExternalStorageDirectory().getPath();
        final FragmentFileList frag = getNewFileFragment(path);

        m.beginTransaction().add(R.id.activity_file_picker_container, frag, path).commit();

        mAdapter.addPath(path);
    }

    public void addFragment(Fragment frag, boolean animate, String tag) {
        FragmentManager m = getSupportFragmentManager();
        FragmentTransaction t = m.beginTransaction();
        if(animate) {
            t.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        mAdded = true;
        t.addToBackStack(tag).replace(R.id.activity_file_picker_container, frag, tag).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.menu_activity_file_picker, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_DIRECTORY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addRootFragment();

                } else {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
        }
    }

    @Override
    public void onFileClicked(File file) {
        if(file.isDirectory()) {
            final String path = file.getPath();
            final FragmentFileList frag = getNewFileFragment(path);

            addFragment(frag, true, path);
            mAdapter.addPath(path);
            mNavigationSpinner.setSelection(mAdapter.getCount() -1);
        } else if(mMode == MODE_CREATE_FILE) {
            String fileName = file.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 1) {
                mEditText.setText(fileName.substring(0, i));
            } else {
                mEditText.setText(fileName);
            }
        } else if(mMode == MODE_PICK_FILE) {
            Intent data = new Intent();
            data.putExtra(INTENT_EXTRA_FILEPATH, file.getPath());
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position < getSupportFragmentManager().getBackStackEntryCount()) {
            for(int i = position; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    @Override
    public void onBackStackChanged() {
        if(mAdapter != null && !mAdded) {
            mAdapter.removePathStartingAt(getSupportFragmentManager().getBackStackEntryCount() +1);

        } else {
            mAdded = false;
        }
    }



    @Override
    public void onClick(View v) {
        String path = mAdapter.getItem(mAdapter.getCount() -1) ;
        String fileName = mEditText.getText().toString();
        File file;
        int count = 0;

        do {
            if(count == 0) {
                file = new File(path, fileName + mFileExtension);
                count++;
            } else {
                file = new File(path, fileName + " (" + String.valueOf(count) + ")" + mFileExtension);
                count++;
            }

        } while(file == null || file.exists());

        try {
            file.createNewFile();
        } catch (IOException e) {}

        Intent data = new Intent();
        data.putExtra(INTENT_EXTRA_FILEPATH, file.getPath());
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        mButtonSave.setEnabled(mEditText.getText().toString().length() > 0);
    }


}

