package com.example.erika.cookbook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Erika on 23. 11. 2016.
 */

public class SlidingTabsBasicFragment extends Fragment{
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter(getFragmentManager()));
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
    }


    class SamplePagerAdapter extends FragmentStatePagerAdapter {
        public SamplePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: {
                    /*view = getActivity().getLayoutInflater().inflate(R.layout.recipe_summary_layout, container, false);
                    container.addView(view);
                    return view;*/
                    return new recipeSummaryPart();
                }
                case 1: {
                    /*view = getActivity().getLayoutInflater().inflate(R.layout.recipe_ingredients_layout, container, false);
                    container.addView(view);
                    return view;*/
                    return new recipeIngredientsPart();
                }
                case 2: {
                    /*view = getActivity().getLayoutInflater().inflate(R.layout.recipe_process_layout, container, false);
                    container.addView(view);
                    return view;*/
                    return new recipeProcessPart();
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.item1);
                case 1:
                    return getString(R.string.item2);
                case 2:
                    return getString(R.string.item3);
                default:
                    return null;
            }
        }



    }
}
