package com.example.masqu.myapplication.Activity;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import br.com.zbra.androidlinq.Stream;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import com.example.masqu.myapplication.Models.Constants;
import com.example.masqu.myapplication.Models.ScrollViewExt;
import com.example.masqu.myapplication.Models.ScrollViewListener;
import com.example.masqu.myapplication.Models.TagClass;
import com.example.masqu.myapplication.R;
import com.example.masqu.myapplication.RecyclerViewAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;
import mabbas007.tagsedittext.TagsEditText;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.*;

import static br.com.zbra.androidlinq.Linq.stream;

public class MainActivity extends AppCompatActivity implements ScrollViewListener, TagsEditText.TagsEditListener {
    private static final String TAG = "DemoActivity";

    private SlidingUpPanelLayout slidingLayout;

    private TagContainerLayout selectedTagsGroup;

    private RecyclerView imageGallery;
    private RecyclerViewAdapter recyclerViewAdapter;

//    private RelativeLayout dragViewiew;

    private TagsEditText tagSearchEditText;
    private ImageButton slidingButton;

    private TagContainerLayout searchResultTagsGroup;

    private List<TagClass> allTags;
    private List<String> allTagText;
    private int maxTagsLimit = 50;
    private ScrollViewExt searchResultScrollView;

    //    private String GALLERY_LOCATION = "image gallery";
//    private File GalleryFolder;
//    private RecyclerView.LayoutManager mLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        PrepareTags();


        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        selectedTagsGroup = (TagContainerLayout) findViewById(R.id.selected_TagsGroup);
        tagSearchEditText = (TagsEditText) findViewById(R.id.tagSearch_EditText);
        slidingButton = (ImageButton) findViewById(R.id.slidingButton);
        searchResultTagsGroup = (TagContainerLayout) findViewById(R.id.searchResult_TagsGroup);
        searchResultScrollView = (ScrollViewExt) findViewById(R.id.tagsSearch_layout);
        searchResultScrollView.setScrollViewListener(this);

        StaggeredGridLayoutManager staggeredGridLayoutManagerVertical = new StaggeredGridLayoutManager(
                2, //The number of Columns in the grid
                LinearLayoutManager.VERTICAL);

        DisplayMetrics metrics = GetDisplayMetrics();

        recyclerViewAdapter = new RecyclerViewAdapter(this, metrics.widthPixels);
        recyclerViewAdapter.setHasStableIds(true);

        ArrayAdapter<String> tagEditViewAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());

        tagSearchEditText.setAdapter(tagEditViewAdapter);

        imageGallery = (RecyclerView) findViewById(R.id.image_gallery);
        // Setting
        imageGallery.setAdapter(recyclerViewAdapter);
        imageGallery.setLayoutManager(staggeredGridLayoutManagerVertical);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageGallery.setNestedScrollingEnabled(false);
        }
        imageGallery.setHasFixedSize(true);
        imageGallery.setItemAnimator(new DefaultItemAnimator());
        imageGallery.setItemViewCacheSize(20);
        imageGallery.setDrawingCacheEnabled(true);
        imageGallery.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


        SetUpSlidingPanel();
        SetUpSlidingButton();
        SetUpSearchResultTagsGroup();
        SetUpTagSearchEditText();

        InitSetTags();

//        CreateImageGallery();

