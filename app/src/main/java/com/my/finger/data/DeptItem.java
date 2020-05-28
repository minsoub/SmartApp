package com.my.finger.data;

import com.my.finger.R;

import tellh.com.recyclertreeview_lib.LayoutItemType;

public class DeptItem implements LayoutItemType
{
    public String deptName;
    public String deptCd;

    public DeptItem(String deptCd, String deptName)
    {
        this.deptCd = deptCd;
        this.deptName = deptName;
    }
    @Override
    public int getLayoutId() {
        return R.layout.item_file;
    }
}