//        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
//        ArrayList<String> fileList = new ArrayList<String>();
//        getFile(path, fileList);

        AddImagesToGallery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (slidingLayout != null) {
            if (slidingLayout.getPanelState() == PanelState.HIDDEN) {
                item.setTitle(R.string.action_show);
            } else {
                item.setTitle(R.string.action_hide);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle: {
                if (slidingLayout != null) {
                    if (slidingLayout.getPanelState() != PanelState.HIDDEN) {
                        slidingLayout.setPanelState(PanelState.HIDDEN);
                        item.setTitle(R.string.action_show);
                    } else {
                        slidingLayout.setPanelState(PanelState.COLLAPSED);
                        item.setTitle(R.string.action_hide);
                    }
                }
                return true;
            }
            case R.id.action_anchor: {
                if (slidingLayout != null) {
                    if (slidingLayout.getAnchorPoint() == 1.0f) {
                        slidingLayout.setAnchorPoint(0.7f);
                        slidingLayout.setPanelState(PanelState.ANCHORED);
                        item.setTitle(R.string.action_anchor_disable);
                    } else {
                        slidingLayout.setAnchorPoint(1.0f);
                        slidingLayout.setPanelState(PanelState.COLLAPSED);
                        item.setTitle(R.string.action_anchor_enable);
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (slidingLayout != null &&
                (slidingLayout.getPanelState() == PanelState.EXPANDED || slidingLayout.getPanelState() == PanelState.ANCHORED)) {
            slidingLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
//        // We take the last son in the scrollview
//        View view = scrollView.getChildAt(0);
//        int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
//        // if diff is zero, then the bottom has been reached
//        Log.i(TAG,Integer.toString(diff));
//        if (diff == 0) {
//            int alreadyInView = searchResultTagsGroup.getTags().size();
//            String searchStr = tagSearchEditText.getText().toString();
//
//            DrawTagsBySearchInput(searchStr, alreadyInView + 10);
//        }
    }

    private void InitSetTags() {
        DrawTagsBySearchInput("", maxTagsLimit);
    }

    private void PrepareTags() {
//        allTags = new ArrayList<>();
        allTagText = new ArrayList<>();
        JSONArray jsonArray;
        JSONObject temp;
        try {
            jsonArray = new JSONArray(Constants.COUNTRIES);
            for (int i = 0; i < jsonArray.length(); i++) {
                temp = jsonArray.getJSONObject(i);
//                allTags.add(new TagClass(temp.getString("code"), temp.getString("name")));
                allTagText.add(temp.getString("name"));
            }
//            allTagText = stream().select(TagClass::getName).toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<String> GetImages(int numberToTake) {
        List<String> imagePaths = new ArrayList<>();
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (isSDPresent) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String orderBy = MediaStore.Images.Media._ID;
            //Stores all the images from the gallery in Cursor
            Cursor cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                    null, orderBy);
            //Total number of images
            int count = cursor.getCount();

            //Create an array to store path to all the images
            String[] arrPath = new String[count];

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                int dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //Store the path of the image
                arrPath[i] = cursor.getString(dataColumnIndex);
                Log.i("PATH", arrPath[i]);
            }
            imagePaths = stream(arrPath).take(numberToTake).toList();
        }
        return imagePaths;
    }

    private void AddImagesToGallery() {
        List<String> imagePaths = GetImages(10);

        // Add to adapter here
        for (String filePath : imagePaths) {
            recyclerViewAdapter.add(0, filePath);
        }

        // Add padding so last item doesn't got clipped out of screen
        imageGallery.setPadding(0, 0, 0, 135);
    }

    private void SetUpSlidingPanel() {
        slidingLayout.addPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                if (newState == PanelState.EXPANDED) {
                    searchResultTagsGroup.setPadding(0, 0, 0, 50);
                } else if (newState == PanelState.ANCHORED) {
                    searchResultTagsGroup.setPadding(0, 0, 0, 250);
                }
            }
        });
        slidingLayout.setTouchEnabled(true);
        slidingLayout.setFadeOnClickListener(view -> slidingLayout.setPanelState(PanelState.COLLAPSED));
        slidingLayout.setAnchorPoint(0.8f);
    }

    private void SetUpSlidingButton() {
        slidingButton.setOnClickListener(view -> {
            if (slidingLayout.getPanelState() == PanelState.COLLAPSED) {
                slidingLayout.setPanelState(PanelState.ANCHORED);
            } else {
                slidingLayout.setPanelState(PanelState.COLLAPSED);
            }
        });
    }

    private void SetUpSearchResultTagsGroup() {
        searchResultTagsGroup.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String clickedTag) {
                if (clickedTag.equals("...")) {
                    AddMoreTags();
//                    DrawTagsBySearchInput(searchStr, alreadyInView + 10);
                } else {
                    String tagInGroup = stream(selectedTagsGroup.getTags()).singleOrNull(t -> t.equals(clickedTag));

                    if (tagInGroup == null) {
                        selectedTagsGroup.addTag(clickedTag);
                        tagSearchEditText.setText(clickedTag);
                        tagSearchEditText.setSelection(clickedTag.length());
                    }
//                    tagSearchEditText.setText(text);
//                    tagSearchEditText.setSelection(text.length());//to set cursor position
                }
            }

            @Override
            public void onTagLongClick(int position, String clickedTag) {

            }

            @Override
            public void onTagCrossClick(int position) {

            }
        });
//        searchResultTagsGroup.setOnTagDeleteListener((tagView, tag, tagNumber) -> {
//            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//            builder.setMessage("\"" + tag.text + "\" will be delete. Are you sure?");
//            builder.setPositiveButton("Yes", (dialog, which) -> {
//                tagView.remove(tagNumber);
//                Toast.makeText(MainActivity.this, "\"" + tag.text + "\" deleted", Toast.LENGTH_SHORT).show();
//            });
//            builder.setNegativeButton("No", null);
//            builder.show();
//        });
    }

    private void AddMoreTags() {
        List<String> alreadyInView = searchResultTagsGroup.getTags();
        String searchStr = tagSearchEditText.getText().toString();

        List<String> filteredList = GetFilteredTagList(searchStr).toList();

        int moreTagInd = alreadyInView.indexOf("...");
        searchResultTagsGroup.removeTag(moreTagInd);

        filteredList.removeAll(alreadyInView);
        filteredList = stream(filteredList).take(10).toList();
        for (String tag : filteredList) {
            searchResultTagsGroup.addTag(tag);
        }
        searchResultTagsGroup.addTag("...");
    }

    private void SetUpTagSearchEditText() {

        tagSearchEditText.setHint("Enter names of fruits");
        tagSearchEditText.setTagsListener(this);
        tagSearchEditText.setTagsWithSpacesEnabled(true);

        tagSearchEditText.setThreshold(1);

        tagSearchEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus && slidingLayout.getPanelState() == PanelState.COLLAPSED) {
                slidingLayout.setPanelState(PanelState.ANCHORED);
            } else if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        tagSearchEditText.setOnClickListener(view -> {
            if (slidingLayout.getPanelState() == PanelState.COLLAPSED) {
                slidingLayout.setPanelState(PanelState.ANCHORED);
            }
        });
        tagSearchEditText.addTextChangedListener(new TextWatcher() {

            private List<String> alreadyInText;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                alreadyInText = Arrays.asList(s.toString().split("\n"));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> newText = Arrays.asList(s.toString().split("\n"));
                String lastAlready = stream(alreadyInText).last().trim();
                String lastNew = stream(newText).last().trim();

                if (!lastAlready.equals(lastNew)) {
                    Log.i(TAG, "Redraw Tags!");

                    DrawTagsBySearchInput(lastNew, maxTagsLimit);
                } else {
                    DrawTagsBySearchInput("", maxTagsLimit);
                }

                String text = tagSearchEditText.getText().toString();
                Log.i(TAG, "Text changed: " + lastAlready + ".");
                Log.i(TAG, "Text in Edit: " + lastNew + ".");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void DrawTagsBySearchInput(String text, int take) {
        List<String> tags;

        Stream<String> linqRes;
        // for empty edittext
        linqRes = GetFilteredTagList(text);

        // Ignore already loaded tags and
        tags = linqRes
                .take(take)
                .toList();

        // Draw on screen
        searchResultTagsGroup.setTags(tags);
        searchResultTagsGroup.addTag("...");
    }

    private Stream<String> GetFilteredTagList(String text) {
        Stream<String> linqRes;
        if (text.equals("")) {
            linqRes = stream(allTagText);
        } else {
            linqRes = stream(allTagText)
                    .where(tag -> tag.toLowerCase().startsWith(text.toLowerCase()));
        }
        return linqRes;
    }

    private DisplayMetrics GetDisplayMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    private boolean IsNullOrWhiteSpace(String value) {
        char[] chars = value.toCharArray();

        if (chars == null) {
            return true;
        }

        for (char aChar : chars) {
            if (!Character.isWhitespace(aChar)) {
                return false;
            }
        }
        return true;
    }

    private boolean IsNullOrEmpty(String value) {
        return value == null || value.length() == 0;
    }

    @Override
    public void onTagsChanged(Collection<String> tags) {
        Log.d(TAG, "Tags changed: ");
        Log.d(TAG, Arrays.toString(tags.toArray()));
        List<String> distinctList = stream(tags).distinct().toList();

        if (!distinctList.equals(tags)) {
            String[] distinctArray = new String[distinctList.size()];
            distinctArray = distinctList.toArray(distinctArray);
            tagSearchEditText.setTags(distinctArray);
        }
    }

    @Override
    public void onEditingFinished() {
        Log.d(TAG, "OnEditing finished");
    }

//    private void CreateImageGallery() {
//        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        GalleryFolder = new File(storageDirectory, GALLERY_LOCATION);
//
//        if (!GalleryFolder.exists()) {
//            GalleryFolder.mkdirs();
//        }
//    }

//    private ArrayList<String> getFile(File dir, ArrayList<String> fileList) {
//        File listFile[] = dir.listFiles();
//        if (listFile != null && listFile.length > 0) {
//            for (File file : listFile) {
//                if (file.isDirectory()) {
//                    getFile(file, fileList);
//                } else {
//                    if (file.getName().endsWith(".png")
//                            || file.getName().endsWith(".jpg")
//                            || file.getName().endsWith(".jpeg")
//                            || file.getName().endsWith(".gif")
//                            || file.getName().endsWith(".bmp")
//                            || file.getName().endsWith(".webp")) {
//                        String temp = file.getPath().substring(0, file.getPath().lastIndexOf('/'));
//                        if (!fileList.contains(temp))
//                            fileList.add(temp);
//                    }
//                }
//            }
//        }
//        return fileList;
//    }

}